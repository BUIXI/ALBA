package com.oriol.alba.alarma

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * La alarma que suena ahora mismo, para que la vean las pantallas. La publica
 * [ServicioAlarma]; null = no suena ninguna. Todo va en el mismo proceso.
 *
 * Los tiempos de [EstadoSesion] van en `SystemClock.elapsedRealtime()`.
 */
object CentralAlarma {
  private val _sesion = MutableStateFlow<EstadoSesion?>(null)
  val sesion: StateFlow<EstadoSesion?> = _sesion.asStateFlow()

  internal fun publicar(estado: EstadoSesion?) {
    _sesion.value = estado
  }
}
