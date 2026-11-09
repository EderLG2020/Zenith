package com.zenith.player.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenith.player.ui.theme.ZenithTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

// Gráficos nativos: en modo LEGACY Robolectric mide 1 px por carácter y no detecta textos cortados.
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
        return results.single()
    }

    private fun setBigFontContent(content: @Composable () -> Unit) = composeRule.setContent {
        val base = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = 2f)) {
            ZenithTheme { content() }
        }
    }

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
    fun `chip crece con letra del sistema grande y muestra el texto completo`() {
        val label = "Agregadas recientemente"
        // Las filas de chips van con scroll horizontal (LazyRow o horizontalScroll): el chip tiene todo el ancho que pida.
        setBigFontContent {
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                ZenithChip(label, selected = false, onClick = {}, modifier = Modifier.testTag("chip"))
            }
        }
        val chip = composeRule.onNodeWithTag("chip").getUnclippedBoundsInRoot().height
        val textNode = composeRule.onNodeWithText(label, useUnmergedTree = true)
        val text = textNode.getUnclippedBoundsInRoot().height
        assertTrue("chip ($chip) debe ser >= 40dp", chip >= 40.dp)
        assertTrue("chip ($chip) debe contener el texto ($text) más su relleno vertical", chip >= text + 16.dp)
        // Con ancho infinito hasVisualOverflow da un falso positivo; se comprueba la línea en sí.
        val layout = textNode.textLayout()
        assertFalse("el texto del chip no debe terminar en …", layout.isLineEllipsized(0))
        assertTrue("el texto del chip no debe cortarse", layout.getLineRight(0) <= layout.size.width)
        assertFalse("el texto del chip no debe cortarse en vertical", layout.didOverflowHeight)
    }

    @Test
    fun `chip sin espacio corta el texto con puntos suspensivos`() {
        val label = "Agregadas recientemente"
        setBigFontContent { Box(Modifier.width(120.dp)) { ZenithChip(label, selected = false, onClick = {}) } }
        val layout = composeRule.onNodeWithText(label, useUnmergedTree = true).textLayout()
        assertTrue("debe terminar en …", layout.isLineEllipsized(0))
    }

    @Test
    fun `título largo no hace crecer la fila y termina en puntos suspensivos`() {
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
        val layout = composeRule.onNodeWithText(largo, useUnmergedTree = true).textLayout()
        assertTrue("el título largo debe terminar en …", layout.isLineEllipsized(0))
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
