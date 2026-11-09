# Fase 2: biblioteca (plan de implementación)

> **Para agentes:** SUB-SKILL REQUERIDA: usar superpowers:executing-plans para implementar este plan tarea por tarea. Los pasos usan casillas (`- [ ]`) para el seguimiento.

**Objetivo:** al abrir **Mi Música**, la app pide el permiso de audio y, una vez concedido, muestra en una lista simple todas las canciones del teléfono con su portada (o el placeholder de degradado si no tiene).

**Arquitectura:** primera vez que se llenan las capas `domain` y `data` ([02-arquitectura.md](../02-arquitectura.md)):

```
LibraryScreen ──▶ LibraryViewModel ──▶ SongRepository (domain, interfaz)
                                              ▲ implementa
                                       SongRepositoryImpl (data)
                                              │ usa
                                       SongSource ◀── MediaStoreSongSource (MediaStore + ContentObserver)
```

**Stack nuevo:** Coil 3 (`coil-compose`, **sin** el módulo de red), `kotlinx-coroutines-test` y `hilt-navigation-compose` (ya instalado) para `hiltViewModel()`.

**Spec:** [01-diseno.md §3 (permisos), §5.3 y §7](../01-diseno.md)

## Restricciones globales

- Las de la [fase 1](fase-01-base.md#restricciones-globales), en especial: **sin permiso `INTERNET`**.
- Permisos: `READ_MEDIA_AUDIO` (Android 13+) y `READ_EXTERNAL_STORAGE` con `maxSdkVersion="32"`.
- Solo se listan archivos con `IS_MUSIC != 0` y duración mínima de **30 s** (diseño §5.4). La duración mínima configurable llega en la fase 8; aquí es una constante.
- El escaneo de MediaStore corre fuera del hilo principal (`Dispatchers.IO`).
- `domain` no conoce `android.*`: las URIs viajan como `String`.
- Comando de verificación: `./gradlew test` (con `JAVA_HOME` en el JDK 21).

## Foco de revisión

1. **Permiso negado:** pantalla amable con "Permitir acceso". Si el sistema ya no muestra el diálogo (negado dos veces), el botón pasa a "Abrir Ajustes". Tarea 6.
2. **Permiso concedido desde Ajustes del sistema:** al volver a la app, la lista aparece sin reiniciar. Tarea 6.
3. **Canción sin portada:** se ve el placeholder de degradado, nunca un hueco vacío. Tarea 5.
4. **Metadatos faltantes:** artista `<unknown>` → "Artista desconocido"; título vacío → nombre del archivo sin extensión. Tarea 3.
5. **Biblioteca grande (más de 2000 canciones):** la consulta no corre en el hilo principal y la lista es `LazyColumn` con `key`. Tareas 4 y 5.
6. **Se borra o añade una canción con la app abierta:** la lista se actualiza sola (ContentObserver). Tarea 4.
7. **Teléfono sin música:** mensaje de lista vacía, no una pantalla en blanco. Tarea 5.

---

### Tarea 1: dependencias y permisos

**Archivos:** `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/test/.../ManifestTest.kt`

- [ ] Test (Robolectric, `PackageManager.getPackageInfo(..., GET_PERMISSIONS)`): la app pide `READ_MEDIA_AUDIO` y `READ_EXTERNAL_STORAGE`, y **no** pide `INTERNET`.
- [ ] Añadir al catálogo `coil = "3.0.4"` (`io.coil-kt.coil3:coil-compose`) y `coroutines = "1.9.0"` (`kotlinx-coroutines-test`).
- [ ] Declarar los permisos en el manifiesto (`READ_EXTERNAL_STORAGE` con `android:maxSdkVersion="32"`).
- [ ] `./gradlew test` en verde.

### Tarea 2: modelo y repositorio en el dominio

**Archivos:** `domain/model/Song.kt`, `domain/repository/SongRepository.kt` (se borra `domain/Domain.kt`)

```kotlin
data class Song(
    val id: Long, val title: String, val artist: String, val album: String, val albumId: Long,
    val durationMs: Long, val uri: String, val artworkUri: String, val folder: String,
    val dateAddedSec: Long, val trackNumber: Int,
)

interface SongRepository {
    /** Canciones ordenadas por título; vuelve a emitir cuando cambia la biblioteca del teléfono. */
    fun observeSongs(): Flow<List<Song>>
}
```

- [ ] Konsist sigue en verde (el dominio no importa `android.*`).

### Tarea 3: lectura de MediaStore

**Archivos:** `data/mediastore/SongSource.kt`, `data/mediastore/MediaStoreSongSource.kt`, test `MediaStoreSongSourceTest.kt`

- `SongSource` (interfaz en `data`): `fun query(): List<Song>` y `fun changes(): Flow<Unit>`.
- `MediaStoreSongSource` consulta `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` con `IS_MUSIC != 0 AND DURATION >= ?`, orden `TITLE COLLATE NOCASE`.
  - `uri` = `ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id)`.
  - `artworkUri` = `content://media/external/audio/albums/{albumId}` (Coil la reconoce y usa `loadThumbnail` en Android 10+).
  - `folder` = `RELATIVE_PATH` sin la barra final (Android 10+) o la carpeta padre de `DATA` (Android 9 o anterior).
  - `changes()` = `callbackFlow` con un `ContentObserver` registrado en la URI de audio.
- [ ] Tests con un `ContentProvider` falso registrado en la autoridad `media` (Robolectric):
  - convierte cada columna al modelo `Song`;
  - `<unknown>` → "Artista desconocido" y "Álbum desconocido";
  - título vacío → nombre del archivo sin extensión;
  - la consulta pide `IS_MUSIC` y la duración mínima de 30 000 ms;
  - `changes()` emite cuando se llama `notifyChange` sobre la URI de audio.

### Tarea 4: implementación del repositorio

**Archivos:** `data/repository/SongRepositoryImpl.kt`, `di/DataModule.kt`, `di/DispatchersModule.kt`, test `SongRepositoryImplTest.kt` (se borran `data/Data.kt` y `di/Di.kt`)

```kotlin
override fun observeSongs() = source.changes()
    .onStart { emit(Unit) }
    .conflate()
    .map { source.query() }
    .flowOn(ioDispatcher)
```

- [ ] Tests con un `SongSource` falso: emite al empezar; vuelve a emitir con cada cambio; la consulta corre en el dispatcher inyectado (no en el del colector).
- [ ] Módulo de Hilt: `@Binds SongRepository → SongRepositoryImpl`, `@Binds SongSource → MediaStoreSongSource`, `@IoDispatcher CoroutineDispatcher`.

### Tarea 5: ViewModel y lista de Mi Música

**Archivos:** `ui/library/LibraryUiState.kt`, `ui/library/LibraryViewModel.kt`, `ui/library/LibraryScreen.kt`, `ui/components/SongCover.kt`, `ui/format/Duration.kt`, tests `DurationTest.kt`, `LibraryViewModelTest.kt`, `LibraryScreenTest.kt`

```kotlin
sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data object Empty : LibraryUiState
    data class Songs(val songs: List<SongItem>) : LibraryUiState
}
data class SongItem(val id: Long, val title: String, val subtitle: String, val artworkUri: String)
```

- `formatDuration(ms)`: `0:00`, `3:20`, `1:02:03`.
- `subtitle` = "{artista} • {duración}" (diseño §5.3).
- `SongCover`: `AsyncImage` de Coil; mientras carga o si falla, `CoverPlaceholder`.
- `LibraryScreen` es sin estado: recibe `LibraryUiState`. Título "Mi Música", contador "N canciones" y `LazyColumn` de `SongRow` con `key = id`, con espacio inferior para la barra flotante.
- [ ] Tests: `formatDuration`; el ViewModel pasa de `Loading` a `Songs` y a `Empty`, y se actualiza cuando el repositorio emite de nuevo; la pantalla muestra filas, el mensaje de vacío o el indicador de carga.

### Tarea 6: pedir el permiso

**Archivos:** `ui/permission/AudioPermission.kt`, `ui/permission/AudioPermissionGate.kt`, `ui/library/LibraryRoute.kt`, `ui/navigation/ZenithNavHost.kt`, test `AudioPermissionGateTest.kt`

- `audioPermission()` = `READ_MEDIA_AUDIO` en Android 13+ y `READ_EXTERNAL_STORAGE` en versiones anteriores.
- `AudioPermissionGate(content)`: si hay permiso, muestra `content`; si no, la pantalla "Zenith necesita acceso a tu música" con el botón "Permitir acceso" (o "Abrir Ajustes" si el sistema ya no muestra el diálogo). Vuelve a comprobar el permiso en cada `ON_RESUME`.
- `LibraryRoute` = `AudioPermissionGate { LibraryScreen(hiltViewModel<LibraryViewModel>().state) }`: el ViewModel solo se crea con el permiso concedido, así la consulta nunca lanza `SecurityException`.
- [ ] Tests: el nombre del permiso según la versión (`@Config(sdk = [32])` y `[33]`); sin permiso se ve la explicación y el botón; con permiso se ve el contenido; conceder el permiso y emitir `ON_RESUME` muestra el contenido.

### Tarea 7: verificación final

- [ ] `./gradlew test assembleDebug` en verde.
- [ ] Captura con Robolectric de Mi Música con canciones falsas en `doc/capturas/fase-02.png`.
- [ ] En el teléfono: conceder el permiso, ver todas las canciones y sus portadas, negarlo y ver la explicación.
- [ ] Actualizar `03-hoja-de-ruta.md` y `04-bitacora.md`.
