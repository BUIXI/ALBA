package com.oriol.alba.ui.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.oriol.alba.dominio.GeneradorCalculo
import com.oriol.alba.dominio.Operacion
import com.oriol.alba.dominio.OperacionesParaApagar

/** Una tecla del teclado numérico. */
sealed interface Tecla {
  data class Digito(val valor: Int) : Tecla

  data object Borrar : Tecla
}

/**
 * La tarea de cálculo mental en marcha. La respuesta se comprueba sola al escribir
 * tantas cifras como tiene el resultado (como un código de desbloqueo): sin botón de
 * "OK" que buscar medio dormido. Si se falla, sale otra operación distinta.
 */
class EstadoCalculo(private val generador: GeneradorCalculo, val total: Int = OperacionesParaApagar) {

  var operacion: Operacion by mutableStateOf(generador.siguiente())
    private set

  var respuesta by mutableStateOf("")
    private set

  var aciertos by mutableIntStateOf(0)
    private set

  /** Cuenta de fallos: cada cambio dispara la sacudida de la pantalla. */
  var fallos by mutableIntStateOf(0)
    private set

  val completada: Boolean
    get() = aciertos >= total

  /** Resultado de pulsar: null = sigue escribiendo; true/false = acierto/fallo. */
  fun pulsar(tecla: Tecla): Boolean? {
    if (completada) return null
    when (tecla) {
      Tecla.Borrar -> respuesta = respuesta.dropLast(1)
      is Tecla.Digito -> {
        // Un cero delante no cuenta ("07" no es una respuesta de dos cifras).
        if (respuesta.isEmpty() && tecla.valor == 0) return null
        respuesta += tecla.valor
        if (respuesta.length >= operacion.resultado.toString().length) return comprobar()
      }
    }
    return null
  }

  private fun comprobar(): Boolean {
    val acierto = respuesta.toIntOrNull() == operacion.resultado
    respuesta = ""
    if (acierto) aciertos++ else fallos++
    if (!completada) operacion = generador.siguiente(operacion)
    return acierto
  }
}
