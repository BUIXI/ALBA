package com.oriol.alba.ui.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oriol.alba.dominio.diasDeLaSemana
import com.oriol.alba.theme.Alba
import java.time.DayOfWeek
import java.time.format.TextStyle

/**
 * Los siete días en círculos que se marcan con un toque. Las letras y el primer día
 * de la semana salen del idioma del móvil (en español: L M X J V S D, desde el lunes).
 */
@Composable
fun SelectorDias(dias: Set<DayOfWeek>, onCambio: (Set<DayOfWeek>) -> Unit, modifier: Modifier = Modifier) {
  val idioma = LocalConfiguration.current.locales[0]
  Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    diasDeLaSemana(idioma).forEach { dia ->
      val marcado = dia in dias
      val fondo by
        animateColorAsState(if (marcado) Alba.colores.acento else Alba.colores.superficieAlta, tween(180), label = "fondo")
      val letra by
        animateColorAsState(if (marcado) Alba.colores.sobreAcento else Alba.colores.texto, tween(180), label = "letra")
      Box(
        Modifier.size(40.dp)
          .clip(CircleShape)
          .toggleable(value = marcado, role = Role.Checkbox) { onCambio(if (it) dias + dia else dias - dia) }
          .background(fondo)
          .semantics { contentDescription = dia.getDisplayName(TextStyle.FULL, idioma) },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          dia.getDisplayName(TextStyle.NARROW, idioma).uppercase(idioma),
          style = Alba.tipos.cabecera.copy(fontSize = 15.sp),
          color = letra,
        )
      }
    }
  }
}
