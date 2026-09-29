package com.oriol.alba.alarma

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.oriol.alba.AlbaApp
import com.oriol.alba.datos.Alarma
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Servicio en primer plano mientras suena una alarma. Mantiene vivo el proceso,
 * enseña la notificación que abre [com.oriol.alba.ui.alarma.ActividadAlarma] a
 * pantalla completa y maneja el [Reproductor] según lo que diga la [SesionAlarma].
 *
 * Órdenes (acciones del Intent): sonar, silenciar (para hacer la tarea) y terminar
 * (tarea hecha).
 */
class ServicioAlarma : Service() {

  private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private var sesion: SesionAlarma? = null
  private var reproductor: Reproductor? = null

  private val contenedor
    get() = (application as AlbaApp).contenedor

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACCION_SONAR -> sonar(intent)
      ACCION_SILENCIAR -> sesion?.silenciar()
      ACCION_TERMINAR -> sesion?.terminar(completada = true)
    }
    if (sesion == null) pararServicio()
    return START_NOT_STICKY
  }

  private fun sonar(intent: Intent) {
    val alarma = intent.leerAlarma()
    val modo = intent.leerModo()
    val prueba = intent.esPrueba()
    val actual = sesion?.estado?.value

    // Lo primero, pasar a primer plano: Android da unos segundos y si no, cierra la app.
    val mostrada = actual?.alarma ?: alarma
    if (mostrada != null) {
      val notificacion = Notificaciones.alarma(this, mostrada, actual?.modo ?: modo)
      val tipo = if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0
      try {
        ServiceCompat.startForeground(this, Notificaciones.ID_ALARMA, notificacion, tipo)
      } catch (e: Exception) {
        // Android no deja pasar a primer plano ahora: la notificación a pantalla
        // completa abre la pantalla de la alarma, y esta vuelve a arrancar el servicio.
        if (actual == null && alarma != null) Notificaciones.alarmaSinServicio(this, alarma, modo)
        return
      }
    }
    if (alarma == null) return

    // Las cuentas de después de sonar (la siguiente vez, o apagarla si era de una vez)
    // se hacen aunque ya sonara otra.
    if (!prueba && modo == Modo.ALARMA && alarma.id != 0L) {
      alcance.launch(Dispatchers.IO) { contenedor.repositorio.despuesDeSonar(alarma.id) }
    }
    // Si ya suena otra, se queda esa: dos alarmas a la vez no aportan nada.
    if (sesion != null) return
    if (modo == Modo.ALARMA) contenedor.programador.cancelarComprobacion()

    val salida = Reproductor(this, alarma.sonido)
    val nueva = SesionAlarma(alarma, modo, prueba, SystemClock::elapsedRealtime, alcance, salida)
    reproductor = salida
    sesion = nueva
    nueva.iniciar()

    alcance.launch {
      var modoMostrado = modo
      nueva.estado.collect { estado ->
        if (estado.terminada) {
          alTerminar(estado)
          return@collect
        }
        CentralAlarma.publicar(estado)
        // La comprobación que nadie respondió pasa a alarma: se cambia el texto.
        if (estado.modo != modoMostrado) {
          modoMostrado = estado.modo
          actualizarNotificacion(estado.alarma, estado.modo)
        }
      }
    }
  }

  private fun alTerminar(estado: EstadoSesion) {
    if (sesion == null) return
    sesion = null
    reproductor = null
    CentralAlarma.publicar(null)
    // Apagada con la tarea: dentro de un rato, "¿Sigues despierto?".
    if (estado.completada && estado.modo == Modo.ALARMA && estado.alarma.comprobar && !estado.prueba) {
      contenedor.programador.programarComprobacion(estado.alarma, LocalDateTime.now().plusMinutes(MinutosComprobacion))
    }
    pararServicio()
  }

  private fun actualizarNotificacion(alarma: Alarma, modo: Modo) =
    Notificaciones.publicar(this, Notificaciones.ID_ALARMA, Notificaciones.alarma(this, alarma, modo))

  private fun pararServicio() {
    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  override fun onDestroy() {
    // Si Android cierra el servicio a medias, que no se quede nada sonando.
    sesion?.terminar(completada = false)
    reproductor?.detener()
    if (sesion != null) CentralAlarma.publicar(null)
    alcance.cancel()
    super.onDestroy()
  }

  companion object {
    const val ACCION_SONAR = "com.oriol.alba.SONAR"
    const val ACCION_SILENCIAR = "com.oriol.alba.SILENCIAR"
    const val ACCION_TERMINAR = "com.oriol.alba.TERMINAR"

    /** Minutos entre apagar la alarma y preguntar "¿Sigues despierto?". */
    const val MinutosComprobacion = 10L

    fun intentSonar(context: Context, alarma: Alarma, modo: Modo, prueba: Boolean = false): Intent =
      Intent(context, ServicioAlarma::class.java)
        .setAction(ACCION_SONAR)
        .ponerAlarma(alarma)
        .ponerModo(modo)
        .ponerPrueba(prueba)

    /** Hace sonar [alarma] ahora mismo, como prueba. Solo desde una pantalla visible. */
    fun probar(context: Context, alarma: Alarma) =
      ContextCompat.startForegroundService(context, intentSonar(context, alarma, Modo.ALARMA, prueba = true))

    /** Silencio para hacer la tarea (o alargarlo). */
    fun silenciar(context: Context) = enviar(context, ACCION_SILENCIAR)

    /** Tarea hecha: se acabó. */
    fun terminar(context: Context) = enviar(context, ACCION_TERMINAR)

    private fun enviar(context: Context, accion: String) {
      // Solo se manda si hay una sesión: si no, arrancaría el servicio para nada.
      if (CentralAlarma.sesion.value == null) return
      runCatching { context.startService(Intent(context, ServicioAlarma::class.java).setAction(accion)) }
    }
  }
}
