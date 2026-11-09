package com.zenith.player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenith.player.domain.model.Song
import com.zenith.player.domain.repository.SongRepository
import com.zenith.player.ui.format.formatDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class LibraryViewModel @Inject constructor(
    songRepository: SongRepository,
) : ViewModel() {

    val state: StateFlow<LibraryUiState> = songRepository.observeSongs()
        .map { songs ->
            if (songs.isEmpty()) LibraryUiState.Empty else LibraryUiState.Songs(songs.map(Song::toItem))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState.Loading)
}

private fun Song.toItem() = SongItem(
    id = id,
    title = title,
    subtitle = "$artist • ${formatDuration(durationMs)}",
    artworkUri = artworkUri,
)
