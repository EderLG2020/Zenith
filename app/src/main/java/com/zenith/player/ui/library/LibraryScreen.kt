package com.zenith.player.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenith.player.ui.components.SongCover
import com.zenith.player.ui.components.SongRow
import com.zenith.player.ui.theme.ZenithColors
import com.zenith.player.ui.theme.ZenithTheme

internal const val LIBRARY_LIST_TAG = "library_list"
internal const val LIBRARY_LOADING_TAG = "library_loading"

/** Espacio libre al final de la lista para que la barra flotante (68 dp + 12 dp de margen) no tape nada. */
private val FloatingBarSpace = 96.dp

/** Mi Música (fase 2): lista simple de todas las canciones. Los chips y los detalles llegan en la fase 5. */
@Composable
fun LibraryScreen(
    state: LibraryUiState,
    modifier: Modifier = Modifier,
    onSongClick: (Long) -> Unit = {},
) {
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Header(state)
        when (state) {
            LibraryUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = ZenithColors.Accent,
                    modifier = Modifier.testTag(LIBRARY_LOADING_TAG),
                )
            }
            LibraryUiState.Empty -> EmptyLibrary()
            is LibraryUiState.Songs -> SongList(state.songs, onSongClick)
        }
    }
}

@Composable
private fun Header(state: LibraryUiState) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Mi Música", style = MaterialTheme.typography.titleLarge, color = ZenithColors.TextPrimary)
        if (state is LibraryUiState.Songs) {
            val count = state.songs.size
            Text(
                text = if (count == 1) "1 canción" else "$count canciones",
                style = MaterialTheme.typography.bodySmall,
                color = ZenithColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun SongList(songs: List<SongItem>, onSongClick: (Long) -> Unit) {
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + FloatingBarSpace
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag(LIBRARY_LIST_TAG),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = bottom),
    ) {
        items(songs, key = { it.id }) { song ->
            SongRow(
                title = song.title,
                subtitle = song.subtitle,
                onClick = { onSongClick(song.id) },
                onPlayClick = { onSongClick(song.id) },
                cover = { SongCover(song.artworkUri, seed = song.title) },
            )
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "No encontramos música en tu teléfono",
            style = MaterialTheme.typography.titleMedium,
            color = ZenithColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Copia canciones a la carpeta Música y aparecerán aquí solas.",
            style = MaterialTheme.typography.bodySmall,
            color = ZenithColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(FloatingBarSpace))
    }
}

@Preview(backgroundColor = 0xFF0E0E12, showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun LibraryScreenPreview() {
    ZenithTheme {
        LibraryScreen(
            LibraryUiState.Songs(
                listOf(
                    SongItem(1, "Starlit Reverie", "Budiarti • 3:20", ""),
                    SongItem(2, "Midnight Confessions", "Alexiao • 4:05", ""),
                ),
            ),
        )
    }
}
