package com.zenith.player.ui.nowplaying

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenith.player.testing.nowPlaying
import com.zenith.player.ui.theme.ZenithTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NowPlayingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val playing = NowPlayingUiState(
        song = nowPlaying(2, "Starlit Reverie"),
        positionMs = 28_000,
        durationMs = 163_000,
        elapsed = "0:28",
        remaining = "-2:15",
        isPlaying = true,
        queue = listOf(nowPlaying(1, "Lost in the Echo"), nowPlaying(2, "Starlit Reverie")),
        currentIndex = 1,
    )

    @Test
    fun `muestra la canción, el artista y los tiempos`() {
        composeRule.setContent { ZenithTheme { NowPlayingScreen(playing, NowPlayingActions()) } }
        composeRule.onNodeWithText("Reproduciendo").assertIsDisplayed()
        composeRule.onNodeWithText("Starlit Reverie").assertIsDisplayed()
        composeRule.onNodeWithText("Budiarti").assertIsDisplayed()
        composeRule.onNodeWithText("0:28").assertIsDisplayed()
        composeRule.onNodeWithText("-2:15").assertIsDisplayed()
    }

    @Test
    fun `atrás llama a volver`() {
        var back = 0
        composeRule.setContent { ZenithTheme { NowPlayingScreen(playing, NowPlayingActions(onBack = { back++ })) } }
        composeRule.onNodeWithContentDescription("Atrás").performClick()
        assertEquals(1, back)
    }

    @Test
    fun `el botón de cola abre la hoja con la cola`() {
        composeRule.setContent { ZenithTheme { NowPlayingScreen(playing, NowPlayingActions()) } }
        composeRule.onNodeWithContentDescription("Cola").performClick()
        composeRule.onNodeWithText("A continuación").assertIsDisplayed()
        composeRule.onNodeWithText("Lost in the Echo").assertIsDisplayed()
    }

    @Test
    fun `sin nada sonando lo dice en lugar de mostrar una pantalla rota`() {
        composeRule.setContent { ZenithTheme { NowPlayingScreen(NowPlayingUiState(), NowPlayingActions()) } }
        composeRule.onNodeWithText("Nada sonando").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Atrás").assertIsDisplayed()
    }
}
