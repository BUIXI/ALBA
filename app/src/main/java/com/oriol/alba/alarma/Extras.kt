package com.oriol.alba.alarma

import android.content.Intent
import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.Convertidores
import com.oriol.alba.datos.Sonido

/** Para qué suena: la alarma de verdad o la pregunta de después. */
enum class Modo {
  ALARMA,

  /** "¿Sigues despierto?" unos minutos después de apagarla. Si no se responde, suena entera. */
  COMPROBACION,
}

// La alarma viaja entera en los extras de los Intent: el servicio puede sonar sin
// tocar la base de datos, aunque esté bloqueada o tarde en abrirse.

private const val ID = "alba.id"
private const val HORA = "alba.hora"
private const val MINUTO = "alba.minuto"
private const val DIAS = "alba.dias"
private const val ETIQUETA = "alba.etiqueta"
private const val TAREAS = "alba.tareas"
private const val SONIDO = "alba.sonido"
private const val COMPROBAR = "alba.comprobar"
private const val MODO = "alba.modo"
private const val PRUEBA = "alba.prueba"

private val convertidores = Convertidores()

fun Intent.ponerAlarma(alarma: Alarma): Intent =
  putExtra(ID, alarma.id)
    .putExtra(HORA, alarma.hora)
    .putExtra(MINUTO, alarma.minuto)
    .putExtra(DIAS, convertidores.diasAMascara(alarma.dias))
    .putExtra(ETIQUETA, alarma.etiqueta)
    .putExtra(TAREAS, convertidores.tareasATexto(alarma.tareas))
    .putExtra(SONIDO, alarma.sonido.name)
    .putExtra(COMPROBAR, alarma.comprobar)

/** La alarma de los extras, o null si no la hay. Los valores raros caen en los de por defecto. */
fun Intent.leerAlarma(): Alarma? {
  if (!hasExtra(HORA)) return null
  return Alarma(
    id = getLongExtra(ID, 0L),
    hora = getIntExtra(HORA, 7).coerceIn(0, 23),
    minuto = getIntExtra(MINUTO, 0).coerceIn(0, 59),
    dias = convertidores.mascaraADias(getIntExtra(DIAS, 0)),
    etiqueta = getStringExtra(ETIQUETA).orEmpty(),
    tareas = convertidores.textoATareas(getStringExtra(TAREAS).orEmpty()),
    sonido = Sonido.entries.firstOrNull { it.name == getStringExtra(SONIDO) } ?: Sonido.AMANECER,
    comprobar = getBooleanExtra(COMPROBAR, true),
  )
}

fun Intent.ponerModo(modo: Modo): Intent = putExtra(MODO, modo.name)

fun Intent.leerModo(): Modo = Modo.entries.firstOrNull { it.name == getStringExtra(MODO) } ?: Modo.ALARMA

/** Una prueba desde el editor: suena igual, pero no toca la base de datos ni programa nada. */
fun Intent.ponerPrueba(prueba: Boolean): Intent = putExtra(PRUEBA, prueba)

fun Intent.esPrueba(): Boolean = getBooleanExtra(PRUEBA, false)
