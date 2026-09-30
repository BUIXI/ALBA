---
tags: [mapa]
---
# Compilar y entorno

Volver al [[00 Indice]]. El detalle completo está en `CLAUDE.md` (raíz del repo). Aquí va lo mínimo.

## En este PC (PowerShell)

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixdomain.tmpdir=$PWD\.gradle\tmp-java"   # solo en la consola de Claude
.\gradlew.bat assembleDebug testDebugUnitTest --console=plain
.\gradlew.bat recordRoborazziDebug --console=plain
.\gradlew.bat lintDebug --console=plain          # sin errores antes de entregar
.\gradlew.bat assembleRelease --console=plain    # APK → app\build\outputs\apk\release\app-release.apk
.\gradlew.bat installDebug                        # con el Realme por USB
```

- APK de entrega: se copia a `entregas\Alba-<versión>.apk` (fuera de Git). Está firmado con la clave de depuración **de este PC**, que vale para instalar encima de la anterior pero no para Play.
- Versión: `versionCode` y `versionName` en `app/build.gradle.kts` (ahora 3 / "0.2").
- Dependencias: `gradle/libs.versions.toml`.
- El error de Gradle sale como `NativeCommandError` en PowerShell 5.1, pero no es un fallo: redirige con `*> log` y filtra.
- Avisos inofensivos: "SDK XML versions up to 3..." y "Unable to strip ... libandroidx.graphics.path.so".
- SDK: Android CLI `android.exe` (paquetes con `/`, **no** usar `sdkmanager.bat`).
- El emulador **no arranca** (Windows Hypervisor Platform desactivado).

## En la nube (Claude Code web, Linux)

Hay que instalar JDK 17 y el SDK con la Android CLI; los comandos están en `CLAUDE.md`. Los APK hechos en la nube **no** se instalan encima de los del PC (otra clave).

## Compilación

- R8 **desactivado** (`isMinifyEnabled = false`) hasta probar en el móvil. El APK pesa unos 30 MB.
- ABIs `arm64-v8a` y `armeabi-v7a` (+ `x86_64` en debug).
- JVM 17. Compose activado. KSP para Room. Esquemas en `app/schemas`.
