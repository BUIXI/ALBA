package com.oriol.alba.ui.alarma

import com.oriol.alba.alarma.EstadoSesion
import com.oriol.alba.alarma.Modo
import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.TipoTarea
import com.oriol.alba.dominio.Etiqueta
import com.oriol.alba.dominio.GeneradorCalculo
import com.oriol.alba.dominio.ObjetoFoto
import org.junit.Assert.assertNotEquals
import com.oriol.alba.ui.tareas.Tecla
import java.time.LocalDateTime
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** El paso por la pantalla de la alarma: despertar, tarea, hecho. */
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmaViewModelTest {

  private class AccionesFalsas : AccionesAlarma {
    var silencios = 0
    var terminada = false

    override fun silenciar() {
      silencios++
    }

    override fun terminar() {
      terminada = true
    }
  }

  private val ahora = LocalDateTime.of(2026, 9, 30, 7, 3)
  private val alarma = Alarma(id = 1, hora = 7, minuto = 0, dias = setOf(java.time.DayOfWeek.THURSDAY))
  private lateinit var acciones: AccionesFalsas
  private lateinit var vm: AlarmaViewModel

  @Before
  fun crear() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    acciones = AccionesFalsas()
    vm = AlarmaViewModel(acciones, flowOf(listOf(alarma)), GeneradorCalculo(Random(9)), reloj = { ahora })
    vm.alCambiarSesion(EstadoSesion(alarma, Modo.ALARMA, prueba = false, inicio = 0, inicioModo = 0))
  }

  @After fun limpiar() = Dispatchers.resetMain()

  private fun responderBien() = vm.calculo.operacion.resultado.toString().forEach { vm.tecla(Tecla.Digito(it.digitToInt())) }

  @Test
  fun despierto_silencia_yPasaALaTarea() {
    assertEquals(Fase.Sonando, vm.fase)
    vm.despierto()
    assertEquals(Fase.Tarea, vm.fase)
    assertEquals(1, acciones.silencios)
  }

  @Test
  fun teclasAntesDeDespertar_noHacenNada() {
    vm.tecla(Tecla.Digito(1))
    assertEquals(Fase.Sonando, vm.fase)
    assertEquals(0, acciones.silencios)
  }

  @Test
  fun tresAciertos_terminan_yDicenLaProxima() {
    vm.despierto()
    repeat(3) { responderBien() }
    assertTrue(acciones.terminada)
    val hecho = vm.fase as Fase.Hecho
    assertEquals(7, hecho.hora.hour)
    assertFalse(hecho.fueComprobacion)
    assertTrue(hecho.comprobaraDespues)
    // La próxima: el jueves 1/10 a las 7:00.
    assertEquals(LocalDateTime.of(2026, 10, 1, 7, 0), hecho.proxima)
    // Cada tecla alargó el silencio.
    assertTrue(acciones.silencios > 3)
  }

  private fun vmFoto(): AlarmaViewModel {
    val conFoto = alarma.copy(tarea = TipoTarea.FOTO)
    return AlarmaViewModel(acciones, flowOf(listOf(conFoto)), GeneradorCalculo(Random(9)), { ahora }, Random(4)).also {
      it.alCambiarSesion(EstadoSesion(conFoto, Modo.ALARMA, prueba = false, inicio = 0, inicioModo = 0))
    }
  }

  private fun lasEtiquetasDe(objeto: ObjetoFoto) = listOf(Etiqueta(objeto.etiquetas.first(), 0.9f))

  @Test
  fun foto_verElObjetoTresVeces_terminaLaAlarma() {
    val vm = vmFoto()
    assertEquals(TipoTarea.FOTO, vm.tareaActual)
    // Antes de "Estoy despierto" la cámara no cuenta.
    vm.etiquetas(lasEtiquetasDe(vm.objeto))
    assertEquals(0, vm.vistosSeguidos)
    vm.despierto()
    vm.etiquetas(lasEtiquetasDe(vm.objeto))
    vm.etiquetas(lasEtiquetasDe(vm.objeto))
    assertEquals(2, vm.vistosSeguidos)
    assertFalse(acciones.terminada)
    vm.etiquetas(lasEtiquetasDe(vm.objeto))
    assertTrue(acciones.terminada)
    assertTrue(vm.fase is Fase.Hecho)
  }

  @Test
  fun foto_otroObjeto_hastaDosVeces_yReiniciaLaCuenta() {
    val vm = vmFoto()
    vm.despierto()
    val primero = vm.objeto
    vm.etiquetas(lasEtiquetasDe(primero))
    vm.cambiarObjeto()
    assertNotEquals(primero, vm.objeto)
    assertEquals(0, vm.vistosSeguidos)
    vm.cambiarObjeto()
    val tercero = vm.objeto
    vm.cambiarObjeto() // ya no quedan
    assertEquals(tercero, vm.objeto)
    assertEquals(0, vm.cambiosRestantes)
  }

  @Test
  fun foto_pasarACalculo_yAcabarConElCalculo() {
    val vm = vmFoto()
    vm.despierto()
    vm.pasarACalculo()
    assertEquals(TipoTarea.CALCULO, vm.tareaActual)
    // La cámara ya no cuenta; el cálculo sí.
    vm.etiquetas(lasEtiquetasDe(vm.objeto))
    assertEquals(0, vm.vistosSeguidos)
    repeat(3) { vm.calculo.operacion.resultado.toString().forEach { c -> vm.tecla(Tecla.Digito(c.digitToInt())) } }
    assertTrue(vm.fase is Fase.Hecho)
  }

  @Test
  fun unaAlarmaNueva_conLaPantallaEnHecho_empiezaDeCero() {
    vm.despierto()
    repeat(3) { responderBien() }
    assertTrue(vm.fase is Fase.Hecho)
    // Diez minutos después, la comprobación reutiliza la misma pantalla.
    vm.alCambiarSesion(EstadoSesion(alarma, Modo.COMPROBACION, prueba = false, inicio = 600_000, inicioModo = 600_000))
    assertEquals(Fase.Sonando, vm.fase)
    assertEquals(0, vm.calculo.aciertos)
  }

  @Test
  fun comprobacion_respondida_terminaSinVolverAComprobar() {
    vm.responderComprobacion()
    assertTrue(acciones.terminada)
    val hecho = vm.fase as Fase.Hecho
    assertTrue(hecho.fueComprobacion)
    assertFalse(hecho.comprobaraDespues)
  }
}
