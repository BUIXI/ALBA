package com.oriol.alba.ui.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

// El estado de los minijuegos para apagar la alarma. Solo lógica (las pantallas están
// en ui/alarma/Juegos.kt): se prueban en el PC sin dibujar nada.
//
// La idea de todos: sencillos, que se entiendan de un vistazo recién despierto y que
// pidan atención de verdad (mirar, recordar, apuntar), sin cuentas.

/**
 * **Atrapa los soles**: sale un sol en un sitio al azar; si se toca, cuenta y sale
 * otro; si no, se escapa a otro sitio. Cada vez duran un poco menos.
 */
class EstadoSoles(private val azar: Random, val total: Int = 12) {

  var aciertos by mutableIntStateOf(0)
    private set

  /** Cambia con cada sol nuevo (atrapado o escapado): reinicia su animación y su tiempo. */
  var turno by mutableIntStateOf(0)
    private set

  /** Dónde está el sol, en fracción del área de juego (0-1 en cada eje). */
  var posicion by mutableStateOf(Offset(azar.nextFloat(), azar.nextFloat()))
    private set

  val completado: Boolean
    get() = aciertos >= total

  /** Milisegundos que dura cada sol antes de escaparse: de 1,9 s a 1,1 s. */
  val vida: Long
    get() = (1_900L - aciertos * 70L).coerceAtLeast(1_100L)

  fun atrapar() {
    if (completado) return
    aciertos++
    mover()
  }

  fun escapar() {
    if (!completado) mover()
  }

  /** El siguiente, lejos del anterior: hay que buscarlo con la vista. */
  private fun mover() {
    var nueva: Offset
    do {
      nueva = Offset(azar.nextFloat(), azar.nextFloat())
    } while ((nueva - posicion).getDistance() < 0.35f)
    posicion = nueva
    turno++
  }
}

/**
 * **Repite la secuencia** (tipo Simón): se encienden unos botones en orden y hay que
 * repetirlo. Dos rondas, de 4 y de 5. Al fallar, otra secuencia de la misma ronda.
 */
class EstadoSecuencia(private val azar: Random, val longitudes: List<Int> = listOf(4, 5)) {

  /** Botones del juego. */
  val botones = 4

  var ronda by mutableIntStateOf(0)
    private set

  var secuencia by mutableStateOf(nueva(longitudes.first()))
    private set

  /** Cambia con cada secuencia nueva: la pantalla la vuelve a enseñar. */
  var version by mutableIntStateOf(0)
    private set

  /** Enseñando la secuencia: aún no se puede pulsar. */
  var mostrando by mutableStateOf(true)
    private set

  /** Botones acertados en esta ronda. */
  var progreso by mutableIntStateOf(0)
    private set

  var fallos by mutableIntStateOf(0)
    private set

  var completado by mutableStateOf(false)
    private set

  fun terminarDeMostrar() {
    mostrando = false
    progreso = 0
  }

  /** Resultado: null = no cuenta (enseñando o ya hecho); true/false = acierto/fallo. */
  fun pulsar(boton: Int): Boolean? {
    if (mostrando || completado) return null
    if (secuencia[progreso] != boton) {
      fallos++
      otra()
      return false
    }
    progreso++
    if (progreso == secuencia.size) {
      ronda++
      if (ronda >= longitudes.size) completado = true else otra()
    }
    return true
  }

  private fun otra() {
    secuencia = nueva(longitudes[ronda])
    version++
    mostrando = true
    progreso = 0
  }

  private fun nueva(largo: Int) = List(largo) { azar.nextInt(botones) }
}

/**
 * **Parejas**: 12 cartas boca abajo, 6 parejas. Se dan la vuelta de dos en dos; si no
 * son pareja, la pantalla las vuelve a tapar al momento ([ocultar]).
 */
class EstadoParejas(azar: Random, pares: Int = 6) {

  /** El símbolo de cada carta (0 hasta pares-1, cada uno dos veces), barajadas. */
  val cartas: List<Int> = (0 until pares).flatMap { listOf(it, it) }.shuffled(azar)

  /** Cartas destapadas que aún no son pareja (como mucho dos). */
  var vueltas by mutableStateOf(emptyList<Int>())
    private set

  var emparejadas by mutableStateOf(emptySet<Int>())
    private set

  var intentos by mutableIntStateOf(0)
    private set

  val pares: Int
    get() = cartas.size / 2

  val completado: Boolean
    get() = emparejadas.size == cartas.size

  fun bocaArriba(carta: Int) = carta in vueltas || carta in emparejadas

  /** Resultado: null = aún falta la segunda (o no cuenta); true/false = pareja o no. */
  fun tocar(carta: Int): Boolean? {
    if (completado || bocaArriba(carta) || vueltas.size >= 2) return null
    vueltas = vueltas + carta
    if (vueltas.size < 2) return null
    intentos++
    val (a, b) = vueltas
    if (cartas[a] != cartas[b]) return false
    emparejadas = emparejadas + a + b
    vueltas = emptyList()
    return true
  }

  /** Tapa las dos que no eran pareja. */
  fun ocultar() {
    if (vueltas.size == 2) vueltas = emptyList()
  }
}

/** **Del 1 al 12**: los números desordenados en una rejilla; hay que tocarlos en orden. */
class EstadoOrden(azar: Random, val total: Int = 12) {

  /** Los números en el orden en que se ven en la rejilla. */
  val numeros: List<Int> = (1..total).shuffled(azar)

  var siguiente by mutableIntStateOf(1)
    private set

  var fallos by mutableIntStateOf(0)
    private set

  val completado: Boolean
    get() = siguiente > total

  /** Resultado: null = ya estaba tocado; true/false = el que tocaba o no. */
  fun tocar(numero: Int): Boolean? {
    if (completado || numero < siguiente) return null
    if (numero != siguiente) {
      fallos++
      return false
    }
    siguiente++
    return true
  }
}
