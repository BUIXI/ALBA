package com.oriol.alba.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colores de una pantalla. Hay dos paletas: la oscura (toda la app) y la clara
 * (la pantalla de la alarma, para no despertar con un fogonazo negro... ni blanco
 * puro). Los tonos siguen los grises de iOS para que el conjunto se sienta de Apple.
 */
@Immutable
data class Paleta(
  /** Fondo de la pantalla. */
  val fondo: Color,
  /** Tarjetas y grupos de filas sobre el fondo. */
  val superficie: Color,
  /** Controles sobre una superficie y fila pulsada. */
  val superficieAlta: Color,
  /** Líneas finas entre filas. */
  val separador: Color,
  val texto: Color,
  val textoSecundario: Color,
  /** Texto apagado: alarmas desactivadas, opciones que aún no existen. */
  val textoTerciario: Color,
  /** El único color de acento. */
  val acento: Color,
  /** Texto e iconos encima del acento. */
  val sobreAcento: Color,
  /** Acciones destructivas (eliminar). */
  val peligro: Color,
  /** Pista del interruptor cuando está apagado. */
  val interruptorApagado: Color,
)

/** Paleta de la app: negro puro (los píxeles OLED apagados no gastan) y ámbar. */
val PaletaOscura =
  Paleta(
    fondo = Color(0xFF000000),
    superficie = Color(0xFF1C1C1E),
    superficieAlta = Color(0xFF2C2C2E),
    separador = Color(0xFF38383A),
    texto = Color(0xFFFFFFFF),
    textoSecundario = Color(0xFF8E8E93),
    textoTerciario = Color(0xFF545458),
    acento = Color(0xFFFF9F0A),
    // Negro sobre ámbar: el blanco apenas contrasta (2:1) y cuesta leerlo.
    sobreAcento = Color(0xFF000000),
    peligro = Color(0xFFFF453A),
    interruptorApagado = Color(0xFF39393D),
  )

/** Paleta de la pantalla de alarma: claro y cálido, como la luz de la mañana. */
val PaletaClara =
  Paleta(
    fondo = Color(0xFFF2F2F7),
    superficie = Color(0xFFFFFFFF),
    superficieAlta = Color(0xFFE5E5EA),
    separador = Color(0xFFC6C6C8),
    texto = Color(0xFF000000),
    textoSecundario = Color(0xFF6E6E73),
    textoTerciario = Color(0xFFAEAEB2),
    acento = Color(0xFFFF9500),
    sobreAcento = Color(0xFF000000),
    peligro = Color(0xFFFF3B30),
    interruptorApagado = Color(0xFFE9E9EA),
  )
