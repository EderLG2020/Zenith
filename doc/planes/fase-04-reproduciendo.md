# Fase 4: pantalla Reproduciendo (plan de implementación)

> **Para agentes:** SUB-SKILL REQUERIDA: usar superpowers:executing-plans para implementar este plan tarea por tarea. Los pasos usan casillas (`- [ ]`) para el seguimiento.

**Objetivo:** al tocar una canción se abre **Reproduciendo**, igual que la pantalla central de `doc/referencia.png`:
- portada circular grande sobre la misma portada difuminada;
- título y artista;
- barra de progreso lima arrastrable con tiempo transcurrido y restante;
- controles: aleatorio, anterior, play/pausa lima, siguiente y cola;
- hoja de la cola para saltar, reordenar y quitar canciones.

**Arquitectura:** `NowPlayingScreen` (sin estado) ← `NowPlayingViewModel` ← `MusicPlayer` (dominio). La cola se publica en un `StateFlow` aparte (`MusicPlayer.queue`), porque cambia poco y puede tener miles de canciones, mientras que la posición cambia cada medio segundo.

**Spec:** [01-diseno.md §5.2](../01-diseno.md)

## Restricciones globales

- Las de las fases anteriores.
- Reproduciendo ocupa toda la pantalla: **sin** barra flotante. Atrás vuelve a la pantalla anterior.
- El difuminado (`Modifier.blur`) solo existe en Android 12+. En versiones anteriores, el fondo es la portada muy oscurecida.
- El corazón se muestra, pero guardar favoritos llega en la fase 6. Hasta entonces el botón no hace nada.
- La zona de la letra queda vacía (fase 9): la portada se centra.

## Foco de revisión

1. **Arrastrar la barra de progreso:** mientras se arrastra, la posición no "salta" hacia atrás con los ticks del reproductor; solo se busca al soltar. Tarea 5.
2. **Canción sin duración conocida (0):** la barra no divide entre cero, y el tiempo restante muestra `-0:00`. Tareas 2 y 5.
3. **Abrir Reproduciendo sin nada sonando** (por ejemplo, al recrear la app): mensaje "Nada sonando" en lugar de una pantalla rota. Tarea 2.
4. **Quitar de la cola la canción que suena:** pasa a la siguiente. Tarea 7.
5. **Lectores de pantalla:** cada control tiene descripción, y la barra de progreso se puede ajustar con TalkBack. Tareas 5 y 6.

---

### Tarea 1: progreso en vivo y cola en el reproductor

**Archivos:** `domain/player/MusicPlayer.kt`, `playback/PlayerController.kt`, `PlayerControllerTest.kt`

- Mientras suena, `PlayerController` publica la posición cada 500 ms y deja de hacerlo al pausar.
- `MusicPlayer.queue: StateFlow<List<NowPlaying>>` se actualiza solo cuando cambia la cola.
- Órdenes nuevas: `skipToQueueItem(index)`, `moveQueueItem(from, to)` y `removeQueueItem(index)`.
- [ ] Tests:
  - la posición avanza en el estado sin otros eventos (con un `SimpleBasePlayer` falso);
  - la cola refleja `playQueue`, mover y quitar;
  - saltar a un elemento cambia de canción.

### Tarea 2: `NowPlayingViewModel`

**Archivos:** `ui/nowplaying/NowPlayingUiState.kt`, `ui/nowplaying/NowPlayingViewModel.kt`, `ui/format/Duration.kt`, tests

- `NowPlayingUiState`: `song` (título, artista, álbum, portada) o `null`, `positionMs`, `durationMs`, `elapsed`, `remaining` (`-2:15`), `isPlaying`, `shuffle`, `repeatMode`, `queue` y `currentIndex`.
- Eventos: `onPlayPause`, `onNext`, `onPrevious`, `onSeek(ms)`, `onShuffleClick`, `onShuffleLongClick` (cambia el modo de repetir), `onQueueItemClick`, `onMoveQueueItem` y `onRemoveQueueItem`.
- [ ] Tests:
  - `formatRemaining`;
  - el estado se arma desde `PlayerState`;
  - sin canción, `song = null`;
  - cada evento llega al `MusicPlayer`.

### Tarea 3: fondo difuminado y portada circular

**Archivos:** `ui/nowplaying/NowPlayingBackground.kt`, `ui/nowplaying/CircularCover.kt`

- Fondo: portada a pantalla completa con `blur(60.dp)` (Android 12+), opacidad 0.6 y degradado vertical hacia `Background` desde la mitad.
- Portada: `SongCover` recortada en círculo, con un ancho del 72 % de la pantalla y un máximo de 320 dp.

### Tarea 4: barra superior y textos

**Archivos:** `ui/nowplaying/NowPlayingScreen.kt`

- Botón atrás y corazón translúcidos (`SurfaceHigh` al 60 %), y título "Reproduciendo" centrado.
- Título en 20 sp semibold, centrado y en una sola línea con "…". Debajo, "artista" en gris.

### Tarea 5: barra de progreso lima

**Archivos:** `ui/nowplaying/SeekBar.kt`, test `SeekBarTest.kt`

- Pista gris de 3 dp, parte recorrida lima y círculo lima de 12 dp. Tiempos debajo, a la izquierda y a la derecha.
- Se puede tocar o arrastrar. Mientras se arrastra se muestra la posición del dedo, y al soltar llama a `onSeek`.
- Semántica de progreso con `setProgress`, para TalkBack.
- [ ] Tests:
  - `setProgress(0.5)` busca la mitad de la duración;
  - con duración 0 no se cae.

### Tarea 6: controles

**Archivos:** `ui/nowplaying/PlaybackControls.kt`, test `PlaybackControlsTest.kt`

- Aleatorio (lima si está activo; la pulsación larga cambia el modo de repetir, con un punto si es "toda la cola" y un "1" si es "una canción").
- Anterior y siguiente en círculos oscuros de 48 dp, play/pausa en un círculo lima de 68 dp, y el botón de la cola.
- [ ] Tests:
  - cada botón llama a su evento;
  - la pulsación larga en aleatorio cambia el modo de repetir;
  - la descripción alterna entre "Pausar" y "Reproducir";
  - los indicadores de repetir.

### Tarea 7: hoja de la cola

**Archivos:** `ui/nowplaying/QueueSheet.kt`, test `QueueSheetTest.kt`

- `ModalBottomSheet` con "A continuación" y la lista. La canción actual se resalta en lima.
- Tocar una fila salta a esa canción. Cada fila tiene "Subir", "Bajar" y "Quitar" (los botones son más accesibles y fiables que arrastrar; arrastrar puede llegar en la fase 10).
- [ ] Tests:
  - tocar, subir, bajar y quitar llaman a sus eventos con el índice correcto;
  - la primera fila no tiene "Subir" y la última no tiene "Bajar".

### Tarea 8: navegación y verificación

**Archivos:** `ui/navigation/ZenithNavHost.kt`, `ui/ZenithRoot.kt`, `ui/library/LibraryRoute.kt`, `ui/nowplaying/NowPlayingRoute.kt`, `NavigationTest.kt`

- Ruta `now_playing` fuera de las pestañas. Tocar una canción en Mi Música la reproduce y abre Reproduciendo.
- `ZenithRoot` oculta la barra flotante en Reproduciendo.
- [ ] Tests: la barra se oculta en `now_playing` y se ve en las pestañas.
- [ ] `./gradlew test assembleDebug` en verde.
- [ ] Captura con Robolectric en `doc/capturas/fase-04.png`, para compararla con la referencia.
- [ ] Actualizar la hoja de ruta y la bitácora.
