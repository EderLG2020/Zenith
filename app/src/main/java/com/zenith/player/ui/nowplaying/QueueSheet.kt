package com.zenith.player.ui.nowplaying

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zenith.player.domain.model.NowPlaying
import com.zenith.player.ui.components.SongCover
import com.zenith.player.ui.theme.ZenithColors
import com.zenith.player.ui.theme.ZenithShapes

/** Hoja inferior con la cola. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    queue: List<NowPlaying>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onItemClick: (Int) -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onRemove: (Int) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ZenithColors.Surface) {
        Text(
            "A continuación",
            style = MaterialTheme.typography.titleLarge,
            color = ZenithColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        QueueList(queue, currentIndex, onItemClick, onMove, onRemove, Modifier.navigationBarsPadding())
    }
}

/**
 * Lista de la cola. Reordenar con "Subir" y "Bajar" en vez de arrastrar: es más fiable
 * y funciona con TalkBack. La canción que suena va en lima.
 */
@Composable
fun QueueList(
    queue: List<NowPlaying>,
    currentIndex: Int,
    onItemClick: (Int) -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxWidth()) {
        itemsIndexed(queue) { index, song ->
            val isCurrent = index == currentIndex
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(index) }
                    .padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(44.dp).clip(ZenithShapes.Cover)) { SongCover(song.artworkUri, seed = song.title) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isCurrent) ZenithColors.Accent else ZenithColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZenithColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (index > 0) QueueAction(Icons.Rounded.KeyboardArrowUp, "Subir ${song.title}") { onMove(index, index - 1) }
                if (index < queue.lastIndex) {
                    QueueAction(Icons.Rounded.KeyboardArrowDown, "Bajar ${song.title}") { onMove(index, index + 1) }
                }
                QueueAction(Icons.Rounded.Close, "Quitar ${song.title}") { onRemove(index) }
            }
        }
    }
}

@Composable
private fun QueueAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = description, tint = ZenithColors.TextSecondary)
    }
}
