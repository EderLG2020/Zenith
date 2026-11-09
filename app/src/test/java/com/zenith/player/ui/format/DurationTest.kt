package com.zenith.player.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationTest {
    @Test
    fun `formatea minutos y segundos`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:09", formatDuration(9_999))
        assertEquals("3:20", formatDuration(200_000))
    }

    @Test
    fun `añade horas cuando la canción dura una hora o más`() {
        assertEquals("1:02:03", formatDuration(3_723_000))
    }

    @Test
    fun `duración negativa se muestra como cero`() {
        assertEquals("0:00", formatDuration(-5))
    }
}
