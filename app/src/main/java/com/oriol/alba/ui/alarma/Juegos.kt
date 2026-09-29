package com.oriol.alba.ui.alarma

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oriol.alba.R
import com.oriol.alba.theme.Alba
import com.oriol.alba.ui.tareas.EstadoOrden
import com.oriol.alba.ui.tareas.EstadoParejas
import com.oriol.alba.ui.tareas.EstadoSecuencia
import com.oriol.alba.ui.tareas.EstadoSoles
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Las pantallas de los minijuegos, en el marco claro de la alarma. La lógica de cada
// uno está en ui/tareas/EstadosJuegos.kt.

/** Colores de los juegos: los del sistema de iOS, vivos pero sin chillar. */
private val ColoresJuego =
  listOf(
    Color(0xFFFF9500), // naranja
    Color(0xFF007AFF), // azul
    Color(0xFF34C759), // verde
    Color(0xFFFF2D55), // rosa
    Color(0xFFAF52DE), // morado
    Color(0xFF32ADE6), // celeste
  )

/** Marco común: estado del sonido, qué hay que hacer y cómo vas. */
@Composable
private fun MarcoJuego(
  instruccion: String,
  progreso: String,
  segundosSilencio: Int?,
  contenido: @Composable ColumnScope.() -> Unit,
) {
  FondoAlarma {
    Spacer(Modifier.height(20.dp))
    EstadoSonido(segundosSilencio, R.string.sonando_juego)
    Spacer(Modifier.height(18.dp))
    Text(instruccion, style = Alba.tipos.cabecera, color = Alba.colores.textoSecundario, textAlign = TextAlign.Center)
    Text(progreso, style = Alba.tipos.tituloGrande, color = Alba.colores.texto, textAlign = TextAlign.Center)
    Spacer(Modifier.height(16.dp))
    contenido()
    Spacer(Modifier.height(28.dp))
  }
}

/** Sacude en horizontal lo que lo lleve (al fallar), como el código de desbloqueo de iOS. */
@Composable
private fun rememberSacudida(fallos: Int): Animatable<Float, *> {
  val haptica = LocalHapticFeedback.current
  val sacudida = remember { Animatable(0f) }
  LaunchedEffect(fallos) {
    if (fallos == 0) return@LaunchedEffect
    haptica.performHapticFeedback(HapticFeedbackType.Reject)
    for (x in listOf(-18f, 15f, -11f, 7f, -3f, 0f)) sacudida.animateTo(x, tween(55))
  }
  return sacudida
}

// --- Atrapa los soles ---

@Composable
fun AlarmaSoles(estado: EstadoSoles, segundosSilencio: Int?, onAtrapar: () -> Unit, onEscapar: () -> Unit) {
  val haptica = LocalHapticFeedback.current
  MarcoJuego(
    instruccion = stringResource(R.string.tarea_soles),
    progreso = stringResource(R.string.progreso_de, estado.aciertos, estado.total),
    segundosSilencio = segundosSilencio,
  ) {
    BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
      val tamano = 88.dp
      val x = (maxWidth - tamano) * estado.posicion.x
      val y = (maxHeight - tamano) * estado.posicion.y
      // Cada sol nuevo (turno) aparece con un rebote y, si no se toca a tiempo, se escapa.
      key(estado.turno) {
        val escala = remember { Animatable(0f) }
        LaunchedEffect(Unit) { escala.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 380f)) }
        LaunchedEffect(Unit) {
          delay(estado.vida)
          onEscapar()
        }
        Sol(
          Modifier.offset(x, y)
            .size(tamano)
            .graphicsLayer {
              scaleX = escala.value
              scaleY = escala.value
            }
            .clip(CircleShape)
            .clickable(interactionSource = null, indication = null) {
              haptica.performHapticFeedback(HapticFeedbackType.Confirm)
              onAtrapar()
            }
        )
      }
    }
  }
}

@Composable
private fun Sol(modifier: Modifier) {
  val acento = Alba.colores.acento
  Canvas(modifier) {
    val radio = size.minDimension / 2
    drawCircle(Brush.radialGradient(listOf(acento.copy(alpha = 0.4f), acento.copy(alpha = 0f)), center, radio), radio)
    drawCircle(acento, radio * 0.55f)
  }
}

// --- Repite la secuencia ---

@Composable
fun AlarmaSecuencia(estado: EstadoSecuencia, segundosSilencio: Int?, onPulsar: (Int) -> Boolean?, onMostrada: () -> Unit) {
  val haptica = LocalHapticFeedback.current
  val alcance = rememberCoroutineScope()
  var encendido by remember { mutableStateOf<Int?>(null) }
  // Enseña la secuencia: cada botón se enciende medio segundo, con una pausa entre uno y otro.
  LaunchedEffect(estado.version, estado.mostrando) {
    if (!estado.mostrando) return@LaunchedEffect
    delay(800)
    for (boton in estado.secuencia) {
      encendido = boton
      delay(520)
      encendido = null
      delay(200)
    }
    onMostrada()
  }
  LaunchedEffect(estado.ronda) { if (estado.ronda > 0) haptica.performHapticFeedback(HapticFeedbackType.Confirm) }
  val sacudida = rememberSacudida(estado.fallos)

  MarcoJuego(
    instruccion = stringResource(if (estado.mostrando) R.string.secuencia_mira else R.string.secuencia_tu_turno),
    progreso =
      stringResource(R.string.ronda_de, min(estado.ronda + 1, estado.longitudes.size), estado.longitudes.size),
    segundosSilencio = segundosSilencio,
  ) {
    Spacer(Modifier.weight(1f))
    Column(
      Modifier.offset { IntOffset(sacudida.value.dp.roundToPx(), 0) },
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      for (fila in 0..1) {
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
          for (columna in 0..1) {
            val boton = fila * 2 + columna
            val activo = encendido == boton
            val alfa by animateFloatAsState(if (activo) 1f else 0.3f, tween(if (activo) 60 else 240), label = "alfa")
            val escala by animateFloatAsState(if (activo) 1.05f else 1f, spring(), label = "escala")
            Box(
              Modifier.size(150.dp)
                .graphicsLayer {
                  scaleX = escala
                  scaleY = escala
                }
                .clip(RoundedCornerShape(36.dp))
                .background(ColoresJuego[boton].copy(alpha = alfa))
                .clickable(
                  enabled = !estado.mostrando && !estado.completado,
                  interactionSource = null,
                  indication = null,
                ) {
                  haptica.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                  // El botón pulsado se enciende un momento, como al enseñarlo.
                  encendido = boton
                  alcance.launch {
                    delay(180)
                    if (encendido == boton) encendido = null
                  }
                  onPulsar(boton)
                }
            )
          }
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    // Un punto por paso de la secuencia: se van llenando al acertar.
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      repeat(estado.secuencia.size) { i ->
        val lleno = !estado.mostrando && i < estado.progreso
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (lleno) Alba.colores.acento else Negro.copy(alpha = 0.12f)))
      }
    }
    Spacer(Modifier.weight(1f))
  }
}

// --- Parejas ---

@Composable
fun AlarmaParejas(estado: EstadoParejas, segundosSilencio: Int?, onTocar: (Int) -> Boolean?, onOcultar: () -> Unit) {
  val haptica = LocalHapticFeedback.current
  // Dos que no son pareja: se ven un momento y se tapan.
  LaunchedEffect(estado.vueltas) {
    if (estado.vueltas.size == 2) {
      delay(800)
      onOcultar()
    }
  }
  MarcoJuego(
    instruccion = stringResource(R.string.parejas_instruccion),
    progreso = stringResource(R.string.parejas_de, estado.emparejadas.size / 2, estado.pares),
    segundosSilencio = segundosSilencio,
  ) {
    BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
      val hueco = 12.dp
      val lado = minOf((maxWidth - hueco * 2) / 3, (maxHeight - hueco * 3) / 4)
      Column(verticalArrangement = Arrangement.spacedBy(hueco)) {
        for (fila in 0 until 4) {
          Row(horizontalArrangement = Arrangement.spacedBy(hueco)) {
            for (columna in 0 until 3) {
              val i = fila * 3 + columna
              Carta(estado.cartas[i], estado.bocaArriba(i), i in estado.emparejadas, lado) {
                haptica.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                if (onTocar(i) == true) haptica.performHapticFeedback(HapticFeedbackType.Confirm)
              }
            }
          }
        }
      }
    }
  }
}

/** Una carta que gira sobre sí misma. Dorso ámbar con un sol; cara blanca con el símbolo. */
@Composable
private fun Carta(simbolo: Int, bocaArriba: Boolean, emparejada: Boolean, lado: Dp, onClick: () -> Unit) {
  val giro by animateFloatAsState(if (bocaArriba) 180f else 0f, tween(320), label = "giro")
  val alfa by animateFloatAsState(if (emparejada) 0.45f else 1f, tween(300), label = "alfa")
  val deCara = giro >= 90f
  Box(
    Modifier.size(lado)
      .graphicsLayer {
        rotationY = giro
        cameraDistance = 14f * density
        alpha = alfa
      }
      .clip(RoundedCornerShape(18.dp))
      .background(if (deCara) Blanco else Alba.colores.acento)
      .clickable(enabled = !bocaArriba, interactionSource = null, indication = null, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    if (deCara) {
      // Girada 180°: se contrarresta para que el símbolo no salga en espejo.
      Simbolo(simbolo, Modifier.fillMaxSize(0.46f).graphicsLayer { rotationY = 180f })
    } else {
      Canvas(Modifier.fillMaxSize(0.28f)) { drawCircle(Blanco.copy(alpha = 0.55f)) }
    }
  }
}

/**
 * Colores de los símbolos de las cartas: sin ámbar, que es el color del dorso (una
 * pareja ya encontrada no debe parecer una carta tapada).
 */
private val ColoresSimbolos =
  listOf(
    Color(0xFF007AFF), // azul
    Color(0xFF34C759), // verde
    Color(0xFFFF2D55), // rosa
    Color(0xFFAF52DE), // morado
    Color(0xFF32ADE6), // celeste
    Color(0xFF5856D6), // índigo
  )

/** Seis formas sencillas, cada una de su color: se distinguen de un vistazo. */
@Composable
private fun Simbolo(simbolo: Int, modifier: Modifier) {
  val color = ColoresSimbolos[simbolo % ColoresSimbolos.size]
  Canvas(modifier) {
    val w = size.width
    val h = size.height
    when (simbolo % 6) {
      0 -> drawCircle(color)
      1 ->
        drawPath(
          Path().apply {
            moveTo(w / 2, 0f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
          },
          color,
        )
      2 -> drawRoundRect(color, cornerRadius = CornerRadius(w * 0.18f))
      3 ->
        drawPath(
          Path().apply {
            moveTo(w / 2, 0f)
            lineTo(w, h / 2)
            lineTo(w / 2, h)
            lineTo(0f, h / 2)
            close()
          },
          color,
        )
      4 -> drawCircle(color, radius = w * 0.4f, style = Stroke(width = w * 0.2f))
      else -> {
        val grosor = w * 0.3f
        drawRoundRect(color, Offset((w - grosor) / 2, 0f), Size(grosor, h), CornerRadius(grosor / 3))
        drawRoundRect(color, Offset(0f, (h - grosor) / 2), Size(w, grosor), CornerRadius(grosor / 3))
      }
    }
  }
}

// --- Del 1 al 12 ---

@Composable
fun AlarmaOrden(estado: EstadoOrden, segundosSilencio: Int?, onTocar: (Int) -> Boolean?) {
  val haptica = LocalHapticFeedback.current
  val sacudida = rememberSacudida(estado.fallos)
  MarcoJuego(
    instruccion = stringResource(R.string.orden_instruccion, estado.total),
    progreso = stringResource(R.string.toca_el, min(estado.siguiente, estado.total)),
    segundosSilencio = segundosSilencio,
  ) {
    BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
      val hueco = 16.dp
      val lado = minOf((maxWidth - hueco * 2) / 3, (maxHeight - hueco * 3) / 4, 96.dp)
      Column(
        Modifier.offset { IntOffset(sacudida.value.dp.roundToPx(), 0) },
        verticalArrangement = Arrangement.spacedBy(hueco),
      ) {
        estado.numeros.chunked(3).forEach { fila ->
          Row(horizontalArrangement = Arrangement.spacedBy(hueco)) {
            fila.forEach { numero ->
              val hecho = numero < estado.siguiente
              val alfa by animateFloatAsState(if (hecho) 0.12f else 1f, tween(200), label = "alfa")
              Box(
                Modifier.size(lado)
                  .graphicsLayer { alpha = alfa }
                  .clip(CircleShape)
                  .clickable(enabled = !hecho) {
                    haptica.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    onTocar(numero)
                  }
                  .background(Blanco),
                contentAlignment = Alignment.Center,
              ) {
                Text(numero.toString(), style = Alba.tipos.horaRueda.copy(fontSize = 30.sp), color = Alba.colores.texto)
              }
            }
          }
        }
      }
    }
  }
}
