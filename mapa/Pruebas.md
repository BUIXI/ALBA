---
tags: [mapa]
carpeta: app/src/test/java/com/oriol/alba/
---
# Pruebas (104, todas en el PC)

Volver al [[00 Indice]]. JUnit 4 + corrutinas de prueba + Robolectric (Android simulado) + Roborazzi (capturas). No hay pruebas instrumentadas en `androidTest`.

```powershell
.\gradlew.bat testDebugUnitTest --console=plain        # pruebas
.\gradlew.bat recordRoborazziDebug --console=plain     # capturas PNG
```

Resultados: `app\build\test-results\testDebugUnitTest\*.xml`. Entorno: [[Compilar y entorno]].

| Archivo | Nº | Qué comprueba |
|---|---|---|
| `dominio/ProgramacionTest` | 11 | `siguienteDisparo` (hoy/mañana, misma hora, laborables, solo hoy, desactivada), `proximaAlarma`, `minutosHasta` |
| `dominio/DiasTest` | 5 | Semana de ES/EE. UU., resúmenes, letras, máscara de ida y vuelta |
| `dominio/CalculoTest` | 6 | Rangos, las tres operaciones, aciertos, fallos, borrar, cero delante |
| `dominio/FotoTest` | 6 | `seVe`, trampa de pantalla, detector de 3 seguidos, objeto al azar |
| `dominio/ActividadesTest` | 5 | `elegirTarea`, `alternativaA`, tareas en texto (compatibles con la v1) |
| `datos/RepositorioAlarmasTest` | 9 | Guardar, reprogramar, activar, eliminar, `despuesDeSonar`, `reprogramarTodas` (Room en memoria) |
| `datos/MigracionTest` | 1 | BD v1 → v2 conserva las alarmas |
| `alarma/SesionAlarmaTest` | 6 | Rampa, silencio, silencio alargado, terminar, 30 min, comprobación → alarma |
| `alarma/AlarmaSistemaTest` | 7 | Extras de ida y vuelta, `setAlarmClock`, cancelar solo una, receptor → servicio, servicio completo, comprobación, pruebas que no programan |
| `ui/alarma/AlarmaViewModelTest` | 15 | Despertar, cálculo, foto (objeto, cambios, alternativa), cada juego termina, el azar reparte entre todas, reinicio, comprobación |
| `ui/editor/EditorViewModelTest` | 3 | Una o varias actividades (con o sin Premium), nunca ninguna |
| `ui/tareas/JuegosTest` | 9 | Lógica de los cuatro juegos |
| `FlujoTest` | 2 | La app entera: crear, apagar y eliminar; cancelar no guarda |
| `CapturasTest` | 19 | Capturas de todas las pantallas (también en inglés, `en_*`) |
| `ProgramadorFalso.kt` | – | `Programador` falso que apunta lo programado |

## Capturas

- `CapturasTest`: `@Config(sdk = [35], qualifiers = REALME_14_PRO_PLUS)` (424×933 dp, xxhdpi), en español. `capturar(nombre)` guarda `build/outputs/roborazzi/<nombre>.png`.
- Las 19: `listaVacia`, `listaConAlarmas`, `editorNueva`, `editorEdicion` (`+h1500dp` para que quepa entero), `permisos`, `alarmaSonando`, `alarmaCalculo`, `alarmaFoto`, `juegoSoles`, `juegoSecuencia`, `juegoParejas`, `juegoOrden`, `alarmaComprobacion`, `alarmaHecha`, `icono` y, en inglés (`+en-rUS`), `listaEnIngles`, `editorEnIngles`, `alarmaSonandoEnIngles` y `permisosEnIngles`.
- Se dibujan las versiones **sin estado** de las pantallas (`ListaAlarmas`, `EditorAlarma`, `AlarmaCalculo(...)`...) con datos fijos.
- Salen en `app\build\outputs\roborazzi\*.png`. Al cerrar una fase se copian a `docs/capturas/<fase>/` (`fase01`, `version01`, `version02`, `idiomas`).
- Para ver una pantalla sin compilar, abre la captura de `docs/capturas/` (en Obsidian se ven dentro de la bóveda).

## Consejos

- `FlujoTest` vuelca el árbol de la pantalla cuando falla una espera. En la pantalla por defecto de Robolectric hace falta `performScrollTo`.
- Pruebas y capturas en verde **varias veces** antes de dar algo por cerrado.
