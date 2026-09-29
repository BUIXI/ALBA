package com.oriol.alba.alarma

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.oriol.alba.MainActivity
import com.oriol.alba.datos.Alarma
import java.time.LocalDateTime
import java.time.ZoneId

/** Programa y anula alarmas en el sistema. Interfaz para poder probar sin Android. */
interface Programador {
  fun programar(alarma: Alarma, momento: LocalDateTime)

  fun cancelar(id: Long)

  /** La pregunta "¿Sigues despierto?" unos minutos después de apagar [alarma]. */
  fun programarComprobacion(alarma: Alarma, momento: LocalDateTime)

  fun cancelarComprobacion()
}

/**
 * Programa con `AlarmManager.setAlarmClock`: es el tipo de alarma más fiable de
 * Android. Suena en reposo profundo (Doze) y con el ahorro de batería puesto, y el
 * sistema enseña el icono de alarma en la barra de estado.
 */
class ProgramadorSistema(private val context: Context) : Programador {

  private val alarmManager = context.getSystemService(AlarmManager::class.java)

  override fun programar(alarma: Alarma, momento: LocalDateTime) =
    programarEn(momento, disparo(alarma, Modo.ALARMA, alarma.id.toInt(), uriAlarma(alarma.id)))

  override fun cancelar(id: Long) = anular(alarma = id.toInt(), uriAlarma(id))

  override fun programarComprobacion(alarma: Alarma, momento: LocalDateTime) =
    programarEn(momento, disparo(alarma, Modo.COMPROBACION, CodigoComprobacion, UriComprobacion))

  override fun cancelarComprobacion() = anular(CodigoComprobacion, UriComprobacion)

  private fun programarEn(momento: LocalDateTime, disparo: PendingIntent) {
    val millis = momento.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    // Al tocar el icono de alarma de la barra de estado se abre la app.
    val mostrar =
      PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )
    try {
      alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(millis, mostrar), disparo)
    } catch (e: SecurityException) {
      // Sin permiso de alarmas exactas (solo puede pasar en Android 12-13 si se quitó a
      // mano): mejor una alarma casi exacta que ninguna. La pantalla de permisos avisa.
      alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, disparo)
    }
  }

  private fun anular(alarma: Int, uri: Uri) {
    val intent = Intent(context, ReceptorAlarma::class.java).setAction(ReceptorAlarma.ACCION_DISPARAR).setData(uri)
    val pendiente =
      PendingIntent.getBroadcast(context, alarma, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
    if (pendiente != null) {
      alarmManager.cancel(pendiente)
      pendiente.cancel()
    }
  }

  /**
   * El aviso que recibirá [ReceptorAlarma]. Lleva la alarma entera en los extras: para
   * sonar no hace falta leer la base de datos. Cada alarma tiene su propio `data`
   * (alba://alarma/ID) para que sus avisos no se pisen entre sí.
   */
  private fun disparo(alarma: Alarma, modo: Modo, codigo: Int, uri: Uri): PendingIntent {
    val intent =
      Intent(context, ReceptorAlarma::class.java)
        .setAction(ReceptorAlarma.ACCION_DISPARAR)
        .setData(uri)
        .ponerAlarma(alarma)
        .ponerModo(modo)
    return PendingIntent.getBroadcast(
      context,
      codigo,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
  }

  private companion object {
    const val CodigoComprobacion = Int.MAX_VALUE - 1
    val UriComprobacion: Uri = "alba://comprobacion".toUri()

    fun uriAlarma(id: Long): Uri = "alba://alarma/$id".toUri()
  }
}
