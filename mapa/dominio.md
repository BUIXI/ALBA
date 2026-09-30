---
tags: [mapa, modulo]
carpeta: app/src/main/java/com/oriol/alba/dominio/
---
# dominio/: lógica pura (sin Android)

Volver al [[00 Indice]]. Todo se prueba en el PC sin Robolectric. Pruebas: `dominio/*Test.kt` ([[Pruebas]]).

## `Programacion.kt`: cuándo suena

- `siguienteDisparo(alarma, ahora): LocalDateTime?`: el próximo momento **estrictamente después** de `ahora`. `null` si está desactivada.
  - Sin días: hoy si aún no ha pasado; si no, mañana.
  - Con días: recorre de hoy a +7 días.
- `proximaAlarma(alarmas, ahora): Pair<Alarma, LocalDateTime>?`: la activa que antes suena.
- `minutosHasta(ahora, momento)`: redondea hacia arriba, como iOS (7 h 11 min 20 s → 7 h 12 min).

## `Dias.kt`: días de la semana

- `diasDeLaSemana(idioma)`: los 7 días en el orden del idioma (lunes en España, domingo en EE. UU.).
- `sealed interface ResumenDias`: `UnaVez`, `TodosLosDias`, `EntreSemana`, `FinDeSemana` o `Sueltos(dias)`.
- `resumirDias(dias, idioma)`. El texto lo pone `ui/componentes/TextosDias.kt` (`textoDias`).

## `Actividades.kt`: qué actividad toca

- `elegirTarea(tareas, azar)`: una al azar entre las disponibles; si no hay ninguna, `PorDefecto`.
- `alternativaA(actual, tareas)`: otra de las marcadas que **no** use la cámara; si no hay, `PorDefecto`; y si esa es la actual, `CALCULO`.

## `Calculo.kt`

- `enum Operador(simbolo)`: `SUMA "+"`, `RESTA "−"`, `MULTIPLICACION "×"`.
- `data class Operacion(a, b, operador)`, con `.resultado` y `.texto` ("47 + 38").
- `GeneradorCalculo(azar).siguiente(anterior)`: nunca repite la anterior.
  - Suma: 12-89 + 12-89, sin acabar en 0.
  - Resta: 41-98 menos un número menor, casi siempre con llevada.
  - Multiplicación: 3-9 × 12-29.
- `OperacionesParaApagar = 3`.
- El estado de la pantalla está en `ui/tareas/EstadoCalculo.kt` ([[Actividades]]).

## `Foto.kt`: reconocer objetos con ML Kit (modelo base, sin internet)

| `ObjetoFoto` | Etiquetas de ML Kit |
|---|---|
| FREGADERO | Sink |
| TAZA | Cup |
| SOFA | Couch, Loveseat |
| TELE | Television |
| ZAPATOS | Shoe, Sneakers |
| PLANTA | Plant, Flowerpot, Flora |
| CUBIERTOS | Cutlery, Tableware, Saucer |
| COCINA | Kitchen, Countertop, Cookware and bakeware |

- El modelo base **no conoce** "nevera" ni "microondas". Lista de etiquetas: developers.google.com/ml-kit/vision/image-labeling/label-map.
- `data class Etiqueta(texto, confianza)`.
- `seVe(objeto, etiquetas, umbral = ConfianzaMinima)`: `ConfianzaMinima = 0.55f`. Si aparecen "Screenshot" o "Web page" con 0,5 o más, no vale.
- `DetectorSeguido(necesarios = 3).fotograma(visto)`: pide 3 fotogramas seguidos; un fallo reinicia la cuenta.
- `objetoAlAzar(azar, excepto)`.
- `CambiosDeObjeto = 2`: veces que se puede pedir otro objeto.
