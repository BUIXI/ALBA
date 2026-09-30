# Vídeo 04: "La alarma más odiada del mundo". Los que hacemos Alba hablando claro:
# gancho provocador, para quién es (los que posponen), lo que nos diferencia (hay que
# levantarse) con un montaje de objetos al ritmo, y cierre con llamada a comentar.
# Reutiliza las piezas de montaje.py; los cortes caen en los golpes de la música.
import functools, json, os, subprocess, sys
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
import montaje as M
from montaje import Plano, etalonar, desenfocado, captura, poner_movil, texto_sombra, fuente, sale, suave, lerp, W, H, FPS
import alarma_generica as AG

BASE = M.BASE
DV = f'{BASE}/v4/voz'
TEXTOS = json.load(open(f'{DV}/textos.json'))
TOMA = json.load(open(f'{DV}/elegidas.json'))
PAL_OIDAS = json.load(open(f'{DV}/palabras.json'))
NARANJA = M.NARANJA

# ---------------------------------------------------------------- música y golpes
MUSICA = f'{BASE}/musica/699.mp3'   # "Pop 07": Fa mayor, 90 bpm, mucha percusión
MUSICA_DESDE = 0.60                  # el primer golpe cae casi en el fotograma 0


@functools.lru_cache(None)
def golpes():
    cache = f'{BASE}/v4/golpes_699.json'
    if not os.path.exists(cache):
        import librosa
        y, sr = librosa.load(MUSICA, sr=22050)
        _, b = librosa.beat.beat_track(y=y, sr=sr, start_bpm=89)
        json.dump([float(x) for x in librosa.frames_to_time(b, sr=sr)], open(cache, 'w'))
    return [g - MUSICA_DESDE for g in json.load(open(cache)) if g >= MUSICA_DESDE]


def al_golpe(t, medio=True):
    """El golpe (o medio golpe) de la música más cercano a t."""
    g = golpes()
    candidatos = g + ([(a + b) / 2 for a, b in zip(g, g[1:])] if medio else [])
    return min(candidatos, key=lambda x: abs(x - t))


def siguiente_golpe(t):
    return min(x for x in golpes() if x >= t - 0.02)

# ---------------------------------------------------------------- voz y horario


def _norma(w):
    w = w.lower()
    for a, b in zip('áéíóú', 'aeiou'):
        w = w.replace(a, b)
    return ''.join(c for c in w if c.isalnum())


def alinear(texto, oidas):
    """Palabras del guion con los tiempos de la transcripción."""
    import difflib
    tw = texto.split()
    if not oidas:
        return [(w, 0.0, 0.3) for w in tw]
    sm = difflib.SequenceMatcher(None, [_norma(w) for w in tw], [_norma(w) for w, _, _ in oidas])
    tiempos = [None] * len(tw)
    for a, b, n in sm.get_matching_blocks():
        for i in range(n):
            tiempos[a + i] = tuple(oidas[b + i][1:])
    conocidos = [i for i, x in enumerate(tiempos) if x]
    for i in range(len(tw)):
        if tiempos[i] is None:
            antes = max([j for j in conocidos if j < i], default=None)
            despues = min([j for j in conocidos if j > i], default=None)
            ini = tiempos[antes][1] if antes is not None else 0.0
            fin = tiempos[despues][0] if despues is not None else oidas[-1][2]
            huecos = [j for j in range(len(tw)) if (antes is None or j > antes) and (despues is None or j < despues)]
            paso = (fin - ini) / len(huecos)
            k = huecos.index(i)
            tiempos[i] = (ini + k * paso, ini + (k + 1) * paso)
    return [(w, a, b) for w, (a, b) in zip(tw, tiempos)]


PAL = {k: alinear(TEXTOS[k], v) for k, v in PAL_OIDAS.items()}
DUR = {k: PAL[k][-1][2] + 0.08 for k in PAL}
ORDEN = ['h1', 'h2', 'p1', 'p2', 'p3', 'm1', 'm2', 'm3', 'o1', 'o2', 'o3', 'o4', 'm5', 'm6', 'c1', 'c2', 'c3']
PAUSA = {'h2': 0.25, 'p1': 0.35, 'p2': 0.12, 'p3': 0.12, 'm1': 0.45, 'm2': 0.30, 'm3': 0.20, 'o1': 0.25,
         'o2': 0.10, 'o3': 0.10, 'o4': 0.10, 'm5': 0.40, 'm6': 0.30, 'c1': 0.50, 'c2': 0.30, 'c3': 0.70}


def horario():
    t = {'h1': 0.15}
    for a, b in zip(ORDEN, ORDEN[1:]):
        t[b] = t[a] + DUR[a] + PAUSA[b]
        if b.startswith('o'):          # los objetos, cada uno en un golpe
            t[b] = siguiente_golpe(t[b])
        if b == 'm5':                  # antes, la tele y el "Buenos días" sin voz (1 golpe y ~1,5 s)
            t_tele = siguiente_golpe(t['o4'] + DUR['o4'] + 0.05)
            t_hecho = siguiente_golpe(t_tele + 0.3)
            t[b] = max(t[b], t_hecho + 1.4)
    return t


T = horario()
FIN = T['c3'] + DUR['c3'] + 2.2


def palabra(frase, n):
    return T[frase] + PAL[frase][n][1]


def fin_frase(frase):
    return T[frase] + DUR[frase]

# ---------------------------------------------------------------- pantallas de la app


@functools.lru_cache(None)
def matte_de(prefijo):
    negro = np.asarray(captura(f'{prefijo}_negro')).astype(np.float32)
    blanco = np.asarray(captura(f'{prefijo}_blanco')).astype(np.float32)
    pasa = np.clip((blanco - negro).mean(axis=2, keepdims=True) / 255, 0, 1)
    ys, xs = np.where(pasa[..., 0] > 0.5)
    return negro, pasa, (xs.min(), ys.min(), xs.max() + 1, ys.max() + 1)


def pantalla_con_camara(prefijo, camara):
    negro, pasa, (x0, y0, x1, y1) = matte_de(prefijo)
    lienzo = np.zeros_like(negro)
    lienzo[y0:y1, x0:x1] = np.asarray(camara.resize((x1 - x0, y1 - y0), Image.LANCZOS)).astype(np.float32)
    return Image.fromarray(np.clip(negro + pasa * lienzo, 0, 255).astype(np.uint8))


def vista(plano, t, desplaz=0.0, zoom=1.0, temblor=1.0, prefijo='anuncio_foto_buscando'):
    """Lo que ve la cámara: el plano recortado a la forma del hueco, con pulso de mano."""
    _, _, (x0, y0, x1, y1) = matte_de(prefijo)
    asp = (x1 - x0) / (y1 - y0)
    if plano.proc is None:
        plano._abrir()
    while plano.i < int(t * FPS):
        plano._leer()
        plano.i += 1
    src = Image.fromarray(plano.ultimo)
    sw, sh = src.size
    ch = sh / zoom
    cw = min(ch * asp, sw)
    ch = cw / asp
    tx = temblor * (14 * np.sin(t * 2.1) + 8 * np.sin(t * 5.3 + 1))
    ty = temblor * (10 * np.sin(t * 1.7 + 2) + 6 * np.sin(t * 4.1))
    cx = sw * (0.5 + desplaz) + tx * sw / 3840
    cy = sh * 0.5 + ty * sh / 2160
    x = min(max(cx - cw / 2, 0), sw - cw)
    y = min(max(cy - ch / 2, 0), sh - ch)
    return src.crop((int(x), int(y), int(x + cw), int(y + ch)))

# ---------------------------------------------------------------- textos en pantalla

F_SUB = fuente('InterDisplay-Bold', 62)
F_TITULO = fuente('InterDisplay-Bold', 84)
F_HORA = fuente('Inter-ExtraLight', 230)


class Subtitulos:
    """Frases cortas; la palabra que se está diciendo, en naranja (como el acento de la app)."""

    def __init__(self, cortes):
        self.trozos = []
        for clave, ns in cortes.items():
            ws = [((w[0].upper() + w[1:]) if k == 0 else w, a, b) for k, (w, a, b) in enumerate(PAL[clave])]
            i = 0
            for n in ns:
                self.trozos.append([(w, T[clave] + a, T[clave] + b) for (w, a, b) in ws[i:i + n]])
                i += n
            if i < len(ws):
                self.trozos[-1] += [(w, T[clave] + a, T[clave] + b) for (w, a, b) in ws[i:]]

    def dibujar(self, im, t, y):
        for k, tr in enumerate(self.trozos):
            ini = tr[0][1] - 0.04
            fin = tr[-1][2] + 0.3
            if k + 1 < len(self.trozos):
                fin = min(fin, self.trozos[k + 1][0][1] - 0.04)
            if ini <= t < fin:
                d = ImageDraw.Draw(im)
                frase = ' '.join(w for w, _, _ in tr)
                x = W / 2 - d.textlength(frase, font=F_SUB) / 2
                u = sale((t - ini) / 0.10)
                esc_y = y + 14 * (1 - u)
                for w, a, b in tr:
                    ahora = a - 0.02 <= t < b + 0.06
                    color = NARANJA if ahora else (255, 255, 255)
                    texto_sombra(im, (x, esc_y), w, F_SUB, color=color, alfa=int(255 * u), ancla='lm', sombra=190)
                    x += d.textlength(w + ' ', font=F_SUB)
                return


CORTES = {'h2': [3, 3], 'p1': [3, 4], 'p2': [3, 3], 'p3': [4, 5], 'm1': [3, 4], 'm2': [2, 3], 'm3': [5, 5],
          'o1': [2], 'o2': [2], 'o3': [2], 'o4': [2], 'm5': [5, 3], 'm6': [4, 5, 5], 'c1': [3, 4], 'c2': [1, 4]}

# ---------------------------------------------------------------- cierre


def cierre4(t, fondo):
    im = M.cierre(t, fondo)  # sol que sale, "Alba" y la frase de siempre
    v = sale((t - 2.4) / 0.6)
    if v > 0:
        d = ImageDraw.Draw(im, 'RGBA')
        f = fuente('InterDisplay-Bold', 50)
        txt = 'Comenta «ALBA» y te avisamos'
        tw = d.textlength(txt, font=f)
        y = 1640
        d.rounded_rectangle((W / 2 - tw / 2 - 44, y - 58, W / 2 + tw / 2 + 44, y + 58), 58, fill=NARANJA + (int(255 * v),))
        d.text((W / 2, y), txt, font=f, fill=(0, 0, 0, int(255 * v)), anchor='mm')
    return im

# ---------------------------------------------------------------- escenas


def escenas():
    E = []

    def plano(t0, t1, p, tono, ysub=1380, extra=None):
        def f(t, tg):
            im = etalonar(p.cuadro(t), tono)
            if extra:
                extra(im, t, tg)
            return im, ysub
        E.append((t0, t1, f, p))

    def punch(t, fuerza=0.06, dur=0.22):
        return 1 + fuerza * (1 - sale(t / dur))

    # --- GANCHO: suena y te pide un fregadero; tú, con la almohada en la cabeza
    c_a = al_golpe(palabra('h1', 3))                 # "alarma"
    fin_h1 = al_golpe(fin_frase('h1') + 0.1)
    cam_cama = Plano('50752', 2.0, 4.0, completo=True)
    fondo_g = Plano('50752', 6.5, 4.0, cx=(2100, 2100))

    def titulo(im, alfa=255):
        for i, l in enumerate(['Hemos hecho la alarma', 'más odiada del mundo']):
            texto_sombra(im, (W / 2, 190 + i * 100), l, F_TITULO, alfa=alfa, sombra=210)

    def gancho(t, tg):
        fondo = desenfocado(etalonar(fondo_g.cuadro(t), 'frio'), 0.4)
        cam = etalonar(vista(cam_cama, t, desplaz=-0.1, zoom=1.0, temblor=2.0), 'frio').filter(ImageFilter.GaussianBlur(2))
        pant = pantalla_con_camara('anuncio_fregadero_sonando', cam)
        vibra = 7 * np.sin(tg * 90) if (tg % 0.7) < 0.35 else 0   # el móvil vibrando
        poner_movil(fondo, pant, 640 * punch(t, 0.08, 0.3), (W / 2 + vibra, 1130))
        titulo(fondo)
        return fondo, None
    E.append((0.0, c_a, gancho, [cam_cama, fondo_g]))
    almohada = Plano('39786', 8.2, fin_h1 - c_a + 0.5, cx=(1450, 1550), z=(1.05, 1.12))
    plano(c_a, fin_h1, almohada, 'frio', ysub=None, extra=lambda im, t, tg: titulo(im))

    # "Y lo hemos hecho a propósito."
    fin_h2 = al_golpe(fin_frase('h2') + 0.2)
    plano(fin_h1, fin_h2, Plano('50931', 2.6, fin_h2 - fin_h1, cx=(2250, 2250), z=(1.12, 1.2)), 'frio')

    # --- PARA QUIÉN: siete alarmas, apagarla dormido, cinco minutos que son una hora
    fin_p1 = al_golpe(fin_frase('p1') + 0.1)
    fondo_l = Plano('50752', 7.0, 4.0, cx=(2100, 2100))
    t_filas = [T['p1'] + 0.1 + i * 0.3 for i in range(7)]

    def siete(t, tg):
        fondo = desenfocado(etalonar(fondo_l.cuadro(t), 'frio'), 0.35)
        vis = sum(1 for x in t_filas if tg >= x) or 1
        ult = tg - t_filas[vis - 1] if vis <= len(t_filas) else 1
        return poner_movil(fondo, AG.lista(vis, ult), 640 * punch(t), (W / 2, 800)), 1600
    E.append((fin_h2, fin_p1, siete, fondo_l))
    fin_p2 = al_golpe(fin_frase('p2') + 0.1)
    plano(fin_p1, fin_p2, Plano('39786', 4.2, fin_p2 - fin_p1, cx=(2450, 2350), z=(1.4, 1.5), cy=0.68), 'frio')
    fin_p3 = al_golpe(fin_frase('p3') + 0.2)
    ini_hora, fin_hora = palabra('p3', 3), fin_frase('p3')

    def contador(im, t, tg):
        u = suave((tg - ini_hora) / max(0.5, fin_hora - ini_hora))
        minutos = int(round(lerp(5, 65, u)))
        h, m = divmod(7 * 60 + minutos, 60)
        texto_sombra(im, (W / 2, 360), f'{h:02d}:{m:02d}', F_HORA, sombra=190)
    plano(fin_p2, fin_p3, Plano('50752', 7.5, fin_p3 - fin_p2, cx=(2100, 2100), z=(1.05, 1.12)), 'frio', extra=contador)

    # --- ALBA: sin posponer; hay que levantarse
    fin_m1 = al_golpe(fin_frase('m1') + 0.15)
    fondo_m1 = Plano('50752', 6.0, 5.0, cx=(2100, 2150))
    z_boton = palabra('m1', 3)

    def m1(t, tg):
        fondo = desenfocado(etalonar(fondo_m1.cuadro(t), 'calido'), 0.5)
        z = suave((tg - z_boton) / 0.8)
        ancho = 640 * punch(t) * lerp(1.0, 1.45, z)
        return poner_movil(fondo, captura('anuncio_sonando'), ancho, (W / 2, lerp(800, 1000, z)), (0.5, lerp(0.5, 0.9, z))), 1600
    E.append((fin_p3, fin_m1, m1, fondo_m1))
    fin_m2 = al_golpe(fin_frase('m2') + 0.1)
    plano(fin_m1, fin_m2, Plano('50754', 9.4, fin_m2 - fin_m1, cx=(2700, 2800), z=(1.0, 1.08)), 'calido')
    c_cam = al_golpe(palabra('m3', 5))    # "la cámara"
    plano(fin_m2, c_cam, Plano('50764', 0.4, c_cam - fin_m2, cx=(1400, 1500), z=(1.0, 1.05)), 'calido')
    cocina = Plano('3110', 1.0, 4.0, completo=True)   # barre un salón vacío buscando
    fondo_c = Plano('50768', 1.0, 4.0, cx=(1950, 1950))

    def buscando(t, tg):
        fondo = desenfocado(etalonar(fondo_c.cuadro(t), 'calido'), 0.45)
        cam = etalonar(vista(cocina, t, desplaz=0.3 - 0.25 * t, zoom=1.1, temblor=2.5), 'calido').filter(ImageFilter.GaussianBlur(2))
        return poner_movil(fondo, pantalla_con_camara('anuncio_foto_buscando', cam), 640 * punch(t), (W / 2, 800)), 1600
    E.append((c_cam, T['o1'], buscando, [cocina, fondo_c]))

    # --- OBJETOS al ritmo: fregadero, taza, sofá, planta (y la tele, de propina)
    objetos = [('o1', 'anuncio_objeto_fregadero', '3891', 0.0), ('o2', 'anuncio_foto_viendo', '808', 0.02),
               ('o3', 'anuncio_objeto_sofa', '3110', 0.0), ('o4', 'anuncio_objeto_planta', '51044', 0.0)]
    t_tele = siguiente_golpe(fin_frase('o4') + 0.05)
    t_hecho = siguiente_golpe(t_tele + 0.3)
    limites = [T['o1'], T['o2'], T['o3'], T['o4'], t_tele, t_hecho]
    for i, (clave, prefijo, clip, desp) in enumerate(objetos):
        cam_o = Plano(clip, 1.0, 3.0, completo=True)

        def obj(t, tg, cam_o=cam_o, prefijo=prefijo, desp=desp):
            fondo = desenfocado(etalonar(vista(cam_o, t, desplaz=desp, temblor=0.5), 'calido').resize((W, H)), 0.45)
            cam = etalonar(vista(cam_o, t, desplaz=desp, zoom=1.05, temblor=1.2, prefijo=prefijo), 'calido')
            return poner_movil(fondo, pantalla_con_camara(prefijo, cam), 700 * punch(t, 0.07, 0.18), (W / 2, 820)), 1640
        E.append((limites[i], limites[i + 1], obj, cam_o))
    cam_tv = Plano('46291', 2.0, 2.0, completo=True)

    def tele(t, tg):
        fondo = desenfocado(etalonar(vista(cam_tv, t, temblor=0.5), 'calido').resize((W, H)), 0.45)
        cam = etalonar(vista(cam_tv, t, zoom=1.1, temblor=1.2, prefijo='anuncio_objeto_tele'), 'calido')
        return poner_movil(fondo, pantalla_con_camara('anuncio_objeto_tele', cam), 700 * punch(t, 0.07, 0.18), (W / 2, 820)), 1640
    E.append((t_tele, t_hecho, tele, cam_tv))
    fin_hecho = al_golpe(T['m5'] - 0.05)
    fondo_h = Plano('50768', 4.0, 3.0, cx=(1950, 1950))

    def hecho(t, tg):
        fondo = desenfocado(etalonar(fondo_h.cuadro(t), 'calido'), 0.5)
        return poner_movil(fondo, captura('anuncio_hecha'), 700 * punch(t, 0.05, 0.25), (W / 2, 820)), 1640
    E.append((t_hecho, fin_hecho, hecho, fondo_h))

    # --- Si no lo encuentras, vuelve a sonar. Si vuelves a la cama, te pregunta.
    fin_m5 = al_golpe(fin_frase('m5') + 0.15)
    pasillo = Plano('3110', 5.0, 3.0, completo=True)
    fondo_p = Plano('50764', 0.4, 3.0, cx=(1500, 1500))

    def m5(t, tg):
        fondo = desenfocado(etalonar(fondo_p.cuadro(t), 'calido'), 0.45)
        cam = etalonar(vista(pasillo, t, desplaz=0.1 * np.sin(t * 2), zoom=1.1, temblor=3.0), 'calido').filter(ImageFilter.GaussianBlur(3))
        vibra = 7 * np.sin(tg * 90) if (tg % 0.7) < 0.35 else 0
        return poner_movil(fondo, pantalla_con_camara('anuncio_fregadero_sonando', cam), 640 * punch(t), (W / 2 + vibra, 800)), 1600
    E.append((fin_hecho, fin_m5, m5, [pasillo, fondo_p]))
    c_diez = al_golpe(palabra('m6', 5))   # "a los diez minutos"
    fin_m6 = al_golpe(fin_frase('m6') + 0.2)
    plano(fin_m5, c_diez, Plano('50931', 0.3, c_diez - fin_m5, cx=(2250, 2250), z=(1.0, 1.06)), 'frio')
    fondo_q = Plano('50754', 0.5, 4.0, cx=(2200, 2200))

    def pregunta(t, tg):
        fondo = desenfocado(etalonar(fondo_q.cuadro(t), 'calido'), 0.45)
        return poner_movil(fondo, captura('anuncio_comprobacion'), 640 * punch(t), (W / 2, 800)), 1600
    E.append((c_diez, fin_m6, pregunta, fondo_q))

    # --- La vas a odiar. Después, nos darás las gracias.
    fin_c1 = al_golpe(fin_frase('c1') + 0.2)
    plano(fin_m6, fin_c1, Plano('50757', 4.5, fin_c1 - fin_m6, cx=(2150, 2150), z=(1.0, 1.06)), 'frio')
    fin_c2 = al_golpe(fin_frase('c2') + 0.35)
    plano(fin_c1, fin_c2, Plano('50760', 0.8, fin_c2 - fin_c1, cx=(1920, 1920), z=(1.0, 1.08)), 'calido')
    negro = Image.new('RGB', (W, H), (0, 0, 0))
    E.append((fin_c2, FIN, lambda t, tg: (cierre4(t, negro.copy()), None), []))
    return E


def main():
    salida = sys.argv[1] if len(sys.argv) > 1 else f'{BASE}/v4/imagen4.mp4'
    solo = [float(x) for x in sys.argv[2:]]
    E = escenas()
    subs = Subtitulos(CORTES)
    if solo:
        for tg in solo:
            for (a, b, f, _) in E:
                if a <= tg < b:
                    im, y = f(tg - a, tg)
                    if y:
                        subs.dibujar(im, tg, y)
                    im.save(f'{BASE}/v4/c4_{tg:05.2f}.png')
        return
    enc = subprocess.Popen(['ffmpeg', '-v', 'error', '-y', '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-s', f'{W}x{H}',
                            '-r', str(FPS), '-i', '-', '-vf', 'vignette=angle=0.4',
                            '-c:v', 'libx264', '-preset', 'medium', '-crf', '16', '-pix_fmt', 'yuv420p', salida],
                           stdin=subprocess.PIPE)
    actual = None
    for i in range(int(FIN * FPS)):
        tg = i / FPS
        for idx, (a, b, f, rec) in enumerate(E):
            if a <= tg < b:
                if actual is not None and actual != idx:
                    for r in (E[actual][3] if isinstance(E[actual][3], list) else [E[actual][3]]):
                        r.cerrar()
                actual = idx
                im, y = f(tg - a, tg)
                break
        if y:
            subs.dibujar(im, tg, y)
        enc.stdin.write(im.tobytes())
        if i % 96 == 0:
            print(f'{tg:5.1f}s', flush=True)
    enc.stdin.close()
    enc.wait()


if __name__ == '__main__':
    main()
