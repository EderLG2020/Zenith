package com.zenith.player.domain.player

import com.zenith.player.domain.model.NowPlaying
import com.zenith.player.domain.model.PlayerState
import com.zenith.player.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

/** Control de la reproducción. La implementación vive en `playback`; la UI solo conoce esta interfaz. */
interface MusicPlayer {
    val state: StateFlow<PlayerState>

    /** Cola en su orden actual. Va aparte de [state] porque cambia poco y puede ser muy larga. */
    val queue: StateFlow<List<NowPlaying>>

    /** Reemplaza la cola por [songs] y empieza a sonar la de la posición [startIndex]. */
    fun playQueue(songs: List<Song>, startIndex: Int)

    fun togglePlayPause()

    fun skipNext()

    /** Vuelve al inicio de la canción, o a la anterior si acaba de empezar. */
    fun skipPrevious()

    fun seekTo(positionMs: Long)

    fun setShuffle(enabled: Boolean)

    fun cycleRepeatMode()

    fun skipToQueueItem(index: Int)

    fun moveQueueItem(from: Int, to: Int)

    /** Si se quita la canción que suena, pasa a la siguiente. */
    fun removeQueueItem(index: Int)
}
