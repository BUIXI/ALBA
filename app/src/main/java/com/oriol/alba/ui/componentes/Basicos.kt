package com.oriol.alba.ui.componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oriol.alba.theme.Alba

// Piezas básicas del sistema de diseño. Medidas de iOS: márgenes de 16-20 dp,
// esquinas de 12 dp en los grupos, filas de 44-48 dp y objetivos táctiles de 44 dp.

/** Radio de las esquinas de grupos y tarjetas. */
val EsquinaGrupo = RoundedCornerShape(12.dp)

/** Contenedor de cada pantalla: pinta su propio fondo y no pisa las barras del sistema. */
@Composable
fun Pantalla(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Column(
    modifier.fillMaxSize().background(Alba.colores.fondo).windowInsetsPadding(WindowInsets.safeDrawing),
    content = content,
  )
}

/** Botón principal: una píldora de color acento. Uno por pantalla como mucho. */
@Composable
fun BotonPrincipal(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  // El clic va antes que el fondo: así el atenuado afecta a todo el botón.
  Box(
    modifier
      .heightIn(min = 52.dp)
      .clip(RoundedCornerShape(percent = 50))
      .clickable(role = Role.Button, onClick = onClick)
      .background(Alba.colores.acento)
      .padding(horizontal = 28.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(texto, style = Alba.tipos.cabecera, color = Alba.colores.sobreAcento)
  }
}

/** Botón de solo texto, como los de las barras de iOS ("Cancelar", "Guardar"). */
@Composable
fun BotonTexto(
  texto: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  estilo: TextStyle = Alba.tipos.cuerpo,
  color: Color = Alba.colores.acento,
  habilitado: Boolean = true,
) {
  Box(
    modifier
      .sizeIn(minWidth = 44.dp, minHeight = 44.dp)
      .clickable(enabled = habilitado, role = Role.Button, onClick = onClick)
      .padding(horizontal = 4.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(texto, style = estilo, color = if (habilitado) color else Alba.colores.textoTerciario)
  }
}

/** Botón de solo icono, con objetivo táctil de 44 dp. */
@Composable
fun BotonIcono(
  @DrawableRes icono: Int,
  descripcion: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  color: Color = Alba.colores.acento,
) {
  Box(
    modifier.size(44.dp).clickable(role = Role.Button, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(painterResource(icono), contentDescription = descripcion, tint = color, modifier = Modifier.size(24.dp))
  }
}

/** Grupo de filas sobre una tarjeta redondeada (las "listas agrupadas" de iOS). */
@Composable
fun Grupo(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Column(
    modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(EsquinaGrupo).background(Alba.colores.superficie),
    content = content,
  )
}

/** Fila de un [Grupo]: título a la izquierda y, opcionalmente, algo a la derecha. */
@Composable
fun FilaGrupo(
  titulo: String,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  colorTitulo: Color = Alba.colores.texto,
  habilitada: Boolean = true,
  alineacionTitulo: Alignment.Horizontal = Alignment.Start,
  final: (@Composable RowScope.() -> Unit)? = null,
) {
  val pulsable =
    if (onClick != null) {
      Modifier.clickable(
        enabled = habilitada,
        interactionSource = null,
        indication = IndicacionResaltado(Alba.colores.superficieAlta),
        role = Role.Button,
        onClick = onClick,
      )
    } else {
      Modifier
    }
  Row(
    modifier.fillMaxWidth().then(pulsable).heightIn(min = 48.dp).padding(horizontal = 16.dp, vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(Modifier.weight(1f), contentAlignment = if (alineacionTitulo == Alignment.Start) Alignment.CenterStart else Alignment.Center) {
      Text(titulo, style = Alba.tipos.cuerpo, color = if (habilitada) colorTitulo else Alba.colores.textoTerciario)
    }
    final?.invoke(this)
  }
}

/** Línea de un píxel entre filas, sangrada por la izquierda como en iOS. */
@Composable
fun SeparadorFila(modifier: Modifier = Modifier, sangria: Dp = 16.dp) {
  val unPixel = with(LocalDensity.current) { 1.toDp() }
  Box(modifier.padding(start = sangria).fillMaxWidth().height(unPixel).background(Alba.colores.separador))
}

/** Texto pequeño encima de un grupo. */
@Composable
fun EncabezadoGrupo(texto: String, modifier: Modifier = Modifier) {
  Text(
    texto,
    style = Alba.tipos.nota,
    color = Alba.colores.textoSecundario,
    modifier = modifier.padding(start = 32.dp, end = 32.dp, top = 28.dp, bottom = 7.dp),
  )
}

/** Texto pequeño debajo de un grupo, para explicar algo. */
@Composable
fun PieGrupo(texto: String, modifier: Modifier = Modifier) {
  Text(
    texto,
    style = Alba.tipos.nota,
    color = Alba.colores.textoSecundario,
    modifier = modifier.padding(start = 32.dp, end = 32.dp, top = 7.dp),
  )
}
