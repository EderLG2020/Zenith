package com.zenith.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "notnight")
class ZenithThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `colores coinciden con el diseño`() {
        assertEquals(Color(0xFF0E0E12), ZenithColors.Background)
        assertEquals(Color(0xFF1C1C22), ZenithColors.Surface)
        assertEquals(Color(0xFFC6E14B), ZenithColors.Accent)
        assertEquals(Color(0xFFC9A3F0), ZenithColors.Featured)
        assertEquals(Color(0xFFFFFFFF), ZenithColors.TextPrimary)
        assertEquals(Color(0xFF8A8A93), ZenithColors.TextSecondary)
    }

    @Test
    fun `el tema es oscuro aunque el sistema esté en modo claro`() {
        var background = Color.Unspecified
        var primary = Color.Unspecified
        composeRule.setContent {
            ZenithTheme {
                background = MaterialTheme.colorScheme.background
                primary = MaterialTheme.colorScheme.primary
            }
        }
        composeRule.waitForIdle()
        assertEquals(ZenithColors.Background, background)
        assertEquals(ZenithColors.Accent, primary)
    }
}
