package com.oriol.alba.alarma

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.oriol.alba.R
import com.oriol.alba.datos.Alarma
import com.oriol.alba.ui.alarma.ActividadAlarma
import com.oriol.alba.ui.componentes.formatoHora

/** Canales y notificaciones de la alarma. */
object Notificaciones {
  const val CANAL_ALARMAS = "alarmas"
  const val ID_ALARMA = 1

  /** Se llama al arrancar la app. Crear un canal que ya existe no hace nada. */
  fun crearCanales(context: Context) {
    val canal =
      NotificationChannel(CANAL_ALARMAS, context.getString(R.string.canal_alarmas), NotificationManager.IMPORTANCE_HIGH)
        .apply {
          description = context.getString(R.string.canal_alarmas_desc)
          // El sonido y la vibración los pone el servicio, con su subida de volumen.
          setSound(null, null)
          enableVibration(false)
          lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
  }

  /**
   * La notificación mientras suena. Con la pantalla apagada o bloqueada abre la
   * pantalla de la alarma entera; si el móvil se está usando, sale arriba y al tocarla
   * se abre. No se puede quitar deslizando: para apagarla hay que hacer la tarea.
   */
  fun alarma(context: Context, alarma: Alarma, modo: Modo, conAlarma: Boolean = false): Notification {
    val intent = ActividadAlarma.intent(context).apply { if (conAlarma) ponerAlarma(alarma).ponerModo(modo) }
    val abrir =
      PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    val hora = formatoHora(alarma.hora, alarma.minuto)
    val titulo =
      if (modo == Modo.COMPROBACION) context.getString(R.string.sigues_despierto)
      else context.getString(R.string.notificacion_alarma, hora)
    val texto =
      when {
        modo == Modo.COMPROBACION -> context.getString(R.string.notificacion_comprobacion)
        alarma.etiqueta.isNotBlank() -> alarma.etiqueta
        else -> context.getString(R.string.notificacion_toca)
      }
    return NotificationCompat.Builder(context, CANAL_ALARMAS)
      .setSmallIcon(R.drawable.ic_notificacion)
      .setColor(0xFFFF9F0A.toInt())
      .setContentTitle(titulo)
      .setContentText(texto)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setOngoing(true)
      .setAutoCancel(false)
      .setContentIntent(abrir)
      .setFullScreenIntent(abrir, true)
      .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
      .build()
  }

  /** Si no se pudo arrancar el servicio: la pantalla de la alarma, con la alarma dentro. */
  fun alarmaSinServicio(context: Context, alarma: Alarma, modo: Modo) =
    publicar(context, ID_ALARMA, alarma(context, alarma, modo, conAlarma = true))

  /**
   * Publica [notificacion] si hay permiso. Sin permiso no se ve nada: por eso la
   * pantalla de permisos lo pide y la lista avisa.
   */
  fun publicar(context: Context, id: Int, notificacion: Notification) {
    val permitido =
      Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    if (!permitido) return
    try {
      NotificationManagerCompat.from(context).notify(id, notificacion)
    } catch (e: SecurityException) {
      // El permiso se ha quitado justo ahora: no hay nada más que hacer.
    }
  }
}
