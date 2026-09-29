package com.oriol.alba.ui.tareas

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** La lógica de los minijuegos, sin pantalla. */
class JuegosTest {

  // --- Atrapa los soles ---

  @Test
  fun soles_cadaSolNuevoSaleLejos_yCadaVezDuraMenos() {
    val soles = EstadoSoles(Random(1))
    val vidaInicial = soles.vida
    repeat(200) {
      val antes = soles.posicion
      val turno = soles.turno
      soles.escapar()
      assertTrue((soles.posicion - antes).getDistance() >= 0.35f)
      assertEquals(turno + 1, soles.turno)
      assertTrue(soles.posicion.x in 0f..1f && soles.posicion.y in 0f..1f)
    }
    assertEquals(0, soles.aciertos)
    repeat(6) { soles.atrapar() }
    assertTrue(soles.vida < vidaInicial)
    assertTrue(soles.vida >= 1_100)
  }

  @Test
  fun soles_doceAtrapados_completan() {
    val soles = EstadoSoles(Random(2))
    repeat(11) { soles.atrapar() }
    assertFalse(soles.completado)
    soles.atrapar()
    assertTrue(soles.completado)
    soles.atrapar()
    assertEquals(12, soles.aciertos)
  }

  // --- Repite la secuencia ---

  @Test
  fun secuencia_mientrasSeEnsena_noSePuedePulsar() {
    val s = EstadoSecuencia(Random(3))
    assertTrue(s.mostrando)
    assertNull(s.pulsar(0))
  }

  @Test
  fun secuencia_dosRondasBien_completan() {
    val s = EstadoSecuencia(Random(4))
    assertEquals(4, s.secuencia.size)
    s.terminarDeMostrar()
    s.secuencia.toList().forEach { assertEquals(true, s.pulsar(it)) }
    assertEquals(1, s.ronda)
    assertEquals(5, s.secuencia.size)
    assertTrue(s.mostrando)
    s.terminarDeMostrar()
    s.secuencia.toList().forEach { s.pulsar(it) }
    assertTrue(s.completado)
  }

  @Test
  fun secuencia_fallar_daOtraDeLaMismaRonda() {
    val s = EstadoSecuencia(Random(5))
    s.terminarDeMostrar()
    val version = s.version
    val mal = (s.secuencia.first() + 1) % s.botones
    assertEquals(false, s.pulsar(mal))
    assertEquals(1, s.fallos)
    assertEquals(0, s.ronda)
    assertEquals(version + 1, s.version)
    assertTrue(s.mostrando)
    assertEquals(0, s.progreso)
  }

  // --- Parejas ---

  @Test
  fun parejas_seisParejasBarajadas() {
    val p = EstadoParejas(Random(6))
    assertEquals(12, p.cartas.size)
    assertEquals(6, p.pares)
    assertTrue(p.cartas.groupBy { it }.values.all { it.size == 2 })
    assertNotEquals((0 until 6).flatMap { listOf(it, it) }, p.cartas)
  }

  @Test
  fun parejas_dosIguales_seQuedanDeCara_yDosDistintas_seTapan() {
    val p = EstadoParejas(Random(7))
    val cartas = p.cartas
    val a = 0
    val pareja = cartas.indices.first { it != a && cartas[it] == cartas[a] }
    val otra = cartas.indices.first { cartas[it] != cartas[a] }
    // Distintas: no son pareja, y hasta taparlas no se puede destapar una tercera.
    assertNull(p.tocar(a))
    assertEquals(false, p.tocar(otra))
    assertNull(p.tocar(pareja))
    p.ocultar()
    assertFalse(p.bocaArriba(a))
    // Iguales: pareja, se quedan de cara.
    p.tocar(a)
    assertEquals(true, p.tocar(pareja))
    assertTrue(p.bocaArriba(a) && p.bocaArriba(pareja))
    assertEquals(2, p.intentos)
    // Una ya emparejada no cuenta.
    assertNull(p.tocar(a))
  }

  @Test
  fun parejas_todas_completan() {
    val p = EstadoParejas(Random(8))
    for (simbolo in 0 until p.pares) {
      val (a, b) = p.cartas.indices.filter { p.cartas[it] == simbolo }
      p.tocar(a)
      p.tocar(b)
    }
    assertTrue(p.completado)
  }

  // --- Del 1 al 12 ---

  @Test
  fun orden_enOrden_completa_yLosFallosCuentan() {
    val o = EstadoOrden(Random(9))
    assertEquals((1..12).toSet(), o.numeros.toSet())
    assertEquals(false, o.tocar(3))
    assertEquals(1, o.fallos)
    assertEquals(true, o.tocar(1))
    assertNull(o.tocar(1)) // ya tocado: ni acierto ni fallo
    (2..12).forEach { assertEquals(true, o.tocar(it)) }
    assertTrue(o.completado)
    assertEquals(1, o.fallos)
  }
}
