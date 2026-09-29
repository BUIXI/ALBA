package com.oriol.alba.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.oriol.alba.theme.AlbaTheme
import org.junit.Rule
import org.junit.Test

/** La misma comprobación que la prueba local, pero en el móvil de verdad. */
class MainScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun muestraTituloYEstadoVacio() {
    composeRule.setContent { AlbaTheme { MainScreen() } }
    composeRule.onNodeWithText("Alarmas").assertExists()
    composeRule.onNodeWithText("Sin alarmas").assertExists()
  }
}
