package com.oriol.alba

import android.app.Application
import android.content.Context
import com.oriol.alba.datos.BaseDatos
import com.oriol.alba.datos.RepositorioAlarmas

/** La aplicación. Solo guarda el [Contenedor] con las piezas compartidas. */
class AlbaApp : Application() {
  val contenedor: Contenedor by lazy { Contenedor(this) }
}

/**
 * Las piezas que se crean una vez y se comparten (base de datos, repositorio...).
 * Se pasan a mano a quien las necesite: la app es pequeña y no hace falta Hilt.
 */
class Contenedor(context: Context) {
  private val baseDatos = BaseDatos.abrir(context)
  val repositorio = RepositorioAlarmas(baseDatos.alarmas())
}
