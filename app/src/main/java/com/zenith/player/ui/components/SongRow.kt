package com.zenith.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenith.player.ui.theme.ZenithColors
import com.zenith.player.ui.theme.ZenithShapes
import com.zenith.player.ui.theme.ZenithTheme

/** Fila de canción, lista o álbum: portada, título, subtítulo y botón de play circular. */
@Composable
fun SongRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    cover: @Composable () -> Unit = { CoverPlaceholder(title) },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(56.dp).clip(ZenithShapes.Cover)) { cover() }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ZenithColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ZenithColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(12.dp))
        CircleIconButton(
            icon = Icons.Rounded.PlayArrow,
            contentDescription = "Reproducir $title",
            onClick = onPlayClick,
            size = 36.dp,
        )
    }
}

@Preview(backgroundColor = 0xFF0E0E12, showBackground = true, widthDp = 360)
@Composable
private fun SongRowPreview() {
    ZenithTheme {
        Column(Modifier.padding(16.dp)) {
            SongRow("Starlit Reverie", "Por Budiarti • 8 canciones", onClick = {}, onPlayClick = {})
            SongRow("Midnight Confessions", "Por Alexiao • 24 canciones", onClick = {}, onPlayClick = {})
        }
    }
}
