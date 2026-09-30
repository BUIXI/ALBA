---
tags: [mapa]
---
# Historial de versiones

Volver al [[00 Indice]]. Todo se hizo el 2026-09-29 (autor: Oriol, con Claude Code). Rama `main`.

| Commit | Versión | Qué entró | Tamaño | Documento |
|---|---|---|---|---|
| `75f9f31` | Fase 0 | Entorno (JDK, SDK, Android Studio) y proyecto base desde la plantilla `empty-activity`. Tema oscuro y primera captura | 46 archivos, +1307 | [[fase00-entorno]] |
| `d572175` | Fase 1 | Sistema de diseño, lista de alarmas, editor, Room v1 e icono | 68 archivos, +2429/−389 | [[fase01-diseno-y-lista]] |
| `b802f33` | **0.1** | Fases 2-5: la alarma suena (servicio, receptores, *direct boot*), cálculo, foto con ML Kit, comprobación y "Buenos días" | 67 archivos, +3793/−58 | [[fase02a05-version01]] |
| `ddce685` | 0.1.1 | Idiomas: inglés por defecto (`values/`) y español (`values-es/`) | 10 archivos, +279/−107 | [[INDICE]] |
| `8b18cb2` | **0.2** | 4 minijuegos, varias actividades por alarma (Premium), BD v2 con migración | 32 archivos, +1582/−69 | [[version02-minijuegos]] |
| `20a4ddf` | – | `CLAUDE.md`: instrucciones para Claude Code en la nube | 2 archivos, +26 | – |

| versionCode | versionName |
|---|---|
| 3 | 0.2 (actual) |

Para ver qué ha cambiado después del mapa: `git log --stat 20a4ddf..HEAD`.
