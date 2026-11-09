package com.zenith.player.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val ZenithColorScheme = darkColorScheme(
    primary = ZenithColors.Accent,
    onPrimary = ZenithColors.OnAccent,
    secondary = ZenithColors.Featured,
    onSecondary = ZenithColors.FeaturedButton,
    background = ZenithColors.Background,
    onBackground = ZenithColors.TextPrimary,
    surface = ZenithColors.Background,
    onSurface = ZenithColors.TextPrimary,
    surfaceVariant = ZenithColors.Surface,
    onSurfaceVariant = ZenithColors.TextSecondary,
)

/**
 * Siempre oscuro: no depende del modo del sistema (doc/01-diseno.md §4).
 * El color de contenido por defecto es blanco para que el ripple al pulsar se vea sobre el fondo oscuro.
 */
@Composable
fun ZenithTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ZenithColorScheme,
        typography = ZenithTypography,
    ) {
        CompositionLocalProvider(LocalContentColor provides ZenithColors.TextPrimary, content = content)
    }
}
