package com.zenith.player.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RepeatModeTest {
    @Test
    fun `repetir cicla entre desactivado, toda la cola y una canción`() {
        assertEquals(RepeatMode.All, RepeatMode.Off.next())
        assertEquals(RepeatMode.One, RepeatMode.All.next())
        assertEquals(RepeatMode.Off, RepeatMode.One.next())
    }
}
