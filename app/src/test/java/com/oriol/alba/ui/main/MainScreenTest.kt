package com.oriol.alba.ui.main

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oriol.alba.theme.AlbaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** La pantalla principal se prueba en el PC con Robolectric, sin móvil ni emulador. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class MainScreenTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun muestraTituloYEstadoVacio() {
    composeRule.setContent { AlbaTheme { MainScreen() } }
    composeRule.onNodeWithText("Alarmas").assertExists()
    composeRule.onNodeWithText("Sin alarmas").assertExists()
  }
}
