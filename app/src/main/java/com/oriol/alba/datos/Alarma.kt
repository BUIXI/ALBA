package com.oriol.alba.datos

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

/** Una alarma tal como se guarda. */
@Entity(tableName = "alarmas")
data class Alarma(
  /** 0 = aún no se ha guardado; Room le da un número al insertarla. */
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  /** Hora en formato de 24 h (0-23). */
  val hora: Int,
  val minuto: Int,
  /** Días en que se repite. Vacío = suena una sola vez. */
  val dias: Set<DayOfWeek> = emptySet(),
  val activa: Boolean = true,
  /** Nombre opcional ("Trabajo", "Gimnasio"...). Vacío = sin nombre. */
  val etiqueta: String = "",
  /**
   * Las actividades para apagarla. Con una, siempre esa; con varias (Premium), cada
   * vez que suena toca una al azar. Nunca vacío.
   */
  val tareas: Set<TipoTarea> = setOf(TipoTarea.PorDefecto),
  val sonido: Sonido = Sonido.AMANECER,
  /** Si a los 10 minutos de apagarla pregunta "¿Sigues despierto?". */
  val comprobar: Boolean = true,
)

// Fuera de la clase para que Room no las tome por columnas.

/** Si se repite algún día de la semana (si no, suena una sola vez). */
val Alarma.repetida: Boolean
  get() = dias.isNotEmpty()

/**
 * Las actividades para apagar la alarma: el catálogo. Para añadir una nueva basta con
 * una constante aquí, sus textos y su pantalla en `ui/tareas`.
 *
 * Room las guarda por su nombre: no renombrar las constantes sin una migración.
 * El orden de las constantes es el orden en que se enseñan en el editor.
 */
enum class TipoTarea(
  /** Si ya se puede elegir. Las que no, se ven apagadas con "Pronto". */
  val disponible: Boolean = true,
  /** Necesita la cámara (sin permiso, se cambia por otra al sonar). */
  val usaCamara: Boolean = false,
) {
  /** Minijuego: tocar soles que aparecen y se escapan. */
  SOLES,

  /** Minijuego: memorizar y repetir una secuencia de colores (tipo Simón). */
  SECUENCIA,

  /** Minijuego: memoria, encontrar las parejas de cartas. */
  PAREJAS,

  /** Minijuego: tocar los números del 1 al 12 en orden. */
  ORDEN,

  /** Enseñar a la cámara un objeto de casa (ML Kit). */
  FOTO(usaCamara = true),

  /** Tres operaciones de cálculo mental. */
  CALCULO;

  companion object {
    /** La de las alarmas nuevas: un minijuego, más amable recién despierto que las cuentas. */
    val PorDefecto = SOLES
  }
}

/** Sonido de la alarma. Se guarda por su nombre, como [TipoTarea]. */
enum class Sonido {
  /** El nuestro: un carillón suave que sube de volumen (res/raw/amanecer.wav). */
  AMANECER,

  /** El sonido de alarma que tenga elegido el móvil. */
  SISTEMA,
}
