# Montaje del anuncio 03 de Alba: "A la primera".
# Vertical 1080x1920 a 24 fps. Lee los planos 4K de Mixkit, las capturas reales de la
# app (Roborazzi) y la voz, y escribe el vídeo sin sonido; el sonido se mezcla aparte.
import json, os, subprocess, sys, math, functools
import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont

W, H, FPS = 1080, 1920, 24
BASE = os.environ.get('ANUNCIO', os.path.expanduser('~/anuncio'))
REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
CLIPS = f'{BASE}/clips'
CAPT = f'{REPO}/app/build/outputs/roborazzi'
FUENTES = '/usr/share/fonts/opentype/inter'
NARANJA = (255, 159, 10)
DURACION = 56.5

def fuente(nombre, tam):
    return ImageFont.truetype(f'{FUENTES}/{nombre}.otf', tam)

def suave(u):  # easing in-out
    u = min(max(u, 0.0), 1.0)
    return u * u * (3 - 2 * u)

def sale(u):  # ease-out cúbico
    u = min(max(u, 0.0), 1.0)
    return 1 - (1 - u) ** 3

def lerp(a, b, u):
    return a + (b - a) * u

# ---------------------------------------------------------------- planos de vídeo

class Plano:
    """Un trozo de un clip 4K recortado en vertical, con paneo y zoom animados."""

    def __init__(self, clip, desde, dur, cx=(1920, 1920), z=(1.0, 1.0), cy=0.5, velocidad=1.0, completo=False):
        self.clip, self.desde, self.dur = clip, desde, dur
        self.completo = completo
        self.cx, self.z, self.cy, self.vel = cx, z, cy, velocidad
        self.proc = None
        self.i = -1
        self.ultimo = None

    def _abrir(self):
        n = int(math.ceil(self.dur * FPS)) + 4
        info = subprocess.run(['ffprobe', '-v', 'error', '-select_streams', 'v:0', '-show_entries',
                               'stream=width,height', '-of', 'csv=p=0', f'{CLIPS}/{self.clip}.mp4'],
                              capture_output=True, text=True).stdout.strip().split(',')
        ancho, alto = int(info[0]), int(info[1])
        esc = alto / 2160
        # solo la franja que va a hacer falta con este paneo y este zoom
        cw = alto / min(self.z) * 9 / 16
        xa = max(0, int(min(self.cx) * esc - cw / 2) - 8)
        xb = min(ancho, int(max(self.cx) * esc + cw / 2) + 8)
        if xb - xa < cw + 16:
            xa = max(0, min(xa, ancho - int(cw) - 16))
            xb = min(ancho, xa + int(cw) + 16)
        if self.completo:
            xa, xb = 0, ancho
        xb -= (xb - xa) % 2
        self.xoff = xa
        self.sw, self.sh = xb - xa, alto
        filtro = f'fps={FPS / self.vel}' if self.vel != 1.0 else f'fps={FPS}'
        cmd = ['ffmpeg', '-v', 'error', '-ss', str(self.desde), '-i', f'{CLIPS}/{self.clip}.mp4',
               '-vf', f'crop={self.sw}:{alto}:{xa}:0,{filtro}', '-frames:v', str(n),
               '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-']
        self.proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, bufsize=10 ** 8)

    def _leer(self):
        tam = self.sw * self.sh * 3
        buf = self.proc.stdout.read(tam)
        if len(buf) < tam:
            return self.ultimo
        self.ultimo = np.frombuffer(buf, np.uint8).reshape(self.sh, self.sw, 3)
        return self.ultimo

    def cuadro(self, t):
        """Fotograma en el instante t (segundos desde el inicio del plano), 1080x1920."""
        if self.proc is None:
            self._abrir()
        objetivo = int(t * FPS)
        while self.i < objetivo:
            self._leer()
            self.i += 1
        src = self.ultimo
        u = suave(t / self.dur)
        z = lerp(self.z[0], self.z[1], u)
        escala = self.sh / 2160
        ch = self.sh / z
        cw = ch * 9 / 16
        cx = lerp(self.cx[0], self.cx[1], u) * escala - self.xoff
        cy = self.cy * self.sh
        x0 = min(max(cx - cw / 2, 0), self.sw - cw)
        y0 = min(max(cy - ch / 2, 0), self.sh - ch)
        im = Image.fromarray(src).crop((int(x0), int(y0), int(x0 + cw), int(y0 + ch)))
        return im.resize((W, H), Image.LANCZOS)

    def cerrar(self):
        if self.proc:
            self.proc.stdout.close()
            self.proc.kill()

# ---------------------------------------------------------------- color

def _lut(tono):
    x = np.arange(256, dtype=np.float32) / 255
    canales = []
    ganancia = {'frio': (0.93, 0.98, 1.08), 'calido': (1.05, 1.0, 0.92)}.get(tono, (1, 1, 1))
    for g in ganancia:
        y = np.clip(x * g, 0, 1)
        if tono == 'frio':
            y = y ** 1.12
        y = y + 0.10 * (y - 0.5) * (1 - np.abs(2 * y - 1))  # curva en S suave
        canales.append((np.clip(y, 0, 1) * 255).round().astype(np.uint8))
    return np.concatenate(canales).tolist()

LUTS = {t: _lut(t) for t in ('frio', 'calido', None)}
SATURACION = {'frio': 0.72, 'calido': 1.08}

def etalonar(im, tono):
    """Etalonado sencillo: 'frio' (antes, apagado) o 'calido' (después, luz de mañana)."""
    from PIL import ImageEnhance
    if tono in SATURACION:
        im = ImageEnhance.Color(im).enhance(SATURACION[tono])
    return im.point(LUTS.get(tono, LUTS[None]))

def desenfocado(im, oscuro=0.55):
    peq = im.resize((W // 6, H // 6), Image.BILINEAR).filter(ImageFilter.GaussianBlur(6))
    grande = peq.resize((W, H), Image.BILINEAR)
    a = np.asarray(grande).astype(np.float32) * oscuro
    return Image.fromarray(a.astype(np.uint8))

# ---------------------------------------------------------------- pantallas de la app

@functools.lru_cache(None)
def captura(nombre):
    return Image.open(f'{CAPT}/{nombre}.png').convert('RGB')

@functools.lru_cache(None)
def matte(estado):
    """Hueco de la cámara: imagen con el hueco en negro y cuánto deja pasar (0-1)."""
    negro = np.asarray(captura(f'anuncio_foto_{estado}_negro')).astype(np.float32)
    blanco = np.asarray(captura(f'anuncio_foto_{estado}_blanco')).astype(np.float32)
    pasa = np.clip((blanco - negro).mean(axis=2, keepdims=True) / 255, 0, 1)
    ys, xs = np.where(pasa[..., 0] > 0.5)
    caja = (xs.min(), ys.min(), xs.max() + 1, ys.max() + 1)
    return negro, pasa, caja

def pantalla_foto(estado, camara):
    """La pantalla de la foto con `camara` (PIL) dentro del hueco."""
    negro, pasa, (x0, y0, x1, y1) = matte(estado)
    lienzo = np.zeros_like(negro)
    lienzo[y0:y1, x0:x1] = np.asarray(camara.resize((x1 - x0, y1 - y0), Image.LANCZOS)).astype(np.float32)
    out = negro + pasa * lienzo
    return Image.fromarray(np.clip(out, 0, 255).astype(np.uint8))

def vista_camara(plano, t, desplaz=0.0, zoom=1.0, temblor=True):
    """Lo que ve la cámara del móvil: un plano 4K recortado a la forma del hueco."""
    _, _, (x0, y0, x1, y1) = matte('buscando')
    asp = (x1 - x0) / (y1 - y0)
    if plano.proc is None:
        plano._abrir()
    objetivo = int(t * FPS)
    while plano.i < objetivo:
        plano._leer()
        plano.i += 1
    src = Image.fromarray(plano.ultimo)
    sw, sh = src.size
    ch = sh / zoom
    cw = ch * asp
    # temblor de mano: suma de senos lentos
    tx = ty = 0
    if temblor:
        tx = 14 * math.sin(t * 2.1) + 8 * math.sin(t * 5.3 + 1)
        ty = 10 * math.sin(t * 1.7 + 2) + 6 * math.sin(t * 4.1)
    cx = sw * (0.5 + desplaz) + tx * sw / 3840
    cy = sh * 0.5 + ty * sh / 2160
    x = min(max(cx - cw / 2, 0), sw - cw)
    y = min(max(cy - ch / 2, 0), sh - ch)
    return src.crop((int(x), int(y), int(x + cw), int(y + ch)))

@functools.lru_cache(None)
def cuerpo_movil(sw, shh):
    """Marco del móvil (negro con canto) y su sombra, para una pantalla sw x shh."""
    bisel = max(4, int(sw * 0.022))
    r_p = int(sw * 0.085)
    r_c = r_p + bisel
    bw, bh = sw + 2 * bisel, shh + 2 * bisel
    margen = int(sw * 0.12)
    capa = Image.new('RGBA', (bw + 2 * margen, bh + 2 * margen), (0, 0, 0, 0))
    sombra = Image.new('L', capa.size, 0)
    ImageDraw.Draw(sombra).rounded_rectangle((margen, margen + int(sw * 0.03), margen + bw, margen + bh + int(sw * 0.03)), r_c, fill=150)
    sombra = sombra.filter(ImageFilter.GaussianBlur(sw * 0.045))
    capa.putalpha(sombra)
    d = ImageDraw.Draw(capa)
    d.rounded_rectangle((margen, margen, margen + bw, margen + bh), r_c, fill=(14, 14, 16, 255), outline=(70, 70, 76, 255), width=max(1, bisel // 5))
    mascara = Image.new('L', (sw, shh), 0)
    ImageDraw.Draw(mascara).rounded_rectangle((0, 0, sw - 1, shh - 1), r_p, fill=255)
    return capa, mascara, margen + bisel

def poner_movil(fondo, pantalla, ancho, centro, ancla=(0.5, 0.5)):
    """Dibuja el móvil con `pantalla` de `ancho` px; el punto `ancla` de la pantalla cae en `centro`."""
    sw = int(ancho)
    shh = int(sw * pantalla.height / pantalla.width)
    capa, mascara, off = cuerpo_movil(sw, shh)
    x = int(centro[0] - ancla[0] * sw) - off
    y = int(centro[1] - ancla[1] * shh) - off
    fondo.paste(capa, (x, y), capa)
    fondo.paste(pantalla.resize((sw, shh), Image.LANCZOS), (x + off, y + off), mascara)
    return fondo

# ---------------------------------------------------------------- textos

F_SUB = fuente('Inter-SemiBold', 54)
F_HORA = fuente('Inter-ExtraLight', 230)

def texto_sombra(im, xy, txt, f, color=(255, 255, 255), alfa=255, ancla='mm', sombra=150):
    # Solo se trabaja en la caja del texto (más un margen para el desenfoque de la sombra).
    d0 = ImageDraw.Draw(im)
    x0, y0, x1, y1 = d0.textbbox(xy, txt, font=f, anchor=ancla)
    m = 28
    caja = (int(x0) - m, int(y0) - m, int(x1) + m, int(y1) + m)
    capa = Image.new('RGBA', (caja[2] - caja[0], caja[3] - caja[1]), (0, 0, 0, 0))
    local = (xy[0] - caja[0], xy[1] - caja[1])
    if sombra:
        d = ImageDraw.Draw(capa)
        d.text(local, txt, font=f, fill=(0, 0, 0, int(sombra * alfa / 255)), anchor=ancla)
        capa = capa.filter(ImageFilter.GaussianBlur(7))
    d = ImageDraw.Draw(capa)
    d.text(local, txt, font=f, fill=color + (int(alfa),), anchor=ancla)
    im.paste(capa, (caja[0], caja[1]), capa)

CORTES = {'v01': [2, 4], 'v02': [1, 3], 'v03': [5], 'v04': [3, 3, 3], 'v05': [4, 6, 7], 'v06': [7],
          'v07': [3, 5, 5, 4], 'v08': [4], 'v09': [3, 4], 'v10': [5, 5], 'v11': [6, 3, 6], 'v12': [2],
          'v13': [3, 4, 2, 5, 2, 2], 'v14': [3, 6]}

class Subtitulos:
    """Frases cortas sincronizadas palabra a palabra con la voz."""

    def __init__(self, inicios):
        pal = json.load(open(f'{BASE}/voz/palabras.json'))
        texto = json.load(open(f'{BASE}/voz/lineas.json'))['texto']
        self.trozos = []
        for clave, t0 in inicios.items():
            ws = pal[clave]
            orig = texto[clave].split()
            if len(orig) == len(ws):
                ws = [(o, a, b) for o, (_, a, b) in zip(orig, ws)]
            ws = [(w[0].upper() + w[1:] if k == 0 else w, a, b) for k, (w, a, b) in enumerate(ws)]
            i = 0
            for n in CORTES[clave]:
                self.trozos.append([(w, t0 + a, t0 + b) for (w, a, b) in ws[i:i + n]])
                i += n
            assert i == len(ws), (clave, i, len(ws))

    def dibujar(self, im, t, y):
        for k, tr in enumerate(self.trozos):
            ini = tr[0][1] - 0.05
            fin = tr[-1][2] + 0.35
            if k + 1 < len(self.trozos):
                fin = min(fin, self.trozos[k + 1][0][1] - 0.05)
            if ini <= t < fin:
                frase = ' '.join(w for w, _, _ in tr)
                d = ImageDraw.Draw(im)
                total = d.textlength(frase, font=F_SUB)
                x = W / 2 - total / 2
                entrada = sale((t - ini) / 0.12)
                for w, a, b in tr:
                    alfa = 255 if t >= a - 0.02 else 110
                    texto_sombra(im, (x, y + 10 * (1 - entrada)), w, F_SUB, alfa=int(alfa * entrada), ancla='lm')
                    x += d.textlength(w + ' ', font=F_SUB)
                return

# ---------------------------------------------------------------- cierre: logo

@functools.lru_cache(None)
def logo_capas(escala):
    """El logo de Alba (medio sol sobre el horizonte y dos líneas), por piezas."""
    s = escala
    sol = Image.new('RGBA', (int(240 * s), int(120 * s)), (0, 0, 0, 0))
    ImageDraw.Draw(sol).pieslice((int(4 * s), int(4 * s), int(236 * s), int(236 * s)), 180, 360, fill=NARANJA + (255,))
    return sol

def cierre(t, fondo):
    """Tarjeta final: el sol sale por el horizonte, luego 'Alba' y la frase."""
    im = fondo
    d = ImageDraw.Draw(im, 'RGBA')
    cx, hy = W // 2, 860
    s = 1.0
    # resplandor
    u = sale(t / 1.6)
    if u > 0:
        glow = Image.new('L', (W, H), 0)
        ImageDraw.Draw(glow).ellipse((cx - 260, hy - 300, cx + 260, hy + 60), fill=int(90 * u))
        glow = glow.filter(ImageFilter.GaussianBlur(90))
        capa = Image.new('RGB', (W, H), NARANJA)
        im.paste(capa, (0, 0), glow)
    # medio sol subiendo, recortado por el horizonte
    sol = logo_capas(s)
    subida = int((1 - sale(t / 1.4)) * sol.height)
    recorte = sol.crop((0, 0, sol.width, sol.height - subida))
    if recorte.height > 0:
        im.paste(recorte, (cx - sol.width // 2, hy - recorte.height), recorte)
    d = ImageDraw.Draw(im, 'RGBA')
    # horizonte y reflejos que se dibujan desde el centro
    for (ancho, dy, alfa, retraso) in [(380, 18, 255, 0.0), (190, 58, 110, 0.25), (90, 92, 60, 0.4)]:
        v = sale((t - retraso) / 0.7)
        if v > 0:
            a = ancho * v / 2
            d.rounded_rectangle((cx - a, hy + dy - 8, cx + a, hy + dy + 8), 8, fill=NARANJA + (alfa,))
    # nombre y frase
    v = sale((t - 0.9) / 0.8)
    if v > 0:
        texto_sombra(im, (cx, 1140 + 20 * (1 - v)), 'Alba', fuente('InterDisplay-Bold', 150), alfa=int(255 * v), sombra=0)
    v = sale((t - 1.6) / 0.8)
    if v > 0:
        f = fuente('Inter-Medium', 46)
        texto_sombra(im, (cx, 1275), 'El despertador que no se apaga', f, color=(200, 200, 205), alfa=int(255 * v), sombra=0)
        texto_sombra(im, (cx, 1335), 'desde la cama.', f, color=(200, 200, 205), alfa=int(255 * v), sombra=0)
    v = sale((t - 2.8) / 0.8)
    if v > 0:
        texto_sombra(im, (cx, 1500), 'Muy pronto en Android', fuente('Inter-Medium', 36), color=(140, 140, 146), alfa=int(255 * v), sombra=0)
    return im

# ---------------------------------------------------------------- guion

VOZ = {'v01': 1.30, 'v02': 5.00, 'v03': 7.10, 'v04': 9.00, 'v05': 12.75, 'v06': 17.95, 'v07': 20.55,
       'v08': 25.95, 'v09': 28.05, 'v10': 30.65, 'v11': 34.45, 'v12': 38.25, 'v13': 39.90, 'v14': 47.40,
       'v15': 51.60}
GIRO = 20.25  # entra Alba: la música se abre

def escenas():
    """Lista de (inicio, fin, función(t_local, t_global) -> (imagen, zona_subtítulo))."""
    E = []

    def plano(t0, t1, p, tono, sello=None):
        def f(t, tg):
            im = etalonar(p.cuadro(t), tono)
            if sello:
                v = sale(t / 0.15)
                texto_sombra(im, (W // 2, 330), sello, F_HORA, alfa=int(235 * v), sombra=170)
            return im, 1400
        E.append((t0, t1, f, p))

    # --- Antes: el despertador de siempre (frío)
    plano(0.00, 1.60, Plano('50753', 0.0, 1.6, cx=(2350, 2350), z=(1.05, 1.12)), 'frio')
    plano(1.60, 4.90, Plano('50931', 0.3, 3.3, cx=(2250, 2250), z=(1.0, 1.08)), 'frio')
    plano(4.90, 7.00, Plano('50752', 1.0, 2.1, cx=(2650, 2750), z=(1.0, 1.04)), 'frio')
    plano(7.00, 9.00, Plano('50752', 6.5, 2.0, cx=(2100, 2100), z=(1.0, 1.05)), 'frio')
    plano(9.00, 10.42, Plano('50753', 3.6, 1.42, cx=(2300, 2300), z=(1.1, 1.14)), 'frio', '07:05')
    plano(10.42, 11.36, Plano('50931', 5.5, 0.94, cx=(2250, 2250), z=(1.15, 1.18)), 'frio', '07:10')
    plano(11.36, 12.60, Plano('50752', 9.0, 1.24, cx=(2100, 2100), z=(1.1, 1.13)), 'frio', '07:15')
    plano(12.60, 17.80, Plano('50754', 0.0, 5.2, cx=(2200, 2300), z=(1.0, 1.1)), 'frio')
    plano(17.80, GIRO, Plano('50757', 0.0, GIRO - 17.80, cx=(2150, 2150), z=(1.0, 1.06)), 'frio')

    # --- Alba suena
    fondo_cama = Plano('50752', 6.0, 6.0, cx=(2100, 2150))
    def m1(t, tg):
        fondo = desenfocado(etalonar(fondo_cama.cuadro(t * 0.9), 'calido'), 0.5)
        entrada = sale(t / 0.5)
        ancho = 640 * lerp(1.08, 1.0, entrada)
        # a partir de "me pide que le enseñe...", zoom a la línea de abajo
        z = suave((tg - 23.4) / 1.2)
        ancho = ancho * lerp(1.0, 1.55, z)
        ancla = (0.5, lerp(0.5, 0.92, z))
        centro = (W / 2, lerp(790, 1060, z))
        return poner_movil(fondo, captura('anuncio_sonando'), ancho, centro, ancla), 1590
    E.append((GIRO, 25.90, m1, fondo_cama))

    # --- "Hoy tocaba una taza": empieza cerca del objeto y se aleja
    fondo_cama2 = Plano('50754', 2.0, 2.2, cx=(2200, 2200))
    cam_cama = Plano('50752', 3.0, 2.2, completo=True)
    def m2(t, tg):
        fondo = desenfocado(etalonar(fondo_cama2.cuadro(t), 'calido'), 0.5)
        vista = etalonar(vista_camara(cam_cama, t, desplaz=-0.1 + 0.05 * t, zoom=1.0), 'calido')
        pant = pantalla_foto('buscando', vista.filter(ImageFilter.GaussianBlur(3)))
        z = 1 - sale(t / 1.8)
        ancho = 640 * lerp(1.0, 1.9, z)
        ancla = (0.5, lerp(0.5, 0.045, z))
        centro = (W / 2, lerp(790, 620, z))
        return poner_movil(fondo, pant, ancho, centro, ancla), 1590
    E.append((25.90, 28.00, m2, [fondo_cama2, cam_cama]))

    # --- Se levanta y va a la cocina
    plano(28.00, 29.35, Plano('50754', 10.5, 1.35, cx=(2750, 2800), z=(1.05, 1.1)), 'calido')
    plano(29.35, 30.55, Plano('50764', 0.0, 1.2, cx=(1400, 1500), z=(1.0, 1.05)), 'calido')

    # --- La cámara encuentra la taza
    taza = Plano('808', 0.0, 4.0, completo=True)
    fondo_cocina = Plano('50768', 0.0, 4.0, cx=(1950, 1950))
    def m3(t, tg):
        fondo = desenfocado(etalonar(fondo_cocina.cuadro(t), 'calido'), 0.5)
        entra = sale(t / 1.6)
        vista = etalonar(vista_camara(taza, t, desplaz=lerp(0.28, 0.02, entra), zoom=1.05), 'calido')
        if tg < 32.40:
            pant = pantalla_foto('buscando', vista)
        else:
            pant = pantalla_foto('viendo', vista)
        cambio = suave((tg - 33.50) / 0.3)
        if cambio > 0:
            pant = Image.blend(pant, captura('anuncio_hecha'), cambio)
        ancho = 640 * lerp(1.04, 1.0, sale(t / 0.4))
        return poner_movil(fondo, pant, ancho, (W / 2, 790)), 1590
    E.append((30.55, 34.35, m3, [taza, fondo_cocina]))

    # --- En la cocina, café
    plano(34.35, 38.20, Plano('50768', 0.0, 3.85, cx=(1950, 1950), z=(1.0, 1.06)), 'calido')
    plano(38.20, 39.90, Plano('50770', 1.0, 1.7, cx=(2000, 2000), z=(1.06, 1.1)), 'calido')

    # --- Lo que lo hace distinto
    plano(39.90, 41.66, Plano('50771', 2.0, 1.76, cx=(2100, 2100), z=(1.05, 1.1)), 'calido')
    taza2 = Plano('808', 4.0, 2.4, completo=True)
    taza2b = Plano('808', 4.0, 2.4, cx=(1900, 1900))
    def m4b(t, tg):
        fondo = desenfocado(etalonar(taza2b.cuadro(t), 'calido'), 0.45)
        vista = etalonar(vista_camara(taza2, t, desplaz=0.02, zoom=1.05), 'calido')
        pant = pantalla_foto('viendo', vista)
        ancho = 640 * lerp(1.0, 1.05, t / 2.3)
        return poner_movil(fondo, pant, ancho, (W / 2, 790)), 1590
    E.append((41.66, 43.92, m4b, [taza2, taza2b]))
    fondo_cocina2 = Plano('50771', 5.0, 3.5, cx=(2100, 2100))
    def m5(t, tg):
        fondo = desenfocado(etalonar(fondo_cocina2.cuadro(t), 'calido'), 0.5)
        ancho = 640 * lerp(1.04, 1.0, sale(t / 0.4))
        return poner_movil(fondo, captura('anuncio_comprobacion'), ancho, (W / 2, 790)), 1590
    E.append((43.92, 47.30, m5, fondo_cocina2))

    # --- En pie a la primera
    plano(47.30, 50.60, Plano('50760', 0.8, 3.3, cx=(1920, 1920), z=(1.0, 1.08)), 'calido')
    azotea = Plano('50759', 2.0, 3.0, cx=(2000, 2000), z=(1.0, 1.04))
    def fin(t, tg):
        negro = Image.new('RGB', (W, H), (0, 0, 0))
        if tg < 51.40:
            im = etalonar(azotea.cuadro(t), 'calido')
            return Image.blend(im, negro, suave((tg - 50.70) / 0.7)), None
        return cierre(tg - 51.40, negro), None
    E.append((50.60, DURACION, fin, azotea))
    return E

def main():
    salida = sys.argv[1] if len(sys.argv) > 1 else f'{BASE}/imagen.mp4'
    solo = [float(x) for x in sys.argv[2:]]  # instantes sueltos para revisar
    E = escenas()
    subs = Subtitulos({k: v for k, v in VOZ.items() if k != 'v15'})
    if solo:
        for tg in solo:
            for (a, b, f, _) in E:
                if a <= tg < b:
                    im, ysub = f(tg - a, tg)
                    if ysub:
                        subs.dibujar(im, tg, ysub)
                    im.save(f'{BASE}/cuadro_{tg:05.2f}.png')
        return
    enc = subprocess.Popen(['ffmpeg', '-v', 'error', '-y', '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-s', f'{W}x{H}',
                            '-r', str(FPS), '-i', '-',
                            '-vf', 'vignette=angle=0.45,noise=alls=5:allf=t',
                            '-c:v', 'libx264', '-preset', 'medium', '-crf', '16', '-pix_fmt', 'yuv420p', salida],
                           stdin=subprocess.PIPE)
    n = int(DURACION * FPS)
    actual = None
    for i in range(n):
        tg = i / FPS
        for idx, (a, b, f, recursos) in enumerate(E):
            if a <= tg < b:
                if actual is not None and actual != idx:
                    for r in (E[actual][3] if isinstance(E[actual][3], list) else [E[actual][3]]):
                        r.cerrar()
                actual = idx
                im, ysub = f(tg - a, tg)
                break
        if ysub:
            subs.dibujar(im, tg, ysub)
        enc.stdin.write(im.tobytes())
        if i % 48 == 0:
            print(f'{tg:5.1f}s', flush=True)
    enc.stdin.close()
    enc.wait()

if __name__ == '__main__':
    main()
