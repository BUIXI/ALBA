package com.oriol.alba.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.oriol.alba.R
import kotlin.math.exp

/**
 * Escala tipográfica. Tamaños y altos de línea copiados de la escala de iOS
 * (Large Title 34, Headline 17, Body 17, Subheadline 15, Footnote 13).
 */
@Immutable
data class Tipos(
  /** Título grande de pantalla ("Alarmas"). */
  val tituloGrande: TextStyle,
  /** Título de un estado vacío o de un aviso. */
  val titulo: TextStyle,
  /** Título de barra y botones importantes. */
  val cabecera: TextStyle,
  val cuerpo: TextStyle,
  val secundario: TextStyle,
  /** Encabezados y pies de grupo. */
  val nota: TextStyle,
  /** La hora de cada alarma en la lista: grande y fina, como el reloj de iOS. */
  val horaLista: TextStyle,
  /** Los números de la rueda para elegir la hora. */
  val horaRueda: TextStyle,
)

// Inter es la fuente libre más parecida a la de Apple. Es variable: el peso y el
// "tamaño óptico" (opsz, de 14 a 32) se eligen al cargarla. Con opsz alto las letras
// se estrechan y se afinan para los tamaños grandes (como SF Pro Display) y con opsz
// bajo se abren para leer bien en pequeño (como SF Pro Text).
private val pesos = listOf(FontWeight.Light, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold)

// Cargar una fuente variable con ajustes aún es API experimental en Compose, aunque
// funciona desde Android 8 (nuestro mínimo).
@OptIn(ExperimentalTextApi::class)
private fun inter(tamanoOptico: Float) =
  FontFamily(
    pesos.map { peso ->
      Font(
        R.font.inter,
        weight = peso,
        variationSettings =
          FontVariation.Settings(FontVariation.weight(peso.weight), FontVariation.Setting("opsz", tamanoOptico)),
      )
    }
  )

private val InterTexto = inter(14f)
private val InterTitulo = inter(32f)

/**
 * Espaciado entre letras que recomienda Inter para cada tamaño ("dynamic metrics"):
 * más apretado cuanto más grande. Sale en em.
 */
internal fun trackingInter(tamanoSp: Float): Float = (-0.0223f + 0.185f * exp(-0.1745f * tamanoSp))

private fun estilo(tamano: Float, alto: Float, peso: FontWeight, cifrasTabulares: Boolean = false) =
  TextStyle(
    fontFamily = if (tamano >= 20f) InterTitulo else InterTexto,
    fontWeight = peso,
    fontSize = tamano.sp,
    lineHeight = alto.sp,
    letterSpacing = trackingInter(tamano).em,
    // Cifras del mismo ancho: las horas no bailan al cambiar un número.
    fontFeatureSettings = if (cifrasTabulares) "tnum" else null,
  )

val TiposAlba =
  Tipos(
    tituloGrande = estilo(34f, 41f, FontWeight.Bold),
    titulo = estilo(22f, 28f, FontWeight.Bold),
    cabecera = estilo(17f, 22f, FontWeight.SemiBold),
    cuerpo = estilo(17f, 22f, FontWeight.Normal),
    secundario = estilo(15f, 20f, FontWeight.Normal),
    nota = estilo(13f, 18f, FontWeight.Normal),
    horaLista = estilo(56f, 62f, FontWeight.Light, cifrasTabulares = true),
    horaRueda = estilo(24f, 30f, FontWeight.Normal, cifrasTabulares = true),
  )
