package com.zenith.player.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenith.player.ui.ZenithRoot
import com.zenith.player.ui.theme.ZenithTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun launch() = composeRule.setContent { ZenithTheme { ZenithRoot() } }

    @Test
    fun `abre en Inicio con su pestaña seleccionada`() {
        launch()
        composeRule.onNodeWithText("Inicio").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Inicio").assertIsSelected()
        composeRule.onNodeWithContentDescription("Ajustes").assertIsNotSelected()
    }

    @Test
    fun `cada pestaña abre su pantalla`() {
        launch()
        listOf("Mi Música", "Historial", "Listas", "Ajustes").forEach { label ->
            composeRule.onNodeWithContentDescription(label).performClick()
            composeRule.onNodeWithText(label).assertIsDisplayed()
            composeRule.onNodeWithContentDescription(label).assertIsSelected()
            composeRule.onNodeWithContentDescription("Inicio").assertIsNotSelected()
        }
    }

    @Test
    fun `atrás desde Ajustes vuelve a Inicio`() {
        launch()
        composeRule.onNodeWithContentDescription("Ajustes").performClick()
        composeRule.waitForIdle() // el manejador de atrás se activa al recomponer
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithText("Inicio").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Inicio").assertIsSelected()
    }

    @Test
    fun `atrás tras visitar varias pestañas vuelve a Inicio, no a la anterior`() {
        launch()
        composeRule.onNodeWithContentDescription("Historial").performClick()
        composeRule.onNodeWithContentDescription("Listas").performClick()
        composeRule.waitForIdle() // el manejador de atrás se activa al recomponer
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithContentDescription("Inicio").assertIsSelected()
    }

    @Test
    fun `la pestaña activa sobrevive a recrear la pantalla`() {
        val restorer = StateRestorationTester(composeRule)
        restorer.setContent { ZenithTheme { ZenithRoot() } }
        composeRule.onNodeWithContentDescription("Historial").performClick()
        restorer.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithContentDescription("Historial").assertIsSelected()
        composeRule.onNodeWithText("Historial").assertIsDisplayed()
    }

    @Test
    fun `pulsar Inicio desde otra pestaña vuelve a Inicio`() {
        launch()
        composeRule.onNodeWithContentDescription("Listas").performClick()
        composeRule.onNodeWithContentDescription("Inicio").performClick()
        composeRule.onNodeWithContentDescription("Inicio").assertIsSelected()
        composeRule.onNodeWithContentDescription("Listas").assertIsNotSelected()
        composeRule.onNodeWithText("Inicio").assertIsDisplayed()
    }
}
