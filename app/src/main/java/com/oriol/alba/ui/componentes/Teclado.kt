package com.oriol.alba.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oriol.alba.R
import com.oriol.alba.theme.Alba
import com.oriol.alba.ui.tareas.Tecla

private val TamanoTecla = 76.dp

/**
 * Teclado numérico redondo, como el del teléfono de iOS: 1-9, 0 y borrar. Cada tecla
 * da un toque háptico.
 */
@Composable
fun Teclado(onTecla: (Tecla) -> Unit, modifier: Modifier = Modifier) {
  val haptica = LocalHapticFeedback.current
  val pulsar = { tecla: Tecla ->
    haptica.performHapticFeedback(HapticFeedbackType.KeyboardTap)
    onTecla(tecla)
  }
  Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    for (fila in listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))) {
      Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        fila.forEach { TeclaDigito(it) { pulsar(Tecla.Digito(it)) } }
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
      Spacer(Modifier.size(TamanoTecla))
      TeclaDigito(0) { pulsar(Tecla.Digito(0)) }
      Box(
        Modifier.size(TamanoTecla).clip(CircleShape).clickable(role = Role.Button) { pulsar(Tecla.Borrar) },
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          painterResource(R.drawable.ic_borrar),
          contentDescription = stringResource(R.string.borrar),
          tint = Alba.colores.texto,
          modifier = Modifier.size(30.dp),
        )
      }
    }
  }
}

@Composable
private fun TeclaDigito(valor: Int, onClick: () -> Unit) {
  Box(
    Modifier.size(TamanoTecla)
      .clip(CircleShape)
      .clickable(role = Role.Button, onClick = onClick)
      .background(Alba.colores.superficie),
    contentAlignment = Alignment.Center,
  ) {
    Text(valor.toString(), style = Alba.tipos.horaRueda.copy(fontSize = 32.sp), color = Alba.colores.texto)
  }
}
