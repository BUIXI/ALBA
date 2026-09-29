# Alba — notas para Claude Code

App Android de alarma que te obliga a hacer una tarea (foto de un objeto, cálculo
mental...) para apagarla. Kotlin + Jetpack Compose. Qué es y qué se ha decidido:
[`docs/PRODUCTO.md`](docs/PRODUCTO.md). Qué hay hecho, fase a fase:
[`docs/INDICE.md`](docs/INDICE.md). Léelos antes de tocar nada.

## Entorno en este PC

- JDK 17 (Temurin): `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`
- SDK de Android: `%LOCALAPPDATA%\Android\Sdk`
- Android Studio: `C:\Program Files\Android\Android Studio` (2026.1)
- La herramienta del SDK es la nueva **Android CLI**:
  `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest\bin\android.exe`. Los paquetes
  se nombran con `/` (`platforms/android-36`), no con `;`. `sdkmanager.bat` está
  obsoleto y parte los nombres por el `;`: no usarlo.
- Emulador e imagen `system-images/android-35/google_apis_playstore/x86_64`
  instalados, pero **Windows Hypervisor Platform está desactivado**: sin eso el
  emulador no arranca. Activarlo es cosa de Oriol (cambio de sistema y reinicio).
- Móvil de pruebas: **Realme 14 Pro+** (Realme UI, Android 15), 1272×2800 px.

## Compilar y verificar (PowerShell)

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
# Solo en la consola de Claude: sin esto Java no puede abrir su socket interno
# ("Unable to establish loopback connection"). Android Studio no lo necesita.
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixdomain.tmpdir=$PWD\.gradle\tmp-java"

.\gradlew.bat assembleDebug testDebugUnitTest --console=plain   # compila + pruebas
.\gradlew.bat recordRoborazziDebug --console=plain              # capturas PNG
```

- Resultados de las pruebas: `app\build\test-results\testDebugUnitTest\*.xml`.
- Capturas: `app\build\outputs\roborazzi\*.png`, al tamaño del Realme y en español.
  Se dibujan en el PC con Robolectric; no hace falta emulador. Al cerrar una fase,
  se copian a `docs\capturas\faseNN\`.
- Con el móvil conectado por USB: `.\gradlew.bat installDebug` y
  `& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices`.
- APK para probar: `.\gradlew.bat assembleRelease` →
  `app\build\outputs\apk\release\app-release.apk`. Se copia a
  `entregas\Alba-<versión>.apk` (fuera de Git). Firmado con la clave de depuración
  del PC: basta para instalar encima de la anterior, no para Play.
- `lintDebug` sin errores antes de entregar (el *lint* vital de `assembleRelease` es
  más flojo: no avisa de API que no existen en Android 8).
- La salida de error de Gradle llega envuelta como `NativeCommandError` en
  PowerShell 5.1: no es un fallo. Mandarla a un archivo con `*> log` y filtrar.
- Avisos inofensivos: "SDK XML versions up to 3 but an SDK XML file of version 4"
  (el AGP es más viejo que la Android CLI) y "Unable to strip ...
  libandroidx.graphics.path.so".

## Cómo se trabaja

- Fase a fase (plan en `docs/PRODUCTO.md`). Código **comentado en español**.
- **Idiomas**: la app sale en el idioma del móvil. `values/strings.xml` es el
  inglés (el que se ve si el idioma no está traducido) y `values-es/` el español.
  Todo texto nuevo va en los dos, con la misma clave (si falta, el *lint* da error).
  Las capturas `en_*` comprueban el inglés.
- Pruebas y capturas en verde **varias veces** antes de dar una fase por cerrada.
- Lo visual se decide **mirando las capturas**, no razonando.
- Al cerrar una fase: su documento en `docs/fases/faseNN-*.md` y `docs/INDICE.md`
  al día.
- Estilo: minimalista tipo Apple. La app, siempre en oscuro; la pantalla de la
  alarma, siempre en claro. Nada de Material por defecto ni colores dinámicos.
- **Nunca** anuncios entre que suena la alarma y que se completa la tarea.
- **Actividades nuevas**: seguir la guía de
  `docs/fases/version02-minijuegos.md` ("Cómo añadir una actividad").
- **Base de datos**: la app ya está instalada en el móvil de Oriol. Todo cambio de
  esquema necesita subir la versión, una migración y su prueba (`MigracionTest`).
- Git local, rama `main`. Para mensajes de commit largos, `git commit -F archivo`.
- **Nunca editar código con `Get-Content`/`Set-Content` de PowerShell 5.1**: leen
  en Windows-1252 y rompen las tildes ("Qué" → "QuÃ©"). Usar la herramienta de
  edición. Si pasa, se deshace releyendo en UTF-8 y codificando en 1252.
