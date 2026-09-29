package com.oriol.alba.datos

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.FRIDAY
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * La base de datos de la versión 0.1 (la que ya hay en el móvil de Oriol) se abre con
 * la versión actual sin perder ninguna alarma.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class MigracionTest {

  private val context: Context = ApplicationProvider.getApplicationContext()

  @Test
  fun de1a2_lasAlarmasSeConservan_yLaActividadPasaASerUnaLista() = runTest {
    val nombre = "migracion.db"
    context.deleteDatabase(nombre)
    // La base de datos tal como la crea la versión 0.1 (sacado de app/schemas/.../1.json).
    val v1 = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(nombre), null)
    v1.execSQL(
      "CREATE TABLE IF NOT EXISTS `alarmas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hora` INTEGER NOT NULL, " +
        "`minuto` INTEGER NOT NULL, `dias` INTEGER NOT NULL, `activa` INTEGER NOT NULL, `etiqueta` TEXT NOT NULL, " +
        "`tarea` TEXT NOT NULL, `sonido` TEXT NOT NULL, `comprobar` INTEGER NOT NULL)"
    )
    v1.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
    v1.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6135500087a33c5bf65e1802a38f0ced')")
    // Lunes y viernes = bits 0 y 4 = 17.
    v1.execSQL("INSERT INTO alarmas VALUES (1, 7, 15, 17, 1, 'Trabajo', 'CALCULO', 'AMANECER', 1)")
    v1.execSQL("INSERT INTO alarmas VALUES (2, 9, 0, 0, 0, '', 'FOTO', 'SISTEMA', 0)")
    v1.version = 1
    v1.close()

    // La abre la versión actual: Room aplica la migración automática.
    val bd = Room.databaseBuilder(context, BaseDatos::class.java, nombre).allowMainThreadQueries().build()
    val alarmas = bd.alarmas().todas().first()
    bd.close()

    assertEquals(2, alarmas.size)
    val trabajo = alarmas.first { it.id == 1L }
    assertEquals(7, trabajo.hora)
    assertEquals(15, trabajo.minuto)
    assertEquals(setOf(MONDAY, FRIDAY), trabajo.dias)
    assertEquals("Trabajo", trabajo.etiqueta)
    assertEquals(setOf(TipoTarea.CALCULO), trabajo.tareas)
    val otra = alarmas.first { it.id == 2L }
    assertEquals(setOf(TipoTarea.FOTO), otra.tareas)
    assertEquals(Sonido.SISTEMA, otra.sonido)
    assertFalse(otra.activa)
    assertFalse(otra.comprobar)
  }
}
