package com.oriol.alba.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.oriol.alba.ui.componentes.IndicacionOpacidad

private val LocalPaleta = staticCompositionLocalOf { PaletaOscura }
private val LocalTipos = staticCompositionLocalOf { TiposAlba }

/** Acceso a los colores y tipos del tema actual: `Alba.colores.fondo`, `Alba.tipos.cuerpo`. */
object Alba {
  val colores: Paleta
    @Composable @ReadOnlyComposable get() = LocalPaleta.current

  val tipos: Tipos
    @Composable @ReadOnlyComposable get() = LocalTipos.current
}

/**
 * Tema de Alba. Por defecto, oscuro (toda la app). La pantalla de la alarma pasa
 * [PaletaClara].
 *
 * No hay colores dinámicos del sistema (Material You): el aspecto es el mismo en
 * cualquier móvil. Material 3 solo se usa por piezas sueltas, y su esquema se rellena
 * con nuestra paleta para que no se cuele ningún morado. Al pulsar algo no hay la
 * "onda" de Android: se atenúa, como en iOS ([IndicacionOpacidad]).
 */
@Composable
fun TemaAlba(paleta: Paleta = PaletaOscura, content: @Composable () -> Unit) {
  val oscuro = paleta == PaletaOscura
  val esquema =
    if (oscuro) {
      darkColorScheme(
        primary = paleta.acento,
        onPrimary = paleta.sobreAcento,
        background = paleta.fondo,
        onBackground = paleta.texto,
        surface = paleta.fondo,
        onSurface = paleta.texto,
        surfaceVariant = paleta.superficie,
        onSurfaceVariant = paleta.textoSecundario,
        error = paleta.peligro,
        outline = paleta.separador,
      )
    } else {
      lightColorScheme(
        primary = paleta.acento,
        onPrimary = paleta.sobreAcento,
        background = paleta.fondo,
        onBackground = paleta.texto,
        surface = paleta.fondo,
        onSurface = paleta.texto,
        surfaceVariant = paleta.superficie,
        onSurfaceVariant = paleta.textoSecundario,
        error = paleta.peligro,
        outline = paleta.separador,
      )
    }

  CompositionLocalProvider(
    LocalPaleta provides paleta,
    LocalTipos provides TiposAlba,
    LocalIndication provides IndicacionOpacidad,
    LocalTextSelectionColors provides TextSelectionColors(paleta.acento, paleta.acento.copy(alpha = 0.35f)),
  ) {
    MaterialTheme(
      colorScheme = esquema,
      typography =
        Typography(
          bodyLarge = TiposAlba.cuerpo,
          bodyMedium = TiposAlba.secundario,
          bodySmall = TiposAlba.nota,
          titleLarge = TiposAlba.titulo,
          titleMedium = TiposAlba.cabecera,
          labelLarge = TiposAlba.cabecera,
        ),
      content = content,
    )
  }
}
