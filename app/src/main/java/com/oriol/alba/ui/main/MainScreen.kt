package com.oriol.alba.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oriol.alba.R
import com.oriol.alba.theme.AlbaTheme

/**
 * Pantalla principal. De momento solo el título y el estado vacío: la lista de
 * alarmas de verdad llega en la fase 1.
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
  // Cada pantalla pinta su propio fondo: no se fía del que haya debajo.
  Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp)) {
    Text(
      text = stringResource(R.string.titulo_alarmas),
      style = MaterialTheme.typography.displaySmall,
      color = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.padding(top = 16.dp),
    )
    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
      Text(
        text = stringResource(R.string.sin_alarmas),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MainScreenPreview() {
  AlbaTheme { MainScreen() }
}
