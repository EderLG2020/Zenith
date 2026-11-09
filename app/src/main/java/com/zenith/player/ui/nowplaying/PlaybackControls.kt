package com.zenith.player.ui.nowplaying

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.player.domain.model.RepeatMode
import com.zenith.player.ui.components.CircleIconButton
import com.zenith.player.ui.theme.ZenithColors

/** Fila de controles de la referencia: aleatorio, anterior, play/pausa lima, siguiente y cola. */
@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    shuffle: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffleClick: () -> Unit,
    onShuffleLongClick: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShuffleRepeatButton(shuffle, repeatMode, onShuffleClick, onShuffleLongClick)
        CircleIconButton(Icons.Rounded.SkipPrevious, "Anterior", onPrevious, size = 48.dp)
        CircleIconButton(
            icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
            onClick = onPlayPause,
            containerColor = ZenithColors.Accent,
            contentColor = ZenithColors.OnAccent,
            size = 68.dp,
        )
        CircleIconButton(Icons.Rounded.SkipNext, "Siguiente", onNext, size = 48.dp)
        CircleIconButton(
            Icons.AutoMirrored.Rounded.QueueMusic,
            "Cola",
            onQueueClick,
            containerColor = ZenithColors.Background.copy(alpha = 0f),
            size = 48.dp,
        )
    }
}

/**
 * Aleatorio: tocar lo activa o desactiva; mantener pulsado cambia el modo de repetir.
 * El modo de repetir se indica con un punto lima (toda la cola) o un "1" (una canción).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShuffleRepeatButton(
    shuffle: Boolean,
    repeatMode: RepeatMode,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val description = buildString {
        append(if (shuffle) "Aleatorio activado" else "Aleatorio desactivado")
        append(
            when (repeatMode) {
                RepeatMode.Off -> ", repetir desactivado"
                RepeatMode.All -> ", repetir toda la cola"
                RepeatMode.One -> ", repetir una canción"
            },
        )
    }
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .combinedClickable(
                role = Role.Button,
                onClickLabel = "Cambiar aleatorio",
                onLongClickLabel = "Cambiar repetir",
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .semantics { stateDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Shuffle,
            contentDescription = "Aleatorio",
            tint = if (shuffle) ZenithColors.Accent else ZenithColors.TextPrimary,
            modifier = Modifier.size(22.dp),
        )
        when (repeatMode) {
            RepeatMode.Off -> Unit
            RepeatMode.All -> Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-6).dp)
                    .size(5.dp)
                    .background(ZenithColors.Accent, CircleShape)
                    .clearAndSetSemantics {},
            )
            RepeatMode.One -> Text(
                "1",
                style = TextStyle(fontSize = 10.sp, color = ZenithColors.Accent),
                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = 6.dp),
            )
        }
    }
}
