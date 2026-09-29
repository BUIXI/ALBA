package com.oriol.alba.ui.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oriol.alba.theme.Alba

/**
 * Interruptor al estilo iOS: pista de 51×31 dp y bola blanca que se desliza con un
 * pequeño rebote. Encendido, la pista es del color de acento.
 */
@Composable
fun Interruptor(
  activo: Boolean,
  onCambio: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  descripcion: String? = null,
) {
  val haptica = LocalHapticFeedback.current
  val colores = Alba.colores
  val pista by animateColorAsState(if (activo) colores.acento else colores.interruptorApagado, tween(220), label = "pista")
  val desplazamiento by
    animateDpAsState(if (activo) 20.dp else 0.dp, spring(dampingRatio = 0.72f, stiffness = 700f), label = "bola")

  Box(
    modifier
      .minimumInteractiveComponentSize()
      .toggleable(value = activo, interactionSource = null, indication = null, role = Role.Switch) { nuevo ->
        haptica.performHapticFeedback(if (nuevo) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
        onCambio(nuevo)
      }
      .then(if (descripcion != null) Modifier.semantics { contentDescription = descripcion } else Modifier),
    contentAlignment = Alignment.Center,
  ) {
    Box(Modifier.size(51.dp, 31.dp).clip(CircleShape).background(pista).padding(2.dp)) {
      Box(
        // Con lambda: la animación solo vuelve a colocar la bola, sin recomponer.
        Modifier.offset { IntOffset(desplazamiento.roundToPx(), 0) }
          .size(27.dp)
          .shadow(elevation = 2.dp, shape = CircleShape, ambientColor = Color.Black, spotColor = Color.Black)
          .background(Color.White, CircleShape)
      )
    }
  }
}
