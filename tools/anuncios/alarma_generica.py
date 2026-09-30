# Pantalla de una alarma de móvil cualquiera (el "antes"): hora grande, Posponer y
# Detener. Genérica a propósito: no imita la app de reloj de ninguna marca.
import functools
from PIL import Image, ImageDraw, ImageFont

F = '/usr/share/fonts/opentype/inter'
AW, AH = 1272, 2799  # mismo tamaño que las capturas de la app


def _f(n, t):
    return ImageFont.truetype(f'{F}/{n}.otf', t)


@functools.lru_cache(None)
def base(hora):
    im = Image.new('RGB', (AW, AH), (12, 12, 14))
    d = ImageDraw.Draw(im)
    # degradado muy leve arriba
    for y in range(900):
        v = int(12 + 14 * (1 - y / 900))
        d.line([(0, y), (AW, y)], fill=(v, v, v + 3))
    d.text((AW // 2, 520), 'Alarma', font=_f('Inter-Medium', 58), fill=(150, 150, 158), anchor='mm')
    d.text((AW // 2, 780), hora, font=_f('Inter-ExtraLight', 300), fill=(245, 245, 247), anchor='mm')
    d.text((AW // 2, 980), 'jueves, 1 de octubre', font=_f('Inter-Regular', 52), fill=(150, 150, 158), anchor='mm')
    # Posponer: botón grande y fácil de tocar dormido (ese es el problema)
    d.rounded_rectangle((170, 1900, AW - 170, 2100), 100, fill=(58, 58, 62))
    d.text((AW // 2, 2000), 'Posponer', font=_f('Inter-SemiBold', 70), fill=(255, 255, 255), anchor='mm')
    d.text((AW // 2, 2330), 'Detener', font=_f('Inter-Medium', 58), fill=(150, 150, 158), anchor='mm')
    return im


def pantalla(hora, toque=None):
    """toque: segundos desde el toque en Posponer (None = sin tocar)."""
    im = base(hora).copy()
    if toque is not None and 0 <= toque < 0.6:
        d = ImageDraw.Draw(im, 'RGBA')
        u = toque / 0.6
        r = 60 + 260 * u
        a = int(150 * (1 - u))
        cx, cy = AW // 2 + 120, 2000
        d.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(255, 255, 255, a))
        # el botón se ilumina al pulsarlo
        d.rounded_rectangle((170, 1900, AW - 170, 2100), 100, fill=(255, 255, 255, int(60 * (1 - u))))
    return im


if __name__ == '__main__':
    pantalla('07:05', 0.1).save('alarma_generica.png')
