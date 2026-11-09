package com.zenith.player.domain.model

/** Lo mínimo para mostrar la canción que suena, aunque la cola se haya creado en otra sesión de la app. */
data class NowPlaying(
    val songId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUri: String,
)

/** Estado del reproductor que observa la UI. */
data class PlayerState(
    val nowPlaying: NowPlaying? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val queueSize: Int = 0,
    val currentIndex: Int = -1,
)
