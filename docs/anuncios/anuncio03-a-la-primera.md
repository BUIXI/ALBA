# Anuncio 03 — "A la primera"

Vídeo vertical (1080×1920, 24 fps, 56 s) para TikTok, Reels y Shorts. Presenta la
alarma de **detectar un objeto con la cámara** y lo que la separa de un despertador
de siempre, contado en primera persona por un hombre, sin tono de anuncio. Hecho
entero en la nube, con la app sacada de GitHub: las pantallas del móvil son las
**capturas reales de la app** (Roborazzi), no un dibujo.

El vídeo no se guarda en Git (pesa). Se rehace con los pasos de abajo.

## La historia

1. **Antes** (color frío, música apagada y filtrada): suena el despertador de
   campanas, estira la mano, a dormir. "Cinco minutos más" con las horas encima
   (07:05, 07:10, 07:15). "El problema no es la alarma: es que se apaga desde la cama."
2. **El giro** (20 s): "Así que me puse una que no." Golpe de música y entra el móvil
   con Alba sonando, con su sonido de verdad (`amanecer.wav`).
3. **La tarea**: no hay botón de posponer. "Hoy tocaba una taza" (pantalla real con
   "Enséñale a la cámara · una taza"). Se levanta, va a la cocina, apunta: el marco
   naranja ("¡Lo estoy viendo!") y "Buenos días".
4. **La recompensa** (color cálido): "Y ya que estás de pie, en la cocina, con una
   taza en la mano… pues café."
5. **Ventajas**, rápidas: sin internet, solo la cámara en directo (nada de galería)
   y "¿Sigues despierto?" a los diez minutos.
6. **Cierre**: "Una sola alarma. Y en pie a la primera." El logo sale como un sol
   por el horizonte: **Alba · El despertador que no se apaga desde la cama · Muy
   pronto en Android.**

Todo lo que dice es verdad en la versión actual: no hay posponer, la cámara
reconoce la taza en el móvil sin conexión y sin botón de disparo, no admite fotos
de la galería y la comprobación llega a los 10 minutos.

## Texto para publicar

**TikTok / Reels**

> Durante años apagué el despertador sin abrir los ojos. Ahora, para callarlo,
> tengo que ir a la cocina y enseñarle una taza a la cámara. ☕ Alba, muy pronto
> en Android.
>
> #despertador #madrugar #rutinademañana #productividad #android #apps

**YouTube Shorts** — título: *El despertador que no se apaga desde la cama ☕*;
descripción: la misma de arriba.

## Cómo se rehace

Todo en Linux (la nube vale). Scripts en [`tools/anuncios/`](../../tools/anuncios/).

```bash
# 1. Capturas de la app para el anuncio (salen en app/build/outputs/roborazzi/anuncio_*.png)
./gradlew recordRoborazziDebug --tests '*CapturasAnuncioTest*'
# 2. Herramientas (ffmpeg, espeak-ng para Kokoro, fuentes Inter)
sudo apt-get install -y ffmpeg espeak-ng fonts-inter
python3 -m venv ~/venv && ~/venv/bin/pip install kokoro soundfile faster-whisper pillow numpy \
  --extra-index-url https://download.pytorch.org/whl/cpu
# 3. Recursos de Mixkit, voz, imagen y sonido
bash tools/anuncios/descargar.sh
~/venv/bin/python tools/anuncios/voz.py
~/venv/bin/python tools/anuncios/montaje.py ~/anuncio/imagen.mp4   # ~15 min
~/venv/bin/python tools/anuncios/sonido.py ~/anuncio/sonido.wav
ffmpeg -i ~/anuncio/imagen.mp4 -i ~/anuncio/sonido.wav -c:v copy -c:a aac -b:a 192k \
  -movflags +faststart ~/anuncio/Alba-anuncio03.mp4
```

- `montaje.py x 3.0 21.5 …` saca fotogramas sueltos (`cuadro_*.png`) para revisar
  sin renderizar todo.
- La cámara de la app se dibuja dos veces (hueco negro y hueco blanco) en
  `CapturasAnuncioTest`; comparándolas sale la máscara exacta del hueco, y ahí
  dentro va el vídeo de la taza con un temblor de mano.
- La música está filtrada (paso bajo) hasta el giro y se abre justo en su golpe
  (segundo 22,11 de la pista). Baja sola cuando habla el narrador.
- Sonoridad final −14 LUFS, la que usan TikTok, Instagram y YouTube.

## Créditos y licencias

- Vídeo, música ("Possible Dreams") y efectos: **Mixkit**, licencia gratuita que
  permite usarlos en anuncios y redes sin atribución. No se pueden revender ni
  redistribuir sueltos. Clips en `tools/anuncios/descargar.sh`.
- Voz: **Kokoro-82M** (Apache 2.0), voz `em_alex`. Es una voz sintética: si el
  anuncio funciona, merece la pena regrabarla con una persona.
- Tipografía: Inter (SIL OFL), la misma de la app.
