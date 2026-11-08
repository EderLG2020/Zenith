package com.zenith.player.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenith.player.ui.theme.ZenithTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `placeholder es determinista y varía según la semilla`() {
        assertEquals(placeholderColorsFor("Starlit Reverie"), placeholderColorsFor("Starlit Reverie"))
        assertNotEquals(placeholderColorsFor("Starlit Reverie"), placeholderColorsFor("Lost in the Echo"))
    }

    @Test
    fun `chip seleccionado expone el estado y responde al clic`() {
        var clicks = 0
        composeRule.setContent {
            ZenithTheme { ZenithChip("Todo", selected = true, onClick = { clicks++ }) }
        }
        composeRule.onNodeWithText("Todo").assertIsSelected().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `chip crece con letra del sistema grande`() {
        val label = "Agregadas recientemente"
        composeRule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = 2f)) {
                ZenithTheme {
                    ZenithChip(label, selected = false, onClick = {}, modifier = Modifier.testTag("chip"))
                }
            }
        }
        val chip = composeRule.onNodeWithTag("chip").getUnclippedBoundsInRoot().height
        val text = composeRule.onNodeWithText(label, useUnmergedTree = true).getUnclippedBoundsInRoot().height
        assertTrue("chip ($chip) debe ser >= 40dp", chip >= 40.dp)
        assertTrue("chip ($chip) debe contener el texto ($text) más su relleno vertical", chip >= text + 16.dp)
    }

    @Test
    fun `título largo no hace crecer la fila`() {
        val largo = "Una canción con un título larguísimo ".repeat(6).trim()
        composeRule.setContent {
            ZenithTheme {
                Column {
                    SongRow("Corto", "Budiarti • 3:20", onClick = {}, onPlayClick = {}, modifier = Modifier.testTag("corta"))
                    SongRow(largo, "Budiarti • 3:20", onClick = {}, onPlayClick = {}, modifier = Modifier.testTag("larga"))
                }
            }
        }
        val corta = composeRule.onNodeWithTag("corta").getUnclippedBoundsInRoot().height
        val larga = composeRule.onNodeWithTag("larga").getUnclippedBoundsInRoot().height
        assertEquals(corta, larga)
        composeRule.onNodeWithContentDescription("Reproducir $largo").assertIsDisplayed()
    }

    @Test
    fun `botón de play de la fila responde al clic`() {
        var plays = 0
        composeRule.setContent {
            ZenithTheme { SongRow("Starlit Reverie", "Budiarti • 3:20", onClick = {}, onPlayClick = { plays++ }) }
        }
        composeRule.onNodeWithContentDescription("Reproducir Starlit Reverie").performClick()
        assertEquals(1, plays)
    }
}
