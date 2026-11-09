# Zenith: bitácora de avance

## Sesión 1: 2026-11-08

### ✅ Lo que quedó hecho

**Documentación** (rama `main`)
- `doc/01-diseno.md`: qué es la app, pantallas, estilo y adaptación a música local (aprobado).
- `doc/02-arquitectura.md`: MVVM + UDF, capas, reglas y Konsist (aprobado).
- `doc/03-hoja-de-ruta.md`: las 10 fases.
- `doc/planes/fase-01-base.md`: plan detallado de la fase 1.

**Fase 1: base del proyecto** (rama `fase-01-base`, 18/18 tests en verde)

| Commit | Qué hace |
|---|---|
| `1b58d96` | Esqueleto Android: Compose, Hilt, catálogo de versiones, icono, sin permiso de internet |
| `84cf5bb` | Tests de Konsist: reglas de capas y de nombres |
| `1a96ae9` | Tema oscuro (`#0E0E12` y lima `#C6E14B`) con Plus Jakarta Sans |
| `2e38b34` | Componentes: `ZenithChip`, `CircleIconButton`, `CoverPlaceholder`, `SongRow` |
| `9b4655c` | Barra flotante con 5 pestañas (Inicio, Mi Música, Historial, Listas, Ajustes) y pantallas provisionales |
| `e7210b7` | Captura de la pantalla de inicio en `doc/capturas/fase-01.png` |

### 🛠️ Entorno instalado (sin Android Studio)

- **JDK 21:** `C:\Users\Lenovo\Android\jdk21` (el Java 25 del equipo **no** sirve para Gradle).
- **Android SDK:** `C:\Users\Lenovo\Android\sdk` (platform 35, build-tools 35.0.0, platform-tools). Se indica en `local.properties`.
- **Comandos** (en Git Bash, desde la carpeta del proyecto):

```bash
export JAVA_HOME="C:/Users/Lenovo/Android/jdk21"
./gradlew test                                  # todos los tests
./gradlew testDebugUnitTest --tests "*Nombre*"  # un test concreto (no usar `test --tests`)
./gradlew assembleDebug                         # genera app/build/outputs/apk/debug/app-debug.apk
```

### ⏸️ Dónde quedamos

1. **Revisión final de la fase 1: recibida. Veredicto: "lista para unir con correcciones".** La navegación, la recreación de la pantalla y los títulos largos están bien. Falta corregir lo siguiente, con test primero:
   - **Importante 1, barras del sistema en modo claro:** `MainActivity.kt:15` usa `enableEdgeToEdge()` sin argumentos. Con el teléfono en modo claro, la hora y la batería se dibujan oscuras sobre el fondo oscuro y no se ven.
     - *Corrección:* `enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT))`.
     - *Test:* Robolectric con `notnight` que lance `MainActivity` y exija `isAppearanceLightStatusBars` y `isAppearanceLightNavigationBars` en `false`.
   - **Importante 2, chip con letra grande:** con la letra del sistema a 2.0x, el texto del chip se corta a mitad de letra y sin "…" (`ZenithChip.kt:46`). El test actual no lo detecta porque Robolectric en modo LEGACY mide 1 px por carácter.
     - *Corrección:* usar `@GraphicsMode(NATIVE)` en `ComponentsTest` y comprobar `!hasVisualOverflow`. En el chip, permitir 2 líneas o documentar que las filas de chips van en `LazyRow`/scroll, y añadir `overflow = TextOverflow.Ellipsis`.
   - **Menores:**
     - El test del título largo debe comprobar `isLineEllipsized(0)` con gráficos nativos.
     - El ripple al pulsar sale negro e invisible: proveer `LocalContentColor = TextPrimary` en `ZenithTheme`.
     - Faltan tres tests de navegación: atrás desde Inicio cierra la app, pulsar Inicio desde otra pestaña, y recrear la `MainActivity` real.
2. **La rama `fase-01-base` todavía no está unida a `main`.** Después de la revisión hay que decidir si se une (merge).
3. **Nunca se probó en un Android real**, solo con tests y una captura renderizada.

### ▶️ Qué continuar mañana (en orden)

1. Aplicar las correcciones de la revisión (ver "Dónde quedamos", punto 1): primero las 2 importantes, luego las menores.
2. **Probar en el teléfono:** activar *Opciones de desarrollador → Depuración USB*, conectar por USB e instalar con `C:/Users/Lenovo/Android/sdk/platform-tools/adb.exe install -r app/build/outputs/apk/debug/app-debug.apk`. Revisar:
   - que no haya destello blanco al abrir;
   - que la barra flotante quede encima de la barra de gestos;
   - que con el modo claro del teléfono la app siga oscura.
3. Unir `fase-01-base` a `main`.
4. **Fase 2, Biblioteca:** escribir `doc/planes/fase-02-biblioteca.md` (permisos `READ_MEDIA_AUDIO` y `READ_EXTERNAL_STORAGE`, escaneo de MediaStore, repositorio de canciones y portadas) y ejecutarlo.

### 📝 Decisiones tomadas durante la ejecución

- El SDK y el JDK se instalaron por terminal en lugar de Android Studio, a elección del usuario. El esqueleto se escribió a mano.
- Los tests unitarios de release están desactivados, porque los tests de Compose necesitan `ui-test-manifest`, que solo existe en debug.
- Se añadió `waitForIdle()` antes de pulsar atrás en los tests de navegación (problema de sincronización del test, no de la app).
- `PlaceholderScreen` se comparte entre las 5 pantallas provisionales.
- La verificación en el emulador se reemplazó por una captura con Robolectric.

### 💡 Notas para el pulido (fase 10)

- En la referencia, los iconos de la barra son de **contorno**; ahora son rellenos.
- El círculo lima de la pestaña activa es un poco más pequeño en la referencia.

## Sesión 2: 2026-11-09

> Commits sin coautor (uno por fase: `fix(fase 1)`, `feat(fase 2)`, `feat(fase 3)` y `feat(fase 4)`). Todo está en `main` (una sola rama) y publicado en https://github.com/EderLG2020/Zenith.

### ✅ Lo que quedó hecho

**Correcciones de la revisión de la fase 1** (todas con test primero)
- Barras del sistema: `enableEdgeToEdge(SystemBarStyle.dark(...))`, así los iconos se ven claros aunque el teléfono esté en modo claro (`MainActivityTest`).
- Chip: `TextOverflow.Ellipsis`. Los tests usan `@GraphicsMode(NATIVE)`. A 2x, "Agregadas recientemente" es **más ancho que la pantalla**, así que las filas de chips deben ir en `LazyRow`/`horizontalScroll` (está documentado en `ZenithChip`).
  - Con ancho infinito, `hasVisualOverflow` da un falso positivo. Por eso los tests comprueban `isLineEllipsized` y `getLineRight`.
- Título largo: el test comprueba `isLineEllipsized(0)`.
- Ripple: `ZenithTheme` provee `LocalContentColor = TextPrimary`.
- Tests nuevos: atrás desde Inicio cierra la app, pulsar Inicio desde otra pestaña y recrear la `MainActivity` real.

**Fase 2: Biblioteca** ([plan](planes/fase-02-biblioteca.md), tareas 1 a 6)
- Permisos `READ_MEDIA_AUDIO` y `READ_EXTERNAL_STORAGE` (`maxSdkVersion 32`); `ManifestTest` vigila que no haya `INTERNET`.
- `domain`: `Song` y `SongRepository`. `data`: `SongSource`, `MediaStoreSongSource` (MediaStore + `ContentObserver`) y `SongRepositoryImpl` (IO, `conflate`). `di`: `DataModule` y `DispatchersModule`.
- Mi Música: `LibraryRoute` (permiso + `hiltViewModel`), `LibraryViewModel` y `LibraryScreen` (lista, vacío y carga), y `SongCover` con Coil 3 sin módulo de red.
- `AudioPermissionGate`: explica y pide el permiso, ofrece "Abrir Ajustes" si ya no se puede pedir y vuelve a comprobarlo en cada `ON_RESUME`.
- Konsist: la regla `RepositoryImpl` ahora mira solo el código de producción (antes también revisaba los tests).
- **51/51 tests** y `assembleDebug` en verde.

**Fase 3: Motor de reproducción** ([plan](planes/fase-03-reproduccion.md), tareas 1 a 5)
- Media3 1.5.1. Permisos `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` y `WAKE_LOCK`. **No** se declaró `POST_NOTIFICATIONS`: las notificaciones multimedia están exentas en Android 13+ (hay que comprobarlo en el teléfono).
- `domain`: `PlayerState`, `NowPlaying`, `RepeatMode` (Off → All → One) y la interfaz `MusicPlayer`.
- `PlaybackService` (`MediaSessionService` + ExoPlayer):
  - foco de audio: pausa en llamadas;
  - pausa al desconectar los auriculares;
  - `WAKE_MODE_LOCAL` para la pantalla apagada;
  - se detiene al cerrar la app desde recientes si no está sonando.
- `MediaItems.kt`: `Song` → `MediaItem`, con la URI también en `requestMetadata`, porque el `MediaController` no la envía. La sesión la recupera con `resolveMediaItems`.
- `PlayerController`: conecta el `MediaController` al crearse. Las órdenes previas a la conexión esperan, publica `PlayerState` y salta los archivos dañados. Los tests usan `TestExoPlayerBuilder` + `FakeMediaSourceFactory`.
- Mi Música: tocar una canción reproduce toda la lista, empezando por ella.
- **67/67 tests** y `assembleDebug` en verde.

**Fase 4: Pantalla Reproduciendo** ([plan](planes/fase-04-reproduciendo.md), 8 tareas)
- `PlayerController`:
  - publica la posición cada 500 ms mientras suena;
  - publica la cola aparte, en `MusicPlayer.queue`, y la actualiza solo con `TIMELINE_CHANGED`;
  - órdenes nuevas para saltar, mover y quitar elementos de la cola.
- `NowPlayingViewModel` + `NowPlayingUiState`, y `formatRemaining` (`-2:15`).
- Pantalla:
  - fondo difuminado (`blur` en Android 12+; más oscuro en versiones anteriores);
  - portada circular (72 % del ancho, máximo 320 dp);
  - barra superior translúcida y textos.
- `SeekBar`: se toca o se arrastra, y solo busca al soltar. Semántica `setProgress` para TalkBack.
- `PlaybackControls`: la pulsación larga en aleatorio cambia el modo de repetir. Se indica con un punto (toda la cola) o un "1" (una canción).
- `QueueSheet`: saltar, subir, bajar y quitar.
- Navegación: la ruta `now_playing` oculta la barra flotante, y tocar una canción en Mi Música abre Reproduciendo.
- **88/88 tests** y APK en verde. Captura en `doc/capturas/fase-04.png`.
- `captureToImage` se cuelga en Robolectric. La captura se hizo con un test temporal que dibuja `window.decorView` en un `Canvas` (con `@GraphicsMode(NATIVE)`).

### ▶️ Qué continuar (en orden)

1. **Probar en el teléfono las fases 1 a 4** (sigue sin haber ninguno conectado por adb):
   - todo lo anterior;
   - que Reproduciendo se vea como la referencia con portadas reales;
   - arrastrar la barra de progreso;
   - la hoja de la cola.
2. ~~Unir a `main`~~: hecho, la rama `fase-01-base` se unió y se borró.
3. Escribir el plan de la fase 5 (pantalla Mi Música completa).

### 📝 Notas
- `IoDispatcher` vive en `di` y `data` lo importa. Si molesta, se puede mover a un paquete `common`.
- El permiso se pide al entrar a Mi Música, no al abrir la app. Inicio lo reutilizará en la fase 7.
- El salto por archivo dañado (`onPlayerError`) no tiene test automático: se revisa en el teléfono.
- El corazón de Reproduciendo todavía no hace nada (los favoritos llegan en la fase 6).
- La cola se muestra en su orden original, aunque esté activado el aleatorio.
- Reordenar la cola arrastrando queda para la fase 10, si hace falta.
