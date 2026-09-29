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
import com.oriol.alba.alarma.EstadoPermisos
import com.oriol.alba.datos.Alarma
import com.oriol.alba.dominio.Operacion
import com.oriol.alba.dominio.Operador
import com.oriol.alba.theme.PaletaClara
import com.oriol.alba.theme.TemaAlba
import com.oriol.alba.ui.alarma.AlarmaCalculo
import androidx.compose.ui.graphics.Brush
import com.oriol.alba.dominio.ObjetoFoto
import com.oriol.alba.ui.alarma.AlarmaComprobacion
import com.oriol.alba.ui.alarma.AlarmaOrden
import com.oriol.alba.ui.alarma.AlarmaParejas
import com.oriol.alba.ui.alarma.AlarmaSecuencia
import com.oriol.alba.ui.alarma.AlarmaSoles
import com.oriol.alba.ui.tareas.EstadoOrden
import com.oriol.alba.ui.tareas.EstadoParejas
import com.oriol.alba.ui.tareas.EstadoSecuencia
import com.oriol.alba.ui.tareas.EstadoSoles
import kotlin.random.Random
import com.oriol.alba.ui.alarma.AlarmaFoto
import com.oriol.alba.ui.alarma.AlarmaHecha
import com.oriol.alba.ui.alarma.AlarmaSonando
import com.oriol.alba.ui.alarma.Fase
import com.oriol.alba.ui.editor.EditorAlarma
import com.oriol.alba.ui.lista.ListaAlarmas
import com.oriol.alba.ui.permisos.ListaPermisos
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDateTime
import java.time.LocalTime
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

  // --- App (oscura) ---

  @Test
  fun listaVacia() {
    composeRule.setContent { TemaAlba { ListaAlarmas(emptyList(), ahora, {}, {}, { _, _ -> }, avisoPermisos = true) } }
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

  /** El editor entero, en una pantalla muy alta para verlo sin desplazar. */
  @Test
  @Config(qualifiers = "+h1500dp")
  fun editorEdicion() {
    composeRule.setContent { TemaAlba { EditorAlarma(ejemplo[1], false, { _, _ -> }, {}, {}, {}, {}, {}, {}) } }
    capturar("editor_edicion")
  }

  @Test
  fun permisos() {
    val estado = EstadoPermisos(notificaciones = true, pantallaCompleta = false, alarmasExactas = true, segundoPlano = true, sinOptimizarBateria = false)
    composeRule.setContent { TemaAlba { ListaPermisos(estado, "Realme", {}, {}, {}, {}, {}, {}, {}) } }
    capturar("permisos")
  }

  // --- Alarma (clara) ---

  @Test
  fun alarmaSonando() {
    composeRule.setContent {
      TemaAlba(PaletaClara) { AlarmaSonando(ejemplo[1], LocalDateTime.of(2026, 9, 30, 7, 0), prueba = false, onDespierto = {}) }
    }
    capturar("alarma_sonando")
  }

  @Test
  fun alarmaCalculo() {
    composeRule.setContent {
      TemaAlba(PaletaClara) {
        AlarmaCalculo(Operacion(47, 38, Operador.SUMA), respuesta = "8", aciertos = 1, total = 3, fallos = 0, segundosSilencio = 24, onTecla = {})
      }
    }
    capturar("alarma_calculo")
  }

  @Test
  fun alarmaFoto() {
    composeRule.setContent {
      TemaAlba(PaletaClara) {
        AlarmaFoto(
          objeto = ObjetoFoto.FREGADERO,
          vistosSeguidos = 1,
          segundosSilencio = 18,
          cambiosRestantes = 2,
          mostrarCalculo = true,
          onCambiar = {},
          onCalculo = {},
        ) { modifier ->
          // En el PC no hay cámara: un hueco con un degradado que haga de imagen.
          Box(modifier.background(Brush.verticalGradient(listOf(Color(0xFF5B6B7A), Color(0xFF2E3A44)))))
        }
      }
    }
    capturar("alarma_foto")
  }

  // --- Minijuegos (a medias, para ver todos sus estados) ---

  @Test
  fun juegoSoles() {
    val estado = EstadoSoles(Random(3)).apply { repeat(5) { atrapar() } }
    composeRule.setContent { TemaAlba(PaletaClara) { AlarmaSoles(estado, segundosSilencio = 26, onAtrapar = {}, onEscapar = {}) } }
    capturar("juego_soles")
  }

  @Test
  fun juegoSecuencia() {
    val estado = EstadoSecuencia(Random(4)).apply {
      terminarDeMostrar()
      pulsar(secuencia[0])
      pulsar(secuencia[1])
    }
    composeRule.setContent { TemaAlba(PaletaClara) { AlarmaSecuencia(estado, segundosSilencio = 22, onPulsar = { null }, onMostrada = {}) } }
    capturar("juego_secuencia")
  }

  @Test
  fun juegoParejas() {
    val estado = EstadoParejas(Random(5)).apply {
      // Dos parejas encontradas y una carta destapada.
      for (simbolo in 0..1) {
        val (a, b) = cartas.indices.filter { cartas[it] == simbolo }
        tocar(a)
        tocar(b)
      }
      tocar(cartas.indexOfFirst { it == 4 })
    }
    composeRule.setContent { TemaAlba(PaletaClara) { AlarmaParejas(estado, segundosSilencio = 19, onTocar = { null }, onOcultar = {}) } }
    capturar("juego_parejas")
  }

  @Test
  fun juegoOrden() {
    val estado = EstadoOrden(Random(6)).apply { (1..4).forEach { tocar(it) } }
    composeRule.setContent { TemaAlba(PaletaClara) { AlarmaOrden(estado, segundosSilencio = 27, onTocar = { null }) } }
    capturar("juego_orden")
  }

  @Test
  fun alarmaComprobacion() {
    composeRule.setContent { TemaAlba(PaletaClara) { AlarmaComprobacion(segundos = 42, onSi = {}) } }
    capturar("alarma_comprobacion")
  }

  @Test
  fun alarmaHecha() {
    val fase = Fase.Hecho(LocalTime.of(7, 3), fueComprobacion = false, comprobaraDespues = true, proxima = LocalDateTime.of(2026, 10, 1, 7, 0))
    composeRule.setContent { TemaAlba(PaletaClara) { AlarmaHecha(fase, onCerrar = {}) } }
    capturar("alarma_hecha")
  }

  // --- En inglés (el idioma por defecto): que ningún texto se corte ---

  @Test
  @Config(qualifiers = "+en-rUS")
  fun listaEnIngles() {
    composeRule.setContent { TemaAlba { ListaAlarmas(ejemplo, ahora, {}, {}, { _, _ -> }, avisoPermisos = true) } }
    capturar("en_lista")
  }

  @Test
  @Config(qualifiers = "+en-rUS-h1500dp")
  fun editorEnIngles() {
    composeRule.setContent { TemaAlba { EditorAlarma(ejemplo[1], false, { _, _ -> }, {}, {}, {}, {}, {}, {}) } }
    capturar("en_editor")
  }

  @Test
  @Config(qualifiers = "+en-rUS")
  fun alarmaSonandoEnIngles() {
    composeRule.setContent {
      TemaAlba(PaletaClara) { AlarmaSonando(ejemplo[1], LocalDateTime.of(2026, 9, 30, 7, 0), prueba = false, onDespierto = {}) }
    }
    capturar("en_alarma_sonando")
  }

  @Test
  @Config(qualifiers = "+en-rUS")
  fun permisosEnIngles() {
    val estado = EstadoPermisos(notificaciones = true, pantallaCompleta = false, alarmasExactas = true, segundoPlano = true, sinOptimizarBateria = false)
    composeRule.setContent { TemaAlba { ListaPermisos(estado, "Realme", {}, {}, {}, {}, {}, {}, {}) } }
    capturar("en_permisos")
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
