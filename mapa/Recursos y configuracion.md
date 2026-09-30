---
tags: [mapa]
aliases: [Recursos y configuración, Gradle, Manifiesto]
---
# Recursos y configuración

Volver al [[00 Indice]]. Todo lo que no es código Kotlin.

## Manifiesto (`app/src/main/AndroidManifest.xml`)

- Permisos: ver [[alarma - sistema#Permisos]]. `SCHEDULE_EXACT_ALARM` lleva `maxSdkVersion="32"`. `uses-feature camera.any` con `required="false"`.
- `<application>`:
  - `.AlbaApp`, tema `@style/Theme.Alba`, `supportsRtl`.
  - Icono `@mipmap/ic_launcher` (+ `_round`).
  - `allowBackup="true"` con `@xml/backup_rules` y `@xml/data_extraction_rules`, que siguen siendo **la plantilla sin configurar**.
- `.MainActivity`: `exported`, LAUNCHER, `windowSoftInputMode="adjustResize"`.
- `.ui.alarma.ActividadAlarma`: no `exported`, `directBootAware`, `excludeFromRecents`, `launchMode="singleInstance"`, `taskAffinity=""`, `screenOrientation="portrait"`, `showWhenLocked` y `turnScreenOn`, tema `@style/Theme.Alba.Alarma`.
- `.alarma.ServicioAlarma`: `foregroundServiceType="mediaPlayback"`, `directBootAware`.
- `.alarma.ReceptorAlarma` y `.alarma.ReceptorSistema`: `directBootAware`, no `exported`. Las acciones del segundo están en [[Arquitectura]].

## `app/src/main/res/`

| Ruta | Contenido |
|---|---|
| `values/strings.xml`, `values-es/strings.xml` | Textos en inglés (por defecto) y en español → [[Textos]] |
| `values/themes.xml` | `Theme.Alba` (Material NoActionBar, ventana negra, barras transparentes) y `Theme.Alba.Alarma` (claro, fondo `@color/fondo_alarma`) |
| `values-v31/themes.xml` | Android 12+: `Theme.Alba` con `windowSplashScreenBackground = fondo_icono` (arranque en negro) |
| `values/colors.xml` | `fondo_icono` `#000000`, `fondo_alarma` `#FFF1E0` (el tono del degradado de amanecer) |
| `mipmap-anydpi/ic_launcher.xml`, `ic_launcher_round.xml` | Icono adaptativo: fondo `fondo_icono`, delante y monocromo `ic_launcher_foreground` |
| `drawable/ic_launcher_foreground.xml` | Medio sol ámbar sobre el mar |
| `drawable/ic_notificacion.xml`, `ic_mas.xml`, `ic_check.xml`, `ic_borrar.xml` | Iconos vectoriales |
| `font/inter.ttf` | Inter variable (877 KB) |
| `raw/amanecer.wav` | Sonido propio (lo genera `tools/GenerarSonido.java`) |
| `xml/backup_rules.xml`, `xml/data_extraction_rules.xml` | Plantillas vacías (sin reglas) |

`app/src/main/assets/licencias/Inter-OFL.txt`: licencia de la fuente.

## Gradle

| Archivo | Qué hay |
|---|---|
| `settings.gradle.kts` | `rootProject.name = "Alba"`, `include(":app")`. Repos `google()` (filtrado a androidx/com.android/com.google) y `mavenCentral()`. Plugin `foojay-resolver-convention` 1.0.0 (descarga el JDK) |
| `build.gradle.kts` (raíz) | Solo declara los plugins con `apply false` |
| `app/build.gradle.kts` | Plugins: android.application, compose.compiler, kotlin.serialization, roborazzi, ksp, room. `room { schemaDirectory }`. `android { ... }`: SDK, versiones, ABIs, release sin R8 y firmado con la clave de depuración, Java 17, `unitTests.isIncludeAndroidResources`. Dependencias por grupos → [[Compilar y entorno]] |
| `gradle/libs.versions.toml` | Catálogo de versiones, librerías y plugins (ver abajo) |
| `gradle.properties` | `jvmargs -Xmx2048m`, `caching=true`, `configuration-cache=true`, `useAndroidX`, `nonTransitiveRClass` |
| `gradle/wrapper/*`, `gradlew`, `gradlew.bat` | Gradle 9.1.0 |
| `app/proguard-rules.pro` | Vacío (solo un comentario: R8 apagado) |

Librerías del catálogo:

| Grupo | Librerías |
|---|---|
| androidx base | core-ktx, activity-compose, lifecycle (runtime-ktx, runtime-compose, viewmodel-compose, viewmodel-navigation3) |
| Compose | BOM, ui, ui-tooling(-preview), material3, ui-test-junit4, ui-test-manifest |
| Navegación | navigation3-runtime, navigation3-ui |
| Room | runtime, ktx, compiler (KSP), testing |
| Cámara y ML | camera-camera2, camera-lifecycle, camera-view, `com.google.mlkit:image-labeling` |
| Pruebas | junit, coroutines-test, androidx.test (core, ext-junit, runner, espresso), robolectric, roborazzi, roborazzi-compose |

## Base de datos: esquema SQL (v2)

```sql
CREATE TABLE alarmas (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, hora INTEGER NOT NULL,
  minuto INTEGER NOT NULL, dias INTEGER NOT NULL, activa INTEGER NOT NULL, etiqueta TEXT NOT NULL,
  tareas TEXT NOT NULL, sonido TEXT NOT NULL, comprobar INTEGER NOT NULL)
```

Archivos: `app/schemas/com.oriol.alba.datos.BaseDatos/1.json` (con `tarea`) y `2.json`. Más en [[datos]].

## Otros archivos de la raíz

- `.gitignore`: `.gradle/`, `.kotlin/`, `build/`, `*.apk`, `*.aab`, `/entregas/`, `local.properties`, `.idea/`, claves (`*.jks`, `*.keystore`, `keystore.properties`) y `.obsidian/`.
- `.gitattributes`: `gradlew` con LF, `*.bat` con CRLF, imágenes y jar binarios.
- `tools/GenerarSonido.java`: `java tools/GenerarSonido.java app/src/main/res/raw/amanecer.wav`. Arpegio de mi mayor con parciales de campana y bucle circular sin salto, normalizado a -1 dBFS, WAV PCM de 16 bits mono.
- `CLAUDE.md`: instrucciones para Claude Code ([[Compilar y entorno]]).
