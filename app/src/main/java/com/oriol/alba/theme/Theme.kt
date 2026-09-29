package com.oriol.alba.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// La app va siempre en modo oscuro, sin colores dinámicos del sistema (Material You):
// el aspecto tiene que ser el nuestro en cualquier móvil. Los huecos del esquema de
// Material se rellenan con la paleta propia para que no se cuele ningún morado.
private val EsquemaOscuro =
  darkColorScheme(
    primary = Ambar,
    onPrimary = Negro,
    background = Negro,
    onBackground = Blanco,
    surface = Negro,
    onSurface = Blanco,
    onSurfaceVariant = GrisSecundario,
  )

@Composable
fun AlbaTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = EsquemaOscuro, typography = Tipografia, content = content)
}
