# Zenith: hoja de ruta

- **Fecha:** 2026-11-08
- **Basado en:** [01-diseno.md](01-diseno.md) · [02-arquitectura.md](02-arquitectura.md)

Cada fase termina con una app que **compila, pasa los tests (incluidos los de Konsist) y se puede abrir en el emulador**. Antes de empezar una fase se escribe su plan detallado en `doc/planes/`, con tareas pequeñas, tests primero y un commit por tarea.

| # | Fase | Qué se puede ver o probar al terminar | Plan | Estado |
|---|---|---|---|---|
| 1 | **Base del proyecto** | La app abre con tema oscuro y lima, la barra flotante navega entre 5 pantallas vacías y Konsist vigila las capas | [fase-01-base.md](planes/fase-01-base.md) | 🟡 Correcciones de la revisión aplicadas; falta probar en el teléfono y unir a `main` |
| 2 | **Biblioteca** | Pide permiso de audio y lista todas las canciones del teléfono con su portada (lista simple) | [fase-02-biblioteca.md](planes/fase-02-biblioteca.md) | 🟡 Implementada (51/51 tests); falta probarla en el teléfono |
| 3 | **Motor de reproducción** | Tocar una canción la reproduce; sigue sonando con la pantalla bloqueada; notificación y auriculares funcionan; aleatorio y repetir | por escribir | ⬜ |
| 4 | **Pantalla Reproduciendo** | Portada circular, fondo difuminado, barra de progreso lima, controles y hoja de cola, igual que la referencia | por escribir | ⬜ |
| 5 | **Pantalla Mi Música** | Chips Todo, Listas, Me gusta, Álbumes, Artistas y Carpetas; detalles de álbum, artista y carpeta; menú de pulsación larga | por escribir | ⬜ |
| 6 | **Datos del usuario** | Favoritos, listas propias, historial y conteo de reproducciones guardados en Room; se restaura la última cola | por escribir | ⬜ |
| 7 | **Pantalla Inicio** | Saludo, chips (Agregadas recientemente, Más escuchadas, Recientes), carrusel "Para ti" con "Redescubre", "Tus listas" y búsqueda | por escribir | ⬜ |
| 8 | **Mini reproductor, Historial y Ajustes** | Píldora flotante con la canción actual; pantalla de historial; ajustes de nombre, avatar, carpetas excluidas y duración mínima | por escribir | ⬜ |
| 9 | **Letras** | Letra sincronizada desde `.lrc` (3 líneas) y letra incrustada estática | por escribir | ⬜ |
| 10 | **Pulido** | Animaciones, estados vacíos y de error, prueba con más de 2000 canciones y verificación de los criterios de éxito en un teléfono real | por escribir | ⬜ |

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
