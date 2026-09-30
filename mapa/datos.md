---
tags: [mapa, modulo]
carpeta: app/src/main/java/com/oriol/alba/datos/
---
# datos/: Room y repositorio

Volver al [[00 Indice]].

## `Alarma.kt`: la entidad (tabla `alarmas`)

| Campo | Tipo / por defecto | Notas |
|---|---|---|
| `id` | `Long`, autoGenerate | 0 = aún no guardada |
| `hora`, `minuto` | `Int` | 24 h |
| `dias` | `Set<DayOfWeek>` = vacío | Vacío = suena una vez. Se guarda como máscara (lunes = bit 0) |
| `activa` | `true` | |
| `etiqueta` | `""` | Máx. 40 caracteres (`EditorViewModel.LargoMaximoEtiqueta`) |
| `tareas` | `Set<TipoTarea>` = `{SOLES}` | **Nunca vacío.** Se guarda como `"SOLES,PAREJAS"` |
| `sonido` | `Sonido.AMANECER` | `AMANECER` (propio) o `SISTEMA` |
| `comprobar` | `true` | "¿Sigues despierto?" a los 10 min |

- `val Alarma.repetida` (extensión) = `dias.isNotEmpty()`.
- `enum TipoTarea(disponible, usaCamara)`: `SOLES, SECUENCIA, PAREJAS, ORDEN, FOTO(usaCamara), CALCULO`. `PorDefecto = SOLES`. **El orden es el del editor. Se guarda por nombre: no renombrar sin migración.** Más en [[Actividades]].
- `enum Sonido { AMANECER, SISTEMA }`, también por nombre.

## `BaseDatos.kt`

- `AlarmaDao`:
  - `todas(): Flow` (orden: hora, minuto, id).
  - `todasAhora()`.
  - `obtener(id)`.
  - `guardar` (`@Upsert`: devuelve el id al insertar y -1 al actualizar).
  - `cambiarActiva(id, activa)`.
  - `eliminar(id)`.
- `Convertidores`: `diasAMascara`/`mascaraADias` y `tareasATexto`/`textoATareas`. Los nombres desconocidos se ignoran; si queda vacío, `PorDefecto`.
- `BaseDatos` **versión 2**:
  - `AutoMigration(1→2)` con `De1a2` (`@RenameColumn tarea → tareas`).
  - `abrir(ctx)` usa `createDeviceProtectedStorageContext()` y el archivo `alba.db` (*direct boot*).
  - `enMemoria(ctx)` es para las pruebas.

| Versión BD | App | Cambio |
|---|---|---|
| 1 | 0.1 | Columna `tarea` (una sola) |
| 2 | 0.2 | Renombrada a `tareas` (lista con comas). Un solo nombre ya es una lista válida, así que no se tocan los datos |

Esquemas en `app/schemas/com.oriol.alba.datos.BaseDatos/{1,2}.json` (van a Git).

## `RepositorioAlarmas.kt`: la única puerta a los datos

Las pantallas **no** van al DAO directamente: cada cambio programa o anula la alarma en el sistema.

| Función | Qué hace |
|---|---|
| `alarmas: Flow<List<Alarma>>` | Todas, ordenadas |
| `obtener(id)` | |
| `guardar(alarma): Long` | Guarda, programa y devuelve el id |
| `cambiarActiva(id, activa)` | Actualiza y programa (o anula) |
| `eliminar(id)` | Borra y `programador.cancelar(id)` |
| `reprogramarTodas()` | Tras reiniciar, cambiar la hora, actualizar o abrir la app |
| `despuesDeSonar(id)` | Si se repite, programa la siguiente desde ahora + 1 min; si era de una vez, la desactiva |
| `programar()` (privado) | `siguienteDisparo` → `programador.programar` o `cancelar` |

El constructor recibe `dao`, `programador` y `reloj` (inyectable para las pruebas).

## Si añades un campo a `Alarma`

Ver [[Recetas#Añadir un campo a la alarma]]: entidad + versión 3 + migración + `MigracionTest` + `Extras.kt` + editor + textos.
