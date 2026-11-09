package com.zenith.player.domain.player

import com.zenith.player.domain.model.PlayerState
import com.zenith.player.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

/** Control de la reproducción. La implementación vive en `playback`; la UI solo conoce esta interfaz. */
interface MusicPlayer {
    val state: StateFlow<PlayerState>

    /** Reemplaza la cola por [songs] y empieza a sonar la de la posición [startIndex]. */
    fun playQueue(songs: List<Song>, startIndex: Int)

    fun togglePlayPause()

    fun skipNext()

    /** Vuelve al inicio de la canción, o a la anterior si acaba de empezar. */
    fun skipPrevious()

    fun seekTo(positionMs: Long)

    fun setShuffle(enabled: Boolean)

    fun cycleRepeatMode()
}
