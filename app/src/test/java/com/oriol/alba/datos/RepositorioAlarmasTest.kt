package com.oriol.alba.datos

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oriol.alba.ProgramadorFalso
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDateTime
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

/** El repositorio contra una base de datos Room de verdad, en memoria, y un programador falso. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class RepositorioAlarmasTest {

  private lateinit var baseDatos: BaseDatos
  private lateinit var programador: ProgramadorFalso
  private lateinit var repositorio: RepositorioAlarmas

  /** Martes 29/09/2026 a las 22:48. */
  private var ahora = LocalDateTime.of(2026, 9, 29, 22, 48)

  @Before
  fun abrir() {
    baseDatos = BaseDatos.enMemoria(ApplicationProvider.getApplicationContext())
    programador = ProgramadorFalso()
    repositorio = RepositorioAlarmas(baseDatos.alarmas(), programador) { ahora }
  }

  @After fun cerrar() = baseDatos.close()

  @Test
  fun guardarNueva_devuelveId_yLaGuardaEntera() = runTest {
    val id =
      repositorio.guardar(
        Alarma(hora = 6, minuto = 45, dias = setOf(MONDAY, SUNDAY), etiqueta = "Gimnasio", sonido = Sonido.SISTEMA, comprobar = false)
      )
    assertTrue(id > 0)
    val guardada = repositorio.obtener(id)!!
    assertEquals(6, guardada.hora)
    assertEquals(45, guardada.minuto)
    assertEquals(setOf(MONDAY, SUNDAY), guardada.dias)
    assertEquals("Gimnasio", guardada.etiqueta)
    assertEquals(TipoTarea.CALCULO, guardada.tarea)
    assertEquals(Sonido.SISTEMA, guardada.sonido)
    assertFalse(guardada.comprobar)
  }

  @Test
  fun guardar_laPrograma() = runTest {
    val id = repositorio.guardar(Alarma(hora = 7, minuto = 0))
    assertEquals(LocalDateTime.of(2026, 9, 30, 7, 0), programador.programadas[id])
  }

  @Test
  fun guardarExistente_laActualiza_yLaReprograma() = runTest {
    val id = repositorio.guardar(Alarma(hora = 7, minuto = 0))
    val otra = repositorio.guardar(repositorio.obtener(id)!!.copy(hora = 23))
    assertEquals(id, otra)
    assertEquals(23, repositorio.obtener(id)!!.hora)
    assertEquals(1, repositorio.alarmas.first().size)
    assertEquals(LocalDateTime.of(2026, 9, 29, 23, 0), programador.programadas[id])
  }

  @Test
  fun lista_vaOrdenadaPorHora() = runTest {
    repositorio.guardar(Alarma(hora = 9, minuto = 0))
    repositorio.guardar(Alarma(hora = 6, minuto = 30))
    repositorio.guardar(Alarma(hora = 6, minuto = 5))
    assertEquals(listOf("6:5", "6:30", "9:0"), repositorio.alarmas.first().map { "${it.hora}:${it.minuto}" })
  }

  @Test
  fun desactivar_laAnula_yActivar_laVuelveAProgramar() = runTest {
    val id = repositorio.guardar(Alarma(hora = 7, minuto = 0))
    repositorio.cambiarActiva(id, false)
    assertFalse(repositorio.obtener(id)!!.activa)
    assertNull(programador.programadas[id])
    repositorio.cambiarActiva(id, true)
    assertEquals(LocalDateTime.of(2026, 9, 30, 7, 0), programador.programadas[id])
  }

  @Test
  fun eliminar_laBorra_yLaAnula() = runTest {
    val id = repositorio.guardar(Alarma(hora = 7, minuto = 0))
    repositorio.eliminar(id)
    assertNull(repositorio.obtener(id))
    assertNull(programador.programadas[id])
  }

  @Test
  fun despuesDeSonar_laRepetida_seProgramaParaLaSiguienteVez() = runTest {
    val id = repositorio.guardar(Alarma(hora = 7, minuto = 0, dias = setOf(MONDAY, WEDNESDAY)))
    // Suena el miércoles a las 7:00 (un pelo antes, incluso): la siguiente es el lunes.
    ahora = LocalDateTime.of(2026, 9, 30, 6, 59, 59)
    repositorio.despuesDeSonar(id)
    assertEquals(LocalDateTime.of(2026, 10, 5, 7, 0), programador.programadas[id])
    assertTrue(repositorio.obtener(id)!!.activa)
  }

  @Test
  fun despuesDeSonar_laDeUnaVez_seApaga() = runTest {
    val id = repositorio.guardar(Alarma(hora = 7, minuto = 0))
    ahora = LocalDateTime.of(2026, 9, 30, 7, 0)
    repositorio.despuesDeSonar(id)
    assertFalse(repositorio.obtener(id)!!.activa)
    assertNull(programador.programadas[id])
  }

  @Test
  fun reprogramarTodas_soloLasActivas() = runTest {
    val a = repositorio.guardar(Alarma(hora = 7, minuto = 0))
    val b = repositorio.guardar(Alarma(hora = 8, minuto = 0, activa = false))
    programador.programadas.clear()
    repositorio.reprogramarTodas()
    assertEquals(setOf(a), programador.programadas.keys)
    assertNull(programador.programadas[b])
  }
}
