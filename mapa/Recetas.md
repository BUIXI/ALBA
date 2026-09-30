---
tags: [mapa]
aliases: [Cómo hacer, Guías]
---
# Recetas: qué tocar para cada cambio

Volver al [[00 Indice]].

## Añadir una actividad

(Guía original: [[version02-minijuegos]], "Cómo añadir una actividad".)

1. Constante en `TipoTarea` (`datos/Alarma.kt`). Su posición = su posición en el editor. Si usa cámara, `usaCamara = true`.
2. Su lógica en `ui/tareas/` (clase de estado, sin pantalla) y sus pruebas en `JuegosTest`.
3. Su pantalla en `ui/alarma/Juegos.kt`, dentro de `MarcoJuego`.
4. En `AlarmaViewModel`:
   - Una función que llame a `acciones.silenciar()` en cada toque y a `completar()` al acabar.
   - Su estado en un `var` y también en `reiniciar()`.
5. En `PantallaAlarma` (el `when (vm.tareaActual)`), en el editor y en la pista: el compilador dice qué `when` faltan.
6. Textos `tarea_x`, `tarea_x_desc` y `pista_x` en `values/` **y** `values-es/`.
7. Una captura en `CapturasTest`.

Detalles: [[Actividades]].

## Añadir un campo a la alarma

1. El campo en `Alarma` (`datos/Alarma.kt`), con un valor por defecto.
2. `BaseDatos`: `version = 3` y una migración (`AutoMigration(from = 2, to = 3)` si solo se añade una columna con valor por defecto).
3. Compilar: se genera `app/schemas/.../3.json` (va a Git).
4. `MigracionTest`: una prueba de la 2 a la 3 que conserve los datos.
5. **`alarma/Extras.kt`**: añadirlo en `ponerAlarma` y en `leerAlarma`. Si no, la alarma sonará con el valor por defecto.
6. `EditorViewModel.cambiarX()` + su fila en `EditorAlarma`.
7. Textos y captura.

## Añadir un texto

- Clave nueva en `app/src/main/res/values/strings.xml` (**inglés**, el de por defecto) y en `values-es/strings.xml` (español), con el mismo nombre. Si falta en uno, el *lint* da error.
- Nombres por prefijo: `tarea_*`, `pista_*`, `permiso_*`, `objeto_*`, `proxima_*`, `notificacion_*`...
- Días y fechas: salen del idioma (`Locale`) y **no** van en `strings.xml`.

## Añadir una pantalla

1. Una clave `@Serializable data object/class ... : NavKey` en `NavigationKeys.kt`.
2. Una `entry<Clave>` en `NavegacionAlba` (`Navigation.kt`). `metadata = TransicionModal` si es una hoja.
3. `PantallaX(vm, ...)` con estado + `X(...)` sin estado para `CapturasTest`.
4. Envolver en `Pantalla { }` (`Basicos.kt`), que pinta su fondo.

## Cambiar un tiempo o un umbral

| Qué | Valor | Dónde |
|---|---|---|
| Subida del volumen | 45 s (de 0,15 a 1) | `SesionAlarma.Ajustes.rampa` / `volumenInicial` |
| Silencio tras "Estoy despierto" | 30 s | `SesionAlarma.Ajustes.silencio` |
| Subida al volver a sonar | 4 s | `SesionAlarma.Ajustes.rampaTrasSilencio` |
| Se para sola | 30 min | `SesionAlarma.Ajustes.duracionMaxima` |
| "¿Sigues despierto?" tras apagar | 10 min | `ServicioAlarma.MinutosComprobacion` |
| Espera de la respuesta / volumen | 60 s / 0,35 | `SesionAlarma.Ajustes.esperaComprobacion` / `volumenComprobacion` |
| Volumen mínimo del sistema | 80 % | `Reproductor` (`init`) |
| Vibración | 600 ms sí / 900 ms no | `Reproductor.vibrar` |
| Silencio mientras busca con la cámara | 9 × 10 s = 1,5 min | `PantallaAlarma.kt` → `TareaFoto` |
| Botón "Cambiar de actividad" en la foto | 45 s | `TareaFoto` |
| Confianza de la foto | 0,55 (ML Kit filtra a 0,4) | `dominio/Foto.kt` `ConfianzaMinima` / `CamaraReconocedora` |
| Fotogramas seguidos | 3 | `DetectorSeguido(necesarios)` |
| Cambios de objeto | 2 | `dominio/Foto.kt` `CambiosDeObjeto` |
| Análisis de la cámara | 1 cada 250 ms | `CamaraReconocedora` → `Analizador` |
| Operaciones de cálculo | 3 | `dominio/Calculo.kt` `OperacionesParaApagar` |
| Soles | 12, de 1,9 s a 1,1 s | `EstadoSoles` (`total`, `vida`) |
| Secuencia | rondas de 4 y 5 | `EstadoSecuencia(longitudes)` |
| Parejas | 6 pares; se tapan a los 0,8 s | `EstadoParejas(pares)` / `Juegos.kt` `AlarmaParejas` |
| Del 1 al … | 12 | `EstadoOrden(total)` |
| Etiqueta máxima | 40 caracteres | `EditorViewModel.LargoMaximoEtiqueta` |
| Hora de una alarma nueva | 7:00 | `EditorViewModel.nuevaAlarma()` |
| Actividad por defecto | SOLES | `TipoTarea.PorDefecto` |
| Límite para reprogramar al encender | 8 s | `ReceptorSistema` |

## Cambiar colores o letras

`theme/Colores.kt` (`PaletaOscura` / `PaletaClara`) y `theme/Tipografia.kt` (`TiposAlba`). Después, `recordRoborazziDebug` y **mirar las capturas**. Ver [[Sistema de diseno]].

## Sacar una versión nueva

1. Subir `versionCode` y `versionName` en `app/build.gradle.kts`.
2. `assembleDebug testDebugUnitTest` + `lintDebug` + capturas.
3. `assembleRelease` → copiar a `entregas\Alba-<versión>.apk`.
4. Su documento en `docs/fases/`, `docs/INDICE.md` al día y capturas en `docs/capturas/<versión>/`.
5. **Actualizar este mapa** (la nota afectada + `commit:` en [[00 Indice]]).
