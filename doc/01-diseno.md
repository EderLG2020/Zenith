# Zenith: reproductor de música local para Android (diseño)

- **Fecha:** 2026-11-08
- **Estado:** aprobado
- **Referencia visual:** [`doc/referencia.png`](referencia.png)

---

## 1. Intención

Zenith es una app Android para escuchar **únicamente la música que ya está descargada en el teléfono**. No usa internet, no tiene cuentas ni streaming. Su valor está en la experiencia: una interfaz oscura, moderna y cuidada, fiel a la imagen de referencia (tres pantallas: Inicio, Reproduciendo y Mi Música), adaptada a lo que una biblioteca local puede ofrecer realmente.

## 2. Objetivos, no-objetivos y anti-objetivos

### Objetivos (v1)
1. Detectar automáticamente todas las canciones del teléfono y mostrarlas organizadas.
2. Reproducir con calidad, también en segundo plano, con controles en la notificación, la pantalla de bloqueo y los auriculares Bluetooth.
3. Reproducir las tres pantallas del diseño con alta fidelidad visual.
4. Guardar favoritos, listas de reproducción, historial y conteo de reproducciones de forma local.
5. Mostrar la letra sincronizada cuando exista un archivo `.lrc` junto a la canción.

### No-objetivos (fuera de la v1)
- Streaming, descarga de música o cualquier función que necesite internet.
- Búsqueda de letras o portadas en línea.
- Ecualizador, edición de etiquetas (ID3), widgets de escritorio, Android Auto, Wear OS.
- Versión para iOS.

### Anti-objetivos (aunque funcione, sería un fracaso si…)
- Se ve como una app genérica de Material Design y no como el diseño de referencia.
- La música se corta al bloquear la pantalla o al cambiar de app.
- Pide permisos innecesarios (ubicación, contactos, internet) o recolecta datos.
- Se traba al escanear una biblioteca grande (más de 2000 canciones).

## 3. Decisiones de plataforma y tecnología

| Tema | Decisión | Origen |
|---|---|---|
| Plataforma | Solo Android | Usuario |
| Lenguaje | Kotlin nativo | Usuario |
| UI | Jetpack Compose + Material 3 (fuertemente personalizado) | Claude |
| Reproducción | AndroidX Media3 (ExoPlayer + `MediaSessionService`) | Claude |
| Fuente de música | `MediaStore.Audio` del sistema | Claude |
| Persistencia local | Room (favoritos, listas, historial, conteos) + DataStore (preferencias) | Claude |
| Imágenes | Coil (portadas incrustadas vía `ContentUris` de álbum / `MediaMetadataRetriever`) | Claude |
| Inyección de dependencias | Hilt | Claude |
| Arquitectura | MVVM + UDF según la guía oficial de Google, ver [02-arquitectura.md](02-arquitectura.md) | Claude |
| Verificación de arquitectura | Konsist (tests de capas y convenciones) | Usuario |
| Navegación | Navigation Compose | Claude |
| Versiones | `minSdk 26` (Android 8.0), `targetSdk`/`compileSdk` 35 | Claude |
| Build | Gradle Kotlin DSL + version catalog (`libs.versions.toml`) | Claude |
| Identificador | Nombre visible "Zenith", `applicationId` `com.zenith.player` | Claude |
| Idioma de la UI | Español | Claude |

### Permisos
- `READ_MEDIA_AUDIO` (Android 13+) / `READ_EXTERNAL_STORAGE` (Android 12 o anterior).
- `POST_NOTIFICATIONS` (Android 13+) para la notificación de reproducción.
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK`.
- **Sin** permiso `INTERNET`.

Si el usuario niega el permiso de audio, se muestra una pantalla amable que explica por qué hace falta, con un botón para volver a pedirlo o abrir Ajustes.

## 4. Lenguaje visual (extraído de la referencia)

- **Fondo:** casi negro (`#0E0E12` aprox.), con un resplandor de color difuminado en la parte superior de Inicio.
- **Superficies:** gris muy oscuro (`#1C1C22` aprox.) para chips, botones circulares y tarjetas.
- **Acento:** verde lima (`#C6E14B` aprox.) para el chip activo, el botón play/pausa, la barra de progreso y la pestaña activa.
- **Tarjeta destacada:** lila/violeta (`#C9A3F0` aprox.) con botón play morado oscuro.
- **Texto:** blanco para títulos y gris medio (`#8A8A93`) para subtítulos.
- **Tipografía:** sans geométrica, como **Plus Jakarta Sans** (Google Fonts, empaquetada en la app). Títulos de sección en ~20sp, y títulos de canción en semibold de ~15sp.
- **Formas:** chips tipo píldora, portadas con esquinas de ~14dp, botones de icono circulares, barra de navegación flotante tipo píldora con efecto translúcido.
- **Portada en Reproduciendo:** circular y grande; el fondo de la pantalla es la misma portada muy difuminada, con un degradado hacia negro.
- **Sin portada:** placeholder con un degradado generado a partir del título (siempre el mismo color para la misma canción) y un icono de nota.
- Solo tema oscuro en la v1.

## 5. Pantallas y su adaptación a música local

### 5.1 Inicio
| Elemento del diseño | Adaptación local |
|---|---|
| Avatar + "Hi, Samantha" | "Hola, {nombre}" con el nombre configurable en Ajustes (por defecto, solo "Hola"). El avatar muestra las iniciales; se puede elegir una imagen de la galería (opcional). |
| Botones de buscar y favoritos | Buscar abre la búsqueda global (canciones, artistas y álbumes); el corazón abre "Me gusta". |
| Chips: All / New Release / Trending / Top | **Todo / Agregadas recientemente / Más escuchadas / Recientes** |
| "Curated & trending", carrusel horizontal | **"Para ti"**: carrusel de tarjetas generadas localmente |
| Tarjeta "Discover weekly" | **"Redescubre"**: mezcla aleatoria de canciones poco escuchadas. Tiene play, favorito (guarda la mezcla como lista), aleatorio y menú. Otras tarjetas del carrusel: "Tus favoritas" y "Mezcla de {artista más escuchado}". |
| "Top daily playlists" + "See all" | **"Tus listas"**: listas del usuario y álbumes más escuchados; "Ver todo" lleva a Mi Música. |

Contenido de los chips:
- **Todo:** la vista descrita arriba.
- **Agregadas recientemente:** por `DATE_ADDED`.
- **Más escuchadas:** por conteo de reproducciones.
- **Recientes:** por historial.

### 5.2 Reproduciendo
- Barra superior: atrás (minimiza al mini reproductor), título "Reproduciendo" y corazón (favorito).
- Portada circular grande sobre un fondo difuminado de la misma portada.
- Título y artista (y álbum si cabe).
- **Letra:** si existe un `.lrc` con el mismo nombre en la misma carpeta, se muestran tres líneas (anterior atenuada, actual blanca y siguiente atenuada) que avanzan con la canción. Si no hay `.lrc` pero el archivo trae letra incrustada sin tiempos, se muestra estática y desplazable. Si no hay nada, la zona queda vacía y la portada se centra.
- Barra de progreso lima arrastrable, con tiempo transcurrido y restante.
- Controles: aleatorio, anterior, **play/pausa (círculo lima)**, siguiente y cola (abre una hoja inferior con la cola, que permite reordenar y quitar canciones).
- Repetir (desactivado, toda la cola o una sola canción): se activa con pulsación larga en aleatorio, y su estado se indica con un pequeño punto o un "1" sobre el icono. *(Decisión de Claude, puede cambiarse.)*

### 5.3 Mi Música
- Barra superior: atrás, "Mi Música" y menú ⋯ (ordenar, volver a escanear).
- Chips: **Todo / Listas / Me gusta / Álbumes / Artistas / Carpetas** (sustituye "Downloads" por "Carpetas").
- Filas: portada, título y "Por {artista} • N canciones" (en listas, álbumes y artistas) o "{artista} • {duración}" (en canciones), con un botón play circular a la derecha.
- Tocar una fila de canción la reproduce dentro del contexto de esa lista; tocar un álbum, una lista o un artista abre su detalle (misma estética).
- Pulsación larga: añadir a lista, añadir a la cola, me gusta, ver álbum o artista.

### 5.4 Elementos globales
- **Barra de navegación flotante** (píldora translúcida, 5 iconos, el activo en círculo lima): Inicio · Mi Música · Historial · Listas · Ajustes.
- **Mini reproductor flotante** (píldora pequeña, como en la tercera pantalla): visible cuando hay algo sonando y no se está en Reproduciendo. Muestra play/pausa y siguiente o aleatorio; al tocarlo se abre Reproduciendo.
- **Ajustes:** nombre y avatar, carpetas excluidas, duración mínima (para ignorar notas de voz o tonos de menos de 30 s por defecto), volver a escanear y "acerca de".

## 6. Comportamiento de reproducción

- Servicio en primer plano con Media3: la música sigue al bloquear o cambiar de app.
- Notificación multimedia y controles de pantalla de bloqueo automáticos (MediaSession).
- Respeta el foco de audio: pausa en llamadas o cuando otra app suena, y pausa al desconectar los auriculares.
- Al reabrir la app se restaura la última cola, la canción y la posición (pausada).
- Una reproducción cuenta en historial y conteo al superar el 50 % de la canción o 30 s, lo que ocurra primero.
- Formatos: los que soporte ExoPlayer de forma nativa (MP3, AAC/M4A, FLAC, OGG, OPUS, WAV).

## 7. Datos

**De MediaStore (solo lectura):** id, título, artista, álbum, id de álbum, duración, ruta o URI, carpeta, fecha de alta y número de pista.

**En Room:**
- `favorite(songId, addedAt)`
- `playlist(id, name, createdAt)` + `playlist_song(playlistId, songId, position)`
- `play_event(songId, playedAt)`: alimenta Recientes, Historial y Más escuchadas.
- `playback_state`: cola, índice, posición, aleatorio y repetición.

**En DataStore:** nombre de usuario, URI del avatar, carpetas excluidas y duración mínima.

Si una canción se borra del teléfono, sus referencias en Room se ignoran al mostrar y se limpian en el siguiente escaneo.

## 8. Criterios de éxito (v1)

1. En un teléfono real con música descargada, la app la encuentra toda sin configurar nada.
2. Una captura de cada pantalla, junto a la referencia, se reconoce como el mismo diseño.
3. Se puede escuchar un álbum completo con la pantalla bloqueada, controlándolo desde la notificación y los auriculares.
4. Favoritos, listas e historial sobreviven a cerrar y reabrir la app.
5. El escaneo de 2000 canciones no bloquea la interfaz.

## 9. Áreas de trabajo (orden propuesto)

El plan detallado, con tareas pequeñas y verificables, se escribirá aparte tras aprobar este documento. A grandes rasgos:

1. **Base del proyecto:** proyecto Android con Compose, Hilt, catálogo de versiones, tema (colores, tipografía, formas) y navegación vacía con la barra flotante.
2. **Biblioteca:** permisos, escaneo de MediaStore, repositorio de canciones, álbumes, artistas y carpetas, y portadas.
3. **Motor de reproducción:** servicio Media3, MediaSession, conexión desde la UI, cola, aleatorio, repetición y restauración de estado.
4. **Pantalla Reproduciendo:** portada circular, fondo difuminado, progreso, controles y hoja de cola.
5. **Pantalla Mi Música:** chips, listas, detalles de álbum, artista y lista, y menú contextual.
6. **Datos del usuario:** Room (favoritos, listas, historial y conteos) y su integración.
7. **Pantalla Inicio:** saludo, chips, carrusel "Para ti", "Tus listas" y búsqueda.
8. **Mini reproductor, Historial y Ajustes.**
9. **Letras:** lector de `.lrc` y letra incrustada.
10. **Pulido:** animaciones, estados vacíos y de error, pruebas en un dispositivo real y rendimiento con bibliotecas grandes.

## 10. Lo que queda a criterio de quien construya

- Estructura exacta de paquetes y nombres de clases.
- Animaciones concretas (transición del mini reproductor a Reproduciendo, cambio de portada).
- Algoritmo exacto de "Redescubre" y de las mezclas, siempre que sea local y determinista por día.
- Ajuste fino de colores y medidas comparando con la referencia.
- Estrategia de pruebas: pruebas unitarias para el lector `.lrc`, los repositorios y la lógica de cola, y pruebas manuales en un dispositivo para la UI y la reproducción en segundo plano.

## 11. Decisiones tomadas por Claude (revisar)

- Stack: Compose, Media3, Room, Hilt, Coil y `minSdk 26`.
- Mapeo de chips: Trending → Más escuchadas, New Release → Agregadas recientemente, Downloads → Carpetas.
- "Discover weekly" → "Redescubre" (canciones poco escuchadas).
- Los 5 iconos de la barra inferior: Inicio, Mi Música, Historial, Listas y Ajustes.
- Solo tema oscuro; tipografía Plus Jakarta Sans.
- Duración mínima de 30 s para ignorar audios cortos.
- Repetir mediante pulsación larga en aleatorio.
