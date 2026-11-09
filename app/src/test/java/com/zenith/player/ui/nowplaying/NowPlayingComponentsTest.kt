package com.zenith.player.ui.nowplaying

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenith.player.domain.model.RepeatMode
import com.zenith.player.testing.nowPlaying
import com.zenith.player.ui.theme.ZenithTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NowPlayingComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ---------- Barra de progreso (tarea 5) ----------

    @Test
    fun `mover la barra de progreso busca la posición elegida`() {
        var sought = -1L
        composeRule.setContent {
            ZenithTheme { SeekBar(positionMs = 0, durationMs = 200_000, elapsed = "0:00", remaining = "-3:20", onSeek = { sought = it }) }
        }
        composeRule.onNodeWithContentDescription("Progreso de la canción")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0.5f) }
        assertEquals(100_000L, sought)
        composeRule.onNodeWithText("-3:20").assertIsDisplayed()
    }

    @Test
    fun `sin duración conocida la barra no se cae`() {
        var sought = -1L
        composeRule.setContent {
            ZenithTheme { SeekBar(positionMs = 5_000, durationMs = 0, elapsed = "0:05", remaining = "-0:00", onSeek = { sought = it }) }
        }
        composeRule.onNodeWithContentDescription("Progreso de la canción")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0.5f) }
        assertEquals(0L, sought)
    }

    // ---------- Controles (tarea 6) ----------

    private val calls = mutableListOf<String>()

    private fun showControls(isPlaying: Boolean = true, shuffle: Boolean = false, repeatMode: RepeatMode = RepeatMode.Off) =
        composeRule.setContent {
            ZenithTheme {
                PlaybackControls(
                    isPlaying = isPlaying,
                    shuffle = shuffle,
                    repeatMode = repeatMode,
                    onPlayPause = { calls += "playPause" },
                    onPrevious = { calls += "previous" },
                    onNext = { calls += "next" },
                    onShuffleClick = { calls += "shuffle" },
                    onShuffleLongClick = { calls += "repeat" },
                    onQueueClick = { calls += "queue" },
                )
            }
        }

    @Test
    fun `cada control llama a su acción`() {
        showControls()
        composeRule.onNodeWithContentDescription("Pausar").performClick()
        composeRule.onNodeWithContentDescription("Anterior").performClick()
        composeRule.onNodeWithContentDescription("Siguiente").performClick()
        composeRule.onNodeWithContentDescription("Aleatorio").performClick()
        composeRule.onNodeWithContentDescription("Cola").performClick()
        assertEquals(listOf("playPause", "previous", "next", "shuffle", "queue"), calls)
    }

    @Test
    fun `en pausa el botón principal dice Reproducir`() {
        showControls(isPlaying = false)
        composeRule.onNodeWithContentDescription("Reproducir").assertIsDisplayed()
    }

    @Test
    fun `pulsación larga en aleatorio cambia el modo de repetir`() {
        showControls()
        composeRule.onNodeWithContentDescription("Aleatorio").performTouchInput { longClick() }
        assertEquals(listOf("repeat"), calls)
    }

    @Test
    fun `repetir una canción se indica con un 1`() {
        showControls(repeatMode = RepeatMode.One)
        composeRule.onNodeWithText("1", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Aleatorio")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Aleatorio desactivado, repetir una canción"))
    }

    @Test
    fun `aleatorio activo y repetir toda la cola se describen para TalkBack`() {
        showControls(shuffle = true, repeatMode = RepeatMode.All)
        composeRule.onNodeWithContentDescription("Aleatorio")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Aleatorio activado, repetir toda la cola"))
    }

    // ---------- Cola (tarea 7) ----------

    @Test
    fun `la cola permite saltar, subir, bajar y quitar con el índice correcto`() {
        composeRule.setContent {
            ZenithTheme {
                QueueList(
                    queue = listOf(nowPlaying(1, "Uno"), nowPlaying(2, "Dos"), nowPlaying(3, "Tres")),
                    currentIndex = 0,
                    onItemClick = { calls += "click($it)" },
                    onMove = { from, to -> calls += "move($from,$to)" },
                    onRemove = { calls += "remove($it)" },
                )
            }
        }
        composeRule.onNodeWithText("Tres").performClick()
        composeRule.onNodeWithContentDescription("Subir Dos").performClick()
        composeRule.onNodeWithContentDescription("Bajar Dos").performClick()
        composeRule.onNodeWithContentDescription("Quitar Uno").performClick()
        assertEquals(listOf("click(2)", "move(1,0)", "move(1,2)", "remove(0)"), calls)

        composeRule.onNodeWithContentDescription("Subir Uno").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Bajar Tres").assertDoesNotExist()
    }
}
