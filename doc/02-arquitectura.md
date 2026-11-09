# Zenith: arquitectura

- **Fecha:** 2026-11-08
- **Estado:** aprobado
- **Relacionado:** [01-diseno.md](01-diseno.md)

---

## 1. Qué arquitectura usamos y por qué

En Android no existe un framework que imponga la arquitectura como lo hace Spring en backend. Lo que existe es la **guía oficial de arquitectura de Google** ("Guide to app architecture", developer.android.com/topic/architecture), que es el estándar de facto. Zenith la sigue:

- **MVVM con flujo de datos unidireccional (UDF):** la pantalla muestra un `UiState` que viene del `ViewModel`, y la pantalla le envía eventos al `ViewModel`. El estado nunca se modifica desde la UI.
- **Tres capas:** UI → Dominio → Datos. Las dependencias solo apuntan hacia abajo.
- **Una capa extra de reproducción**, porque la música vive en un servicio de Android (Media3) y no en la pantalla.

Es una "Clean Architecture" pragmática: separa responsabilidades sin crear interfaces y casos de uso para todo. Los casos de uso se crean solo cuando hay lógica real que no pertenece a un repositorio.

## 2. Diagrama de capas

```
┌──────────────────────────────────────────────────────────┐
│ UI  (Jetpack Compose)                                    │
│   Screen (Composable)  ──eventos──▶  ViewModel           │
│          ▲                              │                │
│          └────────── UiState (StateFlow)┘                │
└───────────────────────────┬──────────────────────────────┘
                            │ usa
┌───────────────────────────▼──────────────────────────────┐
│ DOMINIO  (Kotlin puro, sin Android)                      │
│   Modelos: Song, Album, Artist, Playlist, LyricLine      │
│   Casos de uso: GetRediscoverMixUseCase,                 │
│                 RecordPlayUseCase, ParseLrcUseCase       │
│   Interfaces: SongRepository, PlaylistRepository, ...    │
└───────────────┬───────────────────────────┬──────────────┘
                │ implementa                │ implementa
┌───────────────▼──────────────┐ ┌──────────▼───────────────┐
│ DATOS                        │ │ REPRODUCCIÓN             │
│  MediaStoreDataSource        │ │  PlaybackService         │
│  Room (favoritos, listas,    │ │   (MediaSessionService + │
│        historial, estado)    │ │    ExoPlayer)            │
│  DataStore (preferencias)    │ │  PlayerController        │
│  Repositorios (impl.)        │ │   (MediaController →     │
│                              │ │    StateFlow para la UI) │
└──────────────────────────────┘ └──────────────────────────┘
```

## 3. Estructura de paquetes

Usamos un solo módulo de Gradle (`app`) con paquetes por capa. Para una app de una sola persona, varios módulos añaden complejidad sin beneficio. Si crece, cada paquete de primer nivel se puede convertir en módulo sin reescribir nada.

```
com.zenith.player
├── ZenithApp.kt                 // @HiltAndroidApp
├── MainActivity.kt
├── ui/
│   ├── theme/                   // colores, tipografía, formas
│   ├── components/              // chips, botones circulares, filas, barra flotante, mini player
│   ├── navigation/              // NavHost y rutas
│   ├── home/                    // HomeScreen + HomeViewModel + HomeUiState
│   ├── nowplaying/
│   ├── library/                 // Mi Música y detalles
│   ├── history/
│   ├── playlists/
│   └── settings/
├── domain/
│   ├── model/
│   ├── repository/              // interfaces
│   └── usecase/
├── data/
│   ├── mediastore/
│   ├── local/                   // Room: entidades, DAOs, base de datos
│   ├── preferences/             // DataStore
│   └── repository/              // implementaciones
├── playback/
│   ├── PlaybackService.kt
│   └── PlayerController.kt
└── di/                          // módulos de Hilt
```

## 4. Reglas de dependencia

| Paquete | Puede usar | No puede usar |
|---|---|---|
| `ui` | `domain` (incluida la interfaz `MusicPlayer`, que implementa `playback.PlayerController`) | `data`, Room o MediaStore directamente |
| `domain` | solo Kotlin y coroutines | `android.*`, `data`, `ui`, `playback` |
| `data` | `domain` | `ui`, `playback` |
| `playback` | `domain` | `ui` |
| `di` | todo (es el pegamento) | — |

Otras reglas:
- Los `ViewModel` exponen un único `StateFlow<XUiState>` y funciones `onX(...)` para los eventos.
- Los Composables de pantalla no contienen lógica. Reciben el estado y lambdas.
- Las entidades de Room nunca salen de `data`; se convierten a modelos de `domain`.

## 5. Herramientas para definir y vigilar la arquitectura

El equivalente a lo que se usa en backend:

| En backend (Spring/Java) | En Android/Kotlin | Uso en Zenith |
|---|---|---|
| **Spring Initializr** (genera el esqueleto) | Plantilla de Android Studio "Empty Activity" (Compose) | Sí, como punto de partida |
| **ArchUnit** (tests que verifican capas) | **Konsist**, el ArchUnit de Kotlin (ArchUnit también funciona en Android) | Sí: tests que fallan si `ui` importa `data` o si `domain` usa `android.*` |
| **Spring Modulith** (módulos y límites) | Módulos de Gradle + `dependency-analysis-gradle-plugin` | Más adelante, si se divide en módulos |
| Checkstyle / SpotBugs | **Detekt** (calidad) + **ktlint** (formato) + **Android Lint** | Sí |
| Proyecto de referencia | **Now in Android** (app oficial de Google en GitHub) | Como ejemplo a consultar |

Las reglas de la sección 4 se escriben como tests de Konsist en `app/src/test/.../ArchitectureTest.kt`, igual que se haría con ArchUnit en Spring. Si alguien rompe una regla, el test falla.

### 5.1 Konsist en Zenith (decisión aprobada)

**Qué es:** una librería de pruebas que lee el código Kotlin del proyecto y verifica reglas de arquitectura y convenciones. Funciona como un test unitario normal (JUnit), así que corre con `./gradlew test` y también en CI.

**Instalación** (`gradle/libs.versions.toml` y `app/build.gradle.kts`):

```toml
[versions]
konsist = "0.17.3"

[libraries]
konsist = { module = "com.lemonappdev:konsist", version.ref = "konsist" }
```

```kotlin
dependencies {
    testImplementation(libs.konsist)
}
```

**Reglas de capas** (`app/src/test/java/com/zenith/player/architecture/ArchitectureTest.kt`):

```kotlin
class ArchitectureTest {

    @Test
    fun `las capas respetan sus dependencias`() {
        Konsist.scopeFromProduction()
            .assertArchitecture {
                val ui       = Layer("UI",           "com.zenith.player.ui..")
                val domain   = Layer("Dominio",      "com.zenith.player.domain..")
                val data     = Layer("Datos",        "com.zenith.player.data..")
                val playback = Layer("Reproducción", "com.zenith.player.playback..")

                ui.dependsOn(domain, playback)
                data.dependsOn(domain)
                playback.dependsOn(domain)
                domain.dependsOnNothing()
            }
    }

    @Test
    fun `el dominio no usa clases de Android`() {
        Konsist.scopeFromPackage("com.zenith.player.domain..")
            .files
            .assertFalse { file -> file.imports.any { it.name.startsWith("android.") } }
    }
}
```

**Reglas de convención** (`ConventionTest.kt`):

```kotlin
class ConventionTest {

    @Test
    fun `los ViewModel terminan en ViewModel y viven en ui`() {
        Konsist.scopeFromProduction()
            .classes()
            .withParentNamed("ViewModel")
            .assertTrue { it.hasNameEndingWith("ViewModel") && it.resideInPackage("..ui..") }
    }

    @Test
    fun `los casos de uso terminan en UseCase y tienen un solo invoke`() {
        Konsist.scopeFromPackage("com.zenith.player.domain.usecase..")
            .classes()
            .assertTrue {
                it.hasNameEndingWith("UseCase") &&
                    it.functions().count { f -> f.name == "invoke" && f.hasOperatorModifier } == 1
            }
    }

    @Test
    fun `las entidades de Room solo existen en data_local`() {
        Konsist.scopeFromProduction()
            .classes()
            .withAnnotationNamed("Entity")
            .assertTrue { it.resideInPackage("com.zenith.player.data.local..") }
    }

    @Test
    fun `los repositorios implementados terminan en RepositoryImpl`() {
        Konsist.scopeFromPackage("com.zenith.player.data.repository..")
            .classes()
            .assertTrue { it.hasNameEndingWith("RepositoryImpl") }
    }
}
```

**Cuándo se ejecuta:**
- En local, antes de dar por terminada cada tarea, con `./gradlew test`.
- Estos tests se crean en la **fase 1** (base del proyecto), antes de escribir pantallas, para que la arquitectura se vigile desde el primer día.

**Si un test falla:** se corrige el código para que cumpla la regla. Solo se cambia la regla si la arquitectura cambia, y en ese caso se actualiza también este documento.

## 6. Flujo de ejemplo: tocar una canción en Mi Música

1. `LibraryScreen` llama `viewModel.onSongClick(song, list)`.
2. `LibraryViewModel` llama `musicPlayer.playQueue(list, startIndex)`. `MusicPlayer` es una interfaz del dominio, implementada por `PlayerController`.
3. `PlayerController` envía la cola al `PlaybackService` mediante `MediaController`.
4. ExoPlayer reproduce, y la sesión publica la notificación y los controles de la pantalla de bloqueo.
5. `PlayerController` expone `playerState: StateFlow<PlayerState>`. `NowPlayingViewModel` y el mini reproductor lo observan y se redibujan.
6. Al pasar el 50 % de la canción o 30 s, `RecordPlayUseCase` guarda el evento en Room, lo que actualiza Historial y Más escuchadas.

## 7. Pruebas

- **Unitarias (JVM):** casos de uso, lector `.lrc`, `ViewModel` (con repositorios falsos) y reglas de arquitectura (Konsist).
- **Instrumentadas:** DAOs de Room.
- **Manuales en un dispositivo real:** reproducción en segundo plano, notificación, auriculares y fidelidad visual comparada con `referencia.png`.
