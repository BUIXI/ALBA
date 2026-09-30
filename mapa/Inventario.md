---
tags: [mapa]
commit: 20a4ddf
---
# Inventario: todos los archivos del repositorio

Volver al [[00 Indice]]. Hay 127 archivos en Git (commit `20a4ddf`) y **todos** están aquí, cada uno con la nota que lo explica. Rutas relativas a la raíz del repo; `…/alba/` = `app/src/main/java/com/oriol/alba/` y `test/…/alba/` = `app/src/test/java/com/oriol/alba/`.

## Raíz y configuración → [[Recursos y configuracion]]

| Archivo | Qué es |
|---|---|
| `.gitattributes` | Finales de línea (LF/CRLF) y binarios |
| `.gitignore` | Lo que no va a Git |
| `CLAUDE.md` | Instrucciones para Claude Code → [[Compilar y entorno]] |
| `build.gradle.kts` | Plugins (raíz) |
| `settings.gradle.kts` | Nombre del proyecto, módulo `:app`, repositorios |
| `gradle.properties` | Opciones de Gradle |
| `gradle/libs.versions.toml` | Catálogo de versiones |
| `gradle/wrapper/gradle-wrapper.jar` | Wrapper de Gradle |
| `gradle/wrapper/gradle-wrapper.properties` | Versión del wrapper |
| `gradlew` | Gradle (Linux/macOS) |
| `gradlew.bat` | Gradle (Windows) |
| `tools/GenerarSonido.java` | Genera `amanecer.wav` |
| `app/.gitignore` | Ignora `build/` del módulo |
| `app/build.gradle.kts` | Configuración del módulo app |
| `app/proguard-rules.pro` | Reglas de R8 (vacío) |
| `app/schemas/com.oriol.alba.datos.BaseDatos/1.json` | Esquema de la BD v1 → [[datos]] |
| `app/schemas/com.oriol.alba.datos.BaseDatos/2.json` | Esquema de la BD v2 → [[datos]] |
| `app/src/main/AndroidManifest.xml` | Manifiesto |

## Código: raíz del paquete → [[ui - pantallas de la app]] · [[Arquitectura]]

| Archivo | Qué es |
|---|---|
| `…/alba/AlbaApp.kt` | Application + `Contenedor` |
| `…/alba/MainActivity.kt` | Actividad principal |
| `…/alba/Navigation.kt` | `NavegacionAlba`, transición modal |
| `…/alba/NavigationKeys.kt` | Claves `Lista`, `Editor`, `ClavePermisos` |

## Código: `alarma/` → [[alarma - sistema]]

| Archivo | Qué es |
|---|---|
| `…/alba/alarma/CentralAlarma.kt` | StateFlow de la sesión que suena |
| `…/alba/alarma/Extras.kt` | Alarma y modo en los extras del Intent |
| `…/alba/alarma/Notificaciones.kt` | Canal y notificación de la alarma |
| `…/alba/alarma/Permisos.kt` | Estado de los permisos y cómo ir a cada ajuste |
| `…/alba/alarma/Programador.kt` | `Programador` + `ProgramadorSistema` (`setAlarmClock`) |
| `…/alba/alarma/Receptores.kt` | `ReceptorAlarma` + `ReceptorSistema` |
| `…/alba/alarma/Reproductor.kt` | Altavoz y vibrador |
| `…/alba/alarma/ServicioAlarma.kt` | Servicio en primer plano |
| `…/alba/alarma/SesionAlarma.kt` | Lógica de la alarma sonando |

## Código: `datos/` → [[datos]]

| Archivo | Qué es |
|---|---|
| `…/alba/datos/Alarma.kt` | Entidad, `TipoTarea`, `Sonido` |
| `…/alba/datos/BaseDatos.kt` | DAO, convertidores, `BaseDatos` v2 |
| `…/alba/datos/RepositorioAlarmas.kt` | Puerta única a los datos |

## Código: `dominio/` → [[dominio]]

| Archivo | Qué es |
|---|---|
| `…/alba/dominio/Actividades.kt` | `elegirTarea`, `alternativaA` |
| `…/alba/dominio/Calculo.kt` | Operaciones y generador |
| `…/alba/dominio/Dias.kt` | Semana según el idioma y resúmenes |
| `…/alba/dominio/Foto.kt` | Objetos, etiquetas de ML Kit, detector |
| `…/alba/dominio/Programacion.kt` | `siguienteDisparo`, `proximaAlarma`, `minutosHasta` |

## Código: `premium/` y `theme/` → [[Actividades#Premium]] · [[Sistema de diseno]]

| Archivo | Qué es |
|---|---|
| `…/alba/premium/Premium.kt` | `Premium.activo` (provisional) |
| `…/alba/theme/Colores.kt` | Paletas oscura y clara |
| `…/alba/theme/Tema.kt` | `TemaAlba`, `Alba.colores/tipos` |
| `…/alba/theme/Tipografia.kt` | Inter y escala de tipos |

## Código: `ui/` → [[ui - pantallas de la app]] · [[ui - pantalla de alarma]] · [[Actividades]] · [[Sistema de diseno]]

| Archivo | Qué es |
|---|---|
| `…/alba/ui/alarma/ActividadAlarma.kt` | Actividad de la alarma (sobre el bloqueo) |
| `…/alba/ui/alarma/AlarmaViewModel.kt` | Fases, actividades, completar |
| `…/alba/ui/alarma/Juegos.kt` | Pantallas de los 4 minijuegos |
| `…/alba/ui/alarma/PantallaAlarma.kt` | Enrutador y pantallas: sonando, cálculo, foto, comprobación, hecho |
| `…/alba/ui/componentes/Basicos.kt` | Pantalla, botones, grupos y filas |
| `…/alba/ui/componentes/Indicaciones.kt` | Respuesta al pulsar |
| `…/alba/ui/componentes/Interruptor.kt` | Interruptor iOS |
| `…/alba/ui/componentes/Reloj.kt` | `rememberAhora`, `formatoHora` |
| `…/alba/ui/componentes/Rueda.kt` | Rueda de hora |
| `…/alba/ui/componentes/SelectorDias.kt` | Días en círculos |
| `…/alba/ui/componentes/Teclado.kt` | Teclado numérico |
| `…/alba/ui/componentes/TextosDias.kt` | `textoCuando`, `textoDias` |
| `…/alba/ui/editor/EditorViewModel.kt` | Borrador y guardar/eliminar |
| `…/alba/ui/editor/PantallaEditor.kt` | Editor (hoja) |
| `…/alba/ui/lista/ListaViewModel.kt` | Alarmas + activar |
| `…/alba/ui/lista/PantallaLista.kt` | Lista de alarmas |
| `…/alba/ui/permisos/PantallaPermisos.kt` | "Para que suene siempre" |
| `…/alba/ui/tareas/CamaraReconocedora.kt` | CameraX + ML Kit |
| `…/alba/ui/tareas/EstadoCalculo.kt` | Estado del cálculo, `Tecla` |
| `…/alba/ui/tareas/EstadosJuegos.kt` | Estado de los 4 minijuegos |

## Recursos → [[Recursos y configuracion]] · [[Textos]]

| Archivo | Qué es |
|---|---|
| `app/src/main/assets/licencias/Inter-OFL.txt` | Licencia de Inter |
| `app/src/main/res/drawable/ic_borrar.xml` | Icono de borrar |
| `app/src/main/res/drawable/ic_check.xml` | Marca |
| `app/src/main/res/drawable/ic_launcher_foreground.xml` | Dibujo del icono |
| `app/src/main/res/drawable/ic_mas.xml` | "+" |
| `app/src/main/res/drawable/ic_notificacion.xml` | Icono de la notificación |
| `app/src/main/res/font/inter.ttf` | Fuente |
| `app/src/main/res/mipmap-anydpi/ic_launcher.xml` | Icono adaptativo |
| `app/src/main/res/mipmap-anydpi/ic_launcher_round.xml` | Icono redondo |
| `app/src/main/res/raw/amanecer.wav` | Sonido propio |
| `app/src/main/res/values/colors.xml` | `fondo_icono`, `fondo_alarma` |
| `app/src/main/res/values/strings.xml` | Textos en inglés |
| `app/src/main/res/values/themes.xml` | Temas de ventana |
| `app/src/main/res/values-es/strings.xml` | Textos en español |
| `app/src/main/res/values-v31/themes.xml` | Pantalla de arranque (Android 12+) |
| `app/src/main/res/xml/backup_rules.xml` | Copia de seguridad (plantilla) |
| `app/src/main/res/xml/data_extraction_rules.xml` | Extracción de datos (plantilla) |

## Pruebas → [[Pruebas]]

| Archivo | Qué es |
|---|---|
| `test/…/alba/CapturasTest.kt` | 19 capturas |
| `test/…/alba/FlujoTest.kt` | La app entera |
| `test/…/alba/ProgramadorFalso.kt` | Programador de mentira |
| `test/…/alba/alarma/AlarmaSistemaTest.kt` | Extras, programador, receptor, servicio |
| `test/…/alba/alarma/SesionAlarmaTest.kt` | Rampa, silencio, comprobación |
| `test/…/alba/datos/MigracionTest.kt` | BD v1 → v2 |
| `test/…/alba/datos/RepositorioAlarmasTest.kt` | Repositorio |
| `test/…/alba/dominio/ActividadesTest.kt` | Elegir y alternativa |
| `test/…/alba/dominio/CalculoTest.kt` | Cálculo |
| `test/…/alba/dominio/DiasTest.kt` | Días |
| `test/…/alba/dominio/FotoTest.kt` | Foto |
| `test/…/alba/dominio/ProgramacionTest.kt` | Cuándo suena |
| `test/…/alba/ui/alarma/AlarmaViewModelTest.kt` | ViewModel de la alarma |
| `test/…/alba/ui/editor/EditorViewModelTest.kt` | ViewModel del editor |
| `test/…/alba/ui/tareas/JuegosTest.kt` | Lógica de los juegos |

## Documentación → [[00 Indice#Documentos originales del repo]]

| Archivo | Qué es |
|---|---|
| `docs/INDICE.md` | [[INDICE]] |
| `docs/PRODUCTO.md` | [[PRODUCTO]] |
| `docs/PRUEBAS_EN_EL_MOVIL.md` | [[PRUEBAS_EN_EL_MOVIL]] |
| `docs/fases/fase00-entorno.md` | [[fase00-entorno]] |
| `docs/fases/fase01-diseno-y-lista.md` | [[fase01-diseno-y-lista]] |
| `docs/fases/fase02a05-version01.md` | [[fase02a05-version01]] |
| `docs/fases/version02-minijuegos.md` | [[version02-minijuegos]] |
| `docs/capturas/fase01/` (5 PNG) | [[Capturas]] |
| `docs/capturas/version01/` (11 PNG) | [[Capturas]] |
| `docs/capturas/version02/` (5 PNG) | [[Capturas]] |
| `docs/capturas/idiomas/` (4 PNG) | [[Capturas]] |

## Fuera de Git (no están en el inventario)

`mapa/` (esta bóveda), `.obsidian/`, `build/`, `.gradle/`, `local.properties` y `entregas/` (APK).
