package com.oriol.alba.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tipografía provisional con la fuente del sistema. En la fase 1 entra una fuente
// propia y la escala completa (título grande, cuerpo, pie...).
val Tipografia =
  Typography(
    // Título grande de pantalla, al estilo de los "large titles" de iOS
    displaySmall =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 41.sp,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 22.sp,
      ),
  )
