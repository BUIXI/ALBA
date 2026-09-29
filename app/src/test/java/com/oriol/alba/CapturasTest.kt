package com.oriol.alba

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.oriol.alba.theme.AlbaTheme
import com.oriol.alba.ui.main.MainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Capturas de las pantallas, dibujadas en el PC. Sirven para revisar el diseño
 * mirando la imagen, sin emulador.
 *
 * Solo se escriben al grabar: `gradlew recordRoborazziDebug`. Salen en
 * `app/build/outputs/roborazzi/`.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = REALME_14_PRO_PLUS)
class CapturasTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun principalVacia() {
    composeRule.setContent { AlbaTheme { MainScreen() } }
    composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/principal_vacia.png")
  }
}

/** Pantalla del Realme 14 Pro+ (1272×2800 px a ~3x): 424×933 dp. */
const val REALME_14_PRO_PLUS = "w424dp-h933dp-normal-long-notround-any-xxhdpi-keyshidden-nonav"
