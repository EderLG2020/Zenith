package com.zenith.player.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Portada de una canción. El placeholder de degradado queda debajo: se ve mientras carga
 * y también si el álbum no tiene imagen, así nunca queda un hueco vacío.
 */
@Composable
fun SongCover(artworkUri: String, seed: String, modifier: Modifier = Modifier) {
    Box(modifier) {
        CoverPlaceholder(seed)
        if (artworkUri.isNotEmpty()) {
            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
