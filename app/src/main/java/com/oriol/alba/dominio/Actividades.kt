package com.oriol.alba.dominio

import com.oriol.alba.datos.TipoTarea
import kotlin.random.Random

// Qué actividad toca cuando suena una alarma.

/**
 * La actividad de hoy: una al azar entre las de la alarma (con una sola, esa). Las que
 * aún no están disponibles no cuentan.
 */
fun elegirTarea(tareas: Set<TipoTarea>, azar: Random = Random.Default): TipoTarea {
  val posibles = tareas.filter { it.disponible }.ifEmpty { listOf(TipoTarea.PorDefecto) }
  return posibles[azar.nextInt(posibles.size)]
}

/**
 * Otra actividad para cuando la de ahora no se puede hacer (la foto sin cámara, o un
 * objeto que no reconoce): otra de las elegidas que no use la cámara y, si no hay, la
 * de por defecto. Así la alarma siempre se puede apagar.
 */
fun alternativaA(actual: TipoTarea, tareas: Set<TipoTarea>): TipoTarea =
  tareas.firstOrNull { it != actual && it.disponible && !it.usaCamara }
    ?: TipoTarea.PorDefecto.takeIf { it != actual }
    ?: TipoTarea.CALCULO
