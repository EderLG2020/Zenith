package com.zenith.player.data.repository

import com.zenith.player.data.mediastore.SongSource
import com.zenith.player.domain.model.Song
import com.zenith.player.testing.testSong
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongRepositoryImplTest {

    private class FakeSongSource : SongSource {
        var songs = listOf(testSong(1))
        val changes = MutableSharedFlow<Unit>()
        override fun query(): List<Song> = songs
        override fun changes() = changes
    }

    /** Dispatcher que ejecuta todo en el acto y cuenta cuántas veces se le pidió trabajo. */
    private class CountingDispatcher : CoroutineDispatcher() {
        var dispatches = 0
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            dispatches++
            block.run()
        }
    }

    @Test
    fun `emite las canciones al empezar y otra vez con cada cambio`() = runTest(UnconfinedTestDispatcher()) {
        val source = FakeSongSource()
        val repository = SongRepositoryImpl(source, UnconfinedTestDispatcher(testScheduler))
        val emitted = mutableListOf<List<Song>>()
        val job = launch { repository.observeSongs().collect { emitted += it } }

        source.songs = listOf(testSong(1), testSong(2))
        source.changes.emit(Unit)

        assertEquals(listOf(listOf(1L), listOf(1L, 2L)), emitted.map { list -> list.map { it.id } })
        job.cancel()
    }

    @Test
    fun `la consulta corre en el dispatcher de entrada y salida, no en el del colector`() = runTest {
        val io = CountingDispatcher()
        val repository = SongRepositoryImpl(FakeSongSource(), io)
        repository.observeSongs().first()
        assertTrue("la consulta debe pasar por el dispatcher de IO", io.dispatches > 0)
    }
}
