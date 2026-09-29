package com.oriol.alba.dominio

import com.oriol.alba.datos.Alarma
import com.oriol.alba.datos.repetida
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

// Cuándo suena cada alarma. Todo en hora local de pared (LocalDateTime): la fase 2
// lo pasará a un instante con la zona horaria al programarla en el sistema.

/**
 * El próximo momento, estrictamente después de [ahora], en que sonará la alarma.
 * Null si está desactivada.
 *
 * - Sin días marcados: hoy a su hora si aún no ha pasado; si no, mañana.
 * - Con días: el primero de esos días (desde hoy) cuya hora no haya pasado.
 */
fun siguienteDisparo(alarma: Alarma, ahora: LocalDateTime): LocalDateTime? {
  if (!alarma.activa) return null
  val hora = LocalTime.of(alarma.hora, alarma.minuto)
  // Con 8 días (hoy + una semana) siempre aparece uno válido.
  for (dias in 0L..7L) {
    val fecha = ahora.toLocalDate().plusDays(dias)
    val momento = fecha.atTime(hora)
    if (!momento.isAfter(ahora)) continue
    if (!alarma.repetida || fecha.dayOfWeek in alarma.dias) return momento
  }
  return null
}

/** La alarma activa que antes va a sonar, con su momento. Null si no hay ninguna. */
fun proximaAlarma(alarmas: List<Alarma>, ahora: LocalDateTime): Pair<Alarma, LocalDateTime>? =
  alarmas.mapNotNull { alarma -> siguienteDisparo(alarma, ahora)?.let { alarma to it } }.minByOrNull { it.second }

/**
 * Minutos que faltan hasta [momento], redondeando hacia arriba: si faltan 7 h 11 min
 * y 20 s, son 7 h 12 min (lo que diría el reloj de iOS).
 */
fun minutosHasta(ahora: LocalDateTime, momento: LocalDateTime): Long {
  val segundos = Duration.between(ahora, momento).seconds
  return (segundos + 59) / 60
}
