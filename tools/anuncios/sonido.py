# Mezcla de sonido del anuncio: voz, música (filtrada hasta que entra Alba), timbres y efectos.
import json, os, subprocess, sys
import numpy as np, soundfile as sf

BASE = os.environ.get('ANUNCIO', os.path.expanduser('~/anuncio'))
REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
SR = 48000
DUR = 56.5
GIRO = 20.25
MUSICA_DESDE = 22.11 - GIRO  # el golpe de la música (22,11 s) cae en el giro

VOZ = {'v01': 1.30, 'v02': 5.00, 'v03': 7.10, 'v04': 9.00, 'v05': 12.75, 'v06': 17.95, 'v07': 20.55,
       'v08': 25.95, 'v09': 28.05, 'v10': 30.65, 'v11': 34.45, 'v12': 38.25, 'v13': 39.90, 'v14': 47.40,
       'v15': 51.60}

# campanilla de "encontrado": dos notas suaves
t = np.arange(int(SR * 1.2)) / SR
def nota(f, t0):
    tt = np.clip(t - t0, 0, None)
    env = np.where(t >= t0, np.exp(-tt * 5.5) * np.minimum(1, tt / 0.004), 0)
    return env * (np.sin(2 * np.pi * f * tt) + 0.25 * np.sin(2 * np.pi * 2 * f * tt))
campanilla = 0.35 * (nota(1318.5, 0.0) + nota(1760.0, 0.11))
sf.write(f'{BASE}/sfx/encontrado.wav', campanilla.astype(np.float32), SR)

entradas = []
filtros = []

def entrada(args):
    entradas.extend(args)
    return len([a for a in entradas if a == '-i']) - 1

# voz
voces = []
for k, t0 in VOZ.items():
    i = entrada(['-i', f'{BASE}/voz/lineas/{k}.wav'])
    filtros.append(f'[{i}]aresample={SR},adelay={int(t0 * 1000)}:all=1[{k}]')
    voces.append(f'[{k}]')
filtros.append(f"{''.join(voces)}amix=inputs={len(voces)}:normalize=0,"
               f"highpass=f=85,equalizer=f=220:t=q:w=1:g=1.5,equalizer=f=3200:t=q:w=1.2:g=2.5,"
               f"acompressor=threshold=-20dB:ratio=3:attack=8:release=120:makeup=2,apad=whole_dur={DUR},"
               f"aecho=0.85:0.6:35|70:0.10|0.05,volume=1.6,asplit=2[voz][vozsc]")

# música: filtrada y baja antes del giro, abierta después
m = entrada(['-ss', f'{MUSICA_DESDE:.3f}', '-i', f'{BASE}/musica/599.mp3'])
filtros.append(f'[{m}]aresample={SR},atrim=0:{DUR},asplit=2[ma][mb]')
filtros.append(f'[ma]atrim=0:{GIRO + 0.3},lowpass=f=650,lowpass=f=650,volume=0.55,afade=t=out:st={GIRO - 0.05}:d=0.3[mA]')
filtros.append(f'[mb]atrim={GIRO - 0.05}:{DUR},asetpts=PTS-STARTPTS,afade=t=in:st=0:d=0.12,'
               f'volume=0.75,afade=t=out:st={DUR - 2.4 - GIRO}:d=2.3,adelay={int((GIRO - 0.05) * 1000)}:all=1[mB]')
filtros.append('[mA][mB]amix=inputs=2:normalize=0[mus]')
filtros.append('[mus][vozsc]sidechaincompress=threshold=0.02:ratio=4:attack=40:release=450[musd]')

# efectos: (archivo, desde_en_archivo, dur, en_el_anuncio, volumen, filtro extra)
EF = [
    ('sfx/bell_1003.mp3', 0.12, 1.27, 0.00, 0.55, ''),
    ('sfx/bell_1003.mp3', 0.12, 1.30, 4.90, 0.30, 'lowpass=f=3000,'),
    ('sfx/bell_1003.mp3', 0.12, 0.55, 9.00, 0.32, ''),
    ('sfx/bell_1003.mp3', 0.12, 0.45, 10.42, 0.32, ''),
    ('sfx/bell_1003.mp3', 0.12, 0.50, 11.36, 0.32, ''),
    ('sfx/impact_2903.mp3', 0.0, 3.5, GIRO - 0.40, 0.55, ''),
    ('sfx/whoosh_1492.mp3', 0.0, 1.33, 30.55 - 1.10, 0.30, ''),
    ('sfx/whoosh_1492.mp3', 0.0, 1.33, 41.66 - 1.10, 0.22, ''),
    ('sfx/whoosh_1492.mp3', 0.0, 1.33, 43.92 - 1.10, 0.18, ''),
    ('sfx/encontrado.wav', 0.0, 1.2, 33.50, 0.9, ''),
]
efectos = []
for n, (f, desde, dur, en, vol, extra) in enumerate(EF):
    i = entrada(['-ss', str(desde), '-t', str(dur + 0.05), '-i', f'{BASE}/{f}'])
    filtros.append(f'[{i}]aresample={SR},aformat=channel_layouts=stereo,{extra}afade=t=out:st={dur - 0.04}:d=0.04,'
                   f'volume={vol},adelay={int(en * 1000)}:all=1[e{n}]')
    efectos.append(f'[e{n}]')

# el sonido de Alba (res/raw/amanecer.wav) mientras suena en el móvil; calla al abrir la cámara
i = entrada(['-stream_loop', '3', '-i', f'{REPO}/app/src/main/res/raw/amanecer.wav'])
dur_alba = 25.90 - GIRO
filtros.append(f'[{i}]aresample={SR},aformat=channel_layouts=stereo,atrim=0:{dur_alba},afade=t=in:st=0:d=0.05,'
               f'afade=t=out:st={dur_alba - 0.12}:d=0.12,volume=0.55,adelay={int(GIRO * 1000)}:all=1[alba]')
efectos.append('[alba]')

filtros.append(f"[voz]aformat=channel_layouts=stereo[vozst]")
filtros.append(f"[vozst][musd]{''.join(efectos)}amix=inputs={2 + len(efectos)}:normalize=0,"
               f"atrim=0:{DUR},loudnorm=I=-14:TP=-1.5:LRA=11,aresample={SR}[out]")

salida = sys.argv[1] if len(sys.argv) > 1 else f'{BASE}/sonido.wav'
cmd = ['ffmpeg', '-v', 'error', '-y'] + entradas + ['-filter_complex', ';'.join(filtros), '-map', '[out]',
                                                     '-ar', str(SR), salida]
subprocess.run(cmd, check=True)
print('ok', salida)
