# Alba — índice

Punto de entrada. Producto y decisiones: [`PRODUCTO.md`](PRODUCTO.md).

## Estado

| Fase | Estado | Documento |
|---|---|---|
| 0 — Entorno y proyecto base | Cerrada | [`fases/fase00-entorno.md`](fases/fase00-entorno.md) |
| 1 — Sistema de diseño y lista de alarmas | Cerrada (falta probarla en el Realme) | [`fases/fase01-diseno-y-lista.md`](fases/fase01-diseno-y-lista.md) |
| 2 — Alarma fiable | Pendiente | |
| 3 — Cálculo mental y tareas intercambiables | Pendiente | |
| 4 — Foto ancla con ML Kit | Pendiente | |
| 5 — Segunda comprobación, reintento y pulido | Pendiente | |
| 6 — Premium, IA con *backend* y anuncios | Pendiente | |

## Mapa del código (`app/src/main/java/com/oriol/alba/`)

| Carpeta | Qué hay |
|---|---|
| `theme/` | Paletas, tipografía (Inter) y `TemaAlba` |
| `ui/componentes/` | Piezas del sistema de diseño: rueda, interruptor, grupos, botones, pulsación |
| `ui/lista/`, `ui/editor/` | Pantallas con su ViewModel. Cada una tiene una versión sin estado para las capturas |
| `datos/` | Room: `Alarma`, DAO, base de datos, `RepositorioAlarmas` |
| `dominio/` | Lógica pura: cuándo suena cada alarma, días de la semana |
| `AlbaApp.kt`, `Navigation*.kt` | Contenedor de dependencias y navegación (Navigation 3) |

## Pruebas

27 pruebas locales, todas en el PC: `gradlew testDebugUnitTest`. Detalle en el
documento de la fase 1. Capturas: `gradlew recordRoborazziDebug`, copiadas a
`docs/capturas/faseNN/` al cerrar cada fase.

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
