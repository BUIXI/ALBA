package com.oriol.alba

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Claves de navegación: una por pantalla. Son serializables para que la pila
// sobreviva a que Android cierre el proceso.

/** Pantalla principal: la lista de alarmas. */
@Serializable data object Lista : NavKey

/** Editor de una alarma. [alarmaId] 0 = alarma nueva. */
@Serializable data class Editor(val alarmaId: Long = 0L) : NavKey

/** "Para que suene siempre": permisos y ajustes de batería. */
@Serializable data object ClavePermisos : NavKey
