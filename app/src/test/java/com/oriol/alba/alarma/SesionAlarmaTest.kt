package com.oriol.alba.alarma

import com.oriol.alba.datos.Alarma
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** La lógica de una alarma sonando, con tiempo simulado: 30 minutos pasan en milisegundos. */
@OptIn(ExperimentalCoroutinesApi::class)
class SesionAlarmaTest {

  /** Apunta lo último que se le pidió al altavoz y al vibrador. */
  private class SalidaFalsa : SalidaSonido {
    var volumen = -1f
    var vibrando = false
    var detenida = false

    override fun volumen(valor: Float) {
      volumen = valor
    }

    override fun vibrar(si: Boolean) {
      vibrando = si
    }

    override fun detener() {
      detenida = true
    }
  }

  private val alarma = Alarma(id = 1, hora = 7, minuto = 0)

  private fun TestScope.sesion(salida: SalidaSonido, modo: Modo = Modo.ALARMA) =
    SesionAlarma(alarma, modo, prueba = false, reloj = { testScheduler.currentTime }, alcance = backgroundScope, salida = salida)
      .also { it.iniciar() }

  @Test
  fun empiezaBajita_ySubeHastaElMaximo() = runTest {
    val salida = SalidaFalsa()
    sesion(salida)
    runCurrent()
    assertEquals(0.15f, salida.volumen, 0.01f)
    assertTrue(salida.vibrando)
    advanceTimeBy(22_500)
    assertTrue(salida.volumen in 0.5f..0.65f)
    advanceTimeBy(30_000)
    assertEquals(1f, salida.volumen, 0.001f)
  }

  @Test
  fun silenciar_calla_yAlRatoVuelveASonarCasiDeGolpe() = runTest {
    val salida = SalidaFalsa()
    val s = sesion(salida)
    advanceTimeBy(10_000)
    s.silenciar()
    advanceTimeBy(1_000)
    assertEquals(0f, salida.volumen, 0.001f)
    assertFalse(salida.vibrando)
    assertNotNull(s.estado.value.silenciadaHasta)
    // A los 30 s sin tocar nada, vuelve; en 4 s está al máximo.
    advanceTimeBy(29_500)
    assertNull(s.estado.value.silenciadaHasta)
    assertTrue(salida.vibrando)
    advanceTimeBy(4_500)
    assertEquals(1f, salida.volumen, 0.001f)
  }

  @Test
  fun cadaToqueAlargaElSilencio() = runTest {
    val salida = SalidaFalsa()
    val s = sesion(salida)
    s.silenciar()
    repeat(5) {
      advanceTimeBy(20_000)
      s.silenciar()
    }
    // 100 s después del primer silencio sigue callada: cada toque dio 30 s más.
    advanceTimeBy(1_000)
    assertEquals(0f, salida.volumen, 0.001f)
  }

  @Test
  fun terminar_paraElSonido_yMarcaCompletada() = runTest {
    val salida = SalidaFalsa()
    val s = sesion(salida)
    advanceTimeBy(5_000)
    s.terminar(completada = true)
    assertTrue(salida.detenida)
    assertTrue(s.estado.value.terminada)
    assertTrue(s.estado.value.completada)
    // Terminar dos veces no cambia nada.
    s.terminar(completada = false)
    assertTrue(s.estado.value.completada)
  }

  @Test
  fun sinHacerNada_seParaALaMediaHora() = runTest {
    val salida = SalidaFalsa()
    val s = sesion(salida)
    advanceTimeBy(29 * 60_000L)
    assertFalse(s.estado.value.terminada)
    advanceTimeBy(61_000)
    assertTrue(s.estado.value.terminada)
    assertFalse(s.estado.value.completada)
    assertTrue(salida.detenida)
  }

  @Test
  fun comprobacion_suenaSuave_yEnUnMinutoPasaAAlarmaEntera() = runTest {
    val salida = SalidaFalsa()
    val s = sesion(salida, Modo.COMPROBACION)
    advanceTimeBy(30_000)
    assertEquals(Modo.COMPROBACION, s.estado.value.modo)
    assertEquals(0.35f, salida.volumen, 0.01f)
    // En la comprobación no se puede silenciar: hay que responder.
    s.silenciar()
    assertNull(s.estado.value.silenciadaHasta)
    advanceTimeBy(31_000)
    assertEquals(Modo.ALARMA, s.estado.value.modo)
    advanceTimeBy(5_000)
    assertEquals(1f, salida.volumen, 0.001f)
  }
}
