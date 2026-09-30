#!/usr/bin/env bash
# Recursos del anuncio 03 ("A la primera"), todos de Mixkit (licencia gratuita que
# permite usarlos en anuncios y redes; no se pueden revender sueltos).
# Uso: ANUNCIO=~/anuncio bash tools/anuncios/descargar.sh
set -euo pipefail
A="${ANUNCIO:-$HOME/anuncio}"
mkdir -p "$A/clips" "$A/musica" "$A/sfx"

# Vídeo (4K). La mayoría es una misma sesión: el mismo actor en el mismo piso.
#  50753 despertador sonando · 50931 duerme (primer plano) · 50752 cama desde arriba
#  50754 se despierta · 50757 cara en penumbra · 50764 pies andando · 50768 cocina
#  50770 sorbo de café · 50771 cocina con el móvil · 50760 brazos al cielo
#  50759 azotea · 808 taza humeante (lo que ve la cámara)
for i in 50753 50931 50752 50754 50757 50764 50768 50770 50771 50760 50759 808; do
  [ -s "$A/clips/$i.mp4" ] || curl -sSfL -o "$A/clips/$i.mp4" "https://assets.mixkit.co/videos/$i/$i-2160.mp4"
done

# Música: "Possible Dreams" (Do mayor, 86 bpm; crece hacia el segundo 22).
[ -s "$A/musica/599.mp3" ] || curl -sSfL -o "$A/musica/599.mp3" https://assets.mixkit.co/music/599/599.mp3

# Efectos: despertador de campanas, impacto de entrada y transición.
curl -sSfL -o "$A/sfx/bell_1003.mp3" https://assets.mixkit.co/active_storage/sfx/1003/1003-preview.mp3
curl -sSfL -o "$A/sfx/impact_2903.mp3" https://assets.mixkit.co/active_storage/sfx/2903/2903-preview.mp3
curl -sSfL -o "$A/sfx/whoosh_1492.mp3" https://assets.mixkit.co/active_storage/sfx/1492/1492-preview.mp3
echo "Recursos en $A"
