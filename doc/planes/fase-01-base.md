# Fase 1: base del proyecto (plan de implementación)

> **Para agentes:** SUB-SKILL REQUERIDA: usar superpowers:subagent-driven-development (recomendado) o superpowers:executing-plans para implementar este plan tarea por tarea. Los pasos usan casillas (`- [ ]`) para el seguimiento.

**Objetivo:** dejar un proyecto Android que compila y abre con el tema de Zenith (oscuro y lima), una barra flotante que navega entre 5 pantallas vacías, los componentes visuales base y los tests de Konsist que vigilan la arquitectura.

**Arquitectura:** un módulo `app` con paquetes `ui`, `domain`, `data`, `playback` y `di`, según [02-arquitectura.md](../02-arquitectura.md). En esta fase solo `ui` tiene contenido. Los demás paquetes se crean vacíos (con un `package-info` o un archivo marcador) para que Konsist los reconozca.

**Stack:** Kotlin, Jetpack Compose (Material 3), Navigation Compose, Hilt (KSP), Konsist, JUnit 4 y Robolectric (tests de Compose en la JVM).

**Spec:** [01-diseno.md](../01-diseno.md), [02-arquitectura.md](../02-arquitectura.md)

## Restricciones globales

- `applicationId` y paquete raíz: `com.zenith.player`. Nombre visible: `Zenith`.
- `minSdk 26`, `targetSdk 35`, `compileSdk 35`.
- Gradle Kotlin DSL + `gradle/libs.versions.toml`.
- **No** declarar el permiso `INTERNET`.
- Solo tema oscuro, sin importar el modo del sistema.
- Interfaz en español.
- Colores: fondo `#0E0E12`, superficie `#1C1C22`, acento `#C6E14B`, tarjeta destacada `#C9A3F0`, texto principal `#FFFFFF`, texto secundario `#8A8A93`.
- Tipografía: Plus Jakarta Sans (variable, empaquetada en `res/font`).
- Reglas de capas y convenciones: las de [02-arquitectura.md §5.1](../02-arquitectura.md), verificadas con Konsist.
- Comando de verificación de cada tarea: `./gradlew test` (en PowerShell: `.\gradlew.bat test`).

### Versiones de referencia

Son las versiones mínimas probadas juntas. Si la plantilla de Android Studio trae versiones más nuevas de AGP, Kotlin o el Compose BOM, se conservan, y las demás se suben a su última versión estable compatible. **KSP debe coincidir con la versión de Kotlin.**

| Librería | Versión |
|---|---|
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.1.0 |
| KSP | 2.1.0-1.0.29 |
| Compose BOM | 2024.12.01 |
| Activity Compose | 1.9.3 |
| Lifecycle | 2.8.7 |
| Navigation Compose | 2.8.5 |
| Hilt | 2.54 (`hilt-navigation-compose` 1.2.0) |
| Konsist | 0.17.3 |
| JUnit | 4.13.2 |
| Robolectric | 4.14.1 |

## Foco de revisión

Situaciones que el diseño implica pero que ningún test de una tarea cubriría por sí solo:

1. **El teléfono está en modo claro:** la app debe verse igual de oscura. Test en la tarea 3.
2. **Tamaño de letra del sistema grande (accesibilidad, 2.0x):** el texto de los chips no se recorta, el chip crece. Test en la tarea 4.
3. **Botón atrás desde una pestaña que no es Inicio:** vuelve a Inicio, no cierra la app. Test en la tarea 5.
4. **Girar la pantalla o recrear la actividad:** se conserva la pestaña activa. Test en la tarea 5.
5. **Título de canción muy largo:** se corta con "…" en una sola línea y la fila no crece. Test en la tarea 4.

---

### Tarea 0: entorno de desarrollo y repositorio

**Archivos:**
- Crear: `.gitignore` (raíz)

- [ ] **Paso 1: instalar Android Studio** (versión estable más reciente). En el asistente, instalar Android SDK Platform 35, Build-Tools y un emulador "Pixel 8" con API 35.
- [ ] **Paso 2: usar el JDK de Android Studio para Gradle.** El JDK 25 del equipo no es compatible con estas versiones de Gradle. En Android Studio, *Settings → Build Tools → Gradle → Gradle JDK* = "jbr-21" (JetBrains Runtime 21). Para la terminal, definir `JAVA_HOME` apuntando a `C:\Program Files\Android\Android Studio\jbr`.
- [ ] **Paso 3: verificar.** Ejecutar `"$JAVA_HOME/bin/java" -version`. Esperado: `openjdk version "21...`.
- [ ] **Paso 4: inicializar git** en `C:\Users\Lenovo\Desktop\proyecto\Zenith` y crear `.gitignore` con la plantilla estándar de Android: `.gradle/`, `build/`, `local.properties`, `.idea/`, `*.iml`, `.DS_Store`, `captures/` y `.kotlin/`.
- [ ] **Paso 5: hacer commit.**

```bash
git init
git add .gitignore doc/
git commit -m "docs: diseño, arquitectura y hoja de ruta de Zenith"
```

---

### Tarea 1: esqueleto que compila

**Archivos:**
- Crear desde la plantilla de Android Studio **"Empty Activity"** (Compose), equivalente a Spring Initializr: Name `Zenith`, Package `com.zenith.player`, Save location = la raíz del proyecto, Minimum SDK = API 26, Build configuration language = Kotlin DSL + version catalog.
- Modificar: `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`
- Crear: `app/src/main/java/com/zenith/player/ZenithApp.kt`, y modificar `MainActivity.kt`
- Crear marcadores de paquete: `domain/Domain.kt`, `data/Data.kt`, `playback/Playback.kt` y `di/Di.kt`. Cada uno contiene solo `package ...` y un comentario de una línea con su propósito.

**Interfaces:**
- Produce: `@HiltAndroidApp class ZenithApp : Application()`, y `@AndroidEntryPoint class MainActivity : ComponentActivity()` que llama a `enableEdgeToEdge()` y `setContent { ZenithTheme { ZenithRoot() } }`. `ZenithRoot` y `ZenithTheme` se crean en las tareas 3 y 5; mientras tanto, `setContent { Text("Zenith") }`.

- [ ] **Paso 1: crear el proyecto con la plantilla** y borrar el paquete `ui.theme` que genera (se reemplaza en la tarea 3).
- [ ] **Paso 2: añadir al catálogo** las librerías de la tabla de versiones: plugins `ksp` y `hilt`, `hilt-android`, `hilt-compiler`, `hilt-navigation-compose`, `navigation-compose`, `konsist`, `junit`, `robolectric`, `compose-ui-test-junit4`, `compose-ui-test-manifest` y `compose-material-icons-extended`. Las reglas de Konsist usan nombres (`withParentNamed`, `withAnnotationNamed`), no clases, para no depender de Room ni de Lifecycle en los tests.
- [ ] **Paso 3: configurar `app/build.gradle.kts`:** `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`, Java/Kotlin target 17, `buildFeatures.compose = true` y `testOptions.unitTests.isIncludeAndroidResources = true`. Dependencias: `testImplementation` para konsist, junit, robolectric y compose-ui-test-junit4, y `debugImplementation` para compose-ui-test-manifest.
- [ ] **Paso 4: en el `AndroidManifest.xml`** usar `android:name=".ZenithApp"`, `android:label="Zenith"` y el tema de la ventana `@android:style/Theme.Material.NoActionBar` con `android:windowBackground` = `#0E0E12` (evita el destello blanco al abrir).
- [ ] **Paso 5: compilar.** Ejecutar `./gradlew assembleDebug`. Esperado: `BUILD SUCCESSFUL`.
- [ ] **Paso 6: verificar que no hay permiso de internet.** Ejecutar `./gradlew :app:processDebugMainManifest` y luego `grep -r "android.permission.INTERNET" app/build/intermediates/merged_manifests/`. Esperado: sin resultados.
- [ ] **Paso 7: hacer commit.** `git add -A && git commit -m "chore: esqueleto Android con Compose, Hilt y catálogo de versiones"`

---

### Tarea 2: Konsist vigila la arquitectura

**Archivos:**
- Crear: `app/src/test/java/com/zenith/player/architecture/ArchitectureTest.kt`
- Crear: `app/src/test/java/com/zenith/player/architecture/ConventionTest.kt`

**Interfaces:**
- Consume: los paquetes de la tarea 1.
- Produce: los tests que todas las tareas siguientes deben mantener en verde.

- [ ] **Paso 1: escribir los dos archivos de tests** exactamente con las reglas de [02-arquitectura.md §5.1](../02-arquitectura.md): capas UI → Dominio y Reproducción, Datos → Dominio, Reproducción → Dominio, y Dominio sin dependencias; Dominio sin `android.*`; nombres de `ViewModel`, `UseCase` y `RepositoryImpl`; `@Entity` solo en `data.local`.
- [ ] **Paso 2: comprobar que la regla de capas detecta errores.** Añadir temporalmente en `domain/Domain.kt` la línea `import android.content.Context` (y usarla en un `val` privado) y ejecutar `./gradlew test --tests "*ArchitectureTest*"`. Esperado: **FAIL** en `el dominio no usa clases de Android`.
- [ ] **Paso 3: quitar el import temporal.**
- [ ] **Paso 4: ejecutar `./gradlew test`.** Esperado: todos los tests en PASS. Las reglas sobre clases que aún no existen pasan vacías.
- [ ] **Paso 5: hacer commit.** `git commit -am "test: reglas de arquitectura con Konsist"`

---

### Tarea 3: tema de Zenith

**Archivos:**
- Crear: `app/src/main/res/font/plus_jakarta_sans.ttf`. Es la fuente variable de Google Fonts (licencia OFL), descargada de `https://github.com/google/fonts/raw/main/ofl/plusjakartasans/PlusJakartaSans%5Bwght%5D.ttf`.
- Crear: `app/src/main/java/com/zenith/player/ui/theme/Color.kt`, `Type.kt`, `Shape.kt` y `Theme.kt`
- Test: `app/src/test/java/com/zenith/player/ui/theme/ZenithThemeTest.kt`

**Interfaces:**
- Produce:
  - `object ZenithColors { Background, Surface, SurfaceHigh, Accent, OnAccent, Featured, FeaturedButton, TextPrimary, TextSecondary }`, todos de tipo `Color`.
  - `val ZenithFontFamily: FontFamily`
  - `val ZenithTypography: Typography`
  - `object ZenithShapes { val Cover: Shape; val Card: Shape; val Pill: Shape }`
  - `@Composable fun ZenithTheme(content: @Composable () -> Unit)`

- [ ] **Paso 1: escribir el test que falla** (Robolectric, `@RunWith(AndroidJUnit4::class)`, `@Config(qualifiers = "notnight")` para simular el modo claro del sistema):

```kotlin
@Test fun `colores coinciden con el diseño`() {
    assertEquals(Color(0xFF0E0E12), ZenithColors.Background)
    assertEquals(Color(0xFF1C1C22), ZenithColors.Surface)
    assertEquals(Color(0xFFC6E14B), ZenithColors.Accent)
    assertEquals(Color(0xFFC9A3F0), ZenithColors.Featured)
    assertEquals(Color(0xFF8A8A93), ZenithColors.TextSecondary)
}

@Test fun `el tema es oscuro aunque el sistema esté en modo claro`() {
    var background = Color.Unspecified
    var primary = Color.Unspecified
    composeRule.setContent {
        ZenithTheme {
            background = MaterialTheme.colorScheme.background
            primary = MaterialTheme.colorScheme.primary
        }
    }
    assertEquals(ZenithColors.Background, background)
    assertEquals(ZenithColors.Accent, primary)
}
```

- [ ] **Paso 2: ejecutar `./gradlew test --tests "*ZenithThemeTest*"`.** Esperado: FAIL (no compila: `ZenithColors` no existe).
- [ ] **Paso 3: implementar.**
  - `Color.kt`: valores de las restricciones globales. Además: `SurfaceHigh = #2A2A31` (para los botones sobre fotos), `OnAccent = #0E0E12` y `FeaturedButton = #3B1E5E`. Estos tres son decisión del implementador, ajustables contra la referencia.
  - `Type.kt`: `ZenithFontFamily` con `Font(R.font.plus_jakarta_sans, weight = W, variationSettings = FontVariation.Settings(FontVariation.weight(W)))` para W = 400, 500, 600 y 700. Tamaños: `headlineMedium` 26sp/600 (saludo), `titleLarge` 20sp/600 (secciones), `titleMedium` 15sp/600 (título de canción) y `bodySmall` 12sp/400 (subtítulos).
  - `Shape.kt`: `Cover` = 14.dp, `Card` = 24.dp y `Pill` = `CircleShape`.
  - `Theme.kt`: `MaterialTheme(colorScheme = darkColorScheme(...))` siempre, sin leer `isSystemInDarkTheme()`.
- [ ] **Paso 4: ejecutar el test.** Esperado: PASS.
- [ ] **Paso 5: conectar `ZenithTheme` en `MainActivity`** y ejecutar `./gradlew test`. Esperado: todo en PASS.
- [ ] **Paso 6: hacer commit.** `git add -A && git commit -m "feat: tema oscuro de Zenith con Plus Jakarta Sans"`

---

### Tarea 4: componentes visuales base

**Archivos:**
- Crear en `app/src/main/java/com/zenith/player/ui/components/`: `ZenithChip.kt`, `CircleIconButton.kt`, `CoverPlaceholder.kt` y `SongRow.kt`
- Test: `app/src/test/java/com/zenith/player/ui/components/ComponentsTest.kt`

**Interfaces:**
- Consume: `ZenithColors`, `ZenithShapes` y `ZenithTypography` (tarea 3).
- Produce:
  - `@Composable fun ZenithChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)`: píldora con fondo `Accent` y texto `OnAccent` si está seleccionado, o fondo `Surface` y texto `TextSecondary` si no. Usa `heightIn(min = 40.dp)`, nunca una altura fija, y `Modifier.semantics { this.selected = selected }`.
  - `@Composable fun CircleIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier, containerColor: Color = ZenithColors.Surface, size: Dp = 44.dp)`
  - `fun placeholderColorsFor(seed: String): Pair<Color, Color>`: es determinista, de modo que la misma `seed` da los mismos colores.
  - `@Composable fun CoverPlaceholder(seed: String, modifier: Modifier = Modifier)`: degradado de `placeholderColorsFor(seed)` con el icono `Icons.Rounded.MusicNote`.
  - `@Composable fun SongRow(title: String, subtitle: String, onClick: () -> Unit, onPlayClick: () -> Unit, modifier: Modifier = Modifier, cover: @Composable () -> Unit = { CoverPlaceholder(title) })`: portada de 56dp con forma `Cover`, título (`maxLines = 1`, `TextOverflow.Ellipsis`), subtítulo y un `CircleIconButton` de play de 36dp a la derecha con `contentDescription = "Reproducir $title"`.

- [ ] **Paso 1: escribir los tests que fallan.**

```kotlin
@Test fun `placeholder es determinista y varía según la semilla`() {
    assertEquals(placeholderColorsFor("Starlit Reverie"), placeholderColorsFor("Starlit Reverie"))
    assertNotEquals(placeholderColorsFor("Starlit Reverie"), placeholderColorsFor("Lost in the Echo"))
}

@Test fun `chip seleccionado expone el estado y responde al clic`() {
    var clicks = 0
    composeRule.setContent { ZenithTheme { ZenithChip("Todo", selected = true, onClick = { clicks++ }) } }
    composeRule.onNodeWithText("Todo").assertIsSelected().performClick()
    assertEquals(1, clicks)
}

@Test fun `chip crece con letra del sistema grande`() {
    composeRule.setContent {
        CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale = 2f)) {
            ZenithTheme { ZenithChip("Agregadas recientemente", selected = false, onClick = {}) }
        }
    }
    composeRule.onNodeWithText("Agregadas recientemente").assertHeightIsAtLeast(40.dp)
    // El chip padre (onParent) debe ser al menos tan alto como su texto.
}

@Test fun `título largo no hace crecer la fila`() {
    // Renderizar SongRow con título "Corto" y medir su altura (h1).
    // Renderizar SongRow con un título de 200 caracteres y medir su altura (h2).
    // assertEquals(h1, h2), y el botón "Reproducir …" sigue visible (assertIsDisplayed).
}
```

- [ ] **Paso 2: ejecutar `./gradlew test --tests "*ComponentsTest*"`.** Esperado: FAIL (no compila).
- [ ] **Paso 3: implementar los cuatro componentes** con las firmas de la sección *Interfaces*. `placeholderColorsFor`: tomar `seed.hashCode()`, derivar dos tonos HSV separados unos 40° (saturación 0.45, brillo 0.55 y 0.35).
- [ ] **Paso 4: ejecutar los tests.** Esperado: PASS.
- [ ] **Paso 5: añadir un `@Preview` por componente** (oscuro, con datos de la referencia: "Starlit Reverie", "Por Budiarti • 8 canciones") y revisarlos en Android Studio.
- [ ] **Paso 6: hacer commit.** `git add -A && git commit -m "feat: componentes base (chip, botón circular, portada, fila de canción)"`

---

### Tarea 5: navegación con barra flotante

**Archivos:**
- Crear: `app/src/main/java/com/zenith/player/ui/navigation/Destination.kt`, `ZenithNavHost.kt` y `FloatingBottomBar.kt`
- Crear: `app/src/main/java/com/zenith/player/ui/ZenithRoot.kt`
- Crear pantallas provisionales: `ui/home/HomeScreen.kt`, `ui/library/LibraryScreen.kt`, `ui/history/HistoryScreen.kt`, `ui/playlists/PlaylistsScreen.kt` y `ui/settings/SettingsScreen.kt`. Cada una muestra solo su título centrado con `titleLarge`.
- Modificar: `MainActivity.kt` (usa `ZenithRoot()`)
- Test: `app/src/test/java/com/zenith/player/ui/navigation/NavigationTest.kt`

**Interfaces:**
- Consume: `ZenithTheme` y `ZenithColors` (tarea 3), y `CircleIconButton` (tarea 4).
- Produce:
  - `enum class Destination(val route: String, val label: String, val icon: ImageVector)`, con los valores `Home("home", "Inicio", Icons.Rounded.Home)`, `Library("library", "Mi Música", Icons.Rounded.LibraryMusic)`, `History("history", "Historial", Icons.Rounded.History)`, `Playlists("playlists", "Listas", Icons.Rounded.BookmarkBorder)` y `Settings("settings", "Ajustes", Icons.Rounded.Settings)`. Se necesita `material-icons-extended`.
  - `@Composable fun FloatingBottomBar(current: Destination, onSelect: (Destination) -> Unit, modifier: Modifier = Modifier)`: píldora con fondo `SurfaceHigh` al 85 % de opacidad, margen lateral de 16dp y 5 iconos. El activo va dentro de un círculo `Accent` con el icono en `OnAccent`. Cada icono tiene `contentDescription = label` y `semantics { selected = … }`.
  - `@Composable fun ZenithRoot()`: `rememberNavController()`, un `Box` con `ZenithNavHost` y la `FloatingBottomBar` alineada abajo, encima de la barra de gestos (`navigationBarsPadding`).
  - Navegación entre pestañas con `popUpTo(Destination.Home.route) { saveState = true }`, `launchSingleTop = true` y `restoreState = true`. Así, atrás desde cualquier pestaña vuelve a Inicio, y atrás desde Inicio cierra la app.

- [ ] **Paso 1: escribir los tests que fallan** (Robolectric, `createAndroidComposeRule<ComponentActivity>()`, contenido `ZenithTheme { ZenithRoot() }`):

```kotlin
@Test fun `abre en Inicio con su pestaña seleccionada`() {
    composeRule.onNodeWithText("Inicio").assertIsDisplayed()
    composeRule.onNodeWithContentDescription("Inicio").assertIsSelected()
}

@Test fun `cada pestaña abre su pantalla`() {
    listOf("Mi Música", "Historial", "Listas", "Ajustes").forEach { label ->
        composeRule.onNodeWithContentDescription(label).performClick()
        composeRule.onNodeWithText(label).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(label).assertIsSelected()
    }
}

@Test fun `atrás desde Ajustes vuelve a Inicio`() {
    composeRule.onNodeWithContentDescription("Ajustes").performClick()
    composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
    composeRule.onNodeWithContentDescription("Inicio").assertIsSelected()
}

@Test fun `la pestaña activa sobrevive a recrear la pantalla`() {
    // StateRestorationTester(composeRule).setContent { ZenithTheme { ZenithRoot() } }
    // Pulsar "Historial", luego restorer.emulateSavedInstanceStateRestore().
    // "Historial" sigue seleccionado.
}
```

- [ ] **Paso 2: ejecutar `./gradlew test --tests "*NavigationTest*"`.** Esperado: FAIL (no compila).
- [ ] **Paso 3: implementar** `Destination`, `FloatingBottomBar`, `ZenithNavHost`, las 5 pantallas provisionales y `ZenithRoot`, con las firmas de la sección *Interfaces*.
- [ ] **Paso 4: ejecutar los tests.** Esperado: PASS.
- [ ] **Paso 5: ejecutar la suite completa con `./gradlew test`.** Esperado: todo en PASS, incluido Konsist (las pantallas están en `ui` y no importan nada de `data`).
- [ ] **Paso 6: hacer commit.** `git add -A && git commit -m "feat: navegación con barra flotante y 5 pantallas"`

---

### Tarea 6: verificación de la fase en el emulador

- [ ] **Paso 1: instalar en el emulador con `./gradlew installDebug`** (emulador Pixel 8, API 35, encendido). Esperado: `Installed on 1 device`.
- [ ] **Paso 2: abrir Zenith y comprobar:** fondo oscuro sin destello blanco al abrir, barra flotante con Inicio en círculo lima y las 5 pestañas funcionando.
- [ ] **Paso 3: con el emulador en modo claro** (*Ajustes → Pantalla → Tema oscuro* desactivado), la app se sigue viendo oscura.
- [ ] **Paso 4: capturar la pantalla** en `doc/capturas/fase-01.png` y compararla con la barra inferior de `doc/referencia.png`.
- [ ] **Paso 5: marcar la fase 1 como ✅** en [03-hoja-de-ruta.md](../03-hoja-de-ruta.md) y hacer commit. `git add -A && git commit -m "docs: fase 1 completada"`
