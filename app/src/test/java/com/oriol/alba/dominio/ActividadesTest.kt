package com.oriol.alba.dominio

import com.oriol.alba.datos.Convertidores
import com.oriol.alba.datos.TipoTarea
import com.oriol.alba.datos.TipoTarea.CALCULO
import com.oriol.alba.datos.TipoTarea.FOTO
import com.oriol.alba.datos.TipoTarea.ORDEN
import com.oriol.alba.datos.TipoTarea.PAREJAS
import com.oriol.alba.datos.TipoTarea.SECUENCIA
import com.oriol.alba.datos.TipoTarea.SOLES
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActividadesTest {

  @Test
  fun elegir_unaSola_siempreEsa() {
    repeat(20) { assertEquals(PAREJAS, elegirTarea(setOf(PAREJAS), Random(it))) }
  }

  @Test
  fun elegir_entreVarias_soloDeLasMarcadas_ySalenTodas() {
    val marcadas = setOf(SOLES, ORDEN, FOTO)
    val salidas = (0 until 200).map { elegirTarea(marcadas, Random(it)) }
    assertTrue(salidas.all { it in marcadas })
    assertEquals(marcadas, salidas.toSet())
  }

  @Test
  fun elegir_sinNinguna_laDePorDefecto() {
    assertEquals(TipoTarea.PorDefecto, elegirTarea(emptySet(), Random(1)))
  }

  @Test
  fun alternativa_otraDeLasMarcadasSinCamara_oLaDePorDefecto() {
    assertEquals(PAREJAS, alternativaA(FOTO, setOf(FOTO, PAREJAS)))
    assertEquals(TipoTarea.PorDefecto, alternativaA(FOTO, setOf(FOTO)))
    // Si la de por defecto es la que falla, cálculo.
    assertEquals(CALCULO, alternativaA(TipoTarea.PorDefecto, setOf(TipoTarea.PorDefecto)))
  }

  @Test
  fun tareas_enTexto_idaYVuelta_yCompatibleConLaVersion1() {
    val c = Convertidores()
    val todas = TipoTarea.entries.toSet()
    assertEquals(todas, c.textoATareas(c.tareasATexto(todas)))
    assertEquals("SECUENCIA", c.tareasATexto(setOf(SECUENCIA)))
    // En la versión 1 se guardaba una sola, con su nombre: se lee igual.
    assertEquals(setOf(CALCULO), c.textoATareas("CALCULO"))
    assertEquals(setOf(FOTO), c.textoATareas("FOTO"))
    // Nombres raros o vacío: la de por defecto (nunca vacío).
    assertEquals(setOf(ORDEN), c.textoATareas("ORDEN,NO_EXISTE"))
    assertEquals(setOf(TipoTarea.PorDefecto), c.textoATareas(""))
  }
}
