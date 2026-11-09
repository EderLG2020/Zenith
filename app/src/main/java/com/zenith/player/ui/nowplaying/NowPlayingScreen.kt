package com.zenith.player.ui.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.player.domain.model.NowPlaying
import com.zenith.player.ui.components.CircleIconButton
import com.zenith.player.ui.theme.ZenithColors
import com.zenith.player.ui.theme.ZenithTheme

/** Acciones de la pantalla. Todas tienen un valor por defecto para previews y tests. */
data class NowPlayingActions(
    val onBack: () -> Unit = {},
    val onFavorite: () -> Unit = {},
    val onPlayPause: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onPrevious: () -> Unit = {},
    val onSeek: (Long) -> Unit = {},
    val onShuffleClick: () -> Unit = {},
    val onShuffleLongClick: () -> Unit = {},
    val onQueueItemClick: (Int) -> Unit = {},
    val onMoveQueueItem: (Int, Int) -> Unit = { _, _ -> },
    val onRemoveQueueItem: (Int) -> Unit = {},
)

/** Reproduciendo: la pantalla central de doc/referencia.png (diseño §5.2). */
@Composable
fun NowPlayingScreen(state: NowPlayingUiState, actions: NowPlayingActions, modifier: Modifier = Modifier) {
    var showQueue by rememberSaveable { mutableStateOf(false) }
    val song = state.song

    Box(modifier.fillMaxSize()) {
        NowPlayingBackground(song?.artworkUri.orEmpty(), seed = song?.title.orEmpty())
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(actions.onBack, actions.onFavorite, showFavorite = song != null)
            if (song == null) {
                NothingPlaying()
            } else {
                Playing(song, state, actions, onQueueClick = { showQueue = true })
            }
        }
    }

    if (showQueue) {
        QueueSheet(
            queue = state.queue,
            currentIndex = state.currentIndex,
            onDismiss = { showQueue = false },
            onItemClick = actions.onQueueItemClick,
            onMove = actions.onMoveQueueItem,
            onRemove = actions.onRemoveQueueItem,
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, onFavorite: () -> Unit, showFavorite: Boolean) {
    val translucent = ZenithColors.SurfaceHigh.copy(alpha = 0.6f)
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Atrás", onBack, containerColor = translucent)
        Text(
            "Reproduciendo",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = ZenithColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        if (showFavorite) {
            // Guardar favoritos llega en la fase 6.
            CircleIconButton(Icons.Rounded.FavoriteBorder, "Me gusta", onFavorite, containerColor = translucent)
        } else {
            Spacer(Modifier.size(44.dp))
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.Playing(
    song: NowPlaying,
    state: NowPlayingUiState,
    actions: NowPlayingActions,
    onQueueClick: () -> Unit,
) {
    Spacer(Modifier.weight(1f))
    CircularCover(song.artworkUri, seed = song.title)
    Spacer(Modifier.height(32.dp))
    Text(
        song.title,
        style = MaterialTheme.typography.titleLarge,
        color = ZenithColors.TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        song.artist,
        style = MaterialTheme.typography.bodySmall,
        color = ZenithColors.TextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    // Aquí irá la letra (fase 9). Mientras tanto, la portada queda centrada.
    Spacer(Modifier.weight(1f))
    SeekBar(state.positionMs, state.durationMs, state.elapsed, state.remaining, actions.onSeek)
    Spacer(Modifier.height(16.dp))
    PlaybackControls(
        isPlaying = state.isPlaying,
        shuffle = state.shuffle,
        repeatMode = state.repeatMode,
        onPlayPause = actions.onPlayPause,
        onPrevious = actions.onPrevious,
        onNext = actions.onNext,
        onShuffleClick = actions.onShuffleClick,
        onShuffleLongClick = actions.onShuffleLongClick,
        onQueueClick = onQueueClick,
    )
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun NothingPlaying() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Nada sonando", style = MaterialTheme.typography.titleMedium, color = ZenithColors.TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Elige una canción en Mi Música.",
            style = MaterialTheme.typography.bodySmall,
            color = ZenithColors.TextSecondary,
        )
    }
}

@Preview(backgroundColor = 0xFF0E0E12, showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun NowPlayingScreenPreview() {
    ZenithTheme {
        NowPlayingScreen(
            NowPlayingUiState(
                song = NowPlaying(1, "Starlit Reverie", "Budiarti x Lil magrib", "Noches", ""),
                positionMs = 28_000,
                durationMs = 163_000,
                elapsed = "0:28",
                remaining = "-2:15",
                isPlaying = true,
            ),
            NowPlayingActions(),
        )
    }
}
