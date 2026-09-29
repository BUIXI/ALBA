package com.oriol.alba.dominio

import kotlin.random.Random

// Cálculo mental para apagar la alarma. Las operaciones se hacen de cabeza en unos
// segundos si estás despierto, y medio dormido cuestan: justo lo que se busca.

enum class Operador(val simbolo: String) {
  SUMA("+"),
  RESTA("−"),
  MULTIPLICACION("×"),
}

data class Operacion(val a: Int, val b: Int, val operador: Operador) {
  val resultado: Int
    get() =
      when (operador) {
        Operador.SUMA -> a + b
        Operador.RESTA -> a - b
        Operador.MULTIPLICACION -> a * b
      }

  /** "47 + 38" */
  val texto: String
    get() = "$a ${operador.simbolo} $b"
}

/**
 * Genera operaciones. Con la misma semilla salen las mismas (para las pruebas).
 *
 * - Suma: dos números de dos cifras que no acaban en 0 (47 + 38).
 * - Resta: el resultado siempre positivo y con llevada casi siempre (83 − 47).
 * - Multiplicación: una cifra por dos cifras (7 × 18).
 */
class GeneradorCalculo(private val azar: Random = Random.Default) {

  fun siguiente(anterior: Operacion? = null): Operacion {
    // Nunca la misma dos veces seguidas: al fallar, se cambia de operación.
    var nueva: Operacion
    do {
      nueva = crear()
    } while (nueva == anterior)
    return nueva
  }

  private fun crear(): Operacion =
    when (Operador.entries[azar.nextInt(Operador.entries.size)]) {
      Operador.SUMA -> Operacion(sinCero(12, 89), sinCero(12, 89), Operador.SUMA)
      Operador.RESTA -> {
        val a = sinCero(41, 98)
        // b menor que a, y con las unidades de b mayores que las de a (llevada) si se puede.
        var b = sinCero(12, a - 11)
        repeat(4) { if (b % 10 <= a % 10) b = sinCero(12, a - 11) }
        Operacion(a, b, Operador.RESTA)
      }
      Operador.MULTIPLICACION -> Operacion(azar.nextInt(3, 10), sinCero(12, 29), Operador.MULTIPLICACION)
    }

  /** Un número entre [desde] y [hasta] (ambos incluidos) que no acaba en 0. */
  private fun sinCero(desde: Int, hasta: Int): Int {
    var n: Int
    do {
      n = azar.nextInt(desde, hasta + 1)
    } while (n % 10 == 0)
    return n
  }
}

/** Cuántas operaciones hay que acertar para apagar la alarma. */
const val OperacionesParaApagar = 3
