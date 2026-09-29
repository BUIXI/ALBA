package com.oriol.alba.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.oriol.alba.theme.Alba
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull

// Rueda para elegir la hora, como la de iOS: se gira, se ajusta sola al valor más
// cercano y los números se curvan como si estuvieran en un tambor.

private val AltoFila = 44.dp
private const val FilasVisibles = 5

// La lista "no tiene fin": se repiten los valores muchas veces y se empieza por el
// medio. Con 400 vueltas nadie llega al borde girando.
private const val Vueltas = 400

/**
 * Una rueda de [cantidad] valores (0 hasta cantidad-1). Empieza en [seleccionado] y
 * avisa con [onCambio] cada vez que otro valor queda en el centro.
 */
@Composable
fun Rueda(
  cantidad: Int,
  seleccionado: Int,
  onCambio: (Int) -> Unit,
  descripcion: String,
  modifier: Modifier = Modifier,
  texto: (Int) -> String = { it.toString().padStart(2, '0') },
) {
  val total = cantidad * Vueltas
  // Solo al crearla: después manda el dedo, no el valor que llega de fuera.
  val inicio = remember { total / 2 - (total / 2) % cantidad + seleccionado }
  val estado = rememberLazyListState(initialFirstVisibleItemIndex = inicio)
  val haptica = LocalHapticFeedback.current
  val onCambioActual by rememberUpdatedState(onCambio)
  val centrado by remember { derivedStateOf { indiceCentrado(estado) } }

  LaunchedEffect(estado) {
    snapshotFlow { centrado }
      .filterNotNull()
      .distinctUntilChanged()
      .drop(1) // el primero es el valor inicial, no un cambio
      .collect { indice ->
        haptica.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        onCambioActual(indice % cantidad)
      }
  }

  LazyColumn(
    state = estado,
    flingBehavior = rememberSnapFlingBehavior(estado, SnapPosition.Center),
    contentPadding = PaddingValues(vertical = AltoFila * (FilasVisibles / 2)),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      modifier.height(AltoFila * FilasVisibles).semantics {
        contentDescription = descripcion
        stateDescription = texto(seleccionado)
      },
  ) {
    items(total) { indice ->
      Box(
        Modifier.height(AltoFila).fillMaxWidth().graphicsLayer { efectoTambor(estado, indice) },
        contentAlignment = Alignment.Center,
      ) {
        Text(texto(indice % cantidad), style = Alba.tipos.horaRueda, color = Alba.colores.texto)
      }
    }
  }
}

/** La fila cuyo centro está más cerca del centro de la rueda. */
private fun indiceCentrado(estado: LazyListState): Int? {
  val info = estado.layoutInfo
  // Con relleno arriba, el inicio visible es negativo: el centro sale de los dos extremos.
  val centro = (info.viewportStartOffset + info.viewportEndOffset) / 2
  return info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - centro) }?.index
}

/** Gira, encoge y apaga cada fila según lo lejos que esté del centro. */
private fun androidx.compose.ui.graphics.GraphicsLayerScope.efectoTambor(estado: LazyListState, indice: Int) {
  val info = estado.layoutInfo
  val fila = info.visibleItemsInfo.firstOrNull { it.index == indice } ?: return
  val centro = (info.viewportStartOffset + info.viewportEndOffset) / 2f
  // Distancia al centro medida en filas: 0 en el centro, ±2 en los bordes.
  val distancia = (fila.offset + fila.size / 2f - centro) / fila.size
  val lejania = abs(distancia)
  rotationX = (-distancia * 20f).coerceIn(-80f, 80f)
  alpha = (1f - lejania * 0.42f).coerceIn(0.12f, 1f)
  val escala = 1f - lejania * 0.06f
  scaleX = escala
  scaleY = escala
  cameraDistance = 10f * density
}

/** Dos ruedas, horas y minutos, sobre una banda que marca la selección. */
@Composable
fun RuedaHora(
  hora: Int,
  minuto: Int,
  onCambio: (hora: Int, minuto: Int) -> Unit,
  descripcionHora: String,
  descripcionMinuto: String,
  modifier: Modifier = Modifier,
) {
  val horaActual by rememberUpdatedState(hora)
  val minutoActual by rememberUpdatedState(minuto)
  Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Box(
      Modifier.padding(horizontal = 16.dp)
        .fillMaxWidth()
        .height(AltoFila)
        .clip(RoundedCornerShape(10.dp))
        .background(Alba.colores.superficie)
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      Rueda(24, hora, { onCambio(it, minutoActual) }, descripcionHora, Modifier.width(88.dp))
      Text(":", style = Alba.tipos.horaRueda, color = Alba.colores.texto)
      Rueda(60, minuto, { onCambio(horaActual, it) }, descripcionMinuto, Modifier.width(88.dp))
    }
  }
}
