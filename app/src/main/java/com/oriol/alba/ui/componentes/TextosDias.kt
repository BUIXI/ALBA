package com.oriol.alba.ui.componentes

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.oriol.alba.R
import com.oriol.alba.dominio.ResumenDias
import com.oriol.alba.dominio.resumirDias
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit

/** "hoy a las 07:00", "mañana a las 07:00" o "el lunes a las 07:00". */
@Composable
fun textoCuando(momento: LocalDateTime, ahora: LocalDateTime): String {
  val idioma = LocalConfiguration.current.locales[0]
  val hora = formatoHora(momento.hour, momento.minute)
  return when (ChronoUnit.DAYS.between(ahora.toLocalDate(), momento.toLocalDate())) {
    0L -> stringResource(R.string.hoy_a_las, hora)
    1L -> stringResource(R.string.manana_a_las, hora)
    else -> stringResource(R.string.el_dia_a_las, momento.dayOfWeek.getDisplayName(TextStyle.FULL, idioma), hora)
  }
}

/** "Entre semana", "Fin de semana", "Lun, Mié, Vie"... en el idioma del móvil. */
@Composable
fun textoDias(dias: Set<DayOfWeek>): String {
  val idioma = LocalConfiguration.current.locales[0]
  return when (val resumen = resumirDias(dias, idioma)) {
    ResumenDias.UnaVez -> stringResource(R.string.una_vez)
    ResumenDias.TodosLosDias -> stringResource(R.string.todos_los_dias)
    ResumenDias.EntreSemana -> stringResource(R.string.entre_semana)
    ResumenDias.FinDeSemana -> stringResource(R.string.fin_de_semana)
    is ResumenDias.Sueltos ->
      resumen.dias.joinToString(", ") { dia ->
        // "lun" -> "Lun"; algunos idiomas añaden un punto ("lun."): se quita.
        dia.getDisplayName(TextStyle.SHORT, idioma).trimEnd('.').replaceFirstChar { it.titlecase(idioma) }
      }
  }
}
