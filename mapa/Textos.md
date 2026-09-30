---
tags: [mapa]
aliases: [Strings, Idiomas]
---
# Textos de la app (clave → español)

Volver al [[00 Indice]]. Archivos:
- `app/src/main/res/values/strings.xml`: **inglés**, el de por defecto.
- `app/src/main/res/values-es/strings.xml`: español.

Las dos tienen las mismas claves. Aquí va el español, para saber qué clave buscar a partir de un texto que se ve en pantalla. Días y fechas no están aquí: salen del idioma del móvil. Cómo añadir uno: [[Recetas#Añadir un texto]].

## Lista

| Clave | Texto |
|---|---|
| `app_name` | Alba |
| `titulo_alarmas` | Alarmas |
| `nueva_alarma` | Nueva alarma |
| `sin_alarmas` / `sin_alarmas_texto` | Sin alarmas / Crea una y Alba se asegurará de que te levantes. |
| `crear_alarma` | Crear alarma |
| `proxima_en_min` / `_h` / `_h_min` / `proxima_el_dia` | Próxima alarma en %d min / %d h / %d h %d min / el %s |
| `ninguna_activa` | Ninguna alarma activada |
| `alarma_de_las` | Alarma de las %s |
| `permisos_aviso` / `revisar` | Falta un permiso para que la alarma suene siempre. / Revisar |

## Días y momentos

| Clave | Texto |
|---|---|
| `una_vez`, `todos_los_dias`, `entre_semana`, `fin_de_semana` | Una vez, Todos los días, Entre semana, Fin de semana |
| `separador_detalle` | " · " |
| `hoy_a_las` / `manana_a_las` / `el_dia_a_las` | hoy a las %s / mañana a las %s / el %s a las %s |

## Editor

| Clave | Texto |
|---|---|
| `editar_alarma`, `cancelar`, `guardar` | Editar alarma, Cancelar, Guardar |
| `hora`, `minutos`, `repetir` | Hora, Minutos, Repetir |
| `sonara_una_vez` | Sin días marcados, sonará una sola vez. |
| `etiqueta` / `etiqueta_por_defecto` | Etiqueta / Alarma |
| `para_apagarla` | Para apagarla |
| `tarea_soles` / `_desc` | Atrapa los soles / Toca 12 soles antes de que se escapen. |
| `tarea_secuencia` / `_desc` | Repite la secuencia / Mira cómo se encienden los colores y repítelo. |
| `tarea_parejas` / `_desc` | Parejas / Da la vuelta a las cartas y encuentra las 6 parejas. |
| `tarea_orden` / `_desc` | Del 1 al 12 / Toca los números en orden, lo más rápido que puedas. |
| `tarea_calculo` / `_desc` | Cálculo mental / Resuelve tres operaciones sencillas. |
| `tarea_foto` / `_desc` | Foto de un objeto / Levántate y enséñale a la cámara algo de tu casa: el fregadero, una taza, el sofá… |
| `tareas_marca_varias` | Marca varias y cada vez te tocará una al azar. |
| `tareas_al_azar` (plural) | Cada vez que suene te tocará esta / una de estas %d al azar. |
| `tareas_con_premium` | Con Premium puedes marcar varias y cada vez te tocará una al azar. |
| `pronto` | Pronto |
| `sonido`, `sonido_amanecer` / `_desc` | Sonido, Amanecer / Un carillón suave que va subiendo. |
| `sonido_sistema` / `_desc` | Alarma del teléfono / El sonido de alarma que tengas en el móvil. |
| `despues_de_apagarla` | Después de apagarla |
| `comprobar` / `comprobar_pie` | Comprobar a los %d min / Te preguntará si sigues despierto. Si no respondes en un minuto, vuelve a sonar. |
| `probar_alarma` / `probar_pie` | Probar la alarma / Suena ahora mismo, tal cual sonará. Así ves cómo se apaga. |
| `eliminar_alarma` | Eliminar alarma |

## Pantalla de la alarma

| Clave | Texto |
|---|---|
| `prueba` | Prueba |
| `estoy_despierto` | Estoy despierto |
| `pista_soles`, `_secuencia`, `_parejas`, `_orden`, `_foto` | Después tendrás que atrapar 12 soles / repetir una secuencia de colores / encontrar 6 parejas de cartas / tocar del 1 al 12 en orden / enseñarle a la cámara algo de tu casa. |
| `pista_calculo` (plural) | Después tendrás que resolver %d operación/operaciones. |
| `sonando_juego` / `sonando_foto` / `sonando_pulsa` | Suena · juega para silenciarla / Suena · encuentra el objeto para apagarla / Suena · toca una tecla para silenciarla |
| `silencio_quedan` | Silencio · vuelve a sonar en %d s |
| `progreso_de` | %d de %d |
| `secuencia_mira` / `secuencia_tu_turno` / `ronda_de` | Mira bien… / Tu turno: repítela / Ronda %d de %d |
| `parejas_instruccion` / `parejas_de` | Encuentra las parejas / %d de %d parejas |
| `orden_instruccion` / `toca_el` | Del 1 al %d, en orden / Toca el %d |
| `ensenale` | Enséñale a la cámara |
| `objeto_fregadero`, `_taza`, `_sofa`, `_tele`, `_zapatos`, `_planta`, `_cubiertos`, `_cocina` | un fregadero o un lavabo, una taza, el sofá, la tele, tus zapatos, una planta, unos cubiertos o un plato, la cocina |
| `buscando` / `lo_estoy_viendo` | Buscando… / ¡Lo estoy viendo! No te muevas… |
| `otro_objeto` (plural) | Pedir otro (queda/quedan %d) |
| `otra_actividad` | Cambiar de actividad |
| `borrar` | Borrar |
| `sigues_despierto` / `comprobacion_quedan` / `si_despierto` | ¿Sigues despierto? / Si no respondes en %d s, la alarma volverá a sonar. / Sí, estoy despierto |
| `buenos_dias`, `buenas_tardes`, `buenas_noches` | Buenos días, Buenas tardes, Buenas noches |
| `perfecto`, `buen_dia` | Perfecto, Que tengas un buen día. |
| `te_has_levantado` / `proxima_cuando` | Te has levantado a las %s. / Próxima alarma: %s. |
| `comprobare_en` (plural) | Dentro de %d minuto(s) te preguntaré si sigues despierto. |
| `cerrar` | Cerrar |

## Notificaciones

| Clave | Texto |
|---|---|
| `canal_alarmas` / `_desc` | Alarmas / La alarma mientras suena. |
| `notificacion_alarma` | Alarma · %s |
| `notificacion_toca` | Toca para apagarla. |
| `notificacion_comprobacion` | Toca para responder, o volverá a sonar. |

## Permisos

| Clave | Texto |
|---|---|
| `permisos_titulo` / `permisos_texto` | Para que suene siempre / Android pide unos permisos para que una alarma funcione bien. Sin ellos, Alba puede llegar tarde o no sonar. |
| `permiso_notificaciones` / `_desc` | Notificaciones / Para enseñar la alarma. |
| `permiso_pantalla` / `_desc` | Pantalla completa / Para salir encima de la pantalla de bloqueo. |
| `permiso_exactas` / `_desc` | Alarmas exactas / Para sonar a su hora, ni un minuto tarde. |
| `permiso_segundo_plano` / `_desc` | Actividad en segundo plano / Con la batería restringida, Android puede impedir que suene. |
| `permiso_bateria` / `_desc` | Sin optimizar la batería / Recomendado: algunos móviles cierran las apps optimizadas. |
| `permisos_fabricante` | En %s, entra también en los ajustes de Alba → Uso de la batería, y permite la actividad en segundo plano y el inicio automático… |
| `abrir_ajustes`, `activar`, `concedido`, `listo` | Abrir los ajustes de Alba, Activar, Concedido, Listo |
