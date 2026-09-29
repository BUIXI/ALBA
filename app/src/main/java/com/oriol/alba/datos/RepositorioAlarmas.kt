package com.oriol.alba.datos

import kotlinx.coroutines.flow.Flow

/**
 * Punto único para leer y cambiar alarmas. En la fase 2, cada cambio también
 * programará (o anulará) la alarma en el sistema, así que las pantallas no deben ir
 * al DAO directamente.
 */
class RepositorioAlarmas(private val dao: AlarmaDao) {

  val alarmas: Flow<List<Alarma>> = dao.todas()

  suspend fun obtener(id: Long): Alarma? = dao.obtener(id)

  /** Guarda la alarma y devuelve su id (el nuevo si era una alarma nueva). */
  suspend fun guardar(alarma: Alarma): Long {
    val id = dao.guardar(alarma)
    return if (alarma.id != 0L) alarma.id else id
  }

  suspend fun cambiarActiva(id: Long, activa: Boolean) = dao.cambiarActiva(id, activa)

  suspend fun eliminar(id: Long) = dao.eliminar(id)
}
