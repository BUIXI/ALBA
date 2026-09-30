# Vídeo 04 — "La alarma más odiada del mundo"

Vídeo vertical (1080×1920, 24 fps, ~42 s) para TikTok, Reels y Shorts. Distinto de
los dos anteriores (el 1.º, una reseña corta de la app; el 3.º, una historia con
gancho): aquí **hablamos los que hacemos Alba**, en plural y sin tono de anuncio, a
la gente que la va a usar. Todo gira en torno a lo que nos diferencia de cualquier
otra alarma: **para apagarla hay que levantarse**.

Hecho entero en la nube con la app sacada de GitHub: las pantallas del móvil son las
capturas reales (`CapturasAnuncioTest`), con el vídeo de cada objeto dentro del hueco
de la cámara.

## Guion

| | Voz | Imagen |
|---|---|---|
| Gancho | "Hemos hecho la alarma más odiada del mundo." | Título grande arriba. El móvil vibra en la cama pidiendo "un fregadero o un lavabo"; corte a alguien con la almohada en la cabeza |
| | "Y lo hemos hecho a propósito." | Primer plano, ojos abiertos en la cama |
| Para quién | "Es para ti, si pones siete alarmas…" | Lista de 7 alarmas (07:00…07:30) que aparecen una a una |
| | "si apagas el móvil sin despertarte…" | La mano que apaga el móvil sin abrir los ojos |
| | "o si tus cinco minutos más duran una hora." | Durmiendo, con la hora corriendo de 07:05 a 08:05 |
| Lo nuestro | "Con Alba no hay botón de posponer." | Alba sonando; zoom al único botón: "Estoy despierto" |
| | "Para apagarla, tienes que levantarte. E ir a enseñarle a la cámara algo de tu casa." | Se incorpora, anda, la cámara busca |
| Montaje | "El fregadero. Una taza. El sofá. Una planta." | Un objeto por golpe de música, cada uno con su pantalla real ("¡Lo estoy viendo!") y un "tic"; la tele de propina; "Buenos días" |
| | "Y hasta que no lo encuentras, vuelve a sonar." | El móvil vibrando, buscando |
| | "Si vuelves a la cama, a los diez minutos te pregunta si sigues despierto." | Vuelve a dormirse → "¿Sigues despierto?" |
| Cierre | "Los primeros días la vas a odiar. Después, nos darás las gracias." | Cara de sueño → brazos al cielo |
| | "Alba. Muy pronto en Android." | El sol sale; **Comenta «ALBA» y te avisamos** |

Todo lo que dice es verdad en la versión actual: no hay posponer, los objetos son de
la lista de la app (fregadero o lavabo, taza, sofá, planta, tele…), si no lo
encuentras vuelve a sonar tras el minuto y medio de silencio, y la comprobación llega
a los 10 minutos.

## Cómo está hecho

- **Música**: "Pop 07" (Mixkit), 90 bpm, desde el primer fotograma. Los cortes caen
  en sus golpes y cada objeto del montaje empieza en uno.
- **Subtítulos** grandes, palabra a palabra, con la palabra que suena en el naranja
  de la app.
- **Voz**: Chatterbox Multilingual (MIT), el mismo narrador que el anuncio 03, con más
  energía. Se descartan las tomas que la transcripción no entiende o que salen con un
  tono raro (chillonas o con la voz rota). Se probó Zonos (acento de España) pero más
  de la mitad de las frases salían ininteligibles en CPU.
- Pantallas genéricas del "antes" (lista de alarmas) en `alarma_generica.py`.

## Cómo se rehace

Como el anuncio 03 (ver [`anuncio03-a-la-primera.md`](anuncio03-a-la-primera.md)),
cambiando los últimos pasos:

```bash
./gradlew recordRoborazziDebug --tests '*CapturasAnuncioTest*'
bash tools/anuncios/descargar.sh
~/venvcb/bin/python tools/anuncios/voz4.py
ANUNCIO_VOZ=v4/voz ~/venv/bin/python tools/anuncios/voz_elegir.py
cd tools/anuncios
~/venv/bin/python montaje4.py ~/anuncio/v4/imagen4.mp4      # ~8 min
~/venv/bin/python sonido4.py ~/anuncio/v4/sonido4.wav
# y la compresión en dos pasadas del anuncio 03 (~4,5 Mbps, < 30 MB)
```

Ojo con la memoria: la voz (Chatterbox) y el montaje a la vez no caben; de uno en uno.

## Para publicar

**TikTok / Reels**

> Lo sentimos. Bueno, no. ⏰ ¿A quién se la mandarías? 👇
>
> #productividad #habitos #disciplina #desarrollopersonal #tecnologia

**YouTube Shorts** — título: *Hemos hecho la alarma más odiada del mundo ⏰ #productividad #shorts*.
Marcar "contenido alterado o sintético" (la voz es de IA).

## Licencias

Vídeo, música: Mixkit (uso gratuito en anuncios y redes). Voz: Chatterbox (MIT), con
timbre de referencia sintético (Kokoro, Apache 2.0): no se clona a nadie real.
Tipografía: Inter (OFL).
