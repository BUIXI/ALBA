# Alba — índice

Punto de entrada. Producto y decisiones: [`PRODUCTO.md`](PRODUCTO.md). Cómo
probarla en el móvil: [`PRUEBAS_EN_EL_MOVIL.md`](PRUEBAS_EN_EL_MOVIL.md).

## Estado

Versión **0.2**: minijuegos y varias actividades por alarma
([`fases/version02-minijuegos.md`](fases/version02-minijuegos.md), con la guía para
añadir actividades). Pendiente de probar en el Realme.

**Idiomas** (0.1.1): inglés por defecto (`values/`) y español (`values-es/`); la app
elige sola según el idioma del móvil. Los días, el primer día de la semana y las
fechas ya salen del idioma. Pendiente: formato de 12 h (AM/PM) para quien lo use,
selector de idioma por app (Android 13+) y más idiomas.

| Fase | Estado | Documento |
|---|---|---|
| 0 — Entorno y proyecto base | Cerrada | [`fases/fase00-entorno.md`](fases/fase00-entorno.md) |
| 1 — Sistema de diseño y lista de alarmas | Cerrada | [`fases/fase01-diseno-y-lista.md`](fases/fase01-diseno-y-lista.md) |
| 2 — Alarma fiable | Hecha, sin probar en el móvil | [`fases/fase02a05-version01.md`](fases/fase02a05-version01.md) |
| 3 — Cálculo mental y tareas intercambiables | Hecha | ídem |
| 4 — Foto con ML Kit | Hecha por categorías; falta la foto ancla | ídem |
| 5 — Comprobación, reintento y pulido | Hecha | ídem |
| 6 — Premium, IA con *backend* y anuncios | Pendiente | |

## Mapa del código (`app/src/main/java/com/oriol/alba/`)

| Carpeta | Qué hay |
|---|---|
| `theme/` | Paletas (oscura y clara), tipografía (Inter) y `TemaAlba` |
| `ui/componentes/` | Sistema de diseño: rueda, interruptor, grupos, botones, teclado, pulsación |
| `ui/lista/`, `ui/editor/`, `ui/permisos/` | Pantallas de la app con su ViewModel. Cada una tiene una versión sin estado para las capturas |
| `ui/alarma/` | La pantalla de la alarma (clara): sonando, tarea, comprobación, hecho |
| `ui/tareas/` | Estado del cálculo y cámara con el reconocedor de ML Kit |
| `alarma/` | Programador (`setAlarmClock`), receptores, servicio en primer plano, sesión, reproductor, notificaciones, permisos |
| `datos/` | Room: `Alarma`, DAO, base de datos (almacenamiento protegido), `RepositorioAlarmas` |
| `dominio/` | Lógica pura: cuándo suena, días, cálculo, foto |
| `AlbaApp.kt`, `Navigation*.kt` | Contenedor de dependencias y navegación (Navigation 3) |

`tools/GenerarSonido.java` genera `res/raw/amanecer.wav`.

## Anuncios para redes (`docs/anuncios/`)

| Anuncio | Qué es |
|---|---|
| [`anuncio02-la-taza.md`](anuncios/anuncio02-la-taza.md) | Guion para grabar con un actor: la taza y la cocina |
| [`anuncio03-a-la-primera.md`](anuncios/anuncio03-a-la-primera.md) | Vídeo hecho en la nube (versión 2): gancho, alarma del móvil, capturas reales de la app, voz de Chatterbox y cortes al pulso de la música. Se rehace con `tools/anuncios/` |

## Pruebas

71 pruebas locales, todas en el PC: `gradlew testDebugUnitTest`. Detalle en los
documentos de fase. Capturas: `gradlew recordRoborazziDebug`, copiadas a
`docs/capturas/`.

## Preparar el PC y el móvil (lo hace Oriol)

**Emulador.** Necesita Windows Hypervisor Platform. En PowerShell como
administrador, y luego reiniciar:

```powershell
Enable-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform
```

Después, en Android Studio: Device Manager → crear un dispositivo con la imagen de
Android 15 (API 35, Google Play), que ya está descargada.

**Realme 14 Pro+.** Ajustes → Acerca del dispositivo → Versión → tocar 7 veces
"Número de compilación" (puede pedir el PIN). Después, Ajustes → Ajustes
adicionales → Opciones de desarrollador → activar **Depuración USB**. Al
conectarlo por USB, aceptar la huella del PC.
