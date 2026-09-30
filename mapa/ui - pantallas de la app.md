---
tags: [mapa, modulo]
carpeta: app/src/main/java/com/oriol/alba/ (raíz) + ui/lista, ui/editor, ui/permisos
---
# Pantallas de la app (modo oscuro)

Volver al [[00 Indice]]. La pantalla de la alarma está en [[ui - pantalla de alarma]].

## Arranque y navegación (raíz del paquete)

| Archivo | Símbolos | Qué hace |
|---|---|---|
| `AlbaApp.kt` | `AlbaApp` (`contenedor`), `Contenedor` (`programador`, `repositorio`) | Crea los canales de notificación. Arranca también antes del primer desbloqueo |
| `MainActivity.kt` | `MainActivity.onCreate` | Borde a borde con iconos claros, `reprogramarTodas()` en IO. Si `CentralAlarma.sesion != null`, abre `ActividadAlarma`. `TemaAlba { NavegacionAlba(repositorio) }` |
| `NavigationKeys.kt` | `Lista`, `Editor(alarmaId = 0L)`, `ClavePermisos` | Claves `@Serializable NavKey`. `alarmaId` 0 = nueva |
| `Navigation.kt` | `NavegacionAlba(repositorio)`, `TransicionModal`, `CurvaHoja` | Navigation 3 (`NavDisplay`). El editor y los permisos suben como hoja de iOS (420 ms al entrar, 320 ms al salir) |

## Lista (`ui/lista/`)

- `ListaViewModel(repositorio)`:
  - `alarmas: StateFlow<List<Alarma>?>`: `null` mientras carga, así no parpadea "Sin alarmas".
  - `cambiarActiva(id, activa)`.
- `PantallaLista(vm, onNueva, onAbrir, onPermisos)`: pide `POST_NOTIFICATIONS` en cuanto hay alarmas. Usa `rememberEstadoPermisos()`.
- `ListaAlarmas(...)`: la versión sin estado (capturas). Barra con "+" a la derecha.
- Privadas:
  - `FilaAlarma`: hora grande, etiqueta · días e interruptor. Apagada, se atenúa entera.
  - `AvisoPermisos`: aviso ámbar si falta algo imprescindible.
  - `EstadoVacio`.
  - `textoProxima`: "Próxima alarma en 8 h 12 min".

## Editor (`ui/editor/`)

- `EditorViewModel(repositorio, alarmaId)` trabaja sobre un **borrador** (`borrador: StateFlow<Alarma?>`, `null` mientras carga). Nada se guarda hasta "Guardar".
  - `cambiarHora`, `cambiarDias`, `cambiarEtiqueta` (máx. 40), `cambiarSonido`, `cambiarComprobar`.
  - `alternarTarea(tarea, varias)`: con `varias` (Premium) marca o desmarca, sin quedarse nunca vacío; sin él, sustituye.
  - `guardar(alTerminar)`: la guarda **activa**. `eliminar(alTerminar)`. Ambas con `terminando` para no hacerlo dos veces.
  - `nuevaAlarma()` = 7:00 con `tareas` por defecto (SOLES).
- `PantallaEditor(vm, onCerrar, onProbar)`: lee `Premium.activo`; pide `CAMERA` al elegir FOTO; `onProbar` → `ServicioAlarma.probar`.
- `EditorAlarma(...)`: sin estado. `BarraModal` (Cancelar · título · Guardar), `RuedaHora`, `SelectorDias`, `CampoEtiqueta`, lista de actividades (`FilaTarea`), sonido, "Después de apagarla" (comprobar), "Probar alarma" y "Eliminar alarma".
- `FilaOpcion(..., disponible)`: las no disponibles dicen "Pronto".

## Permisos

`ui/permisos/PantallaPermisos.kt`:

- `rememberEstadoPermisos()`: se relee al volver a primer plano.
- `PantallaPermisos(onVolver)`: "Para que suene siempre".
- `ListaPermisos(...)`: sin estado.
- `FilaPermiso`: marca o "Activar", que lleva al ajuste exacto (`Permisos.ajustesX`).
- Con una marca agresiva (`Permisos.fabricanteAgresivo`), añade instrucciones: actividad en segundo plano e inicio automático.

Lógica: [[alarma - sistema#Permisos]].
