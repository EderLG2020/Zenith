# Zenith: hoja de ruta

- **Fecha:** 2026-11-08
- **Basado en:** [01-diseno.md](01-diseno.md) · [02-arquitectura.md](02-arquitectura.md)

Cada fase termina con una app que **compila, pasa los tests (incluidos los de Konsist) y se puede abrir en el emulador**. Antes de empezar una fase se escribe su plan detallado en `doc/planes/`, con tareas pequeñas, tests primero y un commit por tarea.

| # | Fase | Qué se puede ver o probar al terminar | Plan | Estado |
|---|---|---|---|---|
| 1 | **Base del proyecto** | La app abre con tema oscuro y lima, la barra flotante navega entre 5 pantallas vacías y Konsist vigila las capas | [fase-01-base.md](planes/fase-01-base.md) | 🟡 Correcciones de la revisión aplicadas; falta probar en el teléfono y unir a `main` |
| 2 | **Biblioteca** | Pide permiso de audio y lista todas las canciones del teléfono con su portada (lista simple) | [fase-02-biblioteca.md](planes/fase-02-biblioteca.md) | 🟡 Implementada (51/51 tests); falta probarla en el teléfono |
| 3 | **Motor de reproducción** | Tocar una canción la reproduce; sigue sonando con la pantalla bloqueada; notificación y auriculares funcionan; aleatorio y repetir | [fase-03-reproduccion.md](planes/fase-03-reproduccion.md) | 🟡 Implementada (67/67 tests); falta probarla en el teléfono |
| 4 | **Pantalla Reproduciendo** | Portada circular, fondo difuminado, barra de progreso lima, controles y hoja de cola, igual que la referencia | [fase-04-reproduciendo.md](planes/fase-04-reproduciendo.md) | 🟡 Implementada (88/88 tests, captura en `capturas/fase-04.png`); falta probarla en el teléfono |
| 5 | **Pantalla Mi Música** | Chips Todo, Listas, Me gusta, Álbumes, Artistas y Carpetas; detalles de álbum, artista y carpeta; menú de pulsación larga | por escribir | ⬜ |
| 6 | **Datos del usuario** | Favoritos, listas propias, historial y conteo de reproducciones guardados en Room; se restaura la última cola | por escribir | ⬜ |
| 7 | **Pantalla Inicio** | Saludo, chips (Agregadas recientemente, Más escuchadas, Recientes), carrusel "Para ti" con "Redescubre", "Tus listas" y búsqueda | por escribir | ⬜ |
| 8 | **Mini reproductor, Historial y Ajustes** | Píldora flotante con la canción actual; pantalla de historial; ajustes de nombre, avatar, carpetas excluidas y duración mínima | por escribir | ⬜ |
| 9 | **Letras** | Letra sincronizada desde `.lrc` (3 líneas) y letra incrustada estática | por escribir | ⬜ |
| 10 | **Pulido** | Animaciones, estados vacíos y de error, prueba con más de 2000 canciones y verificación de los criterios de éxito en un teléfono real | por escribir | ⬜ |

## Tareas de cada fase

Las fases 1 a 3 ya tienen plan detallado. Las tareas de las fases 4 a 10 son una **propuesta**: al escribir el plan de cada fase se pueden dividir, unir o ajustar, y esta lista se actualiza.

Leyenda: ✅ hecha · 🟡 hecha en código, falta probarla en el teléfono · ⬜ pendiente

### Fase 1: Base del proyecto (7 tareas)
0. ✅ Entorno de desarrollo y repositorio (JDK 21 y SDK por terminal)
1. ✅ Esqueleto que compila (Compose, Hilt, catálogo de versiones, sin internet)
2. ✅ Konsist vigila la arquitectura (capas y nombres)
3. ✅ Tema de Zenith (colores, Plus Jakarta Sans, formas)
4. ✅ Componentes visuales base (chip, botón circular, portada, fila de canción)
5. ✅ Navegación con barra flotante y 5 pantallas
6. 🟡 Verificación (captura hecha; falta abrirla en el teléfono). Incluye las correcciones de la revisión.

### Fase 2: Biblioteca (7 tareas)
1. ✅ Dependencias y permisos de audio
2. ✅ Modelo `Song` y repositorio en el dominio
3. ✅ Lectura de MediaStore (metadatos faltantes, mínimo de 30 s)
4. ✅ Repositorio en segundo plano que se actualiza solo
5. ✅ ViewModel y lista de Mi Música (carga, vacío, 2500 canciones)
6. ✅ Pedir el permiso (explicación, "Abrir Ajustes", volver desde Ajustes)
7. 🟡 Verificación final (tests en verde; falta la captura y el teléfono)

### Fase 3: Motor de reproducción (6 tareas)
1. ✅ Dependencias, permisos y servicio en el manifiesto
2. ✅ Modelo de reproducción en el dominio (estado, aleatorio, repetir)
3. ✅ Servicio de reproducción (foco de audio, auriculares, pantalla apagada)
4. ✅ `PlayerController` (conexión, órdenes en espera, archivos dañados)
5. ✅ Tocar una canción en Mi Música la reproduce
6. 🟡 Verificación final (tests en verde; falta el teléfono)

### Fase 4: Pantalla Reproduciendo (8 tareas)
1. ✅ Progreso en vivo: `PlayerState` actualiza la posición cada ~500 ms mientras suena
2. ✅ `NowPlayingViewModel` y su `UiState` (canción, tiempos, play/pausa, aleatorio, repetir)
3. ✅ Portada circular grande con la misma portada difuminada de fondo y un degradado hacia negro
4. ✅ Barra superior (atrás, "Reproduciendo", corazón), título, artista y álbum
5. ✅ Barra de progreso lima arrastrable con tiempo transcurrido y restante
6. ✅ Controles: aleatorio, anterior, play/pausa lima, siguiente y cola; repetir con pulsación larga en aleatorio y su indicador
7. ✅ Hoja inferior de la cola: ver, saltar, reordenar (subir y bajar) y quitar
8. 🟡 Navegación: tocar una canción abre Reproduciendo; verificación y captura junto a la referencia

### Fase 5: Pantalla Mi Música (7 tareas)
1. ⬜ Álbumes, artistas y carpetas en el repositorio (agrupados a partir de MediaStore)
2. ⬜ Chips Todo, Listas, Me gusta, Álbumes, Artistas y Carpetas con su `UiState`
3. ⬜ Filas "Por {artista} • N canciones" para álbumes, artistas y carpetas
4. ⬜ Pantallas de detalle de álbum, artista y carpeta (misma estética)
5. ⬜ Menú ⋯: ordenar y volver a escanear
6. ⬜ Menú de pulsación larga: añadir a la cola, ver álbum y ver artista (lista y me gusta en la fase 6)
7. ⬜ Verificación y captura junto a la referencia

### Fase 6: Datos del usuario (7 tareas)
1. ⬜ Room: base de datos, entidades y DAOs (`favorite`, `playlist`, `playlist_song`, `play_event`, `playback_state`)
2. ⬜ Favoritos: repositorio y corazón en Reproduciendo y en el menú de pulsación larga
3. ⬜ Listas propias: crear, renombrar, borrar, añadir canciones y reordenar
4. ⬜ Registrar reproducciones (`RecordPlayUseCase`: al pasar el 50 % o 30 s)
5. ⬜ Guardar y restaurar la última cola, canción y posición (en pausa)
6. ⬜ Limpiar las referencias a canciones borradas en cada escaneo
7. ⬜ Chips Listas y Me gusta de Mi Música con datos reales; verificación

### Fase 7: Pantalla Inicio (7 tareas)
1. ⬜ Saludo "Hola, {nombre}" con avatar de iniciales y botones de buscar y favoritos
2. ⬜ Chips: Todo, Agregadas recientemente, Más escuchadas y Recientes
3. ⬜ `GetRediscoverMixUseCase`: mezcla diaria y determinista de canciones poco escuchadas
4. ⬜ Carrusel "Para ti": tarjetas Redescubre (lila), Tus favoritas y Mezcla de {artista}
5. ⬜ "Tus listas" y "Ver todo" hacia Mi Música
6. ⬜ Búsqueda global de canciones, artistas y álbumes
7. ⬜ Resplandor de color en la parte superior; verificación y captura junto a la referencia

### Fase 8: Mini reproductor, Historial y Ajustes (6 tareas)
1. ⬜ Mini reproductor flotante: canción actual, play/pausa y siguiente; al tocarlo abre Reproduciendo
2. ⬜ Pantalla Historial agrupada por día
3. ⬜ DataStore de preferencias: nombre, avatar, carpetas excluidas y duración mínima
4. ⬜ Pantalla Ajustes con esas opciones, "Volver a escanear" y "Acerca de"
5. ⬜ Aplicar carpetas excluidas y duración mínima al escaneo
6. ⬜ Verificación

### Fase 9: Letras (5 tareas)
1. ⬜ `ParseLrcUseCase`: lector de `.lrc` (tiempos, varias marcas por línea, offset)
2. ⬜ Buscar el `.lrc` con el mismo nombre en la carpeta de la canción
3. ⬜ Leer la letra incrustada sin tiempos como alternativa
4. ⬜ Tres líneas en Reproduciendo (anterior, actual y siguiente) sincronizadas con la canción; letra estática desplazable
5. ⬜ Verificación con archivos reales

### Fase 10: Pulido (6 tareas)
1. ⬜ Animaciones: del mini reproductor a Reproduciendo y cambio de portada
2. ⬜ Estados vacíos y de error en todas las pantallas
3. ⬜ Rendimiento con más de 2000 canciones (escaneo, listas y portadas)
4. ⬜ Ajustes visuales anotados en la bitácora (iconos de contorno, tamaño del círculo activo)
5. ⬜ Accesibilidad: letra grande, descripciones para TalkBack y contraste
6. ⬜ Verificar los criterios de éxito del diseño (§8) en un teléfono real

## Dependencias entre fases

```
1 ──▶ 2 ──▶ 3 ──▶ 4
            │
            ├──▶ 5 ──▶ 6 ──▶ 7 ──▶ 8
            │
            └──▶ 9 (necesita 4)
                              todo ──▶ 10
```

## Cómo se trabaja cada fase

1. Escribir `doc/planes/fase-NN-<nombre>.md` y revisarlo.
2. Ejecutar tarea por tarea: test que falla → código → test que pasa → commit.
3. Al final de la fase: `./gradlew test`, abrir la app en el emulador y comparar con `referencia.png`.
4. Marcar la fase como ✅ en esta tabla.
