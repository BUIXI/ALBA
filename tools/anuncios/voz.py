# Voz del narrador del anuncio 03 ("A la primera"): una frase por archivo, con Kokoro
# (voz em_alex, español) y, con faster-whisper, el momento de cada palabra para los
# subtítulos. Salida en $ANUNCIO/voz/ (por defecto ~/anuncio/voz/).
import json, os
import numpy as np, soundfile as sf
from kokoro import KPipeline
from faster_whisper import WhisperModel

BASE = os.environ.get('ANUNCIO', os.path.expanduser('~/anuncio'))
os.makedirs(f'{BASE}/voz/lineas', exist_ok=True)

L = {
 "v01": "Durante años, mis mañanas empezaban igual.",
 "v02": "Sonaba. Estiraba la mano.",
 "v03": "Y a dormir otra vez.",
 "v04": "Cinco minutos más. Y otros cinco. Y otros cinco.",
 "v05": "Hasta que lo entendí: el problema no es la alarma. Es que se apaga desde la cama.",
 "v06": "Así que me puse una que no.",
 "v07": "Ahora, cuando suena, no hay botón de posponer. Me pide que le enseñe algo de mi casa.",
 "v08": "Hoy tocaba una taza.",
 "v09": "Y las tazas… están en la cocina.",
 "v10": "La apuntas con la cámara, la reconoce… y se calla.",
 "v11": "Y ya que estás de pie, en la cocina, con una taza en la mano…",
 "v12": "pues café.",
 "v13": "Funciona sin internet. Solo vale la cámara, en directo. Y a los diez minutos te pregunta: ¿sigues despierto?",
 "v14": "Una sola alarma. Y en pie a la primera.",
 "v15": "Alba. El despertador que no se apaga desde la cama.",
}

p = KPipeline(lang_code='e')
m = WhisperModel('small', device='cpu', compute_type='int8')
dur, pal = {}, {}
for k, t in L.items():
    a = np.concatenate([x for _, _, x in p(t, voice='em_alex', speed=0.92)])
    sf.write(f'{BASE}/voz/lineas/{k}.wav', a, 24000)
    dur[k] = round(len(a) / 24000, 2)
    segs, _ = m.transcribe(f'{BASE}/voz/lineas/{k}.wav', language='es', word_timestamps=True, initial_prompt=t)
    pal[k] = [(w.word.strip(), round(w.start, 2), round(w.end, 2)) for s in segs for w in s.words]
json.dump({'texto': L, 'dur': dur}, open(f'{BASE}/voz/lineas.json', 'w'), ensure_ascii=False, indent=1)
json.dump(pal, open(f'{BASE}/voz/palabras.json', 'w'), ensure_ascii=False)
print(dur, round(sum(dur.values()), 2))
