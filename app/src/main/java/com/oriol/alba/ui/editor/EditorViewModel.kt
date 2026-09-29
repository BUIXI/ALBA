package com.oriol.alba.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.RepositorioAlarmas
import com.oriol.alba.datos.Sonido
import com.oriol.alba.datos.TipoTarea
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Editor de una alarma. Trabaja sobre un borrador: nada se guarda hasta pulsar
 * "Guardar". [alarmaId] 0 = alarma nueva.
 */
class EditorViewModel(private val repositorio: RepositorioAlarmas, private val alarmaId: Long) : ViewModel() {

  val esNueva = alarmaId == 0L

  private val _borrador = MutableStateFlow(if (esNueva) nuevaAlarma() else null)

  /** Null mientras se carga una alarma existente. */
  val borrador: StateFlow<Alarma?> = _borrador.asStateFlow()

  // Evita guardar dos veces si se pulsa "Guardar" deprisa.
  private var terminando = false

  init {
    if (!esNueva) viewModelScope.launch { _borrador.value = repositorio.obtener(alarmaId) ?: nuevaAlarma() }
  }

  fun cambiarHora(hora: Int, minuto: Int) = editar { it.copy(hora = hora, minuto = minuto) }

  fun cambiarDias(dias: Set<DayOfWeek>) = editar { it.copy(dias = dias) }

  fun cambiarEtiqueta(texto: String) = editar { it.copy(etiqueta = texto.take(LargoMaximoEtiqueta)) }

  fun cambiarTarea(tarea: TipoTarea) {
    if (tarea.disponible) editar { it.copy(tarea = tarea) }
  }

  fun cambiarSonido(sonido: Sonido) = editar { it.copy(sonido = sonido) }

  fun cambiarComprobar(comprobar: Boolean) = editar { it.copy(comprobar = comprobar) }

  /** Guarda (y activa: quien la guarda es porque la quiere) y luego llama a [alTerminar]. */
  fun guardar(alTerminar: () -> Unit) {
    val alarma = _borrador.value ?: return
    if (terminando) return
    terminando = true
    viewModelScope.launch {
      repositorio.guardar(alarma.copy(etiqueta = alarma.etiqueta.trim(), activa = true))
      alTerminar()
    }
  }

  fun eliminar(alTerminar: () -> Unit) {
    if (terminando) return
    terminando = true
    if (esNueva) {
      alTerminar()
      return
    }
    viewModelScope.launch {
      repositorio.eliminar(alarmaId)
      alTerminar()
    }
  }

  private inline fun editar(cambio: (Alarma) -> Alarma) = _borrador.update { it?.let(cambio) }

  companion object {
    const val LargoMaximoEtiqueta = 40

    /** Una alarma nueva empieza a las 7:00, sin repetir y con cálculo mental. */
    fun nuevaAlarma() = Alarma(hora = 7, minuto = 0)
  }
}
