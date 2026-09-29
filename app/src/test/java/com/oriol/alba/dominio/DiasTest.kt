package com.oriol.alba.dominio

import com.oriol.alba.datos.Convertidores
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class DiasTest {

  private val espanol = Locale.forLanguageTag("es-ES")
  private val ingles = Locale.forLanguageTag("en-US")

  @Test
  fun semanaEspanola_empiezaEnLunes_yLaDeEEUU_enDomingo() {
    assertEquals(MONDAY, diasDeLaSemana(espanol).first())
    assertEquals(SUNDAY, diasDeLaSemana(ingles).first())
    assertEquals(7, diasDeLaSemana(espanol).toSet().size)
  }

  @Test
  fun resumenes() {
    assertEquals(ResumenDias.UnaVez, resumirDias(emptySet(), espanol))
    assertEquals(ResumenDias.TodosLosDias, resumirDias(DayOfWeek.entries.toSet(), espanol))
    assertEquals(ResumenDias.EntreSemana, resumirDias(setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), espanol))
    assertEquals(ResumenDias.FinDeSemana, resumirDias(setOf(SATURDAY, SUNDAY), espanol))
  }

  @Test
  fun diasSueltos_vanEnElOrdenDelIdioma() {
    val dias = setOf(SUNDAY, WEDNESDAY, MONDAY)
    assertEquals(ResumenDias.Sueltos(listOf(MONDAY, WEDNESDAY, SUNDAY)), resumirDias(dias, espanol))
    assertEquals(ResumenDias.Sueltos(listOf(SUNDAY, MONDAY, WEDNESDAY)), resumirDias(dias, ingles))
  }

  @Test
  fun letrasDeLosDias_enEspanol() {
    val letras = diasDeLaSemana(espanol).joinToString("") { it.getDisplayName(java.time.format.TextStyle.NARROW, espanol).uppercase(espanol) }
    assertEquals("LMXJVSD", letras)
  }

  @Test
  fun mascaraDeDias_idaYVuelta() {
    val c = Convertidores()
    assertEquals(0, c.diasAMascara(emptySet()))
    assertEquals(0b1111111, c.diasAMascara(DayOfWeek.entries.toSet()))
    assertEquals(0b1000001, c.diasAMascara(setOf(MONDAY, SUNDAY)))
    for (mascara in 0..127) assertEquals(mascara, c.diasAMascara(c.mascaraADias(mascara)))
  }
}
