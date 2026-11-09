package com.zenith.player.playback

import android.content.Intent
import androidx.media3.common.C
import androidx.media3.session.MediaSession
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class PlaybackServiceTest {

    private fun controllerInfo() = MediaSession.ControllerInfo.createTestOnlyControllerInfo(
        "com.zenith.player", 0, 0, 0, 0, false, android.os.Bundle.EMPTY,
    )

    @Test
    fun `al crearse ofrece una sesión con un reproductor de música`() {
        val controller = Robolectric.buildService(PlaybackService::class.java).create()
        val session = controller.get().onGetSession(controllerInfo())
        assertNotNull(session)
        val attributes = session!!.player.audioAttributes
        assertEquals(C.USAGE_MEDIA, attributes.usage)
        assertEquals(C.AUDIO_CONTENT_TYPE_MUSIC, attributes.contentType)
        controller.destroy()
    }

    @Test
    fun `al destruirse libera la sesión`() {
        val controller = Robolectric.buildService(PlaybackService::class.java).create()
        controller.destroy()
        assertNull(controller.get().onGetSession(controllerInfo()))
    }

    @Test
    fun `cerrar la app desde recientes estando en pausa detiene el servicio`() {
        val controller = Robolectric.buildService(PlaybackService::class.java).create()
        controller.get().onTaskRemoved(Intent())
        assertEquals(true, org.robolectric.Shadows.shadowOf(controller.get()).isStoppedBySelf)
        controller.destroy()
    }
}
