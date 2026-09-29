package com.oriol.alba.ui.permisos

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.oriol.alba.R
import com.oriol.alba.alarma.EstadoPermisos
import com.oriol.alba.alarma.Permisos
import com.oriol.alba.theme.Alba
import com.oriol.alba.ui.componentes.BotonTexto
import com.oriol.alba.ui.componentes.FilaGrupo
import com.oriol.alba.ui.componentes.Grupo
import com.oriol.alba.ui.componentes.Pantalla
import com.oriol.alba.ui.componentes.PieGrupo
import com.oriol.alba.ui.componentes.SeparadorFila

/** El estado de los permisos, que se vuelve a leer cada vez que la pantalla vuelve a primer plano. */
@Composable
fun rememberEstadoPermisos(): EstadoPermisos {
  val context = LocalContext.current
  var estado by remember { mutableStateOf(Permisos.estado(context)) }
  LifecycleResumeEffect(Unit) {
    estado = Permisos.estado(context)
    onPauseOrDispose {}
  }
  return estado
}

/** "Para que suene siempre": cada permiso, si está, y cómo activarlo si no. */
@Composable
fun PantallaPermisos(onVolver: () -> Unit) {
  val context = LocalContext.current
  val estado = rememberEstadoPermisos()
  val abrir = { intent: Intent -> runCatching { context.startActivity(intent) } }
  // Notificaciones: el diálogo del sistema; si ya se denegó para siempre, a los ajustes.
  val pedirNotificaciones =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
      if (!concedido) abrir(Permisos.ajustesNotificaciones(context))
    }
  ListaPermisos(
    estado = estado,
    fabricante = Permisos.fabricanteAgresivo,
    onVolver = onVolver,
    onNotificaciones = {
      if (Permisos.notificacionesConDialogo) pedirNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
      else abrir(Permisos.ajustesNotificaciones(context))
    },
    onPantallaCompleta = { abrir(Permisos.ajustesPantallaCompleta(context)) },
    onAlarmasExactas = { abrir(Permisos.ajustesAlarmasExactas(context)) },
    onSegundoPlano = { abrir(Permisos.ajustesApp(context)) },
    onBateria = { abrir(Permisos.ajustesBateria()) },
    onAjustesApp = { abrir(Permisos.ajustesApp(context)) },
  )
}

/** La lista sin estado, para las capturas. */
@Composable
fun ListaPermisos(
  estado: EstadoPermisos,
  fabricante: String?,
  onVolver: () -> Unit,
  onNotificaciones: () -> Unit,
  onPantallaCompleta: () -> Unit,
  onAlarmasExactas: () -> Unit,
  onSegundoPlano: () -> Unit,
  onBateria: () -> Unit,
  onAjustesApp: () -> Unit,
) {
  Pantalla {
    Box(Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp)) {
      BotonTexto(stringResource(R.string.listo), onVolver, Modifier.align(Alignment.CenterEnd), estilo = Alba.tipos.cabecera)
    }
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
      Text(
        stringResource(R.string.permisos_titulo),
        style = Alba.tipos.tituloGrande,
        color = Alba.colores.texto,
        modifier = Modifier.padding(horizontal = 20.dp),
      )
      Text(
        stringResource(R.string.permisos_texto),
        style = Alba.tipos.secundario,
        color = Alba.colores.textoSecundario,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
      )
      Grupo {
        FilaPermiso(R.string.permiso_notificaciones, R.string.permiso_notificaciones_desc, estado.notificaciones, onNotificaciones)
        SeparadorFila()
        FilaPermiso(R.string.permiso_pantalla, R.string.permiso_pantalla_desc, estado.pantallaCompleta, onPantallaCompleta)
        SeparadorFila()
        FilaPermiso(R.string.permiso_exactas, R.string.permiso_exactas_desc, estado.alarmasExactas, onAlarmasExactas)
        SeparadorFila()
        FilaPermiso(R.string.permiso_segundo_plano, R.string.permiso_segundo_plano_desc, estado.segundoPlano, onSegundoPlano)
      }
      Spacer(Modifier.height(24.dp))
      Grupo {
        FilaPermiso(R.string.permiso_bateria, R.string.permiso_bateria_desc, estado.sinOptimizarBateria, onBateria)
      }
      if (fabricante != null) {
        PieGrupo(stringResource(R.string.permisos_fabricante, fabricante))
        Spacer(Modifier.height(12.dp))
        Grupo {
          FilaGrupo(
            stringResource(R.string.abrir_ajustes),
            onClick = onAjustesApp,
            colorTitulo = Alba.colores.acento,
            alineacionTitulo = Alignment.CenterHorizontally,
          )
        }
      }
      Spacer(Modifier.height(40.dp))
    }
  }
}

/** Un permiso: nombre, para qué sirve y, a la derecha, la marca o "Activar". */
@Composable
private fun FilaPermiso(titulo: Int, descripcion: Int, concedido: Boolean, onActivar: () -> Unit) {
  Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 11.dp, bottom = 11.dp), verticalAlignment = Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) {
      Text(stringResource(titulo), style = Alba.tipos.cuerpo, color = Alba.colores.texto)
      Text(stringResource(descripcion), style = Alba.tipos.nota, color = Alba.colores.textoSecundario)
    }
    if (concedido) {
      Icon(
        painterResource(R.drawable.ic_check),
        contentDescription = stringResource(R.string.concedido),
        tint = Alba.colores.textoSecundario,
        modifier = Modifier.padding(horizontal = 8.dp).size(22.dp),
      )
    } else {
      BotonTexto(stringResource(R.string.activar), onActivar, estilo = Alba.tipos.cabecera)
    }
  }
}
