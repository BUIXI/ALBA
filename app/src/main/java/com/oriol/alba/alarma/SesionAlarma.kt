package com.oriol.alba.alarma

import com.oriol.alba.datos.Alarma
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Lo que tiene que hacer el altavoz y el vibrador. En el móvil es [Reproductor]. */
interface SalidaSonido {
  /** 0 = silencio, 1 = todo lo que dé el volumen de alarma. */
  fun volumen(valor: Float)

  fun vibrar(si: Boolean)

  fun detener()
}

/** Cómo está una alarma que suena. Los tiempos van en el reloj de [SesionAlarma]. */
data class EstadoSesion(
  val alarma: Alarma,
  val modo: Modo,
  val prueba: Boolean,
  /** Cuándo empezó a sonar. */
  val inicio: Long,
  /** Cuándo empezó el modo actual (para la cuenta atrás de la comprobación). */
  val inicioModo: Long,
  /** Hasta cuándo está en silencio mientras se hace la tarea; null = sonando. */
  val silenciadaHasta: Long? = null,
  val terminada: Boolean = false,
  /** Terminada porque se hizo la tarea (o se respondió), no por tiempo. */
  val completada: Boolean = false,
)

/**
 * La lógica de una alarma sonando, sin nada de Android: así se prueba en el PC con
 * tiempo simulado.
 *
 * - Empieza bajita y sube hasta el máximo en [Ajustes.rampa].
 * - Al pulsar "Estoy despierto" (o una tecla de volumen) calla [Ajustes.silencio]
 *   para hacer la tarea. Cada toque en la tarea alarga el silencio. Si te quedas
 *   quieto, vuelve a sonar, y ya casi de golpe.
 * - En modo comprobación suena suave; si nadie responde en
 *   [Ajustes.esperaComprobacion], pasa a alarma entera.
 * - Si nadie hace nada en [Ajustes.duracionMaxima], se para.
 */
class SesionAlarma(
  alarma: Alarma,
  modo: Modo,
  prueba: Boolean,
  private val reloj: () -> Long,
  private val alcance: CoroutineScope,
  private val salida: SalidaSonido,
  private val ajustes: Ajustes = Ajustes(),
) {
  data class Ajustes(
    val rampa: Long = 45_000,
    val rampaTrasSilencio: Long = 4_000,
    val volumenInicial: Float = 0.15f,
    val silencio: Long = 30_000,
    val esperaComprobacion: Long = 60_000,
    val volumenComprobacion: Float = 0.35f,
    val duracionMaxima: Long = 30 * 60_000L,
    val paso: Long = 100,
  )

  private val _estado = MutableStateFlow(EstadoSesion(alarma, modo, prueba, inicio = reloj(), inicioModo = reloj()))
  val estado: StateFlow<EstadoSesion> = _estado.asStateFlow()

  private var volumen = 0f
  private var inicioSonido = reloj()
  private var rampaActual = if (modo == Modo.ALARMA) ajustes.rampa else ajustes.rampaTrasSilencio
  private var bucle: Job? = null

  fun iniciar() {
    paso()
    bucle =
      alcance.launch {
        while (isActive && !_estado.value.terminada) {
          delay(ajustes.paso)
          paso()
        }
      }
  }

  /** Silencio para hacer la tarea, o más silencio si ya lo había. Solo en modo alarma. */
  fun silenciar() {
    val e = _estado.value
    if (e.terminada || e.modo != Modo.ALARMA) return
    _estado.value = e.copy(silenciadaHasta = reloj() + ajustes.silencio)
    paso()
  }

  fun terminar(completada: Boolean) {
    val e = _estado.value
    if (e.terminada) return
    bucle?.cancel()
    salida.detener()
    _estado.value = e.copy(terminada = true, completada = completada, silenciadaHasta = null)
  }

  /** Un paso del reloj: decide volumen y vibración. */
  private fun paso() {
    val ahora = reloj()
    var e = _estado.value
    if (e.terminada) return
    if (ahora - e.inicio >= ajustes.duracionMaxima) {
      terminar(completada = false)
      return
    }
    // Se acabó el silencio sin terminar la tarea: vuelve a sonar.
    val hasta = e.silenciadaHasta
    if (hasta != null && ahora >= hasta) {
      e = e.copy(silenciadaHasta = null)
      inicioSonido = ahora
      rampaActual = ajustes.rampaTrasSilencio
    }
    // Nadie ha respondido a la comprobación: alarma entera.
    if (e.modo == Modo.COMPROBACION && ahora - e.inicioModo >= ajustes.esperaComprobacion) {
      e = e.copy(modo = Modo.ALARMA, inicioModo = ahora)
      inicioSonido = ahora
      rampaActual = ajustes.rampaTrasSilencio
    }
    if (e != _estado.value) _estado.value = e

    val objetivo =
      when {
        e.silenciadaHasta != null -> 0f
        e.modo == Modo.COMPROBACION -> ajustes.volumenComprobacion
        else -> {
          val avance = ((ahora - inicioSonido).toFloat() / rampaActual).coerceIn(0f, 1f)
          ajustes.volumenInicial + (1f - ajustes.volumenInicial) * avance
        }
      }
    // Baja en medio segundo (sin cortes secos); sube lo que marque la rampa.
    volumen = if (objetivo < volumen) max(objetivo, volumen - 0.2f) else objetivo
    salida.volumen(volumen)
    salida.vibrar(e.silenciadaHasta == null)
  }
}
