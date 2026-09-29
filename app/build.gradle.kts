plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.ksp)
  alias(libs.plugins.room)
}

// Room guarda aquí el esquema de cada versión de la base de datos. Va a Git:
// hace falta para escribir y probar las migraciones cuando cambie.
room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.oriol.alba"
    compileSdk = 36
    defaultConfig {
        // El applicationId no se puede cambiar una vez publicada la app en Play.
        applicationId = "com.oriol.alba"
        // Android 8.0: cubre casi todos los móviles en uso y ya tiene canales de
        // notificación, que la alarma necesita.
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "0.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Solo las arquitecturas de los móviles de hoy. La librería de ML Kit trae
        // código nativo (unos 10 MB por arquitectura): así el APK pesa mucho menos.
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
    }

    buildTypes {
        debug {
            // La de depuración también para el emulador del PC.
            ndk { abiFilters += "x86_64" }
        }
        release {
            // R8 (minificar) reduciría mucho el tamaño, pero puede romper cosas en
            // tiempo de ejecución: se activará cuando se pueda probar en el móvil.
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // APK de pruebas: firmado con la clave de depuración de este PC. Para
            // publicar en Play hará falta una clave propia (Play rechaza esta).
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }

    // Robolectric necesita los recursos de Android para dibujar las pantallas.
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  testImplementation(composeBom)
  androidTestImplementation(composeBom)

  // Base de Android
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Ciclo de vida y ViewModel desde Compose
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose. Material 3 solo aporta piezas sueltas: el aspecto es propio.
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Navegación (Navigation 3)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Tarea de foto: CameraX y el reconocedor de objetos de ML Kit, con el modelo dentro
  // de la app (funciona sin internet y sin coste por uso).
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)
  implementation(libs.mlkit.etiquetas)

  // Base de datos local (Room)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)
  testImplementation(libs.androidx.room.testing)

  // Pruebas locales en el PC: JUnit, corrutinas, Robolectric y capturas
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)

  // Pruebas en el móvil (instrumentadas)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)
}
