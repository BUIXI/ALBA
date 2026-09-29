package com.oriol.alba.datos

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmaDao {
  /** Todas, de la más temprana a la más tardía. Se vuelve a emitir con cada cambio. */
  @Query("SELECT * FROM alarmas ORDER BY hora, minuto, id") fun todas(): Flow<List<Alarma>>

  /** Todas, una sola vez (para reprogramarlas). */
  @Query("SELECT * FROM alarmas") suspend fun todasAhora(): List<Alarma>

  @Query("SELECT * FROM alarmas WHERE id = :id") suspend fun obtener(id: Long): Alarma?

  /** Inserta o actualiza. Devuelve el id nuevo al insertar y -1 al actualizar. */
  @Upsert suspend fun guardar(alarma: Alarma): Long

  @Query("UPDATE alarmas SET activa = :activa WHERE id = :id") suspend fun cambiarActiva(id: Long, activa: Boolean)

  @Query("DELETE FROM alarmas WHERE id = :id") suspend fun eliminar(id: Long)
}

/** Los días se guardan como una máscara de bits: lunes = bit 0 ... domingo = bit 6. */
class Convertidores {
  @TypeConverter fun diasAMascara(dias: Set<DayOfWeek>): Int = dias.fold(0) { mascara, dia -> mascara or bit(dia) }

  @TypeConverter
  fun mascaraADias(mascara: Int): Set<DayOfWeek> = DayOfWeek.entries.filterTo(mutableSetOf()) { mascara and bit(it) != 0 }

  private fun bit(dia: DayOfWeek) = 1 shl (dia.value - 1)
}

@Database(entities = [Alarma::class], version = 1)
@TypeConverters(Convertidores::class)
abstract class BaseDatos : RoomDatabase() {
  abstract fun alarmas(): AlarmaDao

  companion object {
    /**
     * La base de datos vive en el almacenamiento protegido del dispositivo, no en el
     * normal: así se puede leer antes del primer desbloqueo. Si el móvil se reinicia
     * de madrugada (una actualización), las alarmas se reprograman y suenan igual.
     */
    fun abrir(context: Context): BaseDatos =
      Room.databaseBuilder(context.createDeviceProtectedStorageContext(), BaseDatos::class.java, "alba.db").build()

    /** Solo para pruebas: vive en memoria y desaparece al cerrarla. */
    fun enMemoria(context: Context): BaseDatos =
      Room.inMemoryDatabaseBuilder(context, BaseDatos::class.java).allowMainThreadQueries().build()
  }
}
