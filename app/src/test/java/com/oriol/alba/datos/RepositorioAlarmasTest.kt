package com.oriol.alba.datos

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** El repositorio contra una base de datos Room de verdad, en memoria. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class RepositorioAlarmasTest {

  private lateinit var baseDatos: BaseDatos
  private lateinit var repositorio: RepositorioAlarmas

  @Before
  fun abrir() {
    baseDatos = BaseDatos.enMemoria(ApplicationProvider.getApplicationContext())
    repositorio = RepositorioAlarmas(baseDatos.alarmas())
  }

  @After fun cerrar() = baseDatos.close()

  @Test
  fun guardarNueva_devuelveId_yLaGuardaEntera() = runTest {
    val id = repositorio.guardar(Alarma(hora = 6, minuto = 45, dias = setOf(MONDAY, SUNDAY), etiqueta = "Gimnasio"))
    assertTrue(id > 0)
    val guardada = repositorio.obtener(id)!!
    assertEquals(6, guardada.hora)
    assertEquals(45, guardada.minuto)
    assertEquals(setOf(MONDAY, SUNDAY), guardada.dias)
    assertEquals("Gimnasio", guardada.etiqueta)
    assertEquals(TipoTarea.CALCULO, guardada.tarea)
  }

  @Test
  fun guardarExistente_laActualiza_yDevuelveSuId() = runTest {
    val id = repositorio.guardar(Alarma(hora = 7, minuto = 0))
    val otra = repositorio.guardar(repositorio.obtener(id)!!.copy(hora = 8))
    assertEquals(id, otra)
    assertEquals(8, repositorio.obtener(id)!!.hora)
    assertEquals(1, repositorio.alarmas.first().size)
  }

  @Test
  fun lista_vaOrdenadaPorHora() = runTest {
    repositorio.guardar(Alarma(hora = 9, minuto = 0))
    repositorio.guardar(Alarma(hora = 6, minuto = 30))
    repositorio.guardar(Alarma(hora = 6, minuto = 5))
    assertEquals(listOf("6:5", "6:30", "9:0"), repositorio.alarmas.first().map { "${it.hora}:${it.minuto}" })
  }

  @Test
  fun activarDesactivarYEliminar() = runTest {
    val id = repositorio.guardar(Alarma(hora = 7, minuto = 0))
    repositorio.cambiarActiva(id, false)
    assertFalse(repositorio.obtener(id)!!.activa)
    repositorio.eliminar(id)
    assertNull(repositorio.obtener(id))
  }
}
