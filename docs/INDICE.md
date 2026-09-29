# Alba — índice

Punto de entrada. Producto y decisiones: [`PRODUCTO.md`](PRODUCTO.md).

## Estado

| Fase | Estado | Documento |
|---|---|---|
| 0 — Entorno y proyecto base | Cerrada | [`fases/fase00-entorno.md`](fases/fase00-entorno.md) |
| 1 — Sistema de diseño y lista de alarmas | Pendiente | |
| 2 — Alarma fiable | Pendiente | |
| 3 — Cálculo mental y tareas intercambiables | Pendiente | |
| 4 — Foto ancla con ML Kit | Pendiente | |
| 5 — Segunda comprobación, reintento y pulido | Pendiente | |
| 6 — Premium, IA con *backend* y anuncios | Pendiente | |

## Pruebas

| Prueba | Qué comprueba |
|---|---|
| `ui/main/MainScreenTest` (local) | La pantalla principal muestra el título y el estado vacío |
| `CapturasTest` (local) | Captura `principal_vacia.png` |
| `ui/main/MainScreenTest` (instrumentada) | Lo mismo en el móvil |

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
