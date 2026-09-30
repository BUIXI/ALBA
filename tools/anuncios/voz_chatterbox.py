# Voz del narrador (versión 2) con Chatterbox Multilingual (Resemble AI, licencia MIT),
# mucho más natural que Kokoro. Referencia de timbre: frases sintéticas de Kokoro
# (voz em_alex), para no clonar la voz de ninguna persona real.
# Varias tomas por frase; luego voz_elegir.py se queda con la mejor.
# Uso: ~/venvcb/bin/python tools/anuncios/voz_chatterbox.py  (venv con chatterbox-tts)
import json, os
import numpy as np, soundfile as sf, torch, torchaudio as ta
from chatterbox.mtl_tts import ChatterboxMultilingualTTS

A = os.environ.get('ANUNCIO', os.path.expanduser('~/anuncio'))
D = f'{A}/voz2'
os.makedirs(f'{D}/tomas', exist_ok=True)

L = {
 "h1": "Mi alarma no se apaga hasta que le enseño una taza.",
 "h2": "Te explico.",
 "v01": "Durante años, posponía la alarma sin abrir los ojos.",
 "v02": "Cinco minutos más. Y otros cinco. Y otros cinco.",
 "v03": "Y el problema no era la alarma. Es que se apaga desde la cama.",
 "v04": "Así que me puse una que no.",
 "v05": "Cuando suena, no hay botón de posponer. Me pide que le enseñe algo de mi casa.",
 "v06": "Hoy, una taza. Y las tazas… están en la cocina.",
 "v07": "La apuntas con la cámara, la reconoce… y se calla.",
 "v08": "Y ya que estás de pie, en la cocina, con una taza en la mano…",
 "v09": "pues café.",
 "v11": "Una sola alarma. Y en pie a la primera.",
 "v12": "Alba. El despertador que no se apaga desde la cama.",
}
# v10 va en tres trozos: de una pieza, el "¿…?" del final lo volvía todo pregunta
V10 = ['Funciona sin internet.', 'Solo vale la cámara, en directo.', 'Y a los diez minutos, te pregunta si sigues despierto.']
textos = dict(L, v10=' '.join(V10))
json.dump(textos, open(f'{D}/textos.json', 'w'), ensure_ascii=False, indent=1)

# referencia de timbre: ~14 s de Kokoro (voz.py las deja en $ANUNCIO/voz/lineas)
partes = []
for k in ['v01', 'v05', 'v07']:
    a, sr = sf.read(f'{A}/voz/lineas/{k}.wav')
    partes += [a, np.zeros(int(sr * 0.35))]
sf.write(f'{D}/ref.wav', np.concatenate(partes), sr)

m = ChatterboxMultilingualTTS.from_pretrained(device='cpu')
def decir(t, semilla):
    torch.manual_seed(semilla)
    return m.generate(t, language_id='es', audio_prompt_path=f'{D}/ref.wav', exaggeration=0.6, cfg_weight=0.45, temperature=0.8)

for toma in range(2):
    for k, t in L.items():
        ta.save(f'{D}/tomas/{k}_{toma}.wav', decir(t, 100 + toma), m.sr)
    trozos = [decir(t, 200 + toma).squeeze(0).numpy() for t in V10]
    pausa = np.zeros(int(m.sr * 0.28))
    sf.write(f'{D}/tomas/v10_{toma}.wav', np.concatenate([x for tr in trozos for x in (tr, pausa)][:-1]), m.sr)
print('tomas en', f'{D}/tomas')
