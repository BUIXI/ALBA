package com.oriol.alba.datos

import com.oriol.alba.alarma.Programador
import com.oriol.alba.dominio.siguienteDisparo
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

/**
 * Punto único para leer y cambiar alarmas. Cada cambio programa (o anula) la alarma
 * en el sistema, así que las pantallas no deben ir al DAO directamente.
 */
class RepositorioAlarmas(
  private val dao: AlarmaDao,
  private val programador: Programador,
  private val reloj: () -> LocalDateTime = LocalDateTime::now,
) {

  val alarmas: Flow<List<Alarma>> = dao.todas()

  suspend fun obtener(id: Long): Alarma? = dao.obtener(id)

  /** Guarda la alarma, la programa y devuelve su id (el nuevo si era una alarma nueva). */
  suspend fun guardar(alarma: Alarma): Long {
    val devuelto = dao.guardar(alarma)
    val id = if (alarma.id != 0L) alarma.id else devuelto
    programar(alarma.copy(id = id))
    return id
  }

  suspend fun cambiarActiva(id: Long, activa: Boolean) {
    dao.cambiarActiva(id, activa)
    dao.obtener(id)?.let { programar(it) }
  }

  suspend fun eliminar(id: Long) {
    dao.eliminar(id)
    programador.cancelar(id)
  }

  /**
   * Vuelve a programar todas. Tras reiniciar el móvil, cambiar la hora o la zona
   * horaria, actualizar la app... y al abrirla, por si acaso.
   */
  suspend fun reprogramarTodas() {
    dao.todasAhora().forEach { programar(it) }
  }

  /**
   * Después de sonar: si se repite, se programa la siguiente; si era de una vez, se
   * apaga (como en iOS). Se calcula desde dentro de un minuto: si el sistema la
   * disparara unas milésimas antes de su hora, no se volvería a programar para hoy.
   */
  suspend fun despuesDeSonar(id: Long) {
    val alarma = dao.obtener(id) ?: return
    if (alarma.repetida) {
      programar(alarma, desde = reloj().plusMinutes(1))
    } else {
      cambiarActiva(id, false)
    }
  }

  private fun programar(alarma: Alarma, desde: LocalDateTime = reloj()) {
    val momento = siguienteDisparo(alarma, desde)
    if (momento == null) programador.cancelar(alarma.id) else programador.programar(alarma, momento)
  }
}
