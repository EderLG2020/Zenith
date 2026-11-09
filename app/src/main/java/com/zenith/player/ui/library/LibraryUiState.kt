package com.zenith.player.ui.library

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data object Empty : LibraryUiState
    data class Songs(val songs: List<SongItem>) : LibraryUiState
}

/** Una fila de la lista, ya con el texto listo para mostrar. */
data class SongItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val artworkUri: String,
)
