package com.zenith.player.testing

import com.zenith.player.domain.model.NowPlaying
import com.zenith.player.domain.model.PlayerState
import com.zenith.player.domain.model.Song
import com.zenith.player.domain.player.MusicPlayer
import kotlinx.coroutines.flow.MutableStateFlow

/** Reproductor falso: el estado se fija a mano y cada orden queda anotada en [calls]. */
class FakeMusicPlayer : MusicPlayer {
    override val state = MutableStateFlow(PlayerState())
    override val queue = MutableStateFlow<List<NowPlaying>>(emptyList())

    val calls = mutableListOf<String>()
    var lastQueue: Pair<List<Song>, Int>? = null

    override fun playQueue(songs: List<Song>, startIndex: Int) {
        lastQueue = songs to startIndex
        calls += "playQueue($startIndex)"
    }
    override fun togglePlayPause() { calls += "togglePlayPause" }
    override fun skipNext() { calls += "skipNext" }
    override fun skipPrevious() { calls += "skipPrevious" }
    override fun seekTo(positionMs: Long) { calls += "seekTo($positionMs)" }
    override fun setShuffle(enabled: Boolean) { calls += "setShuffle($enabled)" }
    override fun cycleRepeatMode() { calls += "cycleRepeatMode" }
    override fun skipToQueueItem(index: Int) { calls += "skipToQueueItem($index)" }
    override fun moveQueueItem(from: Int, to: Int) { calls += "moveQueueItem($from, $to)" }
    override fun removeQueueItem(index: Int) { calls += "removeQueueItem($index)" }
}

fun nowPlaying(id: Long, title: String = "Canción $id") =
    NowPlaying(songId = id, title = title, artist = "Budiarti", album = "Noches", artworkUri = "")
