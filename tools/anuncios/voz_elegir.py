# Elige la mejor toma de cada frase: la que la transcripción entiende más parecida al
# texto, recorta silencios del principio y del final y saca el tiempo de cada palabra.
import glob, json, os, re, subprocess, difflib
from faster_whisper import WhisperModel

D = os.path.join(os.environ.get('ANUNCIO', os.path.expanduser('~/anuncio')), os.environ.get('ANUNCIO_VOZ', 'voz2'))
L = json.load(open(f'{D}/textos.json'))
os.makedirs(f'{D}/elegidas', exist_ok=True)
m = WhisperModel('small', device='cpu', compute_type='int8')

def norma(s):
    s = s.lower()
    for a, b in zip('áéíóúü', 'aeiouu'):
        s = s.replace(a, b)
    s = s.replace('10', 'diez').replace('5', 'cinco')
    return re.sub(r'[^a-zñ ]', '', s).split()

eleg, pal, notas = {}, {}, {}
for k, t in L.items():
    mejor = None
    for f in sorted(glob.glob(f'{D}/tomas/{k}_*.wav')):
        segs, _ = m.transcribe(f, language='es')
        txt = ''.join(s.text for s in segs)
        r = difflib.SequenceMatcher(None, norma(t), norma(txt)).ratio()
        if mejor is None or r > mejor[0]:
            mejor = (r, f, txt)
    r, f, txt = mejor
    dest = f'{D}/elegidas/{k}.wav'
    # fuera silencios al principio y al final (con un respiro de 60 ms)
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', f, '-af',
                    'silenceremove=start_periods=1:start_threshold=-45dB:start_silence=0.06,'
                    'areverse,silenceremove=start_periods=1:start_threshold=-45dB:start_silence=0.08,areverse',
                    dest], check=True)
    # tiempos por palabra: sin pista y con el texto como pista; vale la que cuadra en número
    opciones = []
    for pista in (None, t):
        segs, _ = m.transcribe(dest, language='es', word_timestamps=True, initial_prompt=pista)
        opciones.append([(w.word.strip(), round(w.start, 2), round(w.end, 2)) for s in segs for w in s.words])
    n = len(t.split())
    pal[k] = min(opciones, key=lambda o: abs(len(o) - n))
    eleg[k] = dest
    notas[k] = (round(r, 2), os.path.basename(f), txt.strip())
    print(k, notas[k], len(pal[k]), 'palabras', flush=True)
json.dump(eleg, open(f'{D}/elegidas.json', 'w'), indent=1)
json.dump(pal, open(f'{D}/palabras.json', 'w'), ensure_ascii=False)
json.dump(notas, open(f'{D}/notas.json', 'w'), ensure_ascii=False, indent=1)
