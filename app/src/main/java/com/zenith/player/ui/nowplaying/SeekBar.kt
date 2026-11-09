package com.zenith.player.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import com.zenith.player.ui.format.formatDuration
import com.zenith.player.ui.theme.ZenithColors

/**
 * Barra de progreso lima. Se puede tocar o arrastrar: mientras se arrastra muestra la posición
 * del dedo (los ticks del reproductor no la mueven) y al soltar llama a [onSeek].
 */
@Composable
fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    elapsed: String,
    remaining: String,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val playedFraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val fraction = dragFraction ?: playedFraction
    fun seekTo(f: Float) = onSeek((f.coerceIn(0f, 1f) * durationMs).toLong())

    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(24.dp)
                .semantics {
                    contentDescription = "Progreso de la canción"
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    setProgress { target ->
                        seekTo(target)
                        true
                    }
                }
                .pointerInput(durationMs) {
                    detectTapGestures { offset -> seekTo(offset.x / size.width) }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> dragFraction = (offset.x / size.width).coerceIn(0f, 1f) },
                        onDragEnd = {
                            dragFraction?.let(::seekTo)
                            dragFraction = null
                        },
                        onDragCancel = { dragFraction = null },
                        onHorizontalDrag = { change, _ ->
                            dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        },
                    )
                },
        ) {
            val y = size.height / 2
            val track = 3.dp.toPx()
            val playedX = size.width * fraction
            drawLine(ZenithColors.SurfaceHigh, Offset(0f, y), Offset(size.width, y), track, StrokeCap.Round)
            drawLine(ZenithColors.Accent, Offset(0f, y), Offset(playedX, y), track, StrokeCap.Round)
            drawCircle(ZenithColors.Accent, radius = 6.dp.toPx(), center = Offset(playedX, y))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val shownElapsed = dragFraction?.let { formatDuration((it * durationMs).toLong()) } ?: elapsed
            Text(shownElapsed, style = MaterialTheme.typography.bodySmall, color = ZenithColors.TextSecondary)
            Text(remaining, style = MaterialTheme.typography.bodySmall, color = ZenithColors.TextSecondary)
        }
    }
}
