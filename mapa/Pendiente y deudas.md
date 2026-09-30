---
tags: [mapa]
actualizado: 2026-09-30
---
# Pendiente y deudas

Volver al [[00 Indice]].

## Lo siguiente

- [ ] **Probar en el Realme**: nada se ha probado aún en un móvil de verdad. Lista en [[PRUEBAS_EN_EL_MOVIL]].
- [ ] **Fase 6**: Premium de pago (RevenueCat), IA con *backend* (Cloudflare Workers o Firebase; la clave **nunca** en la app) y anuncios **solo después** de la tarea.
- [ ] Foto ancla (*embeddings* en local) y calibrar el umbral de la foto con fotos reales de madrugada.
- [ ] Formato de 12 h (AM/PM): ahora siempre es de 24 h (`formatoHora`).
- [ ] Selector de idioma por app (Android 13+) y más idiomas.
- [ ] Deslizar para borrar en la lista (ahora se borra desde el editor).
- [ ] Más actividades: voz, pasos, sacudir, QR.
- [ ] Clave de firma propia para Play; activar R8 cuando se pueda probar (APK de unos 30 MB).
- [ ] Actualizar las dependencias en bloque (AGP 9.4, Kotlin 2.4, BOM 2026.09, Navigation 3 1.2, Room 2.8.5...).
- [ ] Comprobar que el nombre "Alba" está libre en Google Play.
- [ ] Política de privacidad: ML Kit añade los permisos `INTERNET` y `ACCESS_NETWORK_STATE`.
- [ ] Copia de seguridad: `allowBackup="true"`, pero `xml/backup_rules.xml` y `xml/data_extraction_rules.xml` siguen siendo la plantilla vacía. Decidir si las alarmas se copian o no.
- [ ] (Oriol) Activar Windows Hypervisor Platform para el emulador.

## Cosas desfasadas (detectadas al hacer el mapa, 2026-09-30)

| Dónde | Qué dice | Realidad |
|---|---|---|
| `docs/INDICE.md`, sección "Pruebas" | 71 pruebas | Hay **104** |
| `docs/PRUEBAS_EN_EL_MOVIL.md` | Instalar `Alba-0.1.apk`; probar "con cálculo y con foto" | La versión es la 0.2 y hay 4 minijuegos más |
| `ui/editor/EditorViewModel.kt` → KDoc de `nuevaAlarma()` | "con cálculo mental" | La actividad por defecto es **SOLES** |
| `datos/Alarma.kt` → KDoc de `TipoTarea` | "su pantalla en `ui/tareas`" | La pantalla va en `ui/alarma/Juegos.kt`; en `ui/tareas` va la lógica |
