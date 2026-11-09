package com.zenith.player.data.mediastore

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class MediaStoreSongSourceTest {

    /** MediaStore falso: devuelve las filas indicadas y guarda la última consulta. */
    class FakeMediaProvider : ContentProvider() {
        val rows = mutableListOf<Map<String, Any?>>()
        var lastSelection: String? = null
        var lastSelectionArgs: List<String> = emptyList()

        override fun query(
            uri: Uri, projection: Array<out String>?, selection: String?,
            selectionArgs: Array<out String>?, sortOrder: String?,
        ): Cursor {
            lastSelection = selection
            lastSelectionArgs = selectionArgs.orEmpty().toList()
            val columns = projection!!
            return MatrixCursor(columns).apply {
                rows.forEach { row -> addRow(columns.map { row[it] }) }
            }
        }

        override fun onCreate() = true
        override fun getType(uri: Uri): String? = null
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
        override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
    }

    private lateinit var provider: FakeMediaProvider
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val source by lazy { MediaStoreSongSource(context) }

    @Before
    fun setUp() {
        provider = Robolectric.buildContentProvider(FakeMediaProvider::class.java).create(MediaStore.AUTHORITY).get()
    }

    private fun row(
        id: Long = 7, title: String? = "Starlit Reverie", artist: String? = "Budiarti",
        album: String? = "Noches", albumId: Long = 3, displayName: String = "starlit.mp3",
    ) = mapOf(
        MediaStore.Audio.Media._ID to id,
        MediaStore.Audio.Media.TITLE to title,
        MediaStore.Audio.Media.ARTIST to artist,
        MediaStore.Audio.Media.ALBUM to album,
        MediaStore.Audio.Media.ALBUM_ID to albumId,
        MediaStore.Audio.Media.DURATION to 200_000L,
        MediaStore.Audio.Media.DISPLAY_NAME to displayName,
        MediaStore.Audio.Media.RELATIVE_PATH to "Music/Rock/",
        MediaStore.Audio.Media.DATE_ADDED to 1_700_000_000L,
        MediaStore.Audio.Media.TRACK to 4,
    )

    @Test
    fun `convierte cada fila de MediaStore en una canción`() {
        provider.rows += row()
        val song = source.query().single()
        assertEquals(7L, song.id)
        assertEquals("Starlit Reverie", song.title)
        assertEquals("Budiarti", song.artist)
        assertEquals("Noches", song.album)
        assertEquals(3L, song.albumId)
        assertEquals(200_000L, song.durationMs)
        assertEquals("content://media/external/audio/media/7", song.uri)
        assertEquals("content://media/external/audio/albums/3", song.artworkUri)
        assertEquals("Music/Rock", song.folder)
        assertEquals(1_700_000_000L, song.dateAddedSec)
        assertEquals(4, song.trackNumber)
    }

    @Test
    fun `metadatos desconocidos se muestran en español`() {
        provider.rows += row(artist = "<unknown>", album = null)
        val song = source.query().single()
        assertEquals("Artista desconocido", song.artist)
        assertEquals("Álbum desconocido", song.album)
    }

    @Test
    fun `sin título usa el nombre del archivo sin extensión`() {
        provider.rows += row(title = " ", displayName = "mi grabación.final.flac")
        assertEquals("mi grabación.final", source.query().single().title)
    }

    @Test
    fun `solo pide música de al menos 30 segundos`() {
        source.query()
        val selection = provider.lastSelection.orEmpty()
        assertTrue(selection, selection.contains("${MediaStore.Audio.Media.IS_MUSIC} != 0"))
        assertTrue(selection, selection.contains("${MediaStore.Audio.Media.DURATION} >= ?"))
        assertEquals(listOf("30000"), provider.lastSelectionArgs)
    }

    @Test
    fun `avisa cuando cambia la música del teléfono`() = runTest(UnconfinedTestDispatcher()) {
        var changes = 0
        val job = launch { source.changes().collect { changes++ } }
        context.contentResolver.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null)
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(1, changes)
        job.cancel()
    }
}
