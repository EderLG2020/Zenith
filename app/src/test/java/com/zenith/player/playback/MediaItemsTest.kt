package com.zenith.player.playback

import androidx.media3.common.MediaItem
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenith.player.testing.testSong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaItemsTest {

    @Test
    fun `una canción se convierte en un elemento de reproducción con sus datos`() {
        val item = testSong(id = 7, title = "Starlit Reverie", artist = "Budiarti").toMediaItem()
        assertEquals("7", item.mediaId)
        assertEquals("Starlit Reverie", item.mediaMetadata.title.toString())
        assertEquals("Budiarti", item.mediaMetadata.artist.toString())
        assertEquals("Noches", item.mediaMetadata.albumTitle.toString())
        assertEquals("content://media/external/audio/albums/1", item.mediaMetadata.artworkUri.toString())
        assertEquals("content://media/external/audio/media/7", item.localConfiguration?.uri.toString())
        assertEquals("content://media/external/audio/media/7", item.requestMetadata.mediaUri.toString())
    }

    @Test
    fun `al llegar a la sesión sin URI de reproducción, se recupera desde requestMetadata`() {
        val sent = testSong(id = 7).toMediaItem()
        // Así lo recibe la sesión desde el MediaController: sin localConfiguration.
        val received = MediaItem.Builder()
            .setMediaId(sent.mediaId)
            .setMediaMetadata(sent.mediaMetadata)
            .setRequestMetadata(sent.requestMetadata)
            .build()
        assertNull(received.localConfiguration)

        val resolved = resolveMediaItems(listOf(received)).single()
        assertEquals("content://media/external/audio/media/7", resolved.localConfiguration?.uri.toString())
        assertEquals("7", resolved.mediaId)
    }

    @Test
    fun `el estado de lo que suena se lee de los metadatos`() {
        val nowPlaying = testSong(id = 7, title = "Starlit Reverie", artist = "Budiarti").toMediaItem().toNowPlaying()
        assertEquals(7L, nowPlaying?.songId)
        assertEquals("Starlit Reverie", nowPlaying?.title)
        assertEquals("Budiarti", nowPlaying?.artist)
        assertEquals("Noches", nowPlaying?.album)
    }
}
