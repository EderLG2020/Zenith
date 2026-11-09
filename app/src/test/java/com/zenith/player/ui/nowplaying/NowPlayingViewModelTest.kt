package com.zenith.player.ui.nowplaying

import com.zenith.player.domain.model.PlayerState
import com.zenith.player.domain.model.RepeatMode
import com.zenith.player.testing.FakeMusicPlayer
import com.zenith.player.testing.nowPlaying
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NowPlayingViewModelTest {

    private val player = FakeMusicPlayer()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `sin nada sonando no hay canción`() {
        assertNull(NowPlayingViewModel(player).state.value.song)
    }

    @Test
    fun `arma el estado con la canción, los tiempos, los modos y la cola`() = runTest {
        val viewModel = NowPlayingViewModel(player)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        player.queue.value = listOf(nowPlaying(1), nowPlaying(2, "Starlit Reverie"))
        player.state.value = PlayerState(
            nowPlaying = nowPlaying(2, "Starlit Reverie"),
            isPlaying = true,
            positionMs = 28_000,
            durationMs = 163_000,
            shuffle = true,
            repeatMode = RepeatMode.One,
            queueSize = 2,
            currentIndex = 1,
        )

        val state = viewModel.state.value
        assertEquals("Starlit Reverie", state.song?.title)
        assertEquals("0:28", state.elapsed)
        assertEquals("-2:15", state.remaining)
        assertTrue(state.isPlaying)
        assertTrue(state.shuffle)
        assertEquals(RepeatMode.One, state.repeatMode)
        assertEquals(2, state.queue.size)
        assertEquals(1, state.currentIndex)
        job.cancel()
    }

    @Test
    fun `cada evento llega al reproductor`() {
        val viewModel = NowPlayingViewModel(player)
        player.state.value = PlayerState(shuffle = false)

        viewModel.onPlayPause()
        viewModel.onNext()
        viewModel.onPrevious()
        viewModel.onSeek(10_000)
        viewModel.onShuffleClick()
        viewModel.onShuffleLongClick()
        viewModel.onQueueItemClick(3)
        viewModel.onMoveQueueItem(3, 2)
        viewModel.onRemoveQueueItem(1)

        assertEquals(
            listOf(
                "togglePlayPause", "skipNext", "skipPrevious", "seekTo(10000)", "setShuffle(true)",
                "cycleRepeatMode", "skipToQueueItem(3)", "moveQueueItem(3, 2)", "removeQueueItem(1)",
            ),
            player.calls,
        )
    }
}
