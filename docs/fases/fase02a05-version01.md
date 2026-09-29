# Fases 2 a 5 — Primera versión funcional (0.1)

Fecha: 2026-09-29. Hechas de un tirón, a petición de Oriol, hasta tener un APK para
probar en el móvil. Capturas en [`../capturas/version01/`](../capturas/version01/).

**Nada de esto se ha probado aún en un móvil de verdad** (no había ninguno conectado
y el emulador necesita Windows Hypervisor Platform). Está probado en el PC con
Robolectric: 71 pruebas. Lo que solo se ve en el móvil está en
[`../PRUEBAS_EN_EL_MOVIL.md`](../PRUEBAS_EN_EL_MOVIL.md).

## Fase 2 — La alarma suena (`alarma/`)

Recorrido de una alarma:

1. `RepositorioAlarmas` programa cada cambio con `ProgramadorSistema`, que usa
   **`AlarmManager.setAlarmClock`**: el tipo más fiable, que suena en Doze y enseña el
   icono de alarma. Cada alarma tiene su `PendingIntent` (requestCode = id, data
   `alba://alarma/ID`). **La alarma entera viaja en los extras** (`Extras.kt`): para
   sonar no hace falta la base de datos.
2. `ReceptorAlarma` recibe el disparo y arranca `ServicioAlarma` en primer plano
   (permitido: lo dispara una alarma exacta). Si Android no lo deja, plan B: una
   notificación a pantalla completa con la alarma dentro. `ActividadAlarma` arranca
   el servicio al verse, en `onCreate` o en `onResume`.
3. `ServicioAlarma` pasa a primer plano (tipo `mediaPlayback`) con una notificación
   de categoría alarma, fija y con *full-screen intent* a `ActividadAlarma`. Crea el
   `Reproductor` y la `SesionAlarma`, y publica el estado en `CentralAlarma`, que
   miran las pantallas. Hace las cuentas de después (`despuesDeSonar`): si se repite,
   la siguiente; si era de una vez, se apaga, como en iOS.
4. `SesionAlarma` es **lógica pura** (probada con tiempo simulado):
   - Rampa de volumen de 0,15 a 1 en 45 s.
   - "Estoy despierto" silencia 30 s; cada toque en la tarea alarga el silencio; si
     te quedas quieto, vuelve a sonar al máximo en 4 s.
   - Se para sola a los 30 minutos.
5. `Reproductor`:
   - Usa el uso de audio `USAGE_ALARM` y pide el foco de audio, así la música se pausa.
   - Sube el volumen de alarma del sistema al 80 % si está por debajo, y lo devuelve
     al terminar.
   - Mapea el volumen con el cuadrado, para que la subida suene pareja.
   - Vibra 0,6 s cada 1,5 s.
   - Si el sonido del sistema falla, usa el propio.
6. `ActividadAlarma`:
   - Se muestra sobre el bloqueo y enciende la pantalla.
   - Va en su propia tarea (`singleInstance`, `taskAffinity=""`, fuera de Recientes).
   - "Atrás" no la cierra hasta terminar.
   - **Las teclas de volumen = "Estoy despierto"**: la idea original de Oriol ("le
     das a bajar volumen y te pide la tarea").
   - Si la alarma acaba sola, se cierra.
7. `ReceptorSistema` reprograma todas las alarmas en estos casos:
   - Al encender el móvil, también **antes del primer desbloqueo**.
   - Al actualizar la app.
   - Al cambiar la hora o la zona horaria.
   - Al conceder el permiso de alarmas exactas.

   `MainActivity` también las reprograma al abrirse.

**Arranque directo (*direct boot*)**: la base de datos está en el almacenamiento
protegido del dispositivo, y el receptor, el servicio y la actividad de la alarma
llevan `directBootAware`. Si el móvil se reinicia de madrugada, la alarma suena
antes de desbloquearlo.

**Sonido propio "Amanecer"**: un carillón en mi mayor con timbre de campana, en un
bucle de 2,4 s sin cortes. Lo genera `tools/GenerarSonido.java`, así que no hay
licencias. La otra opción es la alarma del sistema.

**Permisos** (`Permisos.kt`, `ui/permisos/`):

| Permiso | Uso |
|---|---|
| `POST_NOTIFICATIONS` | Se pide al crear la primera alarma |
| `USE_EXACT_ALARM` | Android 13 en adelante; se concede solo a las apps de alarma |
| `SCHEDULE_EXACT_ALARM` | Solo hasta Android 12 (API 32) |
| `USE_FULL_SCREEN_INTENT` | Pantalla completa sobre el bloqueo |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | El servicio que mantiene la alarma sonando |
| `WAKE_LOCK`, `VIBRATE`, `RECEIVE_BOOT_COMPLETED` | Mantener la CPU despierta, vibrar y reprogramar al encender |
| `CAMERA` | Se pide al elegir la tarea de foto |

La pantalla "Para que suene siempre" dice qué falta y lleva al ajuste exacto. En
Realme y otras marcas agresivas (ver dontkillmyapp.com) añade la instrucción de
permitir la actividad en segundo plano y el inicio automático. La lista enseña un
aviso ámbar si falta algo imprescindible.

**Probar la alarma** desde el editor: suena al momento, igual que de verdad, pero
no toca la base de datos ni programa la comprobación.

## Fase 3 — Cálculo mental (`dominio/Calculo.kt`, `ui/tareas/`)

- Hay que acertar 3 operaciones:
  - Suma de dos números de dos cifras que no acaban en 0.
  - Resta positiva, casi siempre con llevada.
  - Multiplicación de una cifra por dos cifras.
- **Se comprueba sola** al escribir tantas cifras como tiene el resultado, como un
  código de desbloqueo, sin botón "OK".
- Al fallar sale otra operación distinta, la pantalla tiembla y el móvil vibra.
- Teclado redondo tipo teléfono de iOS, con vibración en cada tecla.
- "Tareas intercambiables": la pantalla elige la tarea según `tareaActual`, y la
  foto puede pasar a cálculo.

## Fase 4 — Foto (`dominio/Foto.kt`, `ui/tareas/CamaraReconocedora.kt`)

- **ML Kit Image Labeling con el modelo base dentro de la app**: sin internet ni
  coste por uso.
- **El modelo base no conoce "nevera" ni "microondas"**. Se piden cosas que sí
  conoce y que obligan a salir del dormitorio: fregadero o lavabo, taza, sofá, tele,
  zapatos, planta, cubiertos o plato, y cocina. Cada una con sus etiquetas de ML Kit.
- Cámara en vivo, sin botón de disparo. Se analizan unos 4 fotogramas por segundo y
  el objeto tiene que verse en **3 seguidos** con confianza ≥ 0,55. Si salen
  "Screenshot" o "Web page", no vale.
- "Pedir otro" objeto 2 veces, por si no tienes tele o planta.
- **Salida de seguridad**: sin permiso de cámara, o si la cámara falla, pasa a
  cálculo. A los 45 s aparece "Hacer el cálculo". La alarma siempre se puede apagar.
- Mientras se busca, la cámara pide silencio durante 1,5 minutos; después la alarma
  vuelve a sonar hasta encontrarlo.

**Pendiente**: la "foto ancla" (tus objetos concretos, comparados por similitud) y
calibrar el umbral con fotos reales de madrugada.

## Fase 5 — Comprobación y pulido

- **"¿Sigues despierto?"** a los 10 minutos de apagarla. Se puede desactivar por
  alarma y no se hace en las pruebas.
  - Suena suave (0,35) con una cuenta atrás.
  - Si nadie responde en 1 minuto, pasa a alarma entera, con tarea.
  - Si la pantalla se quedó en "Buenos días", la comprobación empieza de cero (se
  vio revisando el código: el ViewModel se reutilizaba).
- Pantalla "Buenos días" con la hora a la que te levantaste y la próxima alarma. El
  saludo cambia a tardes o noches según la hora. Aquí irán los anuncios de la fase
  6, **nunca antes**.

## Revisión a mano de lo que no prueba el PC

Encontrado y corregido antes de entregar:

- `setShowWhenLocked`/`setTurnScreenOn` e `isBackgroundRestricted` no existen en
  Android 8.0/8.1 (nuestro mínimo es 8.0): la app se habría cerrado al sonar.
- `notify()` sin comprobar el permiso (*lint*): ahora todo pasa por
  `Notificaciones.publicar`.
- La cámara podía lanzar una excepción en su hilo si el reconocedor se cerraba con
  un fotograma en marcha.
- `startForeground` podía fallar sin plan B.

## APK

- `assembleRelease`: firmado con la clave de depuración de este PC (Play la
  rechaza: hará falta una propia para publicar).
- Sin R8, porque no se ha podido comprobar en un móvil. Arquitecturas `arm64-v8a` y
  `armeabi-v7a`. Pesa **30 MB**: lo grande es el código sin minificar y la librería
  nativa de ML Kit.
- ML Kit añade por su cuenta los permisos `INTERNET` y `ACCESS_NETWORK_STATE`, para
  sus estadísticas de uso. El reconocimiento es local y las imágenes no salen del
  móvil. Hay que tenerlo en cuenta para la política de privacidad.

## Pruebas (71)

| Prueba | Qué comprueba |
|---|---|
| `alarma/SesionAlarmaTest` (6) | Rampa, silencio y vuelta, silencio alargado, terminar, 30 min, comprobación que escala |
| `alarma/AlarmaSistemaTest` (7) | Extras ida y vuelta, `setAlarmClock` a la hora exacta, cancelar solo una, receptor → servicio, servicio completo (notificación de alarma a pantalla completa, silencio, terminar), comprobación a los 10 min, las pruebas no programan |
| `datos/RepositorioAlarmasTest` (9) | Guardar, reprogramar, activar/desactivar, eliminar, después de sonar (repetida y de una vez), reprogramar todas |
| `dominio/CalculoTest` (6), `FotoTest` (6) | Rangos de operaciones, aciertos y fallos, reconocimiento, detector, objetos al azar |
| `ui/alarma/AlarmaViewModelTest` (8) | Despertar, tarea, hecho con la próxima, foto de principio a fin, cambiar objeto, pasar a cálculo, comprobación, reinicio con una alarma nueva |
| Anteriores | Programación, días, flujo completo, 11 capturas |
