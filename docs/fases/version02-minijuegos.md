# Versión 0.2 — Minijuegos y actividades al azar

Fecha: 2026-09-29. Capturas en [`../capturas/version02/`](../capturas/version02/).

Petición de Oriol: el cálculo recién despierto pone de mal humor. Hay que tener
minijuegos sencillos, dejar el cálculo como una opción más y, con Premium, poder
marcar varias actividades para que cada vez toque una al azar. El catálogo irá
creciendo con cada versión.

## Actividades (catálogo en `TipoTarea`)

| Actividad | Qué hay que hacer | Estado (`ui/tareas/EstadosJuegos.kt`) |
|---|---|---|
| **Atrapa los soles** (por defecto) | Tocar 12 soles; cada uno se escapa si no se toca a tiempo (de 1,9 s a 1,1 s) y el siguiente sale lejos | `EstadoSoles` |
| **Repite la secuencia** | Tipo Simón: 4 botones de colores; rondas de 4 y de 5; al fallar, otra secuencia de la misma ronda | `EstadoSecuencia` |
| **Parejas** | 12 cartas, 6 parejas; giran en 3D; las que no casan se tapan a los 0,8 s | `EstadoParejas` |
| **Del 1 al 12** | Los números desordenados en una rejilla; tocarlos en orden | `EstadoOrden` |
| Foto de un objeto | Como en la 0.1 | — |
| Cálculo mental | Como en la 0.1 | `EstadoCalculo` |

Todas usan el marco claro de la alarma. Cada toque alarga el silencio y hay vibración
al acertar y al fallar. Al fallar, la pantalla tiembla como el código de desbloqueo
de iOS.

Colores de los juegos: los del sistema de iOS. Las cartas de Parejas no usan ámbar
en los símbolos: la primera captura mostró que una pareja encontrada se confundía
con el dorso, que es ámbar.

## Varias actividades por alarma (Premium)

- `Alarma.tareas: Set<TipoTarea>`, nunca vacío. Al sonar, `elegirTarea` escoge una
  al azar, que se queda para toda la sesión, y la pista de "Estoy despierto" dice
  cuál es.
- En el editor:
  - **Con Premium**, se marcan y desmarcan varias, con al menos una.
  - **Sin Premium**, la tocada sustituye a la anterior.
  - El pie explica cada caso.
- `premium/Premium.kt` es **provisional**: está activo siempre, hasta la fase 6
  (pagos). Entonces leerá la compra, guardada en el móvil.
- La foto sin cámara, o sin reconocer nada en 45 s, pasa a otra actividad
  (`alternativaA`): otra de las marcadas que no use cámara o, si no hay, el minijuego
  por defecto. El botón es "Cambiar de actividad" (antes era "Hacer el cálculo").
- Las alarmas nuevas empiezan con **Atrapa los soles**.

## Migración de la base de datos (v1 → v2)

La 0.1 ya está instalada en el Realme. La columna `tarea` (texto, "CALCULO") pasa a
llamarse `tareas` y guarda la lista separada por comas. **Un solo nombre ya es una
lista válida**, así que la migración es automática de Room (`@RenameColumn`) y no
toca los datos. `MigracionTest` crea una base de datos igual que la de la 0.1 (SQL
sacado de `app/schemas/.../1.json`), la abre con la actual y comprueba que todo se
conserva.

## Cómo añadir una actividad nueva

1. Una constante en `TipoTarea` (`datos/Alarma.kt`). Su posición en el enum es su
   posición en el editor. Si usa la cámara, `usaCamara = true`.
2. Su lógica en `ui/tareas/` (clase de estado, sin pantalla) y sus pruebas en
   `JuegosTest`.
3. Su pantalla en `ui/alarma/Juegos.kt`, dentro de `MarcoJuego`.
4. Sus funciones en `AlarmaViewModel`: silenciar en cada toque y `completar` al
   acabar. Crear su estado también en `reiniciar()`.
5. Sus textos (`tarea_x`, `tarea_x_desc`, `pista_x`) en `values/` y `values-es/`.
6. Una captura en `CapturasTest`.

Los `when` sobre `TipoTarea` (editor, pista, pantalla) no compilan hasta que la
nueva está en todos: el compilador dice dónde falta.

## Pruebas: 104

Nuevas:
- `JuegosTest` (9): la lógica de los cuatro juegos.
- `ActividadesTest` (5): elegir al azar, la alternativa y el texto de las tareas
  (compatible con la v1).
- `MigracionTest` (1).
- `EditorViewModelTest` (3): una o varias, nunca ninguna.
- 8 más en `AlarmaViewModelTest`: cada juego termina la alarma, el azar reparte
  entre todas, la alternativa de la foto y los juegos no responden si la actividad
  es otra.
- 4 capturas de los juegos.
