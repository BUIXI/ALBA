package com.oriol.alba.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.launch

// Respuesta visual al pulsar. Android pinta una "onda" (ripple); iOS atenúa lo
// pulsado o resalta la fila. Alba hace lo segundo.

/** Lo pulsado se vuelve medio transparente y recupera la opacidad al soltar. Botones. */
object IndicacionOpacidad : IndicationNodeFactory {
  override fun create(interactionSource: InteractionSource): DelegatableNode = NodoOpacidad(interactionSource)

  override fun equals(other: Any?) = other === this

  override fun hashCode() = System.identityHashCode(this)
}

/** La fila pulsada se rellena con [color]. Filas de listas y grupos. */
data class IndicacionResaltado(val color: Color) : IndicationNodeFactory {
  override fun create(interactionSource: InteractionSource): DelegatableNode = NodoResaltado(interactionSource, color)
}

/** Sigue las pulsaciones y anima [presion]: 1 pulsado, 0 suelto. */
private abstract class NodoPulsacion(private val fuente: InteractionSource) : Modifier.Node(), DrawModifierNode {
  protected val presion = Animatable(0f)

  override fun onAttach() {
    coroutineScope.launch {
      val activas = mutableListOf<PressInteraction.Press>()
      fuente.interactions.collect { interaccion ->
        when (interaccion) {
          is PressInteraction.Press -> activas += interaccion
          is PressInteraction.Release -> activas -= interaccion.press
          is PressInteraction.Cancel -> activas -= interaccion.press
          else -> return@collect
        }
        val pulsado = activas.isNotEmpty()
        launch {
          // Al pulsar, de golpe (que se note enseguida); al soltar, se desvanece.
          if (pulsado) {
            presion.snapTo(1f)
            invalidateDraw()
          } else {
            presion.animateTo(0f, tween(durationMillis = 280)) { invalidateDraw() }
          }
        }
      }
    }
  }
}

private class NodoOpacidad(fuente: InteractionSource) : NodoPulsacion(fuente) {
  override fun ContentDrawScope.draw() {
    val p = presion.value
    if (p == 0f) {
      drawContent()
      return
    }
    // Se dibuja el contenido en una capa con transparencia.
    drawContext.canvas.saveLayer(size.toRect(), Paint().apply { alpha = 1f - 0.65f * p })
    drawContent()
    drawContext.canvas.restore()
  }
}

private class NodoResaltado(fuente: InteractionSource, private val color: Color) : NodoPulsacion(fuente) {
  override fun ContentDrawScope.draw() {
    val p = presion.value
    if (p > 0f) drawRect(color.copy(alpha = color.alpha * p))
    drawContent()
  }
}
