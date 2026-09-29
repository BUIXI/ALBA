package com.oriol.alba.dominio

import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

/** Los siete días en el orden del idioma: desde el lunes en España, desde el domingo en EE. UU. */
fun diasDeLaSemana(idioma: Locale): List<DayOfWeek> {
  val primero = WeekFields.of(idioma).firstDayOfWeek
  return (0L..6L).map { primero.plus(it) }
}

/** Cómo se resume en palabras un conjunto de días. El texto lo pone la pantalla. */
sealed interface ResumenDias {
  data object UnaVez : ResumenDias

  data object TodosLosDias : ResumenDias

  data object EntreSemana : ResumenDias

  data object FinDeSemana : ResumenDias

  /** Cualquier otra combinación: se nombran los días, en el orden del idioma. */
  data class Sueltos(val dias: List<DayOfWeek>) : ResumenDias
}

private val laborables = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
private val finDeSemana = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

fun resumirDias(dias: Set<DayOfWeek>, idioma: Locale): ResumenDias =
  when {
    dias.isEmpty() -> ResumenDias.UnaVez
    dias.size == 7 -> ResumenDias.TodosLosDias
    dias == laborables -> ResumenDias.EntreSemana
    dias == finDeSemana -> ResumenDias.FinDeSemana
    else -> ResumenDias.Sueltos(diasDeLaSemana(idioma).filter { it in dias })
  }
