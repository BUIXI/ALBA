---
tags: [mapa, modulo]
aliases: [Sistema de diseño, Tema, Componentes]
carpeta: app/src/main/java/com/oriol/alba/theme/ + ui/componentes/
---
# Sistema de diseño

Volver al [[00 Indice]]. Estilo Apple/iOS: mucho aire, sin la "onda" de Android y sin Material por defecto. Uso: `Alba.colores.x`, `Alba.tipos.x`.

## `theme/`

| Archivo | Símbolos | Notas |
|---|---|---|
| `Colores.kt` | `data class Paleta`, `PaletaOscura`, `PaletaClara` | Campos: `fondo`, `superficie`, `superficieAlta`, `separador`, `texto`, `textoSecundario`, `textoTerciario`, `acento`, `sobreAcento`, `peligro`, `interruptorApagado` |
| `Tema.kt` | `object Alba { colores; tipos }`, `TemaAlba(paleta = PaletaOscura)` | Esquema de Material 3 relleno con la paleta. `LocalIndication = IndicacionOpacidad` |
| `Tipografia.kt` | `data class Tipos`, `TiposAlba`, `trackingInter(sp)` | Inter variable (`res/font/inter.ttf`): `opsz` 32 si ≥20 sp y 14 si es menor. Cifras tabulares en las horas |

| Color | Oscura (app) | Clara (alarma) |
|---|---|---|
| fondo | `#000000` | `#F2F2F7` |
| superficie | `#1C1C1E` | `#FFFFFF` |
| texto | `#FFFFFF` | `#000000` |
| textoSecundario | `#8E8E93` | `#6E6E73` |
| acento | `#FF9F0A` (ámbar) | `#FF9500` |
| sobreAcento | negro (el blanco sobre ámbar contrasta 2:1) | negro |
| peligro | `#FF453A` | `#FF3B30` |

| Estilo | Tamaño / alto de línea / peso |
|---|---|
| `tituloGrande` | 34/41 Bold |
| `titulo` | 22/28 Bold |
| `cabecera` | 17/22 SemiBold |
| `cuerpo` | 17/22 |
| `secundario` | 15/20 |
| `nota` | 13/18 |
| `horaLista` | 56/62 Light, tnum |
| `horaRueda` | 24/30, tnum |

## `ui/componentes/`

| Archivo | Piezas |
|---|---|
| `Basicos.kt` | `Pantalla` (pinta su fondo), `BotonPrincipal` (píldora de acento, uno por pantalla), `BotonTexto`, `BotonIcono` (44 dp), `Grupo`, `FilaGrupo`, `SeparadorFila`, `EncabezadoGrupo`, `PieGrupo`, `EsquinaGrupo` (12 dp) |
| `Indicaciones.kt` | `IndicacionOpacidad` (atenúa; es la global), `IndicacionResaltado(color)` (rellena la fila) |
| `Interruptor.kt` | `Interruptor`: 51×31 dp, rebote y vibración |
| `Reloj.kt` | `rememberAhora()` (cambia en cada minuto), `formatoHora(h, m)` ("07:05", siempre 24 h) |
| `Rueda.kt` | `Rueda(cantidad, seleccionado, onCambio)` y `RuedaHora`. Lista "sin fin" (400 vueltas), `efectoTambor`, vibración |
| `SelectorDias.kt` | `SelectorDias(dias, onCambio)`: círculos L M X J V S D según el idioma |
| `Teclado.kt` | `Teclado(onTecla)`: teclas redondas de 76 dp como el teléfono de iOS |
| `TextosDias.kt` | `textoCuando(momento, ahora)` ("mañana a las 07:00") y `textoDias(dias)` |

## Recursos

- Icono: `res/drawable/ic_launcher_foreground.xml` (medio sol ámbar sobre el mar), `mipmap-anydpi/`, capa monocroma.
- `ic_notificacion.xml`, `ic_mas`, `ic_check`, `ic_borrar`.
- Sonido `res/raw/amanecer.wav`: carillón en mi mayor, bucle de 2,4 s. Lo genera `tools/GenerarSonido.java` (sin licencias).
- Licencia de Inter: `assets/licencias/Inter-OFL.txt`.
- `values/themes.xml`: `windowBackground` negro, para que no haya destello al abrir.

Lo visual se decide **mirando las capturas** ([[Pruebas#Capturas]]).
