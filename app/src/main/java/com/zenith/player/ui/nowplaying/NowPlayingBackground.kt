package com.zenith.player.ui.nowplaying

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.zenith.player.ui.components.SongCover
import com.zenith.player.ui.theme.ZenithColors

/**
 * La misma portada, muy difuminada, de fondo, con un degradado hacia negro desde la mitad.
 * `blur` solo funciona en Android 12+; en versiones anteriores la portada se oscurece más.
 */
@Composable
fun NowPlayingBackground(artworkUri: String, seed: String, modifier: Modifier = Modifier) {
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    Box(modifier.fillMaxSize().background(ZenithColors.Background)) {
        SongCover(
            artworkUri = artworkUri,
            seed = seed,
            modifier = Modifier
                .fillMaxSize()
                .blur(60.dp)
                .alpha(if (canBlur) 0.6f else 0.3f),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to ZenithColors.Background.copy(alpha = 0.1f),
                        0.55f to ZenithColors.Background.copy(alpha = 0.85f),
                        1f to ZenithColors.Background,
                    ),
                ),
        )
    }
}

/** Portada circular grande: 72 % del ancho, como máximo 320 dp. */
@Composable
fun CircularCover(artworkUri: String, seed: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        val side = min(maxWidth * 0.72f, 320.dp)
        SongCover(artworkUri, seed, Modifier.size(side).clip(CircleShape))
    }
}
