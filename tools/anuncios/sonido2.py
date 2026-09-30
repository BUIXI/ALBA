# Mezcla de la segunda versión: música presente todo el rato (sin filtros), voz encima,
# tono de alarma genérico del móvil en el "antes" y el sonido de Alba cuando suena Alba.
import json, os, subprocess, sys
import numpy as np, soundfile as sf
sys.argv = [sys.argv[0]] + sys.argv[1:2]
import montaje2 as V

BASE = V.BASE
SR = 48000
REPO = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
ALBA_WAV = os.path.join(REPO, 'app', 'src', 'main', 'res', 'raw', 'amanecer.wav')

# --- tono de alarma de móvil cualquiera: marimba sintetizada, arpegio que se repite
def marimba(f, dur=0.6):
    t = np.arange(int(SR * dur)) / SR
    env = np.exp(-t * 9) * np.minimum(1, t / 0.003)
    return env * (np.sin(2 * np.pi * f * t) + 0.35 * np.sin(2 * np.pi * 3.93 * f * t) * np.exp(-t * 25))
tono = np.zeros(int(SR * 12))
for ciclo in range(12):
    for j, f in enumerate([659.3, 830.6, 987.8, 1318.5, 987.8, 1318.5]):
        i0 = int(SR * (ciclo * 1.0 + j * 0.105))
        n = marimba(f)
        tono[i0:i0 + len(n)] += n[:len(tono) - i0]
tono = 0.3 * tono / np.abs(tono).max()
sf.write(f'{BASE}/sfx/tono_movil.wav', tono.astype(np.float32), SR)

t = np.arange(int(SR * 1.2)) / SR
def nota(f, t0):
    tt = np.clip(t - t0, 0, None)
    return np.where(t >= t0, np.exp(-tt * 5.5) * np.minimum(1, tt / 0.004), 0) * (np.sin(2 * np.pi * f * tt) + 0.25 * np.sin(4 * np.pi * f * tt))
sf.write(f'{BASE}/sfx/encontrado.wav', (0.35 * (nota(1318.5, 0) + nota(1760.0, 0.11))).astype(np.float32), SR)

T, DUR, FIN, GOLPE, GIRO = V.T, V.DUR, V.FIN, V.GOLPE, V.GIRO
E = V.escenas()
cortes = sorted(a for a, _, _, _ in E)
def corte_despues(t0):
    return min(c for c in cortes if c > t0 + 0.01)

entradas, filtros, pistas = [], [], []
def entrada(args):
    entradas.extend(args)
    return entradas.count('-i') - 1

# voz
voces = []
for k, t0 in T.items():
    i = entrada(['-i', V.TOMA[k]])
    filtros.append(f'[{i}]aresample={SR},adelay={int(t0 * 1000)}:all=1[{k}]')
    voces.append(f'[{k}]')
filtros.append(f"{''.join(voces)}amix=inputs={len(voces)}:normalize=0,highpass=f=80,"
               f"equalizer=f=3000:t=q:w=1.2:g=1.5,acompressor=threshold=-22dB:ratio=2.5:attack=10:release=150:makeup=2,"
               f"apad=whole_dur={FIN},volume=1.5,asplit=2[voz][vozsc]")

# música: desde antes del golpe para que la batería entre con "Te explico"
desde = V.GOLPE_PISTA - GOLPE
m = entrada(['-ss', f'{desde:.3f}', '-i', f'{BASE}/musica/766.mp3'])
filtros.append(f'[{m}]aresample={SR},atrim=0:{FIN},volume=0.62,afade=t=out:st={FIN - 2.6}:d=2.5[mus]')
filtros.append('[mus][vozsc]sidechaincompress=threshold=0.03:ratio=2.5:attack=60:release=600[musd]')

def efecto(ruta, desde_archivo, dur, en, vol, extra=''):
    i = entrada(['-ss', str(desde_archivo), '-t', str(dur + 0.05), '-i', ruta])
    n = len(pistas)
    filtros.append(f'[{i}]aresample={SR},aformat=channel_layouts=stereo,{extra}afade=t=in:st=0:d=0.02,'
                   f'afade=t=out:st={max(0, dur - 0.05)}:d=0.05,volume={vol},adelay={int(en * 1000)}:all=1[e{n}]')
    pistas.append(f'[e{n}]')

# gancho: suena Alba hasta que reconoce la taza
reconoce = T['h1'] + DUR['h1'] - 0.25
i = entrada(['-stream_loop', '3', '-i', ALBA_WAV])
filtros.append(f'[{i}]aresample={SR},aformat=channel_layouts=stereo,atrim=0:{reconoce},afade=t=out:st={reconoce - 0.06}:d=0.06,volume=0.5[ah]')
pistas.append('[ah]')
efecto(f'{BASE}/sfx/encontrado.wav', 0, 1.2, reconoce, 0.8)

# antes: el tono del móvil hasta cada toque en Posponer
c1 = V.pulso(V.palabra('v01', 2))
c2 = V.pulso(V.palabra('v01', 5))
efecto(f'{BASE}/sfx/tono_movil.wav', 0, (c2 + 0.8) - c1, c1, 0.55)       # hasta que la mano lo pospone
p1 = V.pulso(V.fin_frase('v01') + 0.15)
p2 = V.pulso(V.palabra('v02', 3))
efecto(f'{BASE}/sfx/tono_movil.wav', 0.0, 0.40, p1, 0.5)
efecto(f'{BASE}/sfx/tono_movil.wav', 0.0, 0.35, p2, 0.5)

# Alba sonando en el móvil; calla al abrir la cámara (en la app, 1,5 min de silencio)
fin_m1 = corte_despues(GIRO)
i = entrada(['-stream_loop', '4', '-i', ALBA_WAV])
filtros.append(f'[{i}]aresample={SR},aformat=channel_layouts=stereo,atrim=0:{fin_m1 - GIRO},'
               f'afade=t=out:st={fin_m1 - GIRO - 0.1}:d=0.1,volume=0.45,adelay={int(GIRO * 1000)}:all=1[am]')
pistas.append('[am]')
efecto(f'{BASE}/sfx/encontrado.wav', 0, 1.2, V.palabra('v07', 9) - 0.1, 0.8)

filtros.append('[voz]aformat=channel_layouts=stereo[vozst]')
filtros.append(f"[vozst][musd]{''.join(pistas)}amix=inputs={2 + len(pistas)}:normalize=0,atrim=0:{FIN},"
               f"loudnorm=I=-14:TP=-1.5:LRA=11,aresample={SR}[out]")
salida = sys.argv[1] if len(sys.argv) > 1 else f'{BASE}/sonido2.wav'
subprocess.run(['ffmpeg', '-v', 'error', '-y'] + entradas + ['-filter_complex', ';'.join(filtros), '-map', '[out]', '-ar', str(SR), salida], check=True)
print('ok', salida, round(FIN, 2))
