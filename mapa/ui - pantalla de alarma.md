---
tags: [mapa, modulo]
carpeta: app/src/main/java/com/oriol/alba/ui/alarma/
---
# Pantalla de la alarma (siempre en claro)

Volver al [[00 Indice]]. Cómo se llega aquí: [[Flujo de una alarma]]. La lógica de cada juego está en [[Actividades]].

## `ActividadAlarma.kt`

- Sale sobre el bloqueo y enciende la pantalla: `setShowWhenLocked`/`setTurnScreenOn` en API 27+ y banderas de ventana en la 26. Además, `FLAG_KEEP_SCREEN_ON`.
- Barras del sistema claras. `TemaAlba(PaletaClara) { PantallaAlarma(vm, onCerrar) }`.
- "Atrás" solo funciona en `Fase.Hecho`.
- **Teclas de volumen** (`onKeyDown`) → `vm.despierto()`, y no cambian el volumen.
- Recoge `CentralAlarma.sesion` → `vm.alCambiarSesion`. Si la sesión acaba sin la tarea hecha, se cierra.
- `arrancarSiHaceFalta(intent)`: plan B, arranca el servicio si la notificación traía la alarma. Se llama en `onCreate`, `onNewIntent` y `onResume`.
- `cerrar()` = `finishAndRemoveTask()`. `ActividadAlarma.intent(ctx)`.

## `AlarmaViewModel.kt`

- `interface AccionesAlarma { silenciar(); terminar() }`. En el móvil es `AccionesServicio` (→ `ServicioAlarma`).
- `sealed interface Fase`: `Sonando`, `Tarea` o `Hecho(hora, fueComprobacion, comprobaraDespues, proxima)`.
- Estado:
  - `fase`, `tareaActual`, `sesion`, `vioSesion`.
  - `calculo`, `soles`, `secuencia`, `parejas`, `orden`.
  - Foto: `objeto`, `cambiosRestantes`, `vistosSeguidos`.

| Función | Cuándo |
|---|---|
| `alCambiarSesion(nueva)` | Una sesión nueva (otro `inicio`) → `reiniciar()` + `elegirTarea` |
| `despierto()` | "Estoy despierto" o tecla de volumen → silenciar + `Fase.Tarea` |
| `tecla(Tecla)` | Cálculo |
| `atraparSol` / `escaparSol` | Soles |
| `pulsarSecuencia(i)` / `secuenciaMostrada()` | Secuencia |
| `tocarCarta(i)` / `ocultarCartas()` | Parejas |
| `tocarNumero(n)` | Orden |
| `etiquetas(lista)` / `cambiarObjeto()` / `seguirBuscando()` | Foto |
| `pasarAAlternativa()` | Sin cámara, o sin reconocer nada |
| `responderComprobacion()` | "Sí, estoy despierto" |
| `completar(fueComprobacion)` (privada) | `acciones.terminar()` → `Fase.Hecho`, y después lee la próxima alarma |

- Los juegos solo responden si `haciendo(tarea)`. Cada toque llama a `acciones.silenciar()`.
- `AlarmaViewModel.fabrica(app)`.

## `PantallaAlarma.kt`

`PantallaAlarma(vm, onCerrar)` elige qué dibujar:

| Condición | Composable |
|---|---|
| `fase is Hecho` | `AlarmaHecha` ("Buenos días", hora de levantarse y próxima alarma) |
| `sesion == null` | `FondoAlarma {}` vacío |
| `modo == COMPROBACION` | `AlarmaComprobacion(segundos, onSi)` con cuenta atrás |
| `fase == Tarea` | según `tareaActual`: `TareaFoto`, `AlarmaCalculo`, `AlarmaSoles`, `AlarmaSecuencia`, `AlarmaParejas`, `AlarmaOrden` |
| si no | `AlarmaSonando` (hora grande de 88 sp, fecha, pista de la actividad, botón "Estoy despierto") |

Otras piezas:

- `TareaFoto`: sin permiso → alternativa. Pide silencio 9 veces cada 10 s (1,5 min en total). A los 45 s enseña "Cambiar de actividad".
- `AlarmaFoto`: cámara en tarjeta, con borde ámbar mientras la ve.
- `AlarmaCalculo`: operación, respuesta y `Teclado`; tiembla al fallar.
- `EstadoSonido`: "Silencio, quedan X s".
- `segundosHasta`.
- `FondoAmanecer`: degradado melocotón → blanco.
- `SolAmanecer`: sol que respira.
- `BotonAlarma`: negro y ancho.
- `saludo(hora)`: 5-13 h "Buenos días", 14-20 h "Buenas tardes" y el resto "Buenas noches".

## `Juegos.kt`

- `MarcoJuego` (privado): estado del sonido, instrucción y progreso. Es común a todos.
- `rememberSacudida(fallos)`: temblor y vibración al fallar.
- `AlarmaSoles` + `Sol`.
- `AlarmaSecuencia`: la enseña con 800 ms antes, 520 ms encendido y 200 ms de pausa.
- `AlarmaParejas` + `Carta` (giro 3D) + `Simbolo` (6 formas). Tapa las que no casan a los 800 ms.
- `AlarmaOrden`: rejilla de 3×4.
- `ColoresJuego`: los del sistema de iOS. `ColoresSimbolos`: **sin ámbar**, porque el dorso de las cartas es ámbar.
