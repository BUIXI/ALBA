package com.oriol.alba.ui.lista

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.oriol.alba.alarma.Permisos
import com.oriol.alba.ui.componentes.EsquinaGrupo
import com.oriol.alba.ui.permisos.rememberEstadoPermisos
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oriol.alba.R
import com.oriol.alba.datos.Alarma
import com.oriol.alba.dominio.minutosHasta
import com.oriol.alba.dominio.proximaAlarma
import com.oriol.alba.theme.Alba
import com.oriol.alba.ui.componentes.BotonIcono
import com.oriol.alba.ui.componentes.BotonPrincipal
import com.oriol.alba.ui.componentes.IndicacionResaltado
import com.oriol.alba.ui.componentes.Interruptor
import com.oriol.alba.ui.componentes.Pantalla
import com.oriol.alba.ui.componentes.SeparadorFila
import com.oriol.alba.ui.componentes.formatoHora
import com.oriol.alba.ui.componentes.rememberAhora
import com.oriol.alba.ui.componentes.textoDias
import java.time.LocalDateTime
import java.time.format.TextStyle

/** Pantalla principal: la lista de alarmas. */
@Composable
fun PantallaLista(vm: ListaViewModel, onNueva: () -> Unit, onAbrir: (Long) -> Unit, onPermisos: () -> Unit) {
  val alarmas by vm.alarmas.collectAsStateWithLifecycle()
  val permisos = rememberEstadoPermisos()
  // En cuanto hay una alarma, el diálogo de notificaciones (sin ellas no se ve la alarma).
  val pedirNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
  val hayAlarmas = alarmas?.isNotEmpty() == true
  LaunchedEffect(hayAlarmas) {
    if (hayAlarmas && !permisos.notificaciones && Permisos.notificacionesConDialogo) {
      pedirNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }
  ListaAlarmas(
    alarmas = alarmas,
    ahora = rememberAhora(),
    onNueva = onNueva,
    onAbrir = onAbrir,
    onCambiarActiva = vm::cambiarActiva,
    avisoPermisos = !permisos.imprescindibles,
    onPermisos = onPermisos,
  )
}

/** La lista sin estado: recibe todo hecho. Es lo que dibujan las capturas. */
@Composable
fun ListaAlarmas(
  alarmas: List<Alarma>?,
  ahora: LocalDateTime,
  onNueva: () -> Unit,
  onAbrir: (Long) -> Unit,
  onCambiarActiva: (Long, Boolean) -> Unit,
  modifier: Modifier = Modifier,
  avisoPermisos: Boolean = false,
  onPermisos: () -> Unit = {},
) {
  Pantalla(modifier) {
    // Barra superior: solo el "+", a la derecha, como en el reloj de iOS.
    Row(
      Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BotonIcono(R.drawable.ic_mas, stringResource(R.string.nueva_alarma), onNueva)
    }
    Text(
      stringResource(R.string.titulo_alarmas),
      style = Alba.tipos.tituloGrande,
      color = Alba.colores.texto,
      modifier = Modifier.padding(horizontal = 20.dp),
    )
    // Mientras se lee la base de datos no se pinta nada más.
    if (alarmas == null) return@Pantalla

    if (alarmas.isEmpty()) {
      if (avisoPermisos) AvisoPermisos(onPermisos)
      EstadoVacio(onNueva, Modifier.weight(1f))
    } else {
      Text(
        textoProxima(alarmas, ahora),
        style = Alba.tipos.secundario,
        color = Alba.colores.textoSecundario,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 2.dp),
      )
      if (avisoPermisos) AvisoPermisos(onPermisos)
      LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)) {
        itemsIndexed(alarmas, key = { _, alarma -> alarma.id }) { indice, alarma ->
          Column(Modifier.animateItem()) {
            if (indice > 0) SeparadorFila(sangria = 20.dp)
            FilaAlarma(
              alarma = alarma,
              onClick = { onAbrir(alarma.id) },
              onCambiarActiva = { onCambiarActiva(alarma.id, it) },
            )
          }
        }
      }
    }
  }
}

/** Una alarma: la hora grande, debajo su nombre y sus días, y el interruptor. */
@Composable
private fun FilaAlarma(alarma: Alarma, onClick: () -> Unit, onCambiarActiva: (Boolean) -> Unit) {
  // Apagada, se atenúa entera (como en iOS) en vez de tacharla o esconderla.
  val colorHora by
    animateColorAsState(if (alarma.activa) Alba.colores.texto else Alba.colores.textoTerciario, tween(220), label = "hora")
  val colorDetalle by
    animateColorAsState(
      if (alarma.activa) Alba.colores.textoSecundario else Alba.colores.textoTerciario,
      tween(220),
      label = "detalle",
    )
  val hora = formatoHora(alarma.hora, alarma.minuto)
  val dias = textoDias(alarma.dias)
  val detalle = if (alarma.etiqueta.isBlank()) dias else alarma.etiqueta + stringResource(R.string.separador_detalle) + dias

  Row(
    Modifier.fillMaxWidth()
      .clickable(interactionSource = null, indication = IndicacionResaltado(Alba.colores.superficie), onClick = onClick)
      .padding(start = 20.dp, end = 12.dp, top = 4.dp, bottom = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(Modifier.weight(1f)) {
      Text(hora, style = Alba.tipos.horaLista, color = colorHora)
      Text(detalle, style = Alba.tipos.secundario, color = colorDetalle, maxLines = 1)
    }
    Interruptor(
      activo = alarma.activa,
      onCambio = onCambiarActiva,
      descripcion = stringResource(R.string.alarma_de_las, hora),
    )
  }
}

/** Aviso ámbar y discreto: falta algún permiso imprescindible. Lleva a la pantalla de permisos. */
@Composable
private fun AvisoPermisos(onClick: () -> Unit) {
  Row(
    Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
      .fillMaxWidth()
      .clip(EsquinaGrupo)
      .clickable(role = Role.Button, onClick = onClick)
      .background(Alba.colores.acento.copy(alpha = 0.16f))
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      stringResource(R.string.permisos_aviso),
      style = Alba.tipos.secundario,
      color = Alba.colores.texto,
      modifier = Modifier.weight(1f),
    )
    Spacer(Modifier.width(12.dp))
    Text(stringResource(R.string.revisar), style = Alba.tipos.cabecera, color = Alba.colores.acento)
  }
}

/** Sin alarmas: una frase y el botón para crear la primera. */
@Composable
private fun EstadoVacio(onNueva: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    // El margen inferior sube el bloque por encima del centro exacto: se ve más centrado.
    modifier.fillMaxWidth().padding(start = 40.dp, end = 40.dp, bottom = 96.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(stringResource(R.string.sin_alarmas), style = Alba.tipos.titulo, color = Alba.colores.texto)
    Spacer(Modifier.height(8.dp))
    Text(
      stringResource(R.string.sin_alarmas_texto),
      style = Alba.tipos.secundario,
      color = Alba.colores.textoSecundario,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(28.dp))
    BotonPrincipal(stringResource(R.string.crear_alarma), onNueva)
  }
}

/** "Próxima alarma en 8 h 12 min", "... el lunes" o "Ninguna alarma activada". */
@Composable
private fun textoProxima(alarmas: List<Alarma>, ahora: LocalDateTime): String {
  val idioma = LocalConfiguration.current.locales[0]
  val (_, momento) = proximaAlarma(alarmas, ahora) ?: return stringResource(R.string.ninguna_activa)
  val minutos = minutosHasta(ahora, momento)
  val horas = minutos / 60
  val resto = minutos % 60
  return when {
    minutos >= 24 * 60 -> stringResource(R.string.proxima_el_dia, momento.dayOfWeek.getDisplayName(TextStyle.FULL, idioma))
    horas == 0L -> stringResource(R.string.proxima_en_min, resto)
    resto == 0L -> stringResource(R.string.proxima_en_h, horas)
    else -> stringResource(R.string.proxima_en_h_min, horas, resto)
  }
}
