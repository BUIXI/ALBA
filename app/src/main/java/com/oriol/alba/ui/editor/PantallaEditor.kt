package com.oriol.alba.ui.editor

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import com.oriol.alba.alarma.ServicioAlarma
import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.Sonido
import com.oriol.alba.datos.TipoTarea
import com.oriol.alba.premium.Premium
import androidx.compose.ui.res.pluralStringResource
import com.oriol.alba.ui.componentes.Interruptor
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
fun PantallaEditor(vm: EditorViewModel, onCerrar: () -> Unit, onProbar: (Alarma) -> Unit) {
  val borrador by vm.borrador.collectAsStateWithLifecycle()
  val context = LocalContext.current
  // Al elegir la foto se pide la cámara ya, no a las 7 de la mañana.
  val pedirCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
  // Con Premium se pueden marcar varias actividades (una al azar cada vez).
  val premium by Premium.activo.collectAsStateWithLifecycle()
  val alarma = borrador
  val elegirTarea = { tarea: TipoTarea ->
    val yaEstaba = alarma?.tareas?.contains(tarea) == true
    vm.alternarTarea(tarea, varias = premium)
    val hayCamara = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    if (tarea.usaCamara && !yaEstaba && !hayCamara) pedirCamara.launch(Manifest.permission.CAMERA)
  }
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
    onTarea = elegirTarea,
    onCancelar = onCerrar,
    onGuardar = { vm.guardar(onCerrar) },
    onEliminar = { vm.eliminar(onCerrar) },
    onSonido = vm::cambiarSonido,
    onComprobar = vm::cambiarComprobar,
    onProbar = { onProbar(alarma) },
    variasTareas = premium,
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
  onSonido: (Sonido) -> Unit = {},
  onComprobar: (Boolean) -> Unit = {},
  onProbar: () -> Unit = {},
  variasTareas: Boolean = true,
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
          FilaTarea(tarea, marcada = tarea in alarma.tareas, onClick = { onTarea(tarea) })
        }
      }
      val marcadas = alarma.tareas.size
      PieGrupo(
        when {
          !variasTareas -> stringResource(R.string.tareas_con_premium)
          marcadas > 1 -> pluralStringResource(R.plurals.tareas_al_azar, marcadas, marcadas)
          else -> stringResource(R.string.tareas_marca_varias)
        }
      )

      EncabezadoGrupo(stringResource(R.string.sonido))
      Grupo {
        Sonido.entries.forEachIndexed { indice, sonido ->
          if (indice > 0) SeparadorFila()
          val (titulo, descripcion) =
            when (sonido) {
              Sonido.AMANECER -> R.string.sonido_amanecer to R.string.sonido_amanecer_desc
              Sonido.SISTEMA -> R.string.sonido_sistema to R.string.sonido_sistema_desc
            }
          FilaOpcion(
            stringResource(titulo),
            stringResource(descripcion),
            marcada = alarma.sonido == sonido,
            onClick = { onSonido(sonido) },
          )
        }
      }

      EncabezadoGrupo(stringResource(R.string.despues_de_apagarla))
      Grupo {
        FilaGrupo(stringResource(R.string.comprobar, ServicioAlarma.MinutosComprobacion.toInt())) {
          Interruptor(alarma.comprobar, onComprobar)
        }
      }
      PieGrupo(stringResource(R.string.comprobar_pie))

      Spacer(Modifier.height(24.dp))
      Grupo {
        FilaGrupo(
          stringResource(R.string.probar_alarma),
          onClick = onProbar,
          colorTitulo = Alba.colores.acento,
          alineacionTitulo = Alignment.CenterHorizontally,
        )
      }
      PieGrupo(stringResource(R.string.probar_pie))

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
      TipoTarea.SOLES -> R.string.tarea_soles to R.string.tarea_soles_desc
      TipoTarea.SECUENCIA -> R.string.tarea_secuencia to R.string.tarea_secuencia_desc
      TipoTarea.PAREJAS -> R.string.tarea_parejas to R.string.tarea_parejas_desc
      TipoTarea.ORDEN -> R.string.tarea_orden to R.string.tarea_orden_desc
      TipoTarea.FOTO -> R.string.tarea_foto to R.string.tarea_foto_desc
      TipoTarea.CALCULO -> R.string.tarea_calculo to R.string.tarea_calculo_desc
    }
  FilaOpcion(stringResource(titulo), stringResource(descripcion), marcada, onClick, disponible = tarea.disponible)
}

/** Opción con título y explicación; la elegida lleva una marca. Las no disponibles dicen "Pronto". */
@Composable
private fun FilaOpcion(titulo: String, descripcion: String, marcada: Boolean, onClick: () -> Unit, disponible: Boolean = true) {
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
        titulo,
        style = Alba.tipos.cuerpo,
        color = if (disponible) Alba.colores.texto else Alba.colores.textoTerciario,
      )
      Text(
        descripcion,
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
