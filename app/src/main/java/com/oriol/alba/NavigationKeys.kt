package com.oriol.alba

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Claves de navegación: una por pantalla. Son serializables para que la pila
// sobreviva a que Android cierre el proceso.

/** Pantalla principal: la lista de alarmas. */
@Serializable data object Main : NavKey
