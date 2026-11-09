# Fase 3: motor de reproducción (plan de implementación)

> **Para agentes:** SUB-SKILL REQUERIDA: usar superpowers:executing-plans para implementar este plan tarea por tarea. Los pasos usan casillas (`- [ ]`) para el seguimiento.

**Objetivo:** tocar una canción en Mi Música la reproduce, con el resto de la lista como cola. La música sigue sonando con la pantalla bloqueada o en otra app, y se controla desde la notificación, la pantalla de bloqueo y los auriculares. El motor ya sabe hacer aleatorio y repetir (la pantalla con esos botones llega en la fase 4).

**Arquitectura** ([02-arquitectura.md](../02-arquitectura.md)):

```
LibraryViewModel ──▶ MusicPlayer (domain, interfaz)
                          ▲ implementa
                     PlayerController (playback) ──MediaController──▶ PlaybackService
                                                                    (MediaSessionService + ExoPlayer)
```

- `MusicPlayer` vive en `domain`, así la UI y sus tests no dependen de Media3.
- `PlayerController` recibe un `Player` cualquiera. En la app es un `MediaController` conectado al servicio, y en los tests un `ExoPlayer` de prueba.

**Stack nuevo:** Media3 1.5.1 (`exoplayer`, `session`), `kotlinx-coroutines-guava` (para esperar al `MediaController`) y `media3-test-utils-robolectric` (tests).

## Restricciones globales

- Las de las fases anteriores: **sin `INTERNET`** y `domain` sin `android.*`.
- Permisos nuevos: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` y `WAKE_LOCK` (para que la CPU no se duerma con la pantalla apagada).
- **No** se declara `POST_NOTIFICATIONS`: en Android 13+ la notificación de una sesión multimedia está exenta de ese permiso. *(Decisión de Claude: se comprueba en el teléfono. Si la notificación no aparece, se añade.)*
- Todo acceso al `Player` ocurre en el hilo principal.

## Foco de revisión

1. **Llamada o alarma, otra app que suena:** la música se pausa sola (foco de audio, `handleAudioFocus = true`). Tarea 3.
2. **Se desconectan los auriculares:** pausa, no sigue sonando por el altavoz (`setHandleAudioBecomingNoisy`). Tarea 3.
3. **Pantalla apagada:** sigue sonando (servicio en primer plano + `WAKE_MODE_LOCAL`). Tareas 1 y 3.
4. **Tocar una canción antes de que el servicio termine de conectar:** la orden no se pierde, se ejecuta al conectar. Tarea 4.
5. **Cerrar la app desde recientes estando en pausa:** el servicio se detiene y no deja una notificación huérfana. Si está sonando, sigue. Tarea 3.
6. **Archivo dañado o borrado:** la canción se salta, la app no se cae. Tarea 4.
7. **Repetir:** el ciclo es desactivado → toda la cola → una canción → desactivado. Tarea 2.

---

### Tarea 1: dependencias, permisos y servicio en el manifiesto

**Archivos:** `gradle/libs.versions.toml`, `app/build.gradle.kts`, `AndroidManifest.xml`, `ManifestTest.kt`

- [ ] Tests: se piden `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` y `WAKE_LOCK`. `PlaybackService` está declarado con `foregroundServiceType="mediaPlayback"` y la acción `androidx.media3.session.MediaSessionService`. Sigue sin `INTERNET`.
- [ ] Añadir Media3 y las librerías de test, y declarar permisos y servicio.

### Tarea 2: modelo de reproducción en el dominio

**Archivos:** `domain/model/PlayerState.kt`, `domain/model/RepeatMode.kt`, `domain/player/MusicPlayer.kt`, test `RepeatModeTest.kt`

```kotlin
enum class RepeatMode { Off, All, One; fun next(): RepeatMode }

data class NowPlaying(val songId: Long, val title: String, val artist: String, val album: String, val artworkUri: String)

data class PlayerState(
    val nowPlaying: NowPlaying? = null, val isPlaying: Boolean = false,
    val positionMs: Long = 0, val durationMs: Long = 0,
    val shuffle: Boolean = false, val repeatMode: RepeatMode = RepeatMode.Off,
    val queueSize: Int = 0, val currentIndex: Int = -1,
)

interface MusicPlayer {
    val state: StateFlow<PlayerState>
    fun playQueue(songs: List<Song>, startIndex: Int)
    fun togglePlayPause(); fun skipNext(); fun skipPrevious(); fun seekTo(positionMs: Long)
    fun setShuffle(enabled: Boolean); fun cycleRepeatMode()
}
```

- [ ] Test: `RepeatMode.next()` da Off → All → One → Off.

### Tarea 3: servicio de reproducción

**Archivos:** `playback/PlaybackService.kt`, `playback/MediaItems.kt`, tests `MediaItemsTest.kt`, `PlaybackServiceTest.kt` (se borra `playback/Playback.kt`)

- `Song.toMediaItem()`: `mediaId` = id, metadatos (título, artista, álbum, portada) y la URI en `requestMetadata.mediaUri`.
- `resolveMediaItems(items)`: vuelve a poner la URI desde `requestMetadata` (el `MediaController` no envía la URI de reproducción a la sesión). Es lo que usa `MediaSession.Callback.onAddMediaItems`.
- `PlaybackService`: `ExoPlayer` con `AudioAttributes(USAGE_MEDIA, MUSIC)` y `handleAudioFocus = true`, `setHandleAudioBecomingNoisy(true)` y `WAKE_MODE_LOCAL`. Tocar la notificación abre la app. `onTaskRemoved` detiene el servicio si no está sonando.
- [ ] Tests: ida y vuelta de `Song` → `MediaItem` → URI resuelta; el servicio crea su sesión y la libera al destruirse.

### Tarea 4: `PlayerController`

**Archivos:** `playback/PlayerController.kt`, `di/PlaybackModule.kt`, test `PlayerControllerTest.kt`

- Conecta el `MediaController` al primer uso. Las órdenes que llegan antes de conectar quedan en espera y se ejecutan al conectar.
- Escucha `Player.Listener.onEvents` y publica `PlayerState`.
- `playQueue` = `setMediaItems(items, startIndex, 0)` + `prepare()` + `play()`.
- `onPlayerError`: salta a la siguiente canción si existe.
- [ ] Tests con un `ExoPlayer` de prueba (`TestExoPlayerBuilder`):
  - `playQueue` publica la canción, el índice y el tamaño de la cola;
  - `skipNext` y `skipPrevious` cambian de canción;
  - aleatorio y repetir llegan al reproductor y al estado;
  - `togglePlayPause` alterna;
  - las órdenes dadas antes de conectar se ejecutan después.

### Tarea 5: tocar una canción en Mi Música

**Archivos:** `ui/library/LibraryViewModel.kt`, `ui/library/LibraryRoute.kt`, `LibraryViewModelTest.kt`

- `LibraryViewModel.onSongClick(id)` → `musicPlayer.playQueue(canciones, índice)`: la cola es toda la lista, empezando por la canción tocada.
- [ ] Test con un `MusicPlayer` falso: tocar la tercera canción manda la lista completa con índice 2. Un id que ya no existe no hace nada.

### Tarea 6: verificación final

- [ ] `./gradlew test assembleDebug` en verde.
- [ ] En el teléfono:
  - tocar una canción y que suene;
  - bloquear la pantalla y que siga sonando;
  - controlarla desde la notificación y la pantalla de bloqueo;
  - pausar y reanudar con los auriculares Bluetooth;
  - desconectar los auriculares y que pause;
  - recibir una llamada y que pause.
- [ ] Actualizar `03-hoja-de-ruta.md` y `04-bitacora.md`.
