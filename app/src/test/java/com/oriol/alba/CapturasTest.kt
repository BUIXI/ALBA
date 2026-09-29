package com.oriol.alba

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.oriol.alba.datos.Alarma
import com.oriol.alba.theme.TemaAlba
import com.oriol.alba.ui.editor.EditorAlarma
import com.oriol.alba.ui.lista.ListaAlarmas
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDateTime
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

  private fun capturar(nombre: String) = composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/$nombre.png")

  private val laborables = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

  /** Martes 29/09/2026 a las 22:48. */
  private val ahora = LocalDateTime.of(2026, 9, 29, 22, 48)

  private val ejemplo =
    listOf(
      Alarma(id = 1, hora = 6, minuto = 15, dias = setOf(MONDAY, WEDNESDAY, FRIDAY), etiqueta = "Gimnasio", activa = false),
      Alarma(id = 2, hora = 7, minuto = 0, dias = laborables, etiqueta = "Trabajo"),
      Alarma(id = 3, hora = 9, minuto = 30, dias = setOf(SATURDAY, SUNDAY)),
      Alarma(id = 4, hora = 16, minuto = 45, etiqueta = "Recoger a Marc", activa = false),
    )

  @Test
  fun listaVacia() {
    composeRule.setContent { TemaAlba { ListaAlarmas(emptyList(), ahora, {}, {}, { _, _ -> }) } }
    capturar("lista_vacia")
  }

  @Test
  fun listaConAlarmas() {
    composeRule.setContent { TemaAlba { ListaAlarmas(ejemplo, ahora, {}, {}, { _, _ -> }) } }
    capturar("lista_con_alarmas")
  }

  @Test
  fun editorNueva() {
    composeRule.setContent {
      TemaAlba { EditorAlarma(Alarma(hora = 7, minuto = 0), true, { _, _ -> }, {}, {}, {}, {}, {}, {}) }
    }
    capturar("editor_nueva")
  }

  @Test
  fun editorEdicion() {
    composeRule.setContent { TemaAlba { EditorAlarma(ejemplo[1], false, { _, _ -> }, {}, {}, {}, {}, {}, {}) } }
    capturar("editor_edicion")
  }

  /** El icono de la app con las máscaras más comunes: círculo y "squircle". */
  @Test
  fun icono() {
    composeRule.setContent {
      Row(
        Modifier.background(Color(0xFF3A3A3C)).padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        for (forma in listOf(CircleShape, RoundedCornerShape(28))) {
          Box(Modifier.size(108.dp).clip(forma).background(Color.Black)) {
            Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, Modifier.size(108.dp))
          }
        }
      }
    }
    capturar("icono")
  }
}

/** Pantalla del Realme 14 Pro+ (1272×2800 px a ~3x): 424×933 dp, en español. */
const val REALME_14_PRO_PLUS = "es-rES-w424dp-h933dp-normal-long-notround-any-xxhdpi-keyshidden-nonav"
