package com.zenith.player.playback

import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeMediaSourceFactory
import androidx.media3.test.utils.TestExoPlayerBuilder
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenith.player.domain.model.RepeatMode
import com.zenith.player.testing.testSong
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PlayerControllerTest {

    // Reproductor real de Media3 con fuentes falsas: no necesita archivos de audio.
    private val exoPlayer: ExoPlayer = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext())
        .setMediaSourceFactory(FakeMediaSourceFactory())
        .build()
    private val scope = CoroutineScope(UnconfinedTestDispatcher())
    private val songs = listOf(testSong(1, "Uno"), testSong(2, "Dos"), testSong(3, "Tres"))

    private fun controller(connect: suspend () -> Player = { exoPlayer }) = PlayerController(connect, scope)

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    @After
    fun tearDown() {
        scope.cancel()
        exoPlayer.release()
    }

    @Test
    fun `reproducir una cola publica la canción, su posición y el tamaño de la cola`() {
        val controller = controller()
        controller.playQueue(songs, startIndex = 1)
        TestPlayerRunHelper.runUntilPlaybackState(exoPlayer, Player.STATE_READY)
        idle()

        val state = controller.state.value
        assertEquals("Dos", state.nowPlaying?.title)
        assertEquals(2L, state.nowPlaying?.songId)
        assertEquals(1, state.currentIndex)
        assertEquals(3, state.queueSize)
        assertTrue("debe estar sonando", state.isPlaying)
    }

    @Test
    fun `siguiente y anterior cambian de canción`() {
        val controller = controller()
        controller.playQueue(songs, startIndex = 0)
        controller.skipNext()
        idle()
        assertEquals("Dos", controller.state.value.nowPlaying?.title)

        controller.skipPrevious()
        idle()
        assertEquals("Uno", controller.state.value.nowPlaying?.title)
    }

    @Test
    fun `play y pausa se alternan`() {
        val controller = controller()
        controller.playQueue(songs, startIndex = 0)
        TestPlayerRunHelper.runUntilPlaybackState(exoPlayer, Player.STATE_READY)

        controller.togglePlayPause()
        idle()
        assertFalse(exoPlayer.playWhenReady)
        assertFalse(controller.state.value.isPlaying)

        controller.togglePlayPause()
        idle()
        assertTrue(exoPlayer.playWhenReady)
    }

    @Test
    fun `aleatorio y repetir llegan al reproductor y al estado`() {
        val controller = controller()
        controller.playQueue(songs, startIndex = 0)

        controller.setShuffle(true)
        controller.cycleRepeatMode()
        idle()
        assertTrue(exoPlayer.shuffleModeEnabled)
        assertEquals(Player.REPEAT_MODE_ALL, exoPlayer.repeatMode)
        assertTrue(controller.state.value.shuffle)
        assertEquals(RepeatMode.All, controller.state.value.repeatMode)

        controller.cycleRepeatMode()
        idle()
        assertEquals(RepeatMode.One, controller.state.value.repeatMode)

        controller.cycleRepeatMode()
        idle()
        assertEquals(RepeatMode.Off, controller.state.value.repeatMode)
    }

    @Test
    fun `las órdenes dadas antes de conectar con el servicio no se pierden`() {
        val connection = CompletableDeferred<Player>()
        val controller = controller { connection.await() }

        controller.playQueue(songs, startIndex = 2)
        assertEquals(0, exoPlayer.mediaItemCount)

        connection.complete(exoPlayer)
        idle()
        assertEquals(3, exoPlayer.mediaItemCount)
        assertEquals("Tres", controller.state.value.nowPlaying?.title)
    }

    @Test
    fun `la cola refleja lo que se reproduce, se mueve y se quita`() {
        val controller = controller()
        controller.playQueue(songs, startIndex = 0)
        idle()
        assertEquals(listOf("Uno", "Dos", "Tres"), controller.queue.value.map { it.title })

        controller.moveQueueItem(from = 2, to = 0)
        idle()
        assertEquals(listOf("Tres", "Uno", "Dos"), controller.queue.value.map { it.title })

        controller.removeQueueItem(index = 2)
        idle()
        assertEquals(listOf("Tres", "Uno"), controller.queue.value.map { it.title })
    }

    @Test
    fun `saltar a un elemento de la cola cambia de canción`() {
        val controller = controller()
        controller.playQueue(songs, startIndex = 0)
        controller.skipToQueueItem(2)
        idle()
        assertEquals("Tres", controller.state.value.nowPlaying?.title)
        assertEquals(2, controller.state.value.currentIndex)
    }

    @Test
    fun `mientras suena, la posición se publica sola cada medio segundo`() {
        val dispatcher = StandardTestDispatcher()
        val tickingScope = CoroutineScope(dispatcher)
        val fake = PlayingAtPositionPlayer()
        val controller = PlayerController({ fake }, tickingScope)
        dispatcher.scheduler.runCurrent()
        assertEquals(0L, controller.state.value.positionMs)

        fake.positionMs = 1_500
        dispatcher.scheduler.advanceTimeBy(600)
        dispatcher.scheduler.runCurrent()
        assertEquals(1_500L, controller.state.value.positionMs)
        tickingScope.cancel()
    }

    /** Reproductor mínimo que siempre "está sonando" en la posición que se le indique. */
    @OptIn(UnstableApi::class)
    private class PlayingAtPositionPlayer : SimpleBasePlayer(Looper.getMainLooper()) {
        var positionMs = 0L
        override fun getState(): State = State.Builder()
            .setAvailableCommands(Player.Commands.Builder().addAllCommands().build())
            .setPlaylist(listOf(MediaItemData.Builder("1").build()))
            .setPlayWhenReady(true, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .setPlaybackState(Player.STATE_READY)
            .setContentPositionMs { positionMs }
            .build()
    }
}
