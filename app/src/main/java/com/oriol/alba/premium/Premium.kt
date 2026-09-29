package com.oriol.alba.premium

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Si la persona tiene Alba Premium. Lo que es de Premium lo pregunta aquí (hoy: varias
 * actividades por alarma, al azar).
 *
 * **Provisional**: hasta que lleguen los pagos (fase 6, RevenueCat), todo está
 * desbloqueado para poder probarlo. Entonces esto leerá la compra, guardada en el
 * móvil: la alarma nunca debe depender de internet ni del servidor de pagos.
 */
object Premium {
  private val _activo = MutableStateFlow(true)
  val activo: StateFlow<Boolean> = _activo.asStateFlow()

  /** Solo para pruebas: ver cómo se comporta la app sin Premium. */
  internal fun fijarParaPruebas(activo: Boolean) {
    _activo.value = activo
  }
}
