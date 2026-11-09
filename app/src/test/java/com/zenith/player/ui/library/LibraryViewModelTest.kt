package com.zenith.player.ui.library

import com.zenith.player.domain.model.Song
import com.zenith.player.domain.repository.SongRepository
import com.zenith.player.testing.testSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private class FakeSongRepository : SongRepository {
        val songs = MutableSharedFlow<List<Song>>()
        override fun observeSongs() = songs
    }

    private val repository = FakeSongRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `empieza cargando`() {
        assertEquals(LibraryUiState.Loading, LibraryViewModel(repository).state.value)
    }

    @Test
    fun `muestra las canciones con artista y duración`() = runTest {
        val viewModel = LibraryViewModel(repository)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        repository.songs.emit(listOf(testSong(id = 7, title = "Starlit Reverie", artist = "Budiarti", durationMs = 200_000)))

        val expected = SongItem(7, "Starlit Reverie", "Budiarti • 3:20", "content://media/external/audio/albums/1")
        assertEquals(LibraryUiState.Songs(listOf(expected)), viewModel.state.value)
        job.cancel()
    }

    @Test
    fun `sin canciones muestra el estado vacío y se actualiza si llegan nuevas`() = runTest {
        val viewModel = LibraryViewModel(repository)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        repository.songs.emit(emptyList())
        assertEquals(LibraryUiState.Empty, viewModel.state.value)

        repository.songs.emit(listOf(testSong(1), testSong(2)))
        assertEquals(2, (viewModel.state.value as LibraryUiState.Songs).songs.size)
        job.cancel()
    }
}
