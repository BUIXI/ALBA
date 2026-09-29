package com.oriol.alba

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasSetTextAction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oriol.alba.datos.BaseDatos
import com.oriol.alba.datos.RepositorioAlarmas
import com.oriol.alba.theme.TemaAlba
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * La app entera (navegación, pantallas, ViewModel y Room en memoria) manejada como
 * lo haría una persona: crear una alarma, verla, apagarla y eliminarla.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = REALME_14_PRO_PLUS)
class FlujoTest {

  @get:Rule val composeRule = createComposeRule()

  private lateinit var baseDatos: BaseDatos

  @Before
  fun abrir() {
    baseDatos = BaseDatos.enMemoria(ApplicationProvider.getApplicationContext())
    val repositorio = RepositorioAlarmas(baseDatos.alarmas())
    composeRule.setContent { TemaAlba { NavegacionAlba(repositorio) } }
  }

  @After fun cerrar() = baseDatos.close()

  private fun esperarTexto(texto: String) = esperar("aparezca \"$texto\"") {
    composeRule.onAllNodes(hasText(texto)).fetchSemanticsNodes().isNotEmpty()
  }

  private fun esperarQueDesaparezca(texto: String) = esperar("desaparezca \"$texto\"") {
    composeRule.onAllNodes(hasText(texto)).fetchSemanticsNodes().isEmpty()
  }

  /** Si no se cumple, el error incluye lo que hay en pantalla: se ve enseguida qué pasa. */
  private fun esperar(que: String, condicion: () -> Boolean) {
    try {
      composeRule.waitUntil(timeoutMillis = 5_000, condition = condicion)
    } catch (e: ComposeTimeoutException) {
      throw AssertionError("No se cumplió que $que. En pantalla:\n" + composeRule.onRoot().printToString(), e)
    }
  }

  @Test
  fun crearApagarYEliminar() {
    // Vacía al empezar
    esperarTexto("Sin alarmas")

    // Crear: por defecto a las 07:00, con etiqueta
    composeRule.onNodeWithText("Crear alarma").performClick()
    esperarTexto("Nueva alarma")
    composeRule.onNode(hasSetTextAction()).performTextInput("Trabajo")
    composeRule.onNodeWithText("Guardar").performClick()

    // Aparece en la lista, activada
    esperarTexto("07:00")
    composeRule.onNodeWithText("Trabajo · Una vez").assertExists()
    composeRule.onNodeWithContentDescription("Alarma de las 07:00").assertIsOn()

    // Apagarla con el interruptor
    composeRule.onNodeWithContentDescription("Alarma de las 07:00").performClick()
    esperarTexto("Ninguna alarma activada")
    composeRule.onNodeWithContentDescription("Alarma de las 07:00").assertIsOff()

    // Abrirla y eliminarla
    composeRule.onNodeWithText("07:00").performClick()
    esperarTexto("Editar alarma")
    composeRule.onNodeWithText("Eliminar alarma").performScrollTo().performClick()
    esperarTexto("Sin alarmas")
    esperarQueDesaparezca("07:00")
  }

  @Test
  fun cancelar_noGuardaNada() {
    esperarTexto("Sin alarmas")
    composeRule.onNodeWithText("Crear alarma").performClick()
    esperarTexto("Nueva alarma")
    composeRule.onNodeWithText("Cancelar").performClick()
    esperarQueDesaparezca("Nueva alarma")
    composeRule.onNodeWithText("Sin alarmas").assertExists()
  }
}
