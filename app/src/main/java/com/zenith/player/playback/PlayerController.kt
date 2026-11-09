package com.zenith.player.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.zenith.player.domain.model.NowPlaying
import com.zenith.player.domain.model.PlayerState
import com.zenith.player.domain.model.RepeatMode
import com.zenith.player.domain.model.Song
import com.zenith.player.domain.player.MusicPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

/**
 * Puente entre la app y [PlaybackService]. Recibe cualquier [Player]: en la app es un
 * [MediaController] conectado al servicio, y en los tests un ExoPlayer de prueba.
 *
 * Las órdenes que llegan antes de terminar la conexión esperan y se ejecutan al conectar.
 * Todo acceso al [Player] ocurre en [scope], que debe usar el hilo principal.
 */
@Singleton
class PlayerController internal constructor(
    connect: suspend () -> Player,
    private val scope: CoroutineScope,
) : MusicPlayer {

    @Inject
    constructor(@ApplicationContext context: Context) : this(
        connect = {
            val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            MediaController.Builder(context, token).buildAsync().await()
        },
        scope = MainScope(),
    )

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _queue = MutableStateFlow<List<NowPlaying>>(emptyList())
    override val queue: StateFlow<List<NowPlaying>> = _queue.asStateFlow()

    /** Publica la posición cada [PROGRESS_INTERVAL_MS] mientras suena; null si está en pausa. */
    private var progressJob: Job? = null

    private val player: Deferred<Player> = scope.async(start = CoroutineStart.LAZY) {
        connect().also { player ->
            player.addListener(Listener(player))
            publish(player)
            publishQueue(player)
        }
    }

    init {
        // Conecta enseguida para mostrar lo que ya suena si la app se reabre con música de fondo.
        player.start()
    }

    override fun playQueue(songs: List<Song>, startIndex: Int) = command {
        setMediaItems(songs.map(Song::toMediaItem), startIndex, 0L)
        prepare()
        play()
    }

    override fun togglePlayPause() = command {
        if (playWhenReady && playbackState != Player.STATE_ENDED) {
            pause()
        } else {
            if (playbackState == Player.STATE_IDLE) prepare()
            if (playbackState == Player.STATE_ENDED) seekToDefaultPosition()
            play()
        }
    }

    override fun skipNext() = command { seekToNext() }

    override fun skipPrevious() = command { seekToPrevious() }

    override fun seekTo(positionMs: Long) = command { seekTo(positionMs) }

    override fun setShuffle(enabled: Boolean) = command { shuffleModeEnabled = enabled }

    override fun cycleRepeatMode() = command { repeatMode = repeatMode.toDomain().next().toMedia3() }

    override fun skipToQueueItem(index: Int) = command {
        seekToDefaultPosition(index)
        play()
    }

    override fun moveQueueItem(from: Int, to: Int) = command { moveMediaItem(from, to) }

    override fun removeQueueItem(index: Int) = command { removeMediaItem(index) }

    private fun command(block: Player.() -> Unit) {
        scope.launch {
            val player = player.await()
            player.block()
            publish(player)
        }
    }

    private fun publish(player: Player) {
        _state.value = PlayerState(
            nowPlaying = player.currentMediaItem?.toNowPlaying(),
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition,
            durationMs = player.duration.coerceAtLeast(0L),
            shuffle = player.shuffleModeEnabled,
            repeatMode = player.repeatMode.toDomain(),
            queueSize = player.mediaItemCount,
            currentIndex = player.currentMediaItemIndex,
        )
        updateProgressTicker(player)
    }

    private fun publishQueue(player: Player) {
        _queue.value = (0 until player.mediaItemCount).mapNotNull { player.getMediaItemAt(it).toNowPlaying() }
    }

    private fun updateProgressTicker(player: Player) {
        if (player.isPlaying && progressJob == null) {
            progressJob = scope.launch {
                while (isActive) {
                    delay(PROGRESS_INTERVAL_MS)
                    _state.value = _state.value.copy(positionMs = player.currentPosition)
                }
            }
        } else if (!player.isPlaying) {
            progressJob?.cancel()
            progressJob = null
        }
    }

    private inner class Listener(private val player: Player) : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish(player)
            if (events.contains(Player.EVENT_TIMELINE_CHANGED)) publishQueue(player)
        }

        /** Archivo dañado o borrado: se salta a la siguiente en lugar de quedarse detenido. */
        override fun onPlayerError(error: PlaybackException) {
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
                player.prepare()
                player.play()
            }
        }
    }
}

private const val PROGRESS_INTERVAL_MS = 500L

private fun Int.toDomain(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ALL -> RepeatMode.All
    Player.REPEAT_MODE_ONE -> RepeatMode.One
    else -> RepeatMode.Off
}

private fun RepeatMode.toMedia3(): Int = when (this) {
    RepeatMode.Off -> Player.REPEAT_MODE_OFF
    RepeatMode.All -> Player.REPEAT_MODE_ALL
    RepeatMode.One -> Player.REPEAT_MODE_ONE
}
