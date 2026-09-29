package com.oriol.alba.dominio

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FotoTest {

  @Test
  fun seVe_conConfianzaSuficiente() {
    assertTrue(seVe(ObjetoFoto.FREGADERO, listOf(Etiqueta("Sink", 0.8f), Etiqueta("Tile", 0.6f))))
    assertTrue(seVe(ObjetoFoto.SOFA, listOf(Etiqueta("Loveseat", 0.6f))))
  }

  @Test
  fun noSeVe_siLaConfianzaEsBaja_oEsOtraCosa() {
    assertFalse(seVe(ObjetoFoto.FREGADERO, listOf(Etiqueta("Sink", 0.5f))))
    assertFalse(seVe(ObjetoFoto.FREGADERO, listOf(Etiqueta("Cup", 0.9f))))
    assertFalse(seVe(ObjetoFoto.TAZA, emptyList()))
  }

  @Test
  fun unaPantalla_noVale() {
    assertFalse(seVe(ObjetoFoto.TAZA, listOf(Etiqueta("Cup", 0.9f), Etiqueta("Screenshot", 0.7f))))
    assertFalse(seVe(ObjetoFoto.TAZA, listOf(Etiqueta("Cup", 0.9f), Etiqueta("Web page", 0.6f))))
  }

  @Test
  fun detector_pideTresSeguidos_yUnFalloReinicia() {
    val d = DetectorSeguido(necesarios = 3)
    assertFalse(d.fotograma(true))
    assertFalse(d.fotograma(true))
    assertFalse(d.fotograma(false))
    assertEquals(0, d.seguidos)
    assertFalse(d.fotograma(true))
    assertFalse(d.fotograma(true))
    assertTrue(d.fotograma(true))
  }

  @Test
  fun objetoAlAzar_respetaLosExcluidos() {
    val azar = Random(3)
    repeat(100) {
      val excluidos = setOf(ObjetoFoto.TELE, ObjetoFoto.PLANTA)
      val o = objetoAlAzar(azar, excluidos)
      assertTrue(o !in excluidos)
    }
    // Si se excluyen todos, vuelve a valer cualquiera (no se queda sin objeto).
    objetoAlAzar(azar, ObjetoFoto.entries.toSet())
  }

  @Test
  fun losObjetosSonDistintosEntreSi() {
    val etiquetas = ObjetoFoto.entries.flatMap { it.etiquetas }
    assertEquals("una etiqueta no puede servir para dos objetos", etiquetas.size, etiquetas.toSet().size)
    assertNotEquals(0, etiquetas.size)
  }
}
