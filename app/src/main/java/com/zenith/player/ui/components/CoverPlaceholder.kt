package com.zenith.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Dos colores para el degradado de una canción sin portada.
 * Determinista: la misma semilla (normalmente el título) da siempre los mismos colores.
 */
fun placeholderColorsFor(seed: String): Pair<Color, Color> {
    val hue = (abs(seed.hashCode().toLong()) % 360).toFloat()
    val second = (hue + 40f) % 360f
    return Color.hsv(hue, 0.45f, 0.55f) to Color.hsv(second, 0.45f, 0.35f)
}

/** Portada de reemplazo: degradado según [seed] con una nota musical encima. */
@Composable
fun CoverPlaceholder(seed: String, modifier: Modifier = Modifier) {
    val (start, end) = placeholderColorsFor(seed)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(start, end))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview
@Composable
private fun CoverPlaceholderPreview() {
    CoverPlaceholder("Starlit Reverie", Modifier.size(56.dp))
}
