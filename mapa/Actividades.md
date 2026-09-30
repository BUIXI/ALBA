---
tags: [mapa, modulo]
aliases: [Tareas, Minijuegos]
---
# Actividades (tareas para apagar la alarma)

Volver al [[00 Indice]]. En el código se llaman **tareas** (`TipoTarea`) y en la interfaz, desde la 0.2, **actividades**. Es lo mismo.

## Catálogo

| `TipoTarea` | Nombre | Lógica (estado) | Pantalla | Para completarla |
|---|---|---|---|---|
| `SOLES` (**por defecto**) | Atrapa los soles | `ui/tareas/EstadosJuegos.kt` → `EstadoSoles(total = 12)` | `Juegos.kt` → `AlarmaSoles` | Tocar 12 soles. Cada uno dura `1900 - 70·aciertos` ms (mín. 1100) y el siguiente sale a ≥0,35 de distancia |
| `SECUENCIA` | Repite la secuencia | `EstadoSecuencia(longitudes = [4, 5])`, 4 botones | `AlarmaSecuencia` | 2 rondas (4 y 5). Al fallar, otra secuencia de la misma ronda |
| `PAREJAS` | Parejas | `EstadoParejas(pares = 6)`, 12 cartas | `AlarmaParejas` | Todas las parejas. Las que no casan se tapan (`ocultar`) |
| `ORDEN` | Del 1 al 12 | `EstadoOrden(total = 12)` | `AlarmaOrden` | Tocar del 1 al 12 en orden |
| `FOTO` (`usaCamara`) | Foto de un objeto | `dominio/Foto.kt` + `ui/tareas/CamaraReconocedora.kt` | `PantallaAlarma.kt` → `TareaFoto` / `AlarmaFoto` | Ver el objeto en 3 fotogramas seguidos con confianza ≥0,55. 2 cambios de objeto. A los 45 s, "Cambiar de actividad" |
| `CALCULO` | Cálculo mental | `ui/tareas/EstadoCalculo.kt` + `dominio/Calculo.kt` | `AlarmaCalculo` + `componentes/Teclado.kt` | 3 aciertos. Se comprueba sola al escribir tantas cifras como tiene el resultado |

Textos de cada una: `tarea_x`, `tarea_x_desc` y `pista_x` en `strings.xml`.

## Cómo funcionan todas

- Al sonar, `elegirTarea(alarma.tareas)` escoge una al azar, que se queda toda la sesión (`AlarmaViewModel.tareaActual`).
- Cada toque → `acciones.silenciar()` (alarga el silencio). Al terminar → `completar()`.
- Los métodos del estado devuelven `Boolean?`: `null` = no cuenta, `true` = acierto y `false` = fallo. Al fallar, `rememberSacudida` hace temblar la pantalla y vibra.
- Todo el estado se crea de nuevo en `AlarmaViewModel.reiniciar()`.
- Salida de seguridad: `alternativaA(actual, tareas)` (en `dominio/Actividades.kt`, ver [[dominio]]).

## Cámara (`ui/tareas/CamaraReconocedora.kt`)

- `CamaraReconocedora(onEtiquetas, onError, modifier)`:
  - CameraX (`Preview` + `ImageAnalysis` `KEEP_ONLY_LATEST`) con la cámara trasera.
  - ML Kit `ImageLabeling` con un umbral de 0,4.
- `Analizador`: analiza un fotograma cada 250 ms. Nunca lanza excepciones (cierra la imagen).

## Premium

`premium/Premium.kt` → `Premium.activo: StateFlow<Boolean>`. **Provisional: siempre `true`** hasta la fase 6 (RevenueCat). Después leerá la compra guardada en el móvil, sin depender de internet. `fijarParaPruebas(activo)`.

- Con Premium, varias actividades por alarma (una al azar cada vez).
- Sin Premium, la que se toca sustituye a la anterior (`EditorViewModel.alternarTarea`).

## Pendientes o ideas

Foto ancla (tus objetos, comparados por *embeddings*), voz, pasos, sacudir y QR. Ver [[Pendiente y deudas]].

Para añadir una: [[Recetas#Añadir una actividad]].
