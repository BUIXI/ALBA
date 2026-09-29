package com.oriol.alba.ui.alarma

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.oriol.alba.AlbaApp
import com.oriol.alba.alarma.CentralAlarma
import com.oriol.alba.alarma.ServicioAlarma
import com.oriol.alba.alarma.leerAlarma
import com.oriol.alba.alarma.leerModo
import com.oriol.alba.theme.PaletaClara
import com.oriol.alba.theme.TemaAlba
import kotlinx.coroutines.launch

/**
 * La pantalla de la alarma. Sale encima de la pantalla de bloqueo y la enciende. No
 * se puede cerrar con "atrás" hasta hacer la tarea. Las teclas de volumen hacen lo
 * mismo que "Estoy despierto": callar la alarma para hacer la tarea.
 *
 * Va en su propia tarea (ver el manifiesto): al cerrarla se vuelve a donde estabas,
 * normalmente la pantalla de bloqueo, sin pasar por la lista de alarmas.
 */
class ActividadAlarma : ComponentActivity() {

  private val vm: AlarmaViewModel by viewModels { AlarmaViewModel.fabrica(application as AlbaApp) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Encima del bloqueo y con la pantalla encendida. En Android 8.0 (API 26) aún no
    // existían estos métodos: se usan las banderas antiguas de la ventana.
    if (Build.VERSION.SDK_INT >= 27) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
    } else {
      @Suppress("DEPRECATION")
      window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    // Fondo claro: iconos del sistema oscuros.
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
    )
    // "Atrás" solo sirve cuando ya está apagada.
    onBackPressedDispatcher.addCallback(this) { if (vm.fase is Fase.Hecho) cerrar() }

    if (!arrancarSiHaceFalta(intent)) {
      finish()
      return
    }

    setContent { TemaAlba(PaletaClara) { PantallaAlarma(vm, onCerrar = ::cerrar) } }

    lifecycleScope.launch {
      CentralAlarma.sesion.collect { sesion ->
        vm.alCambiarSesion(sesion)
        // Se acabó sin la tarea (se cansó de sonar, o la paró el sistema): fuera.
        if (sesion == null && vm.vioSesion && vm.fase !is Fase.Hecho) cerrar()
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    arrancarSiHaceFalta(intent)
  }

  override fun onResume() {
    super.onResume()
    // Si en onCreate fue demasiado pronto para arrancar el servicio, ahora que la
    // pantalla ya se ve, otra vez.
    arrancarSiHaceFalta(intent)
  }

  /**
   * Si no hay alarma sonando pero la notificación trae una (el servicio no pudo
   * arrancar solo), se arranca desde aquí: con la pantalla visible, Android sí deja.
   * Devuelve false si no hay nada que enseñar.
   */
  private fun arrancarSiHaceFalta(intent: Intent?): Boolean {
    if (CentralAlarma.sesion.value != null || vm.vioSesion) return true
    val alarma = intent?.leerAlarma() ?: return false
    runCatching { ContextCompat.startForegroundService(this, ServicioAlarma.intentSonar(this, alarma, intent.leerModo())) }
    return true
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      if (vm.fase !is Fase.Hecho) vm.despierto()
      return true // no cambia el volumen del sistema
    }
    return super.onKeyDown(keyCode, event)
  }

  override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) return true
    return super.onKeyUp(keyCode, event)
  }

  private fun cerrar() = finishAndRemoveTask()

  companion object {
    fun intent(context: Context): Intent =
      Intent(context, ActividadAlarma::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
  }
}
