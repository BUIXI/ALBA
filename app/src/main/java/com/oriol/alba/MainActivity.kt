package com.oriol.alba

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.oriol.alba.alarma.CentralAlarma
import com.oriol.alba.theme.Alba
import com.oriol.alba.theme.TemaAlba
import com.oriol.alba.ui.alarma.ActividadAlarma
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Pantalla de borde a borde con las barras del sistema transparentes e iconos
    // claros, aunque el móvil esté en modo claro: la app siempre es oscura.
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )
    val contenedor = (application as AlbaApp).contenedor

    // Por si el sistema ha perdido alguna (o se cambió la hora con la app cerrada).
    lifecycleScope.launch(Dispatchers.IO) { contenedor.repositorio.reprogramarTodas() }

    // Si hay una alarma sonando (o se prueba una desde el editor), su pantalla va
    // encima: desde la lista no se puede esquivar la tarea.
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        CentralAlarma.sesion.collect { sesion ->
          if (sesion != null) startActivity(ActividadAlarma.intent(this@MainActivity))
        }
      }
    }

    setContent {
      TemaAlba {
        // Fondo también por debajo de las pantallas, para las transiciones.
        Box(Modifier.fillMaxSize().background(Alba.colores.fondo)) { NavegacionAlba(contenedor.repositorio) }
      }
    }
  }
}
