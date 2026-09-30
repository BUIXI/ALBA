---
tags: [mapa, modulo]
carpeta: app/src/main/java/com/oriol/alba/alarma/
---
# alarma/: lo que hace sonar la alarma

Volver al [[00 Indice]]. Recorrido paso a paso en [[Flujo de una alarma]].

| Archivo | Símbolos clave | Para qué |
|---|---|---|
| `CentralAlarma.kt` | `object CentralAlarma { sesion; publicar() }` | La alarma que suena ahora (`EstadoSesion?`). La publica el servicio y la miran las pantallas |
| `Extras.kt` | `enum Modo { ALARMA, COMPROBACION }`, `Intent.ponerAlarma/leerAlarma`, `ponerModo/leerModo`, `ponerPrueba/esPrueba` | La alarma entera viaja en los extras (claves `alba.*`). **Si añades un campo a `Alarma`, añádelo aquí** |
| `Notificaciones.kt` | `crearCanales`, `alarma(ctx, alarma, modo, conAlarma)`, `alarmaSinServicio`, `publicar` · `CANAL_ALARMAS="alarmas"`, `ID_ALARMA=1` | Canal de importancia alta sin sonido (el sonido lo pone el servicio). Notificación fija, `CATEGORY_ALARM` y a pantalla completa. `publicar` comprueba el permiso |
| `Permisos.kt` | `EstadoPermisos` (`imprescindibles`), `Permisos.estado()`, `ajustesNotificaciones/PantallaCompleta/AlarmasExactas/Bateria/App()`, `fabricanteAgresivo` | Consultar permisos e ir al ajuste exacto. Lista de marcas agresivas (realme, oppo, xiaomi, samsung...) |
| `Programador.kt` | `interface Programador` (`programar`, `cancelar`, `programarComprobacion`, `cancelarComprobacion`), `ProgramadorSistema` | `setAlarmClock`. requestCode = id, data `alba://alarma/ID`. Comprobación: `Int.MAX_VALUE - 1`, `alba://comprobacion` |
| `Receptores.kt` | `ReceptorAlarma` (`ACCION_DISPARAR`), `ReceptorSistema` | Disparo → servicio. Eventos del sistema → `reprogramarTodas()` (`goAsync`, límite de 8 s) |
| `Reproductor.kt` | `class Reproductor : SalidaSonido` (`volumen`, `vibrar`, `detener`) | `USAGE_ALARM`, foco de audio (pausa la música), volumen del sistema al 80 % como mínimo, volumen al cuadrado, vibración 600/900 ms, *wake lock*. Si falla, usa `amanecer.wav` |
| `ServicioAlarma.kt` | `ACCION_SONAR/SILENCIAR/TERMINAR`, `MinutosComprobacion = 10L`, `intentSonar`, `probar`, `silenciar`, `terminar`, `sonar()`, `alTerminar()` | Servicio en primer plano (mediaPlayback), una sola sesión a la vez, `START_NOT_STICKY` |
| `SesionAlarma.kt` | `interface SalidaSonido`, `data class EstadoSesion`, `class SesionAlarma` (`iniciar`, `silenciar`, `terminar`, `paso`), `SesionAlarma.Ajustes` | **Lógica pura** de una alarma sonando (se prueba con tiempo simulado) |

## `EstadoSesion`

`alarma`, `modo`, `prueba`, `inicio`, `inicioModo`, `silenciadaHasta` (null = sonando), `terminada`, `completada`. Tiempos en `SystemClock.elapsedRealtime()`.

## `SesionAlarma.Ajustes` (valores por defecto)

| Campo | Valor | Significado |
|---|---|---|
| `rampa` | 45 000 ms | Subida del volumen al empezar |
| `rampaTrasSilencio` | 4 000 ms | Subida al volver a sonar |
| `volumenInicial` | 0,15 | |
| `silencio` | 30 000 ms | Tras "Estoy despierto"; cada toque lo alarga |
| `esperaComprobacion` | 60 000 ms | Si no se responde a "¿Sigues despierto?", alarma entera |
| `volumenComprobacion` | 0,35 | |
| `duracionMaxima` | 30 min | Se para sola |
| `paso` | 100 ms | Cada cuánto se recalcula |

Al bajar el volumen, baja 0,2 por paso (sin cortes secos).

## Permisos

| Permiso | Uso |
|---|---|
| `POST_NOTIFICATIONS` | Se pide al crear la primera alarma (`PantallaLista`) |
| `USE_EXACT_ALARM` | Android 13 en adelante (apps de alarma) |
| `SCHEDULE_EXACT_ALARM` | Solo hasta Android 12 (maxSdk 32) |
| `USE_FULL_SCREEN_INTENT` | Pantalla completa sobre el bloqueo |
| `FOREGROUND_SERVICE` + `_MEDIA_PLAYBACK` | El servicio que suena |
| `WAKE_LOCK`, `VIBRATE`, `RECEIVE_BOOT_COMPLETED` | |
| `CAMERA` | Se pide al elegir la foto en el editor |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Los añade ML Kit por su cuenta (estadísticas). Tenerlo en cuenta para la política de privacidad |

Imprescindibles (`EstadoPermisos.imprescindibles`): notificaciones, pantalla completa, alarmas exactas y segundo plano no restringido. Recomendado: sin optimización de batería. Pantalla: [[ui - pantallas de la app#Permisos]].

## Compatibilidad con Android 8.0/8.1 (minSdk 26)

`setShowWhenLocked`/`setTurnScreenOn` (API 27+) e `isBackgroundRestricted` (API 28+) van protegidos con `Build.VERSION.SDK_INT`. Cualquier API nueva, igual: `lintDebug` lo avisa y el lint de `assembleRelease` no.
