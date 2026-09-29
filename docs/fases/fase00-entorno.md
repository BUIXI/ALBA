# Fase 0 — Entorno y proyecto base

Fecha: 2026-09-29.

## Instalado

| Qué | Cómo | Dónde |
|---|---|---|
| JDK 17 (Eclipse Temurin 17.0.20) | `winget` | `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot` |
| Android Studio 2026.1 | `winget` | `C:\Program Files\Android\Android Studio` |
| Herramientas del SDK (Android CLI 23.0) | zip de dl.google.com | `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest` |
| platform-tools 37.0.1, plataforma 36, build-tools 36.1.0 | `android sdk install` | SDK |
| build-tools 36.0.0 | lo instaló solo el AGP en la primera compilación | SDK |
| Emulador + imagen Android 15 con Play (x86_64) | `android sdk install` | SDK |

Las licencias del SDK y la del JDK se aceptaron con permiso de Oriol.

## Hallazgos

- **La Android CLI (`android.exe`) sustituye a `sdkmanager`.** Los paquetes van con
  `/`. `sdkmanager.bat` redirige a ella, pero el `.bat` parte los argumentos por el
  `;` y los nombres clásicos fallan ("Package platforms not found").
- La Android CLI también crea proyectos (`android create empty-activity`), captura
  la pantalla del móvil (`android screen`), instala y ejecuta (`android run`). El
  proyecto sale de su plantilla.
- **Java en la consola de Claude** falla con "Unable to establish loopback
  connection": no puede crear su *socket* local en la carpeta temporal por
  defecto. Se arregla con `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=...`
  apuntando a una carpeta escribible (`.gradle\tmp-java`, fuera de Git).
- **Windows Hypervisor Platform está desactivado**, así que el emulador no arranca.
  Mientras tanto, las pantallas se revisan con capturas de Roborazzi y las pruebas
  reales se hacen en el Realme.

## Proyecto

Sale de la plantilla oficial `empty-activity`, que trae una combinación probada:
**AGP 9.0.1, Kotlin 2.3.20, Gradle 9.1.0, Compose BOM 2026.03.01, Navigation 3
1.0.1**. Hay versiones más nuevas (AGP 9.4, Kotlin 2.4, BOM 2026.09); no se han
subido para no mezclar incompatibilidades desde el primer día.

- `applicationId` y paquete: `com.oriol.alba`. **Se puede cambiar hasta la
  primera publicación**; después ya no.
- `minSdk 26` (Android 8), `targetSdk`/`compileSdk 36`.
- AGP 9 trae Kotlin integrado: no se aplica el plugin `kotlin-android`.

Cambios sobre la plantilla:

- Fuera la demo ("Hello Android", repositorio y ViewModel de ejemplo).
- Tema **solo oscuro**, sin colores dinámicos (Material You): negro puro, texto
  blanco, gris secundario `#8E8E93` y un único acento ámbar `#FF9F0A` (amanecer).
- La ventana es negra desde el primer fotograma (`windowBackground`) para que no
  haya destello blanco al abrir. Barras del sistema transparentes con iconos claros.
- Pantalla principal provisional: título grande "Alarmas" y "Sin alarmas".
- Textos en `strings.xml`. De momento en español en `values/`; cuando se traduzca,
  el inglés pasará a `values/` y el español a `values-es/`.
- **Capturas en el PC con Roborazzi + Robolectric**, al tamaño del Realme (424×933
  dp, xxhdpi). La primera captura sacó un fallo real: la pantalla no pintaba su
  fondo y en la prueba salía gris claro. Ahora cada pantalla pinta el suyo.

## Verificación

- `assembleDebug testDebugUnitTest`: BUILD SUCCESSFUL; 2 pruebas, 0 fallos.
- `recordRoborazziDebug`: `principal_vacia.png` a 1272×2799, fondo negro.

## Pendiente para Oriol

1. Activar **Windows Hypervisor Platform** para el emulador (ver `INDICE.md`).
2. Activar las **opciones de desarrollador y la depuración USB** en el Realme para
   instalar la app desde el PC.
