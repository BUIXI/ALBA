// Archivo de compilación raíz: solo declara los plugins; cada módulo aplica los suyos.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.roborazzi) apply false
}
