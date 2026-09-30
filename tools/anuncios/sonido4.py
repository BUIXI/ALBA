# Sonido del vídeo 04: "Pop 07" desde el primer fotograma, voz encima (la música baja
# un poco cuando habla), el sonido de Alba cuando suena, un "tic" en cada objeto
# reconocido y la campanilla del "Buenos días".
import os, subprocess, sys
import numpy as np, soundfile as sf
salida = sys.argv[1] if len(sys.argv) > 1 else None
sys.argv = [sys.argv[0]]
import montaje4 as V

BASE = V.BASE
SR = 48000
REPO = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
ALBA_WAV = next(p for p in [os.path.join(REPO, 'app/src/main/res/raw/amanecer.wav'),
                            '/home/user/ALBA/app/src/main/res/raw/amanecer.wav'] if os.path.exists(p))
T, FIN = V.T, V.FIN
E = V.escenas()
inicios = sorted(a for a, _, _, _ in E)

# efectos sintetizados: "tic" de reconocido y campanilla de "Buenos días"
t = np.arange(int(SR * 0.25)) / SR
tic = 0.5 * np.exp(-t * 40) * np.sin(2 * np.pi * 1760 * t) * np.minimum(1, t / 0.002)
sf.write(f'{BASE}/sfx/tic.wav', tic.astype(np.float32), SR)
t = np.arange(int(SR * 1.2)) / SR
def nota(f, t0):
    tt = np.clip(t - t0, 0, None)
    return np.where(t >= t0, np.exp(-tt * 5.5) * np.minimum(1, tt / 0.004), 0) * (np.sin(2 * np.pi * f * tt) + 0.25 * np.sin(4 * np.pi * f * tt))
sf.write(f'{BASE}/sfx/encontrado.wav', (0.35 * (nota(1318.5, 0) + nota(1760.0, 0.11))).astype(np.float32), SR)

entradas, filtros, pistas = [], [], []
def entrada(args):
    entradas.extend(args)
    return entradas.count('-i') - 1

voces = []
for k, t0 in T.items():
    i = entrada(['-i', V.TOMA[k]])
    filtros.append(f'[{i}]aresample={SR},aformat=channel_layouts=mono,adelay={int(t0 * 1000)}:all=1[{k}]')
    voces.append(f'[{k}]')
filtros.append(f"{''.join(voces)}amix=inputs={len(voces)}:normalize=0,highpass=f=80,"
               f"equalizer=f=3000:t=q:w=1.2:g=1.5,acompressor=threshold=-22dB:ratio=2.5:attack=10:release=150:makeup=2,"
               f"apad=whole_dur={FIN},volume=1.9,asplit=2[voz][vozsc]")

m = entrada(['-ss', f'{V.MUSICA_DESDE:.3f}', '-i', V.MUSICA])
filtros.append(f'[{m}]aresample={SR},atrim=0:{FIN},volume=0.55,afade=t=out:st={FIN - 2.4}:d=2.3[mus]')
filtros.append('[mus][vozsc]sidechaincompress=threshold=0.025:ratio=4:attack=30:release=450[musd]')

def efecto(ruta, desde, dur, en, vol):
    i = entrada(['-ss', str(desde), '-t', str(dur + 0.05), '-i', ruta])
    n = len(pistas)
    filtros.append(f'[{i}]aresample={SR},aformat=channel_layouts=stereo,afade=t=out:st={max(0, dur - 0.05)}:d=0.05,'
                   f'volume={vol},adelay={int(en * 1000)}:all=1[e{n}]')
    pistas.append(f'[e{n}]')

def alba(desde, hasta, vol):
    i = entrada(['-stream_loop', '6', '-i', ALBA_WAV])
    n = len(pistas)
    filtros.append(f'[{i}]aresample={SR},aformat=channel_layouts=stereo,atrim=0:{hasta - desde},'
                   f'afade=t=out:st={hasta - desde - 0.08}:d=0.08,volume={vol},adelay={int(desde * 1000)}:all=1[e{n}]')
    pistas.append(f'[e{n}]')

def fin_de_escena(t0):
    return min(x for x in inicios if x > t0 + 0.01)

alba(0.0, fin_de_escena(0.0), 0.35)                 # gancho: suena y pide un fregadero
t_m1 = max(x for x in inicios if x <= V.fin_frase('p3') + 0.3)
alba(t_m1, fin_de_escena(t_m1), 0.4)                # "no hay botón de posponer"
for k in ['o1', 'o2', 'o3', 'o4']:
    efecto(f'{BASE}/sfx/tic.wav', 0, 0.25, T[k], 0.6)
t_tele = fin_de_escena(T['o4'])
efecto(f'{BASE}/sfx/tic.wav', 0, 0.25, t_tele, 0.5)
t_hecho = fin_de_escena(t_tele)
efecto(f'{BASE}/sfx/encontrado.wav', 0, 1.2, t_hecho, 0.8)
t_m5 = fin_de_escena(t_hecho)
alba(V.palabra('m5', 8) + 0.3, fin_de_escena(t_m5), 0.4)  # suena justo después de "sonar"

filtros.append('[voz]aformat=channel_layouts=stereo[vozst]')
filtros.append(f"[vozst][musd]{''.join(pistas)}amix=inputs={2 + len(pistas)}:normalize=0,atrim=0:{FIN},"
               f"loudnorm=I=-14:TP=-1.5:LRA=11,aresample={SR}[out]")
salida = salida or f'{BASE}/v4/sonido4.wav'
subprocess.run(['ffmpeg', '-v', 'error', '-y'] + entradas + ['-filter_complex', ';'.join(filtros), '-map', '[out]', '-ar', str(SR), salida], check=True)
print('ok', salida, round(FIN, 2))
