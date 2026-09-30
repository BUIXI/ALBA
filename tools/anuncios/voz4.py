# Voz del vídeo 04 con Chatterbox Multilingual (MIT). Timbre de referencia: la voz del
# anuncio 03 (voz2/elegidas), para que sea el mismo narrador. Genera una toma por frase;
# las que la transcripción no entiende o salen con un tono raro se repiten con otra
# semilla (voz_elegir.py con ANUNCIO_VOZ=v4/voz elige la mejor).
# Uso: ~/venvcb/bin/python tools/anuncios/voz4.py [tomas]
import json, os, sys
import numpy as np, soundfile as sf, torch, torchaudio as ta
from chatterbox.mtl_tts import ChatterboxMultilingualTTS

A = os.environ.get('ANUNCIO', os.path.expanduser('~/anuncio'))
D = f'{A}/v4/voz'
os.makedirs(f'{D}/tomas', exist_ok=True)
L = {
 "h1": "Hemos hecho la alarma más odiada del mundo.",
 "h2": "Y lo hemos hecho a propósito.",
 "p1": "Es para ti, si pones siete alarmas…",
 "p2": "si apagas el móvil sin despertarte…",
 "p3": "o si tus cinco minutos más duran una hora.",
 "m1": "Con Alba no hay botón de posponer.",
 "m2": "Para apagarla, tienes que levantarte.",
 "m3": "E ir a enseñarle a la cámara algo de tu casa.",
 "o1": "El fregadero.",
 "o2": "Una taza.",
 "o3": "El sofá.",
 "o4": "Una planta.",
 "m5": "Y hasta que no lo encuentras, vuelve a sonar.",
 "m6": "Si vuelves a la cama, a los diez minutos te pregunta si sigues despierto.",
 "c1": "Los primeros días la vas a odiar.",
 "c2": "Después, nos darás las gracias.",
 "c3": "Alba. Muy pronto en Android."
}
json.dump(L, open(f'{D}/textos.json', 'w'), ensure_ascii=False, indent=1)

# referencia: ~15 s del narrador del anuncio 03
partes = []
for k in ['v05', 'v08', 'v11', 'v03']:
    a, sr = sf.read(f'{A}/voz2/elegidas/{k}.wav')
    partes += [a, np.zeros(int(sr * 0.3))]
sf.write(f'{D}/ref_cb.wav', np.concatenate(partes), sr)

m = ChatterboxMultilingualTTS.from_pretrained(device='cpu')
for toma in range(int(sys.argv[1]) if len(sys.argv) > 1 else 1):
    for k, t in L.items():
        destino = f'{D}/tomas/{k}_{toma}.wav'
        if os.path.exists(destino):
            continue
        torch.manual_seed(300 + 31 * toma + len(t))
        w = m.generate(t, language_id='es', audio_prompt_path=f'{D}/ref_cb.wav', exaggeration=0.65, cfg_weight=0.4, temperature=0.8)
        ta.save(destino, w, m.sr)
        print(k, toma, round(w.shape[-1] / m.sr, 2), flush=True)
