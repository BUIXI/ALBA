package com.oriol.alba.alarma

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.oriol.alba.AlbaApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Recibe el disparo de [ProgramadorSistema] y arranca el [ServicioAlarma]. */
class ReceptorAlarma : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != ACCION_DISPARAR) return
    val alarma = intent.leerAlarma() ?: return
    val modo = intent.leerModo()
    try {
      // Arrancar un servicio en primer plano desde aquí está permitido: lo ha
      // disparado una alarma de setAlarmClock.
      ContextCompat.startForegroundService(context, ServicioAlarma.intentSonar(context, alarma, modo))
    } catch (e: Exception) {
      // Último recurso si Android no deja arrancar el servicio: la notificación a
      // pantalla completa abre la pantalla de la alarma, y esta lo arranca.
      Notificaciones.alarmaSinServicio(context, alarma, modo)
    }
  }

  companion object {
    const val ACCION_DISPARAR = "com.oriol.alba.DISPARAR"
  }
}

/**
 * Vuelve a programar todas las alarmas cuando el sistema las olvida o las descoloca:
 * al encender el móvil (también antes del primer desbloqueo), al actualizar la app,
 * al cambiar la hora o la zona horaria y al conceder el permiso de alarmas exactas.
 */
class ReceptorSistema : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action !in acciones) return
    val pendiente = goAsync()
    CoroutineScope(Dispatchers.IO).launch {
      try {
        withTimeoutOrNull(8_000) { (context.applicationContext as AlbaApp).contenedor.repositorio.reprogramarTodas() }
      } finally {
        pendiente.finish()
      }
    }
  }

  private companion object {
    val acciones =
      setOf(
        Intent.ACTION_BOOT_COMPLETED,
        Intent.ACTION_LOCKED_BOOT_COMPLETED,
        Intent.ACTION_MY_PACKAGE_REPLACED,
        Intent.ACTION_TIME_CHANGED,
        Intent.ACTION_TIMEZONE_CHANGED,
        AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
      )
  }
}
