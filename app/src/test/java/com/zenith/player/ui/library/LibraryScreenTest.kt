package com.zenith.player.ui.library

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenith.player.ui.theme.ZenithTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(state: LibraryUiState) = composeRule.setContent { ZenithTheme { LibraryScreen(state) } }

    @Test
    fun `muestra cada canción con su subtítulo y el total`() {
        show(
            LibraryUiState.Songs(
                listOf(
                    SongItem(1, "Starlit Reverie", "Budiarti • 3:20", ""),
                    SongItem(2, "Lost in the Echo", "Alexiao • 4:05", ""),
                ),
            ),
        )
        composeRule.onNodeWithText("Mi Música").assertIsDisplayed()
        composeRule.onNodeWithText("2 canciones").assertIsDisplayed()
        composeRule.onNodeWithText("Starlit Reverie").assertIsDisplayed()
        composeRule.onNodeWithText("Alexiao • 4:05").assertIsDisplayed()
    }

    @Test
    fun `una sola canción se cuenta en singular`() {
        show(LibraryUiState.Songs(listOf(SongItem(1, "Starlit Reverie", "Budiarti • 3:20", ""))))
        composeRule.onNodeWithText("1 canción").assertIsDisplayed()
    }

    @Test
    fun `una biblioteca grande se puede recorrer hasta el final`() {
        val songs = (1L..2500L).map { SongItem(it, "Canción $it", "Artista • 3:00", "") }
        show(LibraryUiState.Songs(songs))
        composeRule.onNodeWithTag(LIBRARY_LIST_TAG).performScrollToNode(hasText("Canción 2500"))
        composeRule.onNodeWithText("Canción 2500").assertIsDisplayed()
    }

    @Test
    fun `sin canciones explica qué pasa`() {
        show(LibraryUiState.Empty)
        composeRule.onNodeWithText("No encontramos música en tu teléfono").assertIsDisplayed()
    }

    @Test
    fun `mientras carga muestra un indicador`() {
        show(LibraryUiState.Loading)
        composeRule.onNodeWithTag(LIBRARY_LOADING_TAG).assertIsDisplayed()
    }
}
