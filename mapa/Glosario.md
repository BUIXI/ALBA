---
tags: [mapa]
---
# Glosario

Volver al [[00 Indice]]. El código está en español: esto es lo que significa cada palabra.

| Palabra | Significado | En el código |
|---|---|---|
| **Alarma** | Lo que se guarda: hora, días, actividades... | `datos/Alarma` |
| **Sesión** | Una alarma **sonando** ahora mismo | `SesionAlarma`, `EstadoSesion`, `CentralAlarma.sesion` |
| **Modo** | Para qué suena: la alarma o la pregunta posterior | `Modo.ALARMA` / `Modo.COMPROBACION` |
| **Comprobación** | "¿Sigues despierto?" 10 min después de apagarla | `Alarma.comprobar`, `programarComprobacion` |
| **Prueba** | Hacerla sonar desde el editor: no toca la BD ni programa nada | `ServicioAlarma.probar`, `esPrueba()`, `EstadoSesion.prueba` |
| **Tarea / actividad** | Lo que hay que hacer para apagarla (el código dice tarea y la interfaz, actividad) | `TipoTarea`, `tareaActual` |
| **Fase** (pantalla) | Sonando → Tarea → Hecho | `AlarmaViewModel.Fase` |
| **Fase** (proyecto) | Etapas del plan (0-6) | `docs/fases/`, [[PRODUCTO]] |
| **Silenciar** | Callar para hacer la tarea; cada toque lo alarga | `ACCION_SILENCIAR`, `silenciadaHasta` |
| **Terminar / completar** | Tarea hecha → para | `ACCION_TERMINAR`, `completar()` |
| **Disparo** | El PendingIntent que el sistema lanza a su hora | `ProgramadorSistema.disparo` |
| **Programador** | Quien habla con `AlarmManager` | `Programador`, `ProgramadorSistema`, `ProgramadorFalso` |
| **Receptor** | BroadcastReceiver | `ReceptorAlarma`, `ReceptorSistema` |
| **Central** | El `StateFlow` que comparte la sesión con las pantallas | `CentralAlarma` |
| **Contenedor** | Las piezas compartidas (sustituye a Hilt) | `AlbaApp.contenedor` |
| **Borrador** | La alarma que se edita antes de "Guardar" | `EditorViewModel.borrador` |
| **Pista** | Texto bajo "Estoy despierto" que dice qué actividad toca | `pista_*` |
| **Alternativa** | Otra actividad si la foto no se puede hacer | `alternativaA` |
| **Objeto** | Lo que hay que enseñar a la cámara | `ObjetoFoto` |
| **Foto ancla** | (Pendiente) tus propios objetos comparados por similitud | – |
| **Marco** | El envoltorio común de los minijuegos | `MarcoJuego` |
| **Sacudida** | El temblor al fallar | `rememberSacudida` |
| **Paleta / tipos** | Colores y estilos de texto | `Paleta`, `Tipos`, `Alba.colores/tipos` |
| **Indicación** | Respuesta visual al pulsar (sin la onda de Android) | `IndicacionOpacidad`, `IndicacionResaltado` |
| **Amanecer** | El sonido propio y el degradado de la pantalla de alarma | `Sonido.AMANECER`, `FondoAmanecer` |
| **Premium** | Pago (aún no): varias actividades por alarma... | `Premium.activo` |
| **Entregas** | Carpeta local de APK (fuera de Git) | `entregas/` |
| **Oriol** | Dueño del proyecto (autor de los commits) | – |
| **Realme** | Móvil de pruebas: Realme 14 Pro+, Android 15 | – |
