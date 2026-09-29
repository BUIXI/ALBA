package com.oriol.alba.alarma

import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.net.toUri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/** Qué permisos y ajustes tiene la app. Los cuatro primeros son imprescindibles. */
data class EstadoPermisos(
  val notificaciones: Boolean,
  val pantallaCompleta: Boolean,
  val alarmasExactas: Boolean,
  /** Que la batería no tenga la app "restringida": así no puede ni arrancar. */
  val segundoPlano: Boolean,
  /** Fuera de la optimización de batería. Recomendado, no imprescindible. */
  val sinOptimizarBateria: Boolean,
) {
  val imprescindibles: Boolean
    get() = notificaciones && pantallaCompleta && alarmasExactas && segundoPlano
}

/** Consultas de permisos y los Intent para ir a cada ajuste. */
object Permisos {

  fun estado(context: Context): EstadoPermisos {
    val notificaciones =
      Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    val pantallaCompleta =
      Build.VERSION.SDK_INT < 34 || context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
    val alarmasExactas =
      Build.VERSION.SDK_INT < 31 || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    // La restricción de batería por app llegó en Android 9.
    val segundoPlano =
      Build.VERSION.SDK_INT < 28 || !context.getSystemService(ActivityManager::class.java).isBackgroundRestricted
    val sinOptimizar = context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)
    return EstadoPermisos(notificaciones, pantallaCompleta, alarmasExactas, segundoPlano, sinOptimizar)
  }

  /** Si el permiso de notificaciones se pide con el diálogo del sistema (Android 13+). */
  val notificacionesConDialogo: Boolean
    get() = Build.VERSION.SDK_INT >= 33

  fun ajustesNotificaciones(context: Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

  fun ajustesPantallaCompleta(context: Context): Intent =
    if (Build.VERSION.SDK_INT >= 34) Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, paquete(context))
    else ajustesApp(context)

  fun ajustesAlarmasExactas(context: Context): Intent =
    if (Build.VERSION.SDK_INT >= 31) Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, paquete(context))
    else ajustesApp(context)

  /** La lista de optimización de batería del sistema (buscar Alba y marcar "No optimizar"). */
  fun ajustesBateria(): Intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

  /** La ficha de Alba en Ajustes: batería, inicio automático, etc. */
  fun ajustesApp(context: Context): Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, paquete(context))

  /**
   * Marcas que cierran apps en segundo plano por su cuenta, además de lo que hace
   * Android. En ellas hay que tocar sus propios ajustes (inicio automático, etc.).
   * Ver dontkillmyapp.com.
   */
  val fabricanteAgresivo: String?
    get() {
      val marca = Build.MANUFACTURER.lowercase()
      val agresivos = listOf("realme", "oppo", "oneplus", "xiaomi", "redmi", "poco", "huawei", "honor", "vivo", "samsung", "meizu", "asus")
      return if (agresivos.any { marca.contains(it) }) Build.MANUFACTURER.replaceFirstChar { it.uppercase() } else null
    }

  private fun paquete(context: Context): Uri = "package:${context.packageName}".toUri()
}
