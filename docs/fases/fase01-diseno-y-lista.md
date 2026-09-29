# Fase 1 — Sistema de diseño y lista de alarmas

Fecha: 2026-09-29. Capturas en [`../capturas/fase01/`](../capturas/fase01/).

## Qué hay

La app ya se usa de principio a fin, salvo que las alarmas todavía **no suenan**
(eso es la fase 2):

- **Lista de alarmas**: título grande, "Próxima alarma en 8 h 12 min", filas con la
  hora grande y fina, nombre y días debajo, e interruptor. Apagada, la fila se
  atenúa entera, como en iOS. Tocar la fila abre el editor.
- **Estado vacío**: "Sin alarmas", una frase y el botón "Crear alarma".
- **Editor** (hoja que sube desde abajo): rueda de hora, días de la semana en
  círculos (L M X J V S D), etiqueta, tarea para apagarla y "Eliminar alarma" si se
  está editando. Nada se guarda hasta pulsar "Guardar"; guardar también la activa.
- **Icono** adaptativo: medio sol ámbar sobre el mar, en negro. Tiene capa
  monocroma para los iconos temáticos de Android 13+.

## Sistema de diseño (`theme/` y `ui/componentes/`)

| Pieza | Decisión |
|---|---|
| `Paleta` | Dos: `PaletaOscura` (app) y `PaletaClara` (pantalla de alarma, fase 2). Grises de iOS. Acento ámbar `#FF9F0A`; el texto sobre ámbar va en negro (el blanco contrasta 2:1). |
| `Tipos` | **Inter** variable (OFL, 877 KB, licencia en `assets/licencias/`). Escala de iOS: 34/22/17/15/13. Con el eje de tamaño óptico: `opsz` 32 para ≥ 20 sp y 14 para el resto, como SF Pro Display/Text. Espaciado según la fórmula de Inter (`trackingInter`). Cifras tabulares en las horas. |
| `TemaAlba` / `Alba` | `Alba.colores.x`, `Alba.tipos.x`. Material 3 solo por piezas sueltas, con su esquema relleno con la paleta. Sin colores dinámicos. |
| Pulsación | Sin la "onda" de Android: `IndicacionOpacidad` (atenúa, es la global) e `IndicacionResaltado` (rellena la fila). Son `IndicationNodeFactory`. |
| `Interruptor` | 51×31 dp, bola con rebote, vibración al cambiar. |
| `Rueda` / `RuedaHora` | Lista "sin fin" (400 vueltas, empieza en el medio), se ajusta al centro, efecto tambor (giro, escala y opacidad según la distancia) y un toque háptico por valor. |
| `SelectorDias` | Letras y primer día de la semana según el idioma del móvil. |
| Otros | `Pantalla`, `Grupo`/`FilaGrupo`/`SeparadorFila` (listas agrupadas), `BotonPrincipal`, `BotonTexto`, `BotonIcono`, `EncabezadoGrupo`, `PieGrupo`. |

## Datos y lógica

- **Room** (`datos/`): tabla `alarmas` con hora, minuto, días (máscara de bits
  lunes = bit 0), activa, etiqueta y tarea (por nombre del enum). Esquema v1 en
  `app/schemas/` (va a Git, para las migraciones).
- `RepositorioAlarmas`: la única puerta a los datos. En la fase 2 programará el
  sistema en cada cambio.
- `dominio/Programacion.kt`: `siguienteDisparo` (el próximo momento en que sonará,
  estrictamente después de ahora), `proximaAlarma`, `minutosHasta` (redondeo hacia
  arriba). La fase 2 los usa para programar.
- `dominio/Dias.kt`: orden de la semana según el idioma y resumen ("Entre semana",
  "Fin de semana", "Todos los días", "Una vez" o los días sueltos).
- Sin Hilt: `AlbaApp.contenedor` crea la base de datos y el repositorio, y se pasan a
  mano. La navegación (Navigation 3) crea los ViewModel de cada pantalla.
- La tarea "Foto de un objeto" aparece apagada con "Pronto" (`TipoTarea.disponible`).

## Pruebas

27 pruebas locales (en el PC, con Robolectric donde hace falta Android):

| Prueba | Qué comprueba |
|---|---|
| `dominio/ProgramacionTest` (11) | Una vez hoy/mañana, misma hora que ahora, segundos, laborables del viernes al lunes, solo hoy, desactivada, próxima entre varias, redondeo de minutos |
| `dominio/DiasTest` (5) | Semana española y estadounidense, resúmenes, orden de días sueltos, letras L M X J V S D, máscara de bits ida y vuelta (las 128 combinaciones) |
| `datos/RepositorioAlarmasTest` (4) | Guardar nueva y existente, orden por hora, activar, eliminar (Room en memoria) |
| `FlujoTest` (2) | La app entera: crear con etiqueta, verla activa, apagarla con el interruptor, abrirla y eliminarla; cancelar no guarda |
| `CapturasTest` (5) | Lista vacía, lista con alarmas, editor nuevo, editor en edición e icono |

`FlujoTest` vuelca el árbol de la pantalla cuando falla una espera: así se vio que en
la pantalla por defecto de Robolectric (320×470 px) "Eliminar alarma" quedaba fuera y
hacía falta desplazarse (`performScrollTo`).

## Mirando las capturas

- Rueda: los números vecinos brillaban casi como el elegido. Opacidad por fila de
  0,32 → 0,42 de caída.
- Icono: el sol era pequeño y con un horizonte corto parecía un sombrero. Más grande,
  horizonte ancho y dos reflejos.

## Lint

Sin errores. Corregidos: carpeta `mipmap-anydpi-v26` → `mipmap-anydpi` (el mínimo ya
es 26) y `offset` con lambda en el interruptor. Quedan avisos de **versiones más
nuevas** (AGP 9.4.1, Gradle 9.8, Kotlin 2.4.20, BOM 2026.09, Navigation 3 1.2,
Room 2.8.5...): se actualizarán en bloque, aparte, compilando entre medias.

## Pendiente

- Probar en el Realme (hace falta la depuración USB): tacto de la rueda y del
  interruptor, vibraciones, transición de la hoja y gesto atrás.
- Formato de 12 h para quien lo tenga en el móvil: ahora siempre es 24 h.
- Deslizar para borrar en la lista (ahora se borra desde el editor).
