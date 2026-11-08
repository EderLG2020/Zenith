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

1. **Revisión final de la fase 1:** se lanzó un revisor independiente sobre toda la rama, pero la sesión terminó antes de recibir el resultado. Hay que volver a ejecutarla o revisar sus hallazgos, y corregir los críticos o importantes con test primero.
2. **La rama `fase-01-base` todavía no está unida a `main`.** Después de la revisión hay que decidir si se une (merge).
3. **Nunca se probó en un Android real**, solo con tests y una captura renderizada.

### ▶️ Qué continuar mañana (en orden)

1. Terminar la revisión de la fase 1 y aplicar sus correcciones.
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
