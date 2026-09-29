package com.oriol.alba.ui.alarma

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.oriol.alba.AlbaApp
import com.oriol.alba.alarma.EstadoSesion
import com.oriol.alba.alarma.ServicioAlarma
import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.TipoTarea
import com.oriol.alba.dominio.CambiosDeObjeto
import com.oriol.alba.dominio.DetectorSeguido
import com.oriol.alba.dominio.Etiqueta
import com.oriol.alba.dominio.GeneradorCalculo
import com.oriol.alba.dominio.ObjetoFoto
import com.oriol.alba.dominio.objetoAlAzar
import com.oriol.alba.dominio.proximaAlarma
import com.oriol.alba.dominio.seVe
import kotlin.random.Random
import com.oriol.alba.ui.tareas.EstadoCalculo
import com.oriol.alba.ui.tareas.Tecla
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Órdenes al servicio que hace sonar la alarma. Interfaz para poder probar sin él. */
interface AccionesAlarma {
  /** Silencio para hacer la tarea (o alargarlo). */
  fun silenciar()

  /** Tarea hecha: parar. */
  fun terminar()
}

private class AccionesServicio(private val context: Context) : AccionesAlarma {
  override fun silenciar() = ServicioAlarma.silenciar(context)

  override fun terminar() = ServicioAlarma.terminar(context)
}

/** En qué punto está la persona frente a la alarma. */
sealed interface Fase {
  /** Suena; aún no ha pulsado "Estoy despierto". */
  data object Sonando : Fase

  /** Haciendo la tarea. */
  data object Tarea : Fase

  /** Apagada. [proxima] llega un instante después (se lee de la base de datos). */
  data class Hecho(
    val hora: LocalTime,
    val fueComprobacion: Boolean,
    val comprobaraDespues: Boolean,
    val proxima: LocalDateTime? = null,
  ) : Fase
}

class AlarmaViewModel(
  private val acciones: AccionesAlarma,
  private val alarmas: Flow<List<Alarma>>,
  private val generador: GeneradorCalculo = GeneradorCalculo(),
  private val reloj: () -> LocalDateTime = LocalDateTime::now,
  private val azar: Random = Random.Default,
) : ViewModel() {

  var fase: Fase by mutableStateOf(Fase.Sonando)
    private set

  var calculo by mutableStateOf(EstadoCalculo(generador))
    private set

  /** La tarea que se está haciendo. Empieza siendo la de la alarma; la foto puede pasar a cálculo. */
  var tareaActual: TipoTarea by mutableStateOf(TipoTarea.CALCULO)
    private set

  // --- Tarea de foto ---

  var objeto: ObjetoFoto by mutableStateOf(objetoAlAzar(azar))
    private set

  var cambiosRestantes by mutableIntStateOf(CambiosDeObjeto)
    private set

  /** Fotogramas seguidos en los que se ha visto el objeto (para iluminar el borde). */
  var vistosSeguidos by mutableIntStateOf(0)
    private set

  private val pedidos = mutableSetOf(objeto)
  private var detector = DetectorSeguido()

  /** La última sesión vista. Se queda aunque la alarma ya haya terminado. */
  var sesion: EstadoSesion? by mutableStateOf(null)
    private set

  /** Si alguna vez hubo sesión: si luego desaparece sin tarea hecha, la alarma se acabó sola. */
  var vioSesion = false
    private set

  fun alCambiarSesion(nueva: EstadoSesion?) {
    if (nueva == null) return
    val anterior = sesion
    // Otra alarma distinta (por ejemplo, la comprobación con esta pantalla aún abierta
    // en "Buenos días"): se empieza de cero.
    if (anterior != null && nueva.inicio != anterior.inicio) reiniciar()
    if (!vioSesion || anterior?.inicio != nueva.inicio) tareaActual = nueva.alarma.tarea
    sesion = nueva
    vioSesion = true
  }

  private fun reiniciar() {
    fase = Fase.Sonando
    calculo = EstadoCalculo(generador)
    objeto = objetoAlAzar(azar)
    pedidos.clear()
    pedidos += objeto
    cambiosRestantes = CambiosDeObjeto
    detector = DetectorSeguido()
    vistosSeguidos = 0
  }

  /** Lo que ve la cámara. Tres fotogramas seguidos con el objeto: tarea hecha. */
  fun etiquetas(etiquetas: List<Etiqueta>) {
    if (fase !is Fase.Tarea || tareaActual != TipoTarea.FOTO) return
    val hecho = detector.fotograma(seVe(objeto, etiquetas))
    vistosSeguidos = detector.seguidos
    if (hecho) completar(fueComprobacion = false)
  }

  /** "Pedir otro objeto": por si no tienes tele o planta. Pocas veces, o sería trampa. */
  fun cambiarObjeto() {
    if (cambiosRestantes <= 0) return
    cambiosRestantes--
    objeto = objetoAlAzar(azar, excepto = pedidos)
    pedidos += objeto
    detector = DetectorSeguido()
    vistosSeguidos = 0
    acciones.silenciar()
  }

  /** Mientras se busca el objeto no se toca la pantalla: la cámara pide el silencio. */
  fun seguirBuscando() {
    if (fase is Fase.Tarea && tareaActual == TipoTarea.FOTO) acciones.silenciar()
  }

  /** Sin cámara (o si no reconoce nada): cálculo. La alarma siempre se tiene que poder apagar. */
  fun pasarACalculo() {
    if (tareaActual == TipoTarea.CALCULO) return
    tareaActual = TipoTarea.CALCULO
    if (fase is Fase.Tarea) acciones.silenciar()
  }

  /** "Estoy despierto" o una tecla de volumen: silencio y a la tarea. */
  fun despierto() {
    if (fase is Fase.Hecho) return
    acciones.silenciar()
    fase = Fase.Tarea
  }

  /** Una tecla del cálculo. Cada toque alarga el silencio. Devuelve acierto/fallo si lo hubo. */
  fun tecla(tecla: Tecla): Boolean? {
    if (fase !is Fase.Tarea || tareaActual != TipoTarea.CALCULO) return null
    acciones.silenciar()
    val resultado = calculo.pulsar(tecla)
    if (calculo.completada) completar(fueComprobacion = false)
    return resultado
  }

  /** "Sí, estoy despierto" en la comprobación. */
  fun responderComprobacion() = completar(fueComprobacion = true)

  private fun completar(fueComprobacion: Boolean) {
    if (fase is Fase.Hecho) return
    val actual = sesion
    acciones.terminar()
    val comprobara = !fueComprobacion && actual != null && actual.alarma.comprobar && !actual.prueba
    fase = Fase.Hecho(reloj().toLocalTime(), fueComprobacion, comprobara)
    viewModelScope.launch {
      val proxima = proximaAlarma(alarmas.first(), reloj())?.second
      (fase as? Fase.Hecho)?.let { fase = it.copy(proxima = proxima) }
    }
  }

  companion object {
    fun fabrica(app: AlbaApp) = viewModelFactory {
      initializer { AlarmaViewModel(AccionesServicio(app), app.contenedor.repositorio.alarmas) }
    }
  }
}
