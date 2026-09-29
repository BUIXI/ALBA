package com.oriol.alba.dominio

import com.oriol.alba.datos.Alarma
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Cuándo suena cada alarma. 29/09/2026 es martes. */
class ProgramacionTest {

  private val martes2248 = LocalDateTime.of(2026, 9, 29, 22, 48)
  private val laborables = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

  @Test
  fun unaVezQueAunNoHaPasado_sueneHoy() {
    val alarma = Alarma(hora = 23, minuto = 30)
    assertEquals(LocalDateTime.of(2026, 9, 29, 23, 30), siguienteDisparo(alarma, martes2248))
  }

  @Test
  fun unaVezQueYaHaPasado_sueneManana() {
    val alarma = Alarma(hora = 7, minuto = 0)
    assertEquals(LocalDateTime.of(2026, 9, 30, 7, 0), siguienteDisparo(alarma, martes2248))
  }

  @Test
  fun mismaHoraQueAhora_noSuenaYa_sinoManana() {
    val alarma = Alarma(hora = 22, minuto = 48)
    assertEquals(LocalDateTime.of(2026, 9, 30, 22, 48), siguienteDisparo(alarma, martes2248))
  }

  @Test
  fun segundosPasadosDeLaHora_cuentanComoPasada() {
    val alarma = Alarma(hora = 22, minuto = 48)
    val ahora = martes2248.withSecond(30)
    assertEquals(LocalDateTime.of(2026, 9, 30, 22, 48), siguienteDisparo(alarma, ahora))
  }

  @Test
  fun laborablesElViernesPorLaNoche_sueneElLunes() {
    val alarma = Alarma(hora = 7, minuto = 0, dias = laborables)
    val viernes = LocalDateTime.of(2026, 10, 2, 21, 0)
    assertEquals(LocalDateTime.of(2026, 10, 5, 7, 0), siguienteDisparo(alarma, viernes))
  }

  @Test
  fun soloElDiaDeHoy_yaPasada_sueneLaSemanaQueViene() {
    val alarma = Alarma(hora = 7, minuto = 0, dias = setOf(TUESDAY))
    assertEquals(LocalDateTime.of(2026, 10, 6, 7, 0), siguienteDisparo(alarma, martes2248))
  }

  @Test
  fun soloElDiaDeHoy_aunNoPasada_sueneHoy() {
    val alarma = Alarma(hora = 23, minuto = 0, dias = setOf(TUESDAY))
    assertEquals(LocalDateTime.of(2026, 9, 29, 23, 0), siguienteDisparo(alarma, martes2248))
  }

  @Test
  fun desactivada_noSuena() {
    assertNull(siguienteDisparo(Alarma(hora = 7, minuto = 0, activa = false), martes2248))
  }

  @Test
  fun proxima_eligeLaMasCercanaEntreLasActivas() {
    val trabajo = Alarma(id = 1, hora = 7, minuto = 0, dias = laborables)
    val finde = Alarma(id = 2, hora = 9, minuto = 0, dias = setOf(SATURDAY, SUNDAY))
    val apagada = Alarma(id = 3, hora = 6, minuto = 0, activa = false)
    val (alarma, momento) = proximaAlarma(listOf(finde, apagada, trabajo), martes2248)!!
    assertEquals(1L, alarma.id)
    assertEquals(LocalDateTime.of(2026, 9, 30, 7, 0), momento)
  }

  @Test
  fun proxima_sinActivas_esNull() {
    assertNull(proximaAlarma(listOf(Alarma(hora = 7, minuto = 0, activa = false)), martes2248))
  }

  @Test
  fun minutosHasta_redondeaHaciaArriba() {
    val momento = LocalDateTime.of(2026, 9, 30, 7, 0)
    assertEquals(8 * 60L + 12, minutosHasta(martes2248, momento))
    assertEquals(8 * 60L + 12, minutosHasta(martes2248.withSecond(20), momento))
    assertEquals(1L, minutosHasta(momento.minusSeconds(1), momento))
  }
}
