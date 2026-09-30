# Anuncio 03 — "A la primera"

Vídeo vertical (1080×1920, 24 fps, ~47 s) para TikTok, Reels y Shorts. Presenta la
alarma de **detectar un objeto con la cámara** y lo que la separa de una alarma
normal, contado en primera persona por un hombre, sin tono de anuncio. Hecho entero
en la nube, con la app sacada de GitHub: las pantallas del móvil son las **capturas
reales de la app** (Roborazzi), no un dibujo.

El vídeo no se guarda en Git (pesa). Se rehace con los pasos de abajo.

## Versión 2 (la buena)

Cambios pedidos por Oriol sobre la primera: voz mucho más real, música más natural
e integrada, la alarma del **móvil** (nadie usa ya un despertador de campanas) y más
gancho al principio.

1. **Gancho** (0–3 s): el móvil sonando con la cámara viendo una taza humeante, y
   arriba, grande: *"Mi alarma no se apaga hasta que le enseño una taza"*. Suena
   Alba, la reconoce, "Buenos días" y silencio.
2. **"Te explico."**: entra la batería de la música justo ahí.
3. **Antes** (color frío): la alarma de un móvil cualquiera (pantalla genérica con
   *Posponer* y *Detener*, dibujada en `alarma_generica.py`), la mano que la pospone
   sin abrir los ojos, "cinco minutos más" con 07:05 y 07:10, la almohada a la cabeza.
   "El problema no era la alarma: es que se apaga desde la cama."
4. **Alba** (color cálido): no hay botón de posponer; "Hoy, una taza". Se levanta,
   va a la cocina, apunta: marco naranja y "Buenos días". "…pues café."
5. **Ventajas**: sin internet, solo la cámara en directo y "¿Sigues despierto?" a los
   diez minutos.
6. **Cierre**: "Una sola alarma. Y en pie a la primera." El logo sale como un sol.

Detalles del montaje:

- Todos los cortes caen en el pulso de la música (0,845 s) o en su mitad; los tiempos
  se calculan solos a partir de lo que dura cada frase (`montaje2.py`).
- La música suena todo el rato, sin filtros, y baja un poco cuando habla el narrador.
- El tono del "antes" es una marimba sintetizada (genérica, no el tono de ninguna
  marca); el de Alba, su sonido real (`amanecer.wav`), y calla al abrir la cámara,
  como en la app.

Todo lo que dice es verdad en la versión actual: no hay posponer, la cámara reconoce
la taza en el móvil sin conexión y sin botón de disparo, no admite fotos de la
galería, si no encuentras el objeto vuelve a sonar y la comprobación llega a los 10
minutos.

## Texto para publicar

**TikTok / Reels**

> Durante años pospuse la alarma sin abrir los ojos. Ahora, para callarla, tengo que
> ir a la cocina y enseñarle una taza a la cámara. ☕ Alba, muy pronto en Android.
>
> #despertador #madrugar #rutinademañana #productividad #android #apps

**YouTube Shorts** — título: *Mi alarma no se apaga hasta que le enseño una taza ☕*;
descripción: la misma de arriba.

## Cómo se rehace

Todo en Linux (la nube vale). Scripts en [`tools/anuncios/`](../../tools/anuncios/).

```bash
# 1. Capturas de la app para el anuncio (app/build/outputs/roborazzi/anuncio_*.png)
./gradlew recordRoborazziDebug --tests '*CapturasAnuncioTest*'
# 2. Herramientas
sudo apt-get install -y ffmpeg espeak-ng fonts-inter
python3 -m venv ~/venv && ~/venv/bin/pip install kokoro soundfile faster-whisper pillow numpy \
  --extra-index-url https://download.pytorch.org/whl/cpu
python3 -m venv ~/venvcb && ~/venvcb/bin/pip install chatterbox-tts \
  --extra-index-url https://download.pytorch.org/whl/cpu
# 3. Recursos, voz, imagen y sonido
bash tools/anuncios/descargar.sh
~/venv/bin/python tools/anuncios/voz.py                 # Kokoro: solo hace falta como referencia de timbre
~/venvcb/bin/python tools/anuncios/voz_chatterbox.py    # voz buena, 2 tomas por frase (~40 min en CPU)
~/venv/bin/python tools/anuncios/voz_elegir.py          # mejor toma y tiempo de cada palabra
cd tools/anuncios
~/venv/bin/python montaje2.py ~/anuncio/imagen2.mp4     # ~6 min
~/venv/bin/python sonido2.py ~/anuncio/sonido2.wav
# 4. Para redes (y por debajo de 30 MB): dos pasadas a ~3,7 Mbps
ffmpeg -i ~/anuncio/imagen2.mp4 -an -c:v libx264 -preset slow -b:v 3700k -pass 1 -f null /dev/null
ffmpeg -i ~/anuncio/imagen2.mp4 -i ~/anuncio/sonido2.wav -map 0:v -map 1:a -c:v libx264 -preset slow \
  -b:v 3700k -maxrate 6M -bufsize 8M -pass 2 -c:a aac -b:a 160k -shortest -movflags +faststart \
  ~/anuncio/Alba-anuncio03.mp4
```

- `montaje2.py x 1.0 5.7 …` saca fotogramas sueltos (`c2_*.png`) para revisar sin
  renderizar todo.
- La cámara de la app se dibuja dos veces (hueco negro y hueco blanco) en
  `CapturasAnuncioTest`; comparándolas sale la máscara exacta del hueco, y ahí dentro
  va el vídeo de la taza con un temblor de mano.
- Sin grano de película: TikTok e Instagram recomprimen y el grano se vuelve manchas
  (y dispara el peso).
- Sonoridad final −14 LUFS, la que usan TikTok, Instagram y YouTube.
- La primera versión (despertador de campanas, "Possible Dreams", voz de Kokoro) sigue
  en `montaje.py` y `sonido.py`.

## Créditos y licencias

- Vídeo, música ("Lo-Fi 04") y efectos: **Mixkit**, licencia gratuita que permite
  usarlos en anuncios y redes sin atribución. No se pueden revender ni redistribuir
  sueltos. Clips en `tools/anuncios/descargar.sh`.
- Voz: **Chatterbox Multilingual** (Resemble AI, MIT), con el timbre de una voz
  sintética de **Kokoro-82M** (Apache 2.0) como referencia: no se clona a nadie real.
  Lleva la marca de agua inaudible de Chatterbox (Perth). Si hay presupuesto,
  ElevenLabs o una persona real sonarían aún mejor.
- Tipografía: Inter (SIL OFL), la misma de la app.
