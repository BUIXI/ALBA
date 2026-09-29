package com.oriol.alba.dominio

import com.oriol.alba.ui.tareas.EstadoCalculo
import com.oriol.alba.ui.tareas.Tecla
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculoTest {

  @Test
  fun operaciones_estanEnSusRangos_ySinCasosTriviales() {
    val generador = GeneradorCalculo(Random(42))
    var anterior: Operacion? = null
    repeat(2_000) {
      val op = generador.siguiente(anterior)
      assertNotEquals("no se repite seguida", anterior, op)
      when (op.operador) {
        Operador.SUMA -> {
          assertTrue(op.a in 12..89 && op.b in 12..89)
          assertTrue(op.a % 10 != 0 && op.b % 10 != 0)
        }
        Operador.RESTA -> {
          assertTrue(op.resultado > 0)
          assertTrue(op.a in 41..98 && op.b >= 12)
        }
        Operador.MULTIPLICACION -> {
          assertTrue(op.a in 3..9 && op.b in 12..29)
        }
      }
      anterior = op
    }
  }

  @Test
  fun lasTresOperacionesSalen() {
    val generador = GeneradorCalculo(Random(7))
    val tipos = (1..200).map { generador.siguiente().operador }.toSet()
    assertEquals(Operador.entries.toSet(), tipos)
  }

  @Test
  fun texto() {
    assertEquals("47 + 38", Operacion(47, 38, Operador.SUMA).texto)
    assertEquals("7 × 18", Operacion(7, 18, Operador.MULTIPLICACION).texto)
    assertEquals(126, Operacion(7, 18, Operador.MULTIPLICACION).resultado)
  }

  private fun EstadoCalculo.escribir(numero: Int): Boolean? {
    var resultado: Boolean? = null
    numero.toString().forEach { resultado = pulsar(Tecla.Digito(it.digitToInt())) }
    return resultado
  }

  @Test
  fun acertarTres_completa() {
    val estado = EstadoCalculo(GeneradorCalculo(Random(1)))
    repeat(3) {
      assertFalse(estado.completada)
      assertEquals(true, estado.escribir(estado.operacion.resultado))
    }
    assertTrue(estado.completada)
    assertEquals(3, estado.aciertos)
  }

  @Test
  fun fallar_cambiaDeOperacion_yCuentaElFallo() {
    val estado = EstadoCalculo(GeneradorCalculo(Random(2)))
    val antes = estado.operacion
    // Un número con las mismas cifras que el resultado, pero distinto.
    val mal = if (antes.resultado == 99) 98 else antes.resultado + 1
    val largo = antes.resultado.toString().length
    val respuesta = if (mal.toString().length == largo) mal else antes.resultado - 1
    assertEquals(false, estado.escribir(respuesta))
    assertEquals(1, estado.fallos)
    assertEquals(0, estado.aciertos)
    assertNotEquals(antes, estado.operacion)
    assertEquals("", estado.respuesta)
  }

  @Test
  fun borrar_yCeroDelante() {
    val estado = EstadoCalculo(GeneradorCalculo(Random(3)))
    assertNull(estado.pulsar(Tecla.Digito(0)))
    assertEquals("", estado.respuesta)
    estado.pulsar(Tecla.Digito(1))
    estado.pulsar(Tecla.Borrar)
    assertEquals("", estado.respuesta)
  }
}
