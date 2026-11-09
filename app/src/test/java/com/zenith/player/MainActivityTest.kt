package com.zenith.player

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "notnight")
class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `iconos de las barras del sistema son claros aunque el teléfono esté en modo claro`() {
        composeRule.activityRule.scenario.onActivity { activity ->
            val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            assertFalse("barra de estado con iconos oscuros", controller.isAppearanceLightStatusBars)
            assertFalse("barra de navegación con iconos oscuros", controller.isAppearanceLightNavigationBars)
        }
    }

    @Test
    fun `la pestaña activa sobrevive a recrear la actividad real`() {
        composeRule.onNodeWithContentDescription("Historial").performClick()
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithContentDescription("Historial").assertIsSelected()
        composeRule.onNodeWithText("Historial").assertIsDisplayed()
    }

    @Test
    fun `atrás desde Inicio cierra la app`() {
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.activityRule.scenario.onActivity { assertTrue(it.isFinishing) }
    }
}
