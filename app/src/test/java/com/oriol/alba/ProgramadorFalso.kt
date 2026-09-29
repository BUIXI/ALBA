package com.oriol.alba

import com.oriol.alba.alarma.Programador
import com.oriol.alba.datos.Alarma
import java.time.LocalDateTime

/** Programador de mentira para las pruebas: apunta qué se programaría, sin Android. */
class ProgramadorFalso : Programador {
  /** Id de alarma -> momento en que sonaría. */
  val programadas = mutableMapOf<Long, LocalDateTime>()
  var comprobacion: Pair<Alarma, LocalDateTime>? = null

  override fun programar(alarma: Alarma, momento: LocalDateTime) {
    programadas[alarma.id] = momento
  }

  override fun cancelar(id: Long) {
    programadas.remove(id)
  }

  override fun programarComprobacion(alarma: Alarma, momento: LocalDateTime) {
    comprobacion = alarma to momento
  }

  override fun cancelarComprobacion() {
    comprobacion = null
  }
}
