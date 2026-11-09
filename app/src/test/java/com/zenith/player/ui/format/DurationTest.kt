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

class RemainingTest {
    @Test
    fun `el tiempo restante va con signo menos`() {
        assertEquals("-2:15", formatRemaining(positionMs = 28_000, durationMs = 163_000))
    }

    @Test
    fun `sin duración conocida o pasada de largo muestra cero`() {
        assertEquals("-0:00", formatRemaining(positionMs = 5_000, durationMs = 0))
        assertEquals("-0:00", formatRemaining(positionMs = 9_000, durationMs = 8_000))
    }
}
