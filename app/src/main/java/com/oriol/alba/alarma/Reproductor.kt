package com.oriol.alba.alarma

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.net.toUri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.oriol.alba.R
import com.oriol.alba.datos.Sonido
import kotlin.math.roundToInt

/**
 * El altavoz y el vibrador de verdad. Si el sonido elegido falla (por ejemplo, el
 * del sistema antes del primer desbloqueo), usa el nuestro, que va dentro de la app.
 */
class Reproductor(private val context: Context, sonido: Sonido) : SalidaSonido {

  private val audio = context.getSystemService(AudioManager::class.java)
  private val vibrador: Vibrator =
    if (Build.VERSION.SDK_INT >= 31) {
      context.getSystemService(VibratorManager::class.java).defaultVibrator
    } else {
      @Suppress("DEPRECATION") context.getSystemService(Vibrator::class.java)
    }
  private val atributos =
    AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_ALARM)
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .build()
  private val foco =
    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(atributos).build()

  private val volumenOriginal = audio.getStreamVolume(AudioManager.STREAM_ALARM)
  private var volumenCambiado = false
  private var reproductor: MediaPlayer? = null
  private var vibrando = false

  init {
    // El volumen de alarma del sistema, al menos al 80 %: una alarma que no se oye no
    // sirve de nada. Al terminar se deja como estaba.
    val maximo = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)
    val minimo = (maximo * 0.8f).roundToInt()
    if (volumenOriginal < minimo) {
      volumenCambiado = runCatching { audio.setStreamVolume(AudioManager.STREAM_ALARM, minimo, 0) }.isSuccess
    }
    // Pide el audio para sí: la música que estuviera sonando se pausa.
    audio.requestAudioFocus(foco)
    reproductor = crear(sonido)?.also { it.start() }
  }

  private fun crear(sonido: Sonido): MediaPlayer? {
    val propio = "android.resource://${context.packageName}/${R.raw.amanecer}".toUri()
    val candidatos =
      if (sonido == Sonido.SISTEMA) listOfNotNull(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), propio)
      else listOf(propio)
    for (uri in candidatos) {
      val reproductor = MediaPlayer()
      try {
        reproductor.setAudioAttributes(atributos)
        reproductor.setDataSource(context, uri)
        reproductor.isLooping = true
        reproductor.setVolume(0f, 0f)
        // Mantiene la CPU despierta mientras suena, aunque la pantalla esté apagada.
        reproductor.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        reproductor.prepare()
        return reproductor
      } catch (e: Exception) {
        reproductor.release()
      }
    }
    return null
  }

  override fun volumen(valor: Float) {
    // El oído percibe el volumen de forma logarítmica: con el cuadrado, la subida
    // suena pareja en vez de dar casi todo el salto al principio.
    val amplitud = valor * valor
    reproductor?.setVolume(amplitud, amplitud)
  }

  override fun vibrar(si: Boolean) {
    if (si == vibrando) return
    vibrando = si
    if (!si) {
      vibrador.cancel()
      return
    }
    // Vibra 0,6 s, para 0,9 s, y vuelta a empezar.
    val patron = VibrationEffect.createWaveform(longArrayOf(0, 600, 900), 0)
    if (Build.VERSION.SDK_INT >= 33) {
      vibrador.vibrate(patron, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
    } else {
      @Suppress("DEPRECATION") vibrador.vibrate(patron, atributos)
    }
  }

  override fun detener() {
    reproductor?.let {
      runCatching { it.stop() }
      it.release()
    }
    reproductor = null
    vibrador.cancel()
    vibrando = false
    audio.abandonAudioFocusRequest(foco)
    if (volumenCambiado) runCatching { audio.setStreamVolume(AudioManager.STREAM_ALARM, volumenOriginal, 0) }
    volumenCambiado = false
  }
}
