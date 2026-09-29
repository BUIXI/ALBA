package com.oriol.alba.ui.componentes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import java.time.LocalDateTime
import kotlinx.coroutines.delay

/** La hora actual, que se actualiza justo al empezar cada minuto. */
@Composable
fun rememberAhora(): LocalDateTime {
  val ahora by
    produceState(LocalDateTime.now()) {
      while (true) {
        val t = LocalDateTime.now()
        value = t
        // Hasta el próximo cambio de minuto, más un pelo para no quedarse corto.
        delay(60_000L - t.second * 1_000L - t.nano / 1_000_000L + 20L)
      }
    }
  return ahora
}

/** "07:05": la hora en formato de 24 h con dos cifras. */
fun formatoHora(hora: Int, minuto: Int): String =
  hora.toString().padStart(2, '0') + ":" + minuto.toString().padStart(2, '0')
