package com.oriol.alba.ui.editor

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oriol.alba.ProgramadorFalso
import com.oriol.alba.datos.BaseDatos
import com.oriol.alba.datos.RepositorioAlarmas
import com.oriol.alba.datos.TipoTarea
import com.oriol.alba.datos.TipoTarea.FOTO
import com.oriol.alba.datos.TipoTarea.PAREJAS
import com.oriol.alba.datos.TipoTarea.SOLES
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Elegir las actividades de una alarma en el editor. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class EditorViewModelTest {

  private lateinit var baseDatos: BaseDatos
  private lateinit var vm: EditorViewModel

  @Before
  fun crear() {
    baseDatos = BaseDatos.enMemoria(ApplicationProvider.getApplicationContext())
    vm = EditorViewModel(RepositorioAlarmas(baseDatos.alarmas(), ProgramadorFalso()), alarmaId = 0)
  }

  @After fun cerrar() = baseDatos.close()

  private val tareas get() = vm.borrador.value!!.tareas

  @Test
  fun nueva_empiezaConLaDePorDefecto() {
    assertEquals(setOf(TipoTarea.PorDefecto), tareas)
  }

  @Test
  fun sinPremium_laTocadaSustituyeALaAnterior() {
    vm.alternarTarea(PAREJAS, varias = false)
    assertEquals(setOf(PAREJAS), tareas)
    vm.alternarTarea(FOTO, varias = false)
    assertEquals(setOf(FOTO), tareas)
  }

  @Test
  fun conPremium_seMarcanYDesmarcanVarias_peroNuncaNinguna() {
    vm.alternarTarea(PAREJAS, varias = true)
    vm.alternarTarea(FOTO, varias = true)
    assertEquals(setOf(SOLES, PAREJAS, FOTO), tareas)
    vm.alternarTarea(SOLES, varias = true)
    vm.alternarTarea(FOTO, varias = true)
    assertEquals(setOf(PAREJAS), tareas)
    // La última no se puede quitar: alguna tiene que haber.
    vm.alternarTarea(PAREJAS, varias = true)
    assertEquals(setOf(PAREJAS), tareas)
  }
}
