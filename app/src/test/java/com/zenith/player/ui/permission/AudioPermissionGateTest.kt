package com.zenith.player.ui.permission

import android.Manifest
import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zenith.player.ui.theme.ZenithTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class AudioPermissionGateTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private fun launchGate() = composeRule.setContent {
        ZenithTheme { AudioPermissionGate(screenTitle = "Mi Música") { Text("Contenido protegido") } }
    }

    @Test
    fun `en Android 13 o superior el permiso es READ_MEDIA_AUDIO`() {
        assertEquals(Manifest.permission.READ_MEDIA_AUDIO, audioPermission())
    }

    @Test
    @Config(sdk = [32])
    fun `en Android 12 o anterior el permiso es READ_EXTERNAL_STORAGE`() {
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE, audioPermission())
    }

    @Test
    fun `sin permiso explica por qué lo necesita y ofrece pedirlo`() {
        launchGate()
        composeRule.onNodeWithText("Mi Música").assertIsDisplayed()
        composeRule.onNodeWithText("Zenith necesita acceso a tu música").assertIsDisplayed()
        composeRule.onNodeWithText("Permitir acceso").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Contenido protegido").assertDoesNotExist()
    }

    @Test
    fun `con permiso muestra el contenido`() {
        shadowOf(app).grantPermissions(audioPermission())
        launchGate()
        composeRule.onNodeWithText("Contenido protegido").assertIsDisplayed()
    }

    @Test
    fun `si el permiso se concede en Ajustes, al volver a la app aparece el contenido`() {
        launchGate()
        composeRule.onNodeWithText("Contenido protegido").assertDoesNotExist()

        shadowOf(app).grantPermissions(audioPermission())
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeRule.onNodeWithText("Contenido protegido").assertIsDisplayed()
    }

    @Test
    fun `si el sistema ya no muestra el diálogo, ofrece abrir Ajustes`() {
        composeRule.setContent {
            ZenithTheme {
                PermissionRequiredScreen(
                    screenTitle = "Mi Música",
                    permanentlyDenied = true,
                    onAllow = {},
                    onOpenSettings = {},
                )
            }
        }
        composeRule.onNodeWithText("Abrir Ajustes").assertIsDisplayed()
        composeRule.onNodeWithText("Permitir acceso").assertDoesNotExist()
    }
}
