# Anuncio 03, segunda versión: gancho al principio, alarma del móvil (no despertador
# antiguo), voz nueva y cortes al pulso de la música. Reutiliza las piezas de montaje.py.
import json, os, sys, subprocess, math
from PIL import Image, ImageDraw, ImageFilter
import montaje as M
from montaje import Plano, etalonar, desenfocado, captura, pantalla_foto, vista_camara, poner_movil, \
    texto_sombra, fuente, sale, suave, lerp, W, H, FPS
import alarma_generica as AG

BASE = M.BASE
VOZ2 = f'{BASE}/voz2'
TOMA = json.load(open(f'{VOZ2}/elegidas.json'))  # frase -> archivo elegido
TEXTOS = json.load(open(f'{VOZ2}/textos.json'))
PAL_WHISPER = json.load(open(f'{VOZ2}/palabras.json'))


def _norma(w):
    w = w.lower()
    for a, b in zip('áéíóú', 'aeiou'):
        w = w.replace(a, b)
    return ''.join(c for c in w if c.isalnum())


def alinear(texto, oidas):
    """Palabras del guion con los tiempos de la transcripción (aunque esta parta distinto)."""
    import difflib
    tw = texto.split()
    sm = difflib.SequenceMatcher(None, [_norma(w) for w in tw], [_norma(w) for w, _, _ in oidas])
    tiempos = [None] * len(tw)
    for a, b, n in sm.get_matching_blocks():
        for i in range(n):
            tiempos[a + i] = oidas[b + i][1:]
    # huecos: repartir entre los vecinos conocidos
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


PAL = {k: alinear(TEXTOS[k], v) for k, v in PAL_WHISPER.items()}
DUR = {k: PAL[k][-1][2] + 0.08 for k in PAL}  # hasta el final de la última palabra

PULSO = 0.845       # "Lo-Fi 04": un pulso cada 0,845 s
GOLPE_PISTA = 9.06  # donde entra la batería en la pista


def horario():
    """Cuándo empieza cada frase. Todo encadenado, con respiraciones cortas."""
    orden = ['h1', 'h2', 'v01', 'v02', 'v03', 'v04', 'v05', 'v06', 'v07', 'v08', 'v09', 'v10', 'v11', 'v12']
    pausa = {'h2': 0.30, 'v01': 0.35, 'v02': 0.30, 'v03': 0.40, 'v04': 0.35, 'v05': 0.55, 'v06': 0.35,
             'v07': 0.45, 'v08': 0.45, 'v09': 0.25, 'v10': 0.55, 'v11': 0.45, 'v12': 0.90}
    t = {'h1': 0.20}
    for a, b in zip(orden, orden[1:]):
        t[b] = t[a] + DUR[a] + pausa[b]
    return t


T = horario()
GOLPE = T['h2'] - 0.05          # entra la música justo con "Te explico"
GIRO = T['v05'] - 0.30          # aparece Alba
FIN = T['v12'] + DUR['v12'] + 1.6


def pulso(t, rejilla=0.5):
    """Ajusta un instante al pulso (o medio pulso) de la música más cercano."""
    paso = PULSO * rejilla
    k = round((t - GOLPE) / paso)
    return GOLPE + k * paso


def palabra(frase, n):
    """Instante (en el anuncio) en que empieza la palabra n de una frase."""
    return T[frase] + PAL[frase][n][1]


def fin_frase(frase):
    return T[frase] + DUR[frase]


class Subtitulos:
    def __init__(self, cortes):
        self.trozos = []
        for clave, ns in cortes.items():
            ws = PAL[clave]
            orig = TEXTOS[clave].split()
            if len(orig) == len(ws):
                ws = [(o, a, b) for o, (_, a, b) in zip(orig, ws)]
            ws = [((w[0].upper() + w[1:]) if k == 0 else w, a, b) for k, (w, a, b) in enumerate(ws)]
            i = 0
            for n in ns:
                self.trozos.append([(w, T[clave] + a, T[clave] + b) for (w, a, b) in ws[i:i + n]])
                i += n
            if i != len(ws):  # si la transcripción parte distinto, lo que sobre va en el último trozo
                self.trozos[-1] += [(w, T[clave] + a, T[clave] + b) for (w, a, b) in ws[i:]]

    def dibujar(self, im, t, y):
        for k, tr in enumerate(self.trozos):
            ini = tr[0][1] - 0.05
            fin = tr[-1][2] + 0.35
            if k + 1 < len(self.trozos):
                fin = min(fin, self.trozos[k + 1][0][1] - 0.05)
            if ini <= t < fin:
                frase = ' '.join(w for w, _, _ in tr)
                d = ImageDraw.Draw(im)
                x = W / 2 - d.textlength(frase, font=M.F_SUB) / 2
                entrada = sale((t - ini) / 0.12)
                for w, a, b in tr:
                    alfa = 255 if t >= a - 0.02 else 110
                    texto_sombra(im, (x, y + 10 * (1 - entrada)), w, M.F_SUB, alfa=int(alfa * entrada), ancla='lm')
                    x += d.textlength(w + ' ', font=M.F_SUB)
                return


def cortes_por_frase():
    """Trozos de subtítulo (número de palabras de cada uno), a mano para que se lean bien."""
    return {'h2': [2], 'v01': [2, 3, 4], 'v02': [3, 3, 3], 'v03': [7, 7], 'v04': [7], 'v05': [2, 5, 5, 4],
            'v06': [3, 3, 4], 'v07': [5, 5], 'v08': [6, 3, 6], 'v09': [2], 'v10': [3, 6, 5, 5], 'v11': [3, 6]}


def escenas():
    E = []

    def plano(t0, t1, p, tono, ysub=1400):
        def f(t, tg):
            return etalonar(p.cuadro(t), tono), ysub
        E.append((t0, t1, f, p))

    # ---------- GANCHO: el móvil sonando, la cámara ve la taza, "Buenos días"
    taza_g = Plano('808', 1.0, 3.5, completo=True)
    fondo_g = Plano('808', 1.0, 3.5, cx=(1900, 1900))
    reconoce = fin_frase('h1') - 0.25
    titulo = fuente('InterDisplay-Bold', 76)

    def gancho(t, tg):
        fondo = desenfocado(etalonar(fondo_g.cuadro(t), 'calido'), 0.42)
        vista = etalonar(vista_camara(taza_g, t, desplaz=lerp(0.12, 0.02, sale(t / 0.8)), zoom=1.08), 'calido')
        pant = pantalla_foto('sonando', vista)
        cambio = suave((tg - reconoce) / 0.25)
        if cambio > 0:
            pant = Image.blend(pant, captura('anuncio_hecha'), cambio)
        ancho = 600 * lerp(1.10, 1.0, sale(t / 1.2))
        poner_movil(fondo, pant, ancho, (W / 2, 1120))
        for i, linea in enumerate(['Mi alarma no se apaga', 'hasta que le enseño', 'una taza']):
            texto_sombra(fondo, (W / 2, 190 + i * 92), linea, titulo, sombra=190)
        return fondo, None
    E.append((0.0, GOLPE, gancho, [taza_g, fondo_g]))

    # ---------- ANTES: la alarma del móvil y el botón de posponer
    c1 = pulso(palabra('v01', 2))           # "posponía"
    c2 = pulso(palabra('v01', 5))           # "sin abrir los ojos"
    plano(GOLPE, c1, Plano('50931', 0.3, c1 - GOLPE, cx=(2250, 2250), z=(1.0, 1.08)), 'frio')

    fondo_cama = Plano('50752', 6.5, 12.0, cx=(2100, 2100))

    def alarma_ui(hora, t_toque, zoom=(1.0, 1.06)):
        def f(t, tg):
            fondo = desenfocado(etalonar(fondo_cama.cuadro(min(t, 5)), 'frio'), 0.35)
            pant = AG.pantalla(hora, (tg - t_toque) if tg >= t_toque else None)
            ancho = 600 * lerp(zoom[0], zoom[1], t / 1.5)
            return poner_movil(fondo, pant, ancho, (W / 2, 800)), 1590
        return f
    E.append((c1, c2, alarma_ui('07:00', c2 - 0.45), [fondo_cama]))
    # la mano que lo pospone (sin cara)
    mano = Plano('39786', 4.2, 2.0, cx=(2450, 2350), z=(1.4, 1.5), cy=0.68)
    fin_v01 = pulso(fin_frase('v01') + 0.15)
    plano(c2, fin_v01, mano, 'frio')

    # "Cinco minutos más. / Y otros cinco. / Y otros cinco."
    p1 = fin_v01
    p2 = pulso(palabra('v02', 3))
    p3 = pulso(palabra('v02', 6))
    p4 = pulso(fin_frase('v02') + 0.25)
    fondo_cama2 = Plano('50752', 7.0, 12.0, cx=(2100, 2100))

    def alarma_ui2(hora, t_toque):
        def f(t, tg):
            fondo = desenfocado(etalonar(fondo_cama2.cuadro(min(t, 5)), 'frio'), 0.35)
            pant = AG.pantalla(hora, (tg - t_toque) if tg >= t_toque else None)
            return poner_movil(fondo, pant, 640, (W / 2, 800)), 1590
        return f
    E.append((p1, p2, alarma_ui2('07:05', p1 + 0.35), [fondo_cama2]))
    fondo_cama3 = Plano('50752', 8.0, 12.0, cx=(2100, 2100))

    def alarma_ui3(t, tg):
        fondo = desenfocado(etalonar(fondo_cama3.cuadro(min(t, 5)), 'frio'), 0.35)
        pant = AG.pantalla('07:10', (tg - p2 - 0.3) if tg >= p2 + 0.3 else None)
        return poner_movil(fondo, pant, 680, (W / 2, 820)), 1590
    E.append((p2, p3, alarma_ui3, [fondo_cama3]))
    plano(p3, p4, Plano('39786', 8.3, p4 - p3, cx=(1450, 1550), z=(1.0, 1.05)), 'frio')  # almohada a la cabeza

    # "Y el problema no era la alarma. Es que se apaga desde la cama."
    p5 = pulso(fin_frase('v03') + 0.2)
    plano(p4, p5, Plano('50754', 0.0, p5 - p4, cx=(2200, 2300), z=(1.0, 1.1)), 'frio')
    plano(p5, GIRO, Plano('50757', 0.0, GIRO - p5, cx=(2150, 2150), z=(1.0, 1.06)), 'frio')

    # ---------- ALBA SUENA
    g2 = pulso(palabra('v05', 7))  # "Me pide que le enseñe..."
    fondo_m1 = Plano('50752', 6.0, 8.0, cx=(2100, 2150))

    def m1(t, tg):
        fondo = desenfocado(etalonar(fondo_m1.cuadro(t * 0.9), 'calido'), 0.5)
        ancho = 640 * lerp(1.08, 1.0, sale(t / 0.5))
        z = suave((tg - g2) / 1.1)
        ancho *= lerp(1.0, 1.5, z)
        return poner_movil(fondo, captura('anuncio_sonando'), ancho, (W / 2, lerp(790, 1060, z)), (0.5, lerp(0.5, 0.92, z))), 1590
    fin_m1 = pulso(T['v06'] - 0.1)
    E.append((GIRO, fin_m1, m1, fondo_m1))

    # "Hoy, una taza." (de cerca a lejos)  /  "Y las tazas… están en la cocina."
    c_tazas = pulso(palabra('v06', 3))
    fondo_m2 = Plano('50754', 2.0, 4.0, cx=(2200, 2200))
    cam_cama = Plano('50752', 3.0, 4.0, completo=True)

    def m2(t, tg):
        fondo = desenfocado(etalonar(fondo_m2.cuadro(t), 'calido'), 0.5)
        vista = etalonar(vista_camara(cam_cama, t, desplaz=-0.1 + 0.05 * t), 'calido')
        pant = pantalla_foto('buscando', vista.filter(ImageFilter.GaussianBlur(3)))
        z = 1 - sale(t / 1.6)
        return poner_movil(fondo, pant, 640 * lerp(1.0, 1.9, z), (W / 2, lerp(790, 620, z)), (0.5, lerp(0.5, 0.045, z))), 1590
    E.append((fin_m1, c_tazas, m2, [fondo_m2, cam_cama]))
    c_pies = pulso(palabra('v06', 6))
    fin_v06 = pulso(fin_frase('v06') + 0.2)
    plano(c_tazas, c_pies, Plano('50754', 10.5, c_pies - c_tazas, cx=(2750, 2800), z=(1.05, 1.1)), 'calido')
    plano(c_pies, fin_v06, Plano('50764', 0.0, fin_v06 - c_pies, cx=(1400, 1500), z=(1.0, 1.05)), 'calido')

    # "La apuntas con la cámara, la reconoce… y se calla."
    viendo = palabra('v07', 6)       # "reconoce"
    calla = palabra('v07', 9) - 0.1  # "calla"
    fin_v07 = pulso(fin_frase('v07') + 0.35)
    taza = Plano('808', 0.0, 6.0, completo=True)
    fondo_coc = Plano('50768', 0.0, 6.0, cx=(1950, 1950))

    def m3(t, tg):
        fondo = desenfocado(etalonar(fondo_coc.cuadro(t), 'calido'), 0.5)
        vista = etalonar(vista_camara(taza, t, desplaz=lerp(0.28, 0.02, sale(t / 1.6)), zoom=1.05), 'calido')
        pant = pantalla_foto('buscando' if tg < viendo else 'viendo', vista)
        cambio = suave((tg - calla) / 0.3)
        if cambio > 0:
            pant = Image.blend(pant, captura('anuncio_hecha'), cambio)
        return poner_movil(fondo, pant, 640 * lerp(1.04, 1.0, sale(t / 0.4)), (W / 2, 790)), 1590
    E.append((fin_v06, fin_v07, m3, [taza, fondo_coc]))

    # "Y ya que estás de pie, en la cocina, con una taza en la mano…"  "pues café."
    c_cafe = pulso(T['v09'] - 0.05)
    fin_cafe = pulso(fin_frase('v09') + 0.5)
    plano(fin_v07, c_cafe, Plano('50768', 0.0, c_cafe - fin_v07, cx=(1950, 1950), z=(1.0, 1.06)), 'calido')
    plano(c_cafe, fin_cafe, Plano('50770', 1.0, fin_cafe - c_cafe, cx=(2000, 2000), z=(1.06, 1.1)), 'calido')

    # Ventajas
    c_cam = pulso(palabra('v10', 3))    # "Solo vale la cámara"
    c_diez = pulso(palabra('v10', 9))   # "Y a los diez minutos"
    fin_v10 = pulso(fin_frase('v10') + 0.3)
    plano(fin_cafe, c_cam, Plano('50771', 2.0, c_cam - fin_cafe, cx=(2100, 2100), z=(1.05, 1.1)), 'calido')
    taza2 = Plano('808', 4.0, 4.0, completo=True)
    taza2b = Plano('808', 4.0, 4.0, cx=(1900, 1900))

    def m4(t, tg):
        fondo = desenfocado(etalonar(taza2b.cuadro(t), 'calido'), 0.45)
        vista = etalonar(vista_camara(taza2, t, desplaz=0.02, zoom=1.05), 'calido')
        return poner_movil(fondo, pantalla_foto('viendo', vista), 640 * lerp(1.0, 1.05, t / 2.3), (W / 2, 790)), 1590
    E.append((c_cam, c_diez, m4, [taza2, taza2b]))
    fondo_c2 = Plano('50771', 5.0, 5.0, cx=(2100, 2100))

    def m5(t, tg):
        fondo = desenfocado(etalonar(fondo_c2.cuadro(t), 'calido'), 0.5)
        return poner_movil(fondo, captura('anuncio_comprobacion'), 640 * lerp(1.04, 1.0, sale(t / 0.4)), (W / 2, 790)), 1590
    E.append((c_diez, fin_v10, m5, fondo_c2))

    # "Una sola alarma. Y en pie a la primera."  y cierre
    fin_v11 = pulso(fin_frase('v11') + 0.5)
    plano(fin_v10, fin_v11, Plano('50760', 0.8, fin_v11 - fin_v10, cx=(1920, 1920), z=(1.0, 1.08)), 'calido')
    azotea = Plano('50759', 2.0, 3.0, cx=(2000, 2000), z=(1.0, 1.04))
    negro_desde = fin_v11 + 0.8

    def fin(t, tg):
        negro = Image.new('RGB', (W, H), (0, 0, 0))
        if tg < negro_desde:
            im = etalonar(azotea.cuadro(t), 'calido')
            return Image.blend(im, negro, suave((tg - fin_v11) / 0.8)), None
        return M.cierre(tg - negro_desde, negro), None
    E.append((fin_v11, FIN, fin, azotea))
    return E


def main():
    salida = sys.argv[1] if len(sys.argv) > 1 else f'{BASE}/imagen2.mp4'
    solo = [float(x) for x in sys.argv[2:]]
    E = escenas()
    subs = Subtitulos(cortes_por_frase())
    if solo:
        for tg in solo:
            for (a, b, f, _) in E:
                if a <= tg < b:
                    im, y = f(tg - a, tg)
                    if y:
                        subs.dibujar(im, tg, y)
                    im.save(f'{BASE}/c2_{tg:05.2f}.png')
        return
    enc = subprocess.Popen(['ffmpeg', '-v', 'error', '-y', '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-s', f'{W}x{H}',
                            '-r', str(FPS), '-i', '-', '-vf', 'vignette=angle=0.45',
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
