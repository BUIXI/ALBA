package com.oriol.alba.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oriol.alba.R
import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.TipoTarea
import com.oriol.alba.theme.Alba
import com.oriol.alba.ui.componentes.BotonTexto
import com.oriol.alba.ui.componentes.EncabezadoGrupo
import com.oriol.alba.ui.componentes.FilaGrupo
import com.oriol.alba.ui.componentes.Grupo
import com.oriol.alba.ui.componentes.IndicacionResaltado
import com.oriol.alba.ui.componentes.Pantalla
import com.oriol.alba.ui.componentes.PieGrupo
import com.oriol.alba.ui.componentes.RuedaHora
import com.oriol.alba.ui.componentes.SelectorDias
import com.oriol.alba.ui.componentes.SeparadorFila
import com.oriol.alba.ui.componentes.textoDias
import java.time.DayOfWeek

/** Crear o editar una alarma. Se abre desde abajo, como una hoja de iOS. */
@Composable
fun PantallaEditor(vm: EditorViewModel, onCerrar: () -> Unit) {
  val borrador by vm.borrador.collectAsStateWithLifecycle()
  val alarma = borrador
  if (alarma == null) {
    // Cargando una alarma existente: un instante, solo el fondo.
    Pantalla {}
    return
  }
  EditorAlarma(
    alarma = alarma,
    esNueva = vm.esNueva,
    onHora = vm::cambiarHora,
    onDias = vm::cambiarDias,
    onEtiqueta = vm::cambiarEtiqueta,
    onTarea = vm::cambiarTarea,
    onCancelar = onCerrar,
    onGuardar = { vm.guardar(onCerrar) },
    onEliminar = { vm.eliminar(onCerrar) },
  )
}

/** El editor sin estado. Es lo que dibujan las capturas. */
@Composable
fun EditorAlarma(
  alarma: Alarma,
  esNueva: Boolean,
  onHora: (Int, Int) -> Unit,
  onDias: (Set<DayOfWeek>) -> Unit,
  onEtiqueta: (String) -> Unit,
  onTarea: (TipoTarea) -> Unit,
  onCancelar: () -> Unit,
  onGuardar: () -> Unit,
  onEliminar: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Pantalla(modifier) {
    BarraModal(
      titulo = stringResource(if (esNueva) R.string.nueva_alarma else R.string.editar_alarma),
      onCancelar = onCancelar,
      onGuardar = onGuardar,
    )
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      RuedaHora(
        hora = alarma.hora,
        minuto = alarma.minuto,
        onCambio = onHora,
        descripcionHora = stringResource(R.string.hora),
        descripcionMinuto = stringResource(R.string.minutos),
      )

      EncabezadoGrupo(stringResource(R.string.repetir))
      Grupo { SelectorDias(alarma.dias, onDias, Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) }
      PieGrupo(if (alarma.dias.isEmpty()) stringResource(R.string.sonara_una_vez) else textoDias(alarma.dias))

      Spacer(Modifier.height(24.dp))
      Grupo {
        FilaGrupo(stringResource(R.string.etiqueta)) {
          CampoEtiqueta(alarma.etiqueta, onEtiqueta, Modifier.weight(1.4f))
        }
      }

      EncabezadoGrupo(stringResource(R.string.para_apagarla))
      Grupo {
        TipoTarea.entries.forEachIndexed { indice, tarea ->
          if (indice > 0) SeparadorFila()
          FilaTarea(tarea, marcada = alarma.tarea == tarea, onClick = { onTarea(tarea) })
        }
      }

      if (!esNueva) {
        Spacer(Modifier.height(32.dp))
        Grupo {
          FilaGrupo(
            stringResource(R.string.eliminar_alarma),
            onClick = onEliminar,
            colorTitulo = Alba.colores.peligro,
            alineacionTitulo = Alignment.CenterHorizontally,
          )
        }
      }
      Spacer(Modifier.height(40.dp))
    }
  }
}

/** Barra de hoja modal: "Cancelar" · título · "Guardar". */
@Composable
private fun BarraModal(titulo: String, onCancelar: () -> Unit, onGuardar: () -> Unit) {
  Box(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp)) {
    BotonTexto(stringResource(R.string.cancelar), onCancelar, Modifier.align(Alignment.CenterStart))
    Text(titulo, style = Alba.tipos.cabecera, color = Alba.colores.texto, modifier = Modifier.align(Alignment.Center))
    BotonTexto(
      stringResource(R.string.guardar),
      onGuardar,
      Modifier.align(Alignment.CenterEnd),
      estilo = Alba.tipos.cabecera,
    )
  }
}

/** Campo de texto de la etiqueta, alineado a la derecha dentro de su fila. */
@Composable
private fun CampoEtiqueta(texto: String, onCambio: (String) -> Unit, modifier: Modifier = Modifier) {
  BasicTextField(
    value = texto,
    onValueChange = onCambio,
    singleLine = true,
    textStyle = Alba.tipos.cuerpo.copy(color = Alba.colores.textoSecundario, textAlign = TextAlign.End),
    cursorBrush = SolidColor(Alba.colores.acento),
    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
    modifier = modifier,
    decorationBox = { campo ->
      Box(contentAlignment = Alignment.CenterEnd) {
        if (texto.isEmpty()) {
          Text(stringResource(R.string.etiqueta_por_defecto), style = Alba.tipos.cuerpo, color = Alba.colores.textoTerciario)
        }
        campo()
      }
    },
  )
}

/** Una tarea para apagar la alarma, con su explicación. La elegida lleva una marca. */
@Composable
private fun FilaTarea(tarea: TipoTarea, marcada: Boolean, onClick: () -> Unit) {
  val (titulo, descripcion) =
    when (tarea) {
      TipoTarea.CALCULO -> R.string.tarea_calculo to R.string.tarea_calculo_desc
      TipoTarea.FOTO -> R.string.tarea_foto to R.string.tarea_foto_desc
    }
  val disponible = tarea.disponible
  Row(
    Modifier.fillMaxWidth()
      .selectable(
        selected = marcada,
        enabled = disponible,
        role = Role.RadioButton,
        interactionSource = null,
        indication = IndicacionResaltado(Alba.colores.superficieAlta),
        onClick = onClick,
      )
      .padding(horizontal = 16.dp, vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(Modifier.weight(1f)) {
      Text(
        stringResource(titulo),
        style = Alba.tipos.cuerpo,
        color = if (disponible) Alba.colores.texto else Alba.colores.textoTerciario,
      )
      Text(
        stringResource(descripcion),
        style = Alba.tipos.nota,
        color = if (disponible) Alba.colores.textoSecundario else Alba.colores.textoTerciario,
      )
    }
    when {
      !disponible -> Text(stringResource(R.string.pronto), style = Alba.tipos.nota, color = Alba.colores.textoTerciario)
      marcada ->
        Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Alba.colores.acento, modifier = Modifier.size(22.dp))
    }
  }
}
