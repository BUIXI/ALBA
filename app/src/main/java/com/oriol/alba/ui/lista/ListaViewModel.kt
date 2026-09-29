package com.oriol.alba.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.RepositorioAlarmas
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListaViewModel(private val repositorio: RepositorioAlarmas) : ViewModel() {

  /** Null mientras se lee la base de datos: así no parpadea el "Sin alarmas" al abrir. */
  val alarmas: StateFlow<List<Alarma>?> =
    repositorio.alarmas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  fun cambiarActiva(id: Long, activa: Boolean) {
    viewModelScope.launch { repositorio.cambiarActiva(id, activa) }
  }
}
