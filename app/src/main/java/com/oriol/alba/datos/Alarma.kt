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
  /** Lo que hay que hacer para apagarla. */
  val tarea: TipoTarea = TipoTarea.CALCULO,
  val sonido: Sonido = Sonido.AMANECER,
  /** Si a los 10 minutos de apagarla pregunta "¿Sigues despierto?". */
  val comprobar: Boolean = true,
)

// Fuera de la clase para que Room no la tome por una columna.
/** Si se repite algún día de la semana (si no, suena una sola vez). */
val Alarma.repetida: Boolean
  get() = dias.isNotEmpty()

/**
 * Tareas para apagar la alarma. Room las guarda por su nombre: no renombrar las
 * constantes sin una migración.
 */
enum class TipoTarea(
  /** Si ya se puede elegir. Las que no, se ven apagadas con "Pronto". */
  val disponible: Boolean
) {
  CALCULO(disponible = true),
  FOTO(disponible = true),
}

/** Sonido de la alarma. Se guarda por su nombre, como [TipoTarea]. */
enum class Sonido {
  /** El nuestro: un carillón suave que sube de volumen (res/raw/amanecer.wav). */
  AMANECER,

  /** El sonido de alarma que tenga elegido el móvil. */
  SISTEMA,
}
