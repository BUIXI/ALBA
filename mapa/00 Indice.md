---
aliases: [Índice, Mapa de Alba, Inicio]
tags: [mapa]
actualizado: 2026-09-30
commit: 20a4ddf
---
# Mapa de Alba

> [!info] Para Claude: cómo usar este mapa (ahorra lecturas)
> 1. Lee **esta nota** y después **solo** la nota del tema que toque. No recorras el código entero.
> 2. Cada nota dice qué archivo y qué símbolo buscar: ve directo con Grep (`fun despuesDeSonar`), sin leer carpetas enteras.
> 3. El mapa está al día con el commit **`20a4ddf`** (ver `commit:` arriba). Para saber qué ha cambiado desde entonces: `git log --stat 20a4ddf..HEAD`. Si cambió algo, actualiza **solo** la nota afectada y el `commit:` de esta.

**Alba** es una app Android de alarma que obliga a hacer una actividad (minijuego, foto de un objeto o cálculo) para apagarla. Kotlin + Jetpack Compose, sin *backend*. Versión **0.2** (versionCode 3). Repo `github.com/buixi/alba` (privado), rama `main`. En este PC: `C:\Users\925Broker\Documents\ALBA`.

## Datos rápidos

| | |
|---|---|
| Paquete / applicationId | `com.oriol.alba` (se puede cambiar hasta publicar) |
| Código | `app/src/main/java/com/oriol/alba/` |
| Pruebas | `app/src/test/java/com/oriol/alba/`: 104, todas en el PC (Robolectric) |
| SDK | min 26 (Android 8) · target/compile 36 |
| Base de datos | Room, `alba.db`, **versión 2**, esquemas en `app/schemas/` |
| Librerías | AGP 9.0.1 · Kotlin 2.3.20 · Compose BOM 2026.03.01 · Navigation 3 1.0.1 · Room 2.8.4 · CameraX 1.5.3 · ML Kit labeling 17.0.9 · Robolectric 4.17 · Roborazzi 1.75 |
| No hay | Hilt, *backend*, anuncios ni pagos (Premium provisional, siempre activo) |
| Móvil de pruebas | Realme 14 Pro+ (Android 15). **Aún no se ha probado en el móvil.** |

## ¿Qué busco? → ¿Adónde voy?

| Quiero... | Nota | Archivos |
|---|---|---|
| Entender cómo suena una alarma, de principio a fin | [[Flujo de una alarma]] | `alarma/*`, `ui/alarma/*` |
| Cambiar volumen, silencios o tiempos | [[Recetas#Cambiar un tiempo o un umbral]] | `alarma/SesionAlarma.kt` → `Ajustes` |
| Servicio, receptores, notificación, permisos | [[alarma - sistema]] | `alarma/*` |
| Cuándo suena (próximo disparo, días) | [[dominio]] | `dominio/Programacion.kt`, `Dias.kt` |
| Guardar o leer alarmas, BD, migraciones | [[datos]] | `datos/*` |
| Añadir o tocar una actividad o un minijuego | [[Actividades]] · [[Recetas#Añadir una actividad]] | `datos/Alarma.kt`, `ui/tareas/`, `ui/alarma/Juegos.kt` |
| La pantalla que sale al sonar | [[ui - pantalla de alarma]] | `ui/alarma/*` |
| Lista, editor, pantalla de permisos, navegación | [[ui - pantallas de la app]] | `ui/lista`, `ui/editor`, `ui/permisos`, `Navigation.kt` |
| Colores, fuentes, botones, rueda | [[Sistema de diseno]] | `theme/`, `ui/componentes/` |
| Textos e idiomas | [[Recetas#Añadir un texto]] | `res/values/strings.xml` (inglés) y `res/values-es/` |
| Pruebas y capturas | [[Pruebas]] | `app/src/test/...` |
| Qué clave tiene un texto que se ve en pantalla | [[Textos]] | `res/values-es/strings.xml` |
| Manifiesto, temas, iconos, Gradle, esquema SQL | [[Recursos y configuracion]] | |
| Ver cómo son las pantallas | [[Capturas]] | `docs/capturas/` |
| Compilar, APK, móvil | [[Compilar y entorno]] | `CLAUDE.md` |
| Cómo encaja todo | [[Arquitectura]] | |
| Qué entró en cada versión | [[Historial]] | |
| Qué nota explica un archivo concreto | [[Inventario]] (los 127 archivos) | |
| Qué falta y cosas desfasadas | [[Pendiente y deudas]] | |
| Qué significa cada palabra del código | [[Glosario]] | |

## Reglas de oro (no romper)

- **Nunca anuncios** entre que suena la alarma y que se completa la tarea.
- La alarma **siempre se puede apagar**: sin cámara, o si no reconoce nada, pasa a otra actividad.
- La alarma **suena sin internet y sin base de datos**: viaja entera en los extras del Intent (`alarma/Extras.kt`).
- Cambio de esquema de BD → subir la versión + migración + `MigracionTest` (la app ya está instalada en el móvil de Oriol).
- Todo texto nuevo va en `values/` **y** `values-es/`, con la misma clave.
- La app siempre en oscuro y la pantalla de alarma siempre en claro. Nada de Material por defecto.
- Código comentado en español. **No** editar con `Get-Content`/`Set-Content` de PowerShell 5.1 (rompe las tildes).

## Documentos originales del repo

- [[PRODUCTO]]: idea, decisiones, diseño, monetización y plan por fases.
- [[INDICE]]: estado por fases y mapa corto de carpetas.
- [[PRUEBAS_EN_EL_MOVIL]]: lista de comprobación para el Realme.
- Fases: [[fase00-entorno]] · [[fase01-diseno-y-lista]] · [[fase02a05-version01]] · [[version02-minijuegos]]
- `CLAUDE.md`: entorno y comandos (Claude Code lo carga solo al trabajar en la carpeta).

## Qué NO está en el mapa (a propósito)

- El interior de cada pantalla (márgenes, tamaños, animaciones): abre el archivo que indica la nota.
- Números de línea, porque cambian con cada edición. Busca por el nombre del símbolo.
- El contenido de cada prueba: solo qué comprueba (nombres en [[Pruebas]]).
- Las conversaciones de claude.ai (chats) sobre Alba: no son accesibles desde Claude Code. Si hay decisiones allí, hay que pegarlas y añadirlas a la nota que toque.

## Todas las notas del mapa

[[Arquitectura]] · [[Flujo de una alarma]] · [[alarma - sistema]] · [[datos]] · [[dominio]] · [[ui - pantallas de la app]] · [[ui - pantalla de alarma]] · [[Actividades]] · [[Sistema de diseno]] · [[Textos]] · [[Recursos y configuracion]] · [[Pruebas]] · [[Capturas]] · [[Compilar y entorno]] · [[Recetas]] · [[Historial]] · [[Inventario]] · [[Glosario]] · [[Pendiente y deudas]]
