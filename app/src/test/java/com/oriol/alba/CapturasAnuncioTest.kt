package com.oriol.alba

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.TipoTarea
import com.oriol.alba.dominio.ObjetoFoto
import com.oriol.alba.theme.PaletaClara
import com.oriol.alba.theme.TemaAlba
import com.oriol.alba.ui.alarma.AlarmaComprobacion
import com.oriol.alba.ui.alarma.AlarmaFoto
import com.oriol.alba.ui.alarma.AlarmaHecha
import com.oriol.alba.ui.alarma.AlarmaSonando
import com.oriol.alba.ui.alarma.Fase
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Pantallas para los anuncios (docs/anuncios): la alarma con la tarea de foto y el
 * objeto "una taza". Solo se escriben al grabar (`gradlew recordRoborazziDebug`) y
 * salen en `app/build/outputs/roborazzi/anuncio_*.png`.
 *
 * La cámara se dibuja dos veces, en negro y en blanco: comparando las dos se saca la
 * máscara exacta del hueco (esquinas redondeadas incluidas) para poner detrás un
 * vídeo real de la taza al montar el anuncio.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = REALME_14_PRO_PLUS)
class CapturasAnuncioTest {

  @get:Rule val composeRule = createComposeRule()

  private fun capturar(nombre: String) = composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/$nombre.png")

  /** Jueves 1/10/2026 a las 7:00. */
  private val sonando = LocalDateTime.of(2026, 10, 1, 7, 0)

  private val alarma = Alarma(id = 1, hora = 7, minuto = 0, tareas = setOf(TipoTarea.FOTO))

  @Test
  fun sonando() {
    composeRule.setContent { TemaAlba(PaletaClara) { AlarmaSonando(alarma, sonando, prueba = false, onDespierto = {}) } }
    capturar("anuncio_sonando")
  }

  private fun foto(nombre: String, vistos: Int, silencio: Int, fondo: Color) {
    composeRule.setContent {
      TemaAlba(PaletaClara) {
        AlarmaFoto(
          objeto = ObjetoFoto.TAZA,
          vistosSeguidos = vistos,
          segundosSilencio = silencio,
          cambiosRestantes = 2,
          mostrarCalculo = false,
          onCambiar = {},
          onCalculo = {},
        ) { modifier ->
          Box(modifier.background(fondo))
        }
      }
    }
    capturar(nombre)
  }

  @Test fun buscandoNegro() = foto("anuncio_foto_buscando_negro", vistos = 0, silencio = 87, fondo = Color.Black)

  @Test fun buscandoBlanco() = foto("anuncio_foto_buscando_blanco", vistos = 0, silencio = 87, fondo = Color.White)

  @Test fun viendoNegro() = foto("anuncio_foto_viendo_negro", vistos = 2, silencio = 81, fondo = Color.Black)

  @Test fun viendoBlanco() = foto("anuncio_foto_viendo_blanco", vistos = 2, silencio = 81, fondo = Color.White)

  @Test
  fun hecha() {
    val fase = Fase.Hecho(LocalTime.of(7, 2), fueComprobacion = false, comprobaraDespues = true, proxima = sonando.plusDays(1))
    composeRule.setContent { TemaAlba(PaletaClara) { AlarmaHecha(fase, onCerrar = {}) } }
    capturar("anuncio_hecha")
  }

  @Test
  fun comprobacion() {
    composeRule.setContent { TemaAlba(PaletaClara) { AlarmaComprobacion(segundos = 48, onSi = {}) } }
    capturar("anuncio_comprobacion")
  }
}
