package com.oriol.alba

import android.app.Application
import android.content.Context
import com.oriol.alba.alarma.Notificaciones
import com.oriol.alba.alarma.Programador
import com.oriol.alba.alarma.ProgramadorSistema
import com.oriol.alba.datos.BaseDatos
import com.oriol.alba.datos.RepositorioAlarmas

/**
 * La aplicación. Guarda el [Contenedor] con las piezas compartidas y crea los canales
 * de notificación. Arranca también antes del primer desbloqueo (para las alarmas):
 * aquí no se toca el almacenamiento normal.
 */
class AlbaApp : Application() {
  val contenedor: Contenedor by lazy { Contenedor(this) }

  override fun onCreate() {
    super.onCreate()
    Notificaciones.crearCanales(this)
  }
}

/**
 * Las piezas que se crean una vez y se comparten (base de datos, repositorio...).
 * Se pasan a mano a quien las necesite: la app es pequeña y no hace falta Hilt.
 */
class Contenedor(context: Context) {
  private val baseDatos = BaseDatos.abrir(context)
  val programador: Programador = ProgramadorSistema(context)
  val repositorio = RepositorioAlarmas(baseDatos.alarmas(), programador)
}
