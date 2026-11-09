package com.zenith.player.ui.nowplaying

import com.zenith.player.domain.model.NowPlaying
import com.zenith.player.domain.model.RepeatMode

data class NowPlayingUiState(
    /** null cuando no suena nada (por ejemplo, al reabrir la app sin cola). */
    val song: NowPlaying? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val elapsed: String = "0:00",
    val remaining: String = "-0:00",
    val isPlaying: Boolean = false,
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val queue: List<NowPlaying> = emptyList(),
    val currentIndex: Int = -1,
)
