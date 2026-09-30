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
#  39786 (versión 2) la mano que pospone la alarma del móvil; se recorta sin cara
#  (vídeo 04) 3891 grifo del fregadero · 3110 salón con sofá gris · 51044 cactus en maceta · 46291 tele
for i in 50753 50931 50752 50754 50757 50764 50768 50770 50771 50760 50759 808 39786 3891 3110 51044 46291; do
  [ -s "$A/clips/$i.mp4" ] && continue
  for r in 2160 1080 720; do  # no todos existen en 4K
    curl -sSfL -o "$A/clips/$i.mp4" "https://assets.mixkit.co/videos/$i/$i-$r.mp4" && break
  done
done

# Música: "Possible Dreams" (Do mayor, 86 bpm; crece hacia el segundo 22).
[ -s "$A/musica/599.mp3" ] || curl -sSfL -o "$A/musica/599.mp3" https://assets.mixkit.co/music/599/599.mp3

# Música de la versión 2: "Lo-Fi 04" (Fa mayor; la batería entra en el segundo 9,06).
[ -s "$A/musica/766.mp3" ] || curl -sSfL -o "$A/musica/766.mp3" https://assets.mixkit.co/music/766/766.mp3

# Música del vídeo 04: "Pop 07" (Fa mayor, 90 bpm, mucha percusión desde el primer segundo).
[ -s "$A/musica/699.mp3" ] || curl -sSfL -o "$A/musica/699.mp3" https://assets.mixkit.co/music/699/699.mp3

# Efectos (versión 1): despertador de campanas, impacto de entrada y transición.
curl -sSfL -o "$A/sfx/bell_1003.mp3" https://assets.mixkit.co/active_storage/sfx/1003/1003-preview.mp3
curl -sSfL -o "$A/sfx/impact_2903.mp3" https://assets.mixkit.co/active_storage/sfx/2903/2903-preview.mp3
curl -sSfL -o "$A/sfx/whoosh_1492.mp3" https://assets.mixkit.co/active_storage/sfx/1492/1492-preview.mp3
echo "Recursos en $A"
