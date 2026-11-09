package com.zenith.player.ui.nowplaying

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Reproduciendo conectada a su ViewModel. */
@Composable
fun NowPlayingRoute(onBack: () -> Unit, viewModel: NowPlayingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack) {
        NowPlayingActions(
            onBack = onBack,
            onPlayPause = viewModel::onPlayPause,
            onNext = viewModel::onNext,
            onPrevious = viewModel::onPrevious,
            onSeek = viewModel::onSeek,
            onShuffleClick = viewModel::onShuffleClick,
            onShuffleLongClick = viewModel::onShuffleLongClick,
            onQueueItemClick = viewModel::onQueueItemClick,
            onMoveQueueItem = viewModel::onMoveQueueItem,
            onRemoveQueueItem = viewModel::onRemoveQueueItem,
        )
    }
    NowPlayingScreen(state, actions)
}
