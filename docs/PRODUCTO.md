# Alba — documento de producto

Nombre provisional: **Alba**. Falta comprobar que está libre en Google Play.

## Idea

Alarma para Android que garantiza que te despiertas de verdad. Cuando suena, al
pararla la app te exige una tarea sencilla que demuestra que estás activo (foto de
un objeto de casa, cálculo mental...). Si no la completas, vuelve a sonar. El
objetivo: con **una sola alarma** te levantas y te activas.

Se publicará en Google Play y se monetizará (ver más abajo).

## Decisiones tomadas

- **Plataforma**: solo Android, nativo. Kotlin + Jetpack Compose.
- **La alarma suena siempre**, también sin internet: `AlarmManager` exacto,
  servicio en primer plano, *full-screen intent* sobre el bloqueo y volumen que sube
  poco a poco. Se prueba en un móvil real: los fabricantes cierran procesos en
  segundo plano. Móvil de pruebas: **Realme 14 Pro+** (Realme UI, Android 15), que
  es de los agresivos.
- **Tareas intercambiables**, todas detrás de la misma interfaz:
  - *Foto de categoría*: ML Kit en el móvil ("enséñale a la cámara el fregadero" y
    el modelo confirma que lo ve). Gratis y sin conexión. **Hecha en la 0.1.** El
    modelo base no conoce "nevera" ni "microondas": se piden fregadero, taza, sofá,
    tele, zapatos, planta, cubiertos y cocina.
  - *Foto ancla*: al configurar registras 3-4 objetos de tu casa (nevera, cafetera,
    cepillo...). Por la mañana se pide uno al azar y se compara con la foto de
    referencia por similitud de *embeddings*, en local. Obliga a levantarse y
    andar. Solo cámara, nunca galería. **Pendiente** (necesita un modelo de
    *embeddings* y calibrarlo con fotos reales).
  - *Cálculo mental, pasos, sacudir, QR*: sin IA. Son el respaldo sin conexión.
  - *Voz*: reconocimiento de voz de Android, que funciona sin conexión (leer una
    frase en voz alta).
- **Segunda comprobación** a los ~10 minutos ("¿sigues despierto?") para no volver
  a dormirse.
- Si no se completa la tarea, la alarma vuelve a sonar y sube el volumen.

## Diseño

- Estilo Apple: minimalista, simple, intuitivo. Mucho aire, tipografía grande y
  limpia, esquinas redondeadas, sin bordes ni sombras pesadas, animaciones suaves.
  **No debe parecer Material Design por defecto**: el tema de Compose se hace a
  medida.
- **La app va en modo oscuro**: negro puro (bien para OLED) y un solo color de
  acento.
- **La pantalla de alarma va siempre en modo claro**, para no cegar de golpe con
  un fogonazo, con mucho contraste y botones grandes.
- Nada de anuncios agresivos ni de aspecto de "app mal hecha".

## Monetización

- **Nunca hay anuncios entre que suena la alarma y que se completa la tarea.**
  Motivos: las políticas de Play y AdMob, los toques accidentales de alguien medio
  dormido, la mala experiencia y el riesgo de que un anuncio que se cuelga
  comprometa la fiabilidad de la alarma.
- Anuncios solo **después** de completar la tarea: pantalla de "buenos días" con un
  anuncio nativo o un *banner* discreto, un anuncio recompensado opcional y un
  *banner* en ajustes o estadísticas.
- **Premium** por suscripción (orientativo: 2-3 €/mes o 15-25 €/año, más una opción
  vitalicia y una prueba de 3-7 días), gestionado con RevenueCat. Incluye: sin
  anuncios, alarmas ilimitadas, todas las tareas, estadísticas y rachas, y sonidos
  extra.
- **La IA con LLM (visión y conversación) solo va en premium**: conversación
  ("cuéntame tus planes de hoy"), tareas libres ("foto de algo azul"), anti-trampa
  avanzada (detectar una foto de una pantalla) y tareas generadas cada día. Necesita
  un *backend* intermediario (Cloudflare Workers o Firebase Functions): **la clave
  de la API nunca va dentro de la app**. Límites de uso por usuario.
- El plan gratis usa solo modelos locales, así que no cuesta nada por usuario.
- La tarea de foto tiene **dos motores intercambiables** (local / LLM), para poder
  añadir el premium sin rehacer nada.

## Plan por fases

| Fase | Contenido |
|---|---|
| 0 | Entorno (JDK, SDK, Android Studio, emulador) y proyecto que compila |
| 1 | Sistema de diseño (colores, tipografía, componentes) y lista de alarmas |
| 2 | Alarma fiable: programación exacta, sonido creciente, pantalla completa sobre el bloqueo |
| 3 | Tarea de cálculo mental y sistema de tareas intercambiables |
| 4 | Foto ancla con ML Kit (categoría y similitud) |
| 5 | Segunda comprobación, reintento si falla, pulido |
| 6 | Premium (RevenueCat), IA con *backend*, anuncios después de la tarea |

**MVP = fases 0-5**, sin monetización ni *backend*.
