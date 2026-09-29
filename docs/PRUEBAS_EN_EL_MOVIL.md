# Probar Alba en el móvil

## Instalar

1. Pasa `Alba-0.1.apk` al móvil (cable USB, Google Drive, Telegram a ti mismo...).
2. Ábrelo desde el móvil. Android pedirá permitir "instalar apps desconocidas" a la
   app con la que lo abras (Archivos, Drive...). Permítelo solo para esa app.
3. Si Play Protect avisa de que la app no es conocida: "Instalar de todas formas".
   Es normal en una app que no viene de Play.

Para actualizar a una versión nueva, se instala encima, sin desinstalar: las
alarmas se conservan.

## Preparar (una vez)

1. Abre Alba y crea una alarma: pedirá permiso para las **notificaciones**. Acéptalo.
2. Si sale el aviso ámbar "Falta un permiso...", tócalo y activa lo que falte.
3. **En el Realme**, desde esa misma pantalla: "Abrir los ajustes de Alba" → Uso de
   la batería → permitir **actividad en segundo plano** y **inicio automático**.
   Sin esto, Realme puede cerrar Alba y la alarma no sonaría.
4. Opcional: "Sin optimizar la batería" → buscar Alba → No optimizar.

## Qué probar

Cada punto, con la tarea de **cálculo** y con la de **foto**:

- [ ] **Probar la alarma** desde el editor: suena, sube de volumen, vibra.
- [ ] Una tecla de volumen = "Estoy despierto": calla y pasa a la tarea.
- [ ] Sin tocar nada durante la tarea, vuelve a sonar a los 30 s.
- [ ] "Atrás" no cierra la pantalla de la alarma.
- [ ] Hecha la tarea: "Buenos días" con la próxima alarma.
- [ ] **Pantalla bloqueada**: programa una alarma para dentro de 2 minutos, bloquea
      el móvil y espera. Tiene que encender la pantalla y salir encima del bloqueo.
- [ ] **App cerrada**: igual, pero quitando Alba de Recientes antes.
- [ ] **Reinicio**: programa una alarma para dentro de 5 minutos, reinicia el móvil y
      **no lo desbloquees**. Tiene que sonar igual.
- [ ] A los 10 minutos de apagarla: "¿Sigues despierto?". Si no respondes en un
      minuto, suena entera.
- [ ] Una alarma "de una vez" se apaga sola en la lista después de sonar; una que se
      repite, no.
- [ ] Foto: que reconozca un fregadero, una taza, el sofá... ¿con poca luz también?
      Apunta cuáles fallan.
- [ ] Con música puesta: al sonar la alarma, la música se pausa.
- [ ] Con el volumen de alarma del sistema bajo: suena igual de alto.

Si algo falla, di qué estabas haciendo y, si puede ser, una captura. Con el móvil
conectado por USB y la depuración activada, se puede ver el registro de errores
desde el PC.
