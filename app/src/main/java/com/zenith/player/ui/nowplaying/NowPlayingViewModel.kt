package com.zenith.player.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenith.player.domain.model.NowPlaying
import com.zenith.player.domain.model.PlayerState
import com.zenith.player.domain.player.MusicPlayer
import com.zenith.player.ui.format.formatDuration
import com.zenith.player.ui.format.formatRemaining
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val musicPlayer: MusicPlayer,
) : ViewModel() {

    val state: StateFlow<NowPlayingUiState> = combine(musicPlayer.state, musicPlayer.queue, ::toUiState)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            toUiState(musicPlayer.state.value, musicPlayer.queue.value),
        )

    fun onPlayPause() = musicPlayer.togglePlayPause()

    fun onNext() = musicPlayer.skipNext()

    fun onPrevious() = musicPlayer.skipPrevious()

    fun onSeek(positionMs: Long) = musicPlayer.seekTo(positionMs)

    fun onShuffleClick() = musicPlayer.setShuffle(!musicPlayer.state.value.shuffle)

    /** La pulsación larga en aleatorio cambia el modo de repetir (diseño §5.2). */
    fun onShuffleLongClick() = musicPlayer.cycleRepeatMode()

    fun onQueueItemClick(index: Int) = musicPlayer.skipToQueueItem(index)

    fun onMoveQueueItem(from: Int, to: Int) = musicPlayer.moveQueueItem(from, to)

    fun onRemoveQueueItem(index: Int) = musicPlayer.removeQueueItem(index)
}

private fun toUiState(player: PlayerState, queue: List<NowPlaying>) = NowPlayingUiState(
    song = player.nowPlaying,
    positionMs = player.positionMs,
    durationMs = player.durationMs,
    elapsed = formatDuration(player.positionMs),
    remaining = formatRemaining(player.positionMs, player.durationMs),
    isPlaying = player.isPlaying,
    shuffle = player.shuffle,
    repeatMode = player.repeatMode,
    queue = queue,
    currentIndex = player.currentIndex,
)
