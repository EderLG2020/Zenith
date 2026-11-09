package com.zenith.player

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class ManifestTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun requestedPermissions(): List<String> =
        context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions.orEmpty().toList()

    @Test
    fun `en Android 13 o superior pide READ_MEDIA_AUDIO y no el permiso antiguo`() {
        val requested = requestedPermissions()
        assertTrue(Manifest.permission.READ_MEDIA_AUDIO in requested)
        assertFalse(Manifest.permission.READ_EXTERNAL_STORAGE in requested)
    }

    @Test
    @Config(sdk = [32])
    fun `en Android 12 o anterior pide READ_EXTERNAL_STORAGE`() {
        assertTrue(Manifest.permission.READ_EXTERNAL_STORAGE in requestedPermissions())
    }

    @Test
    fun `no pide permiso de internet`() {
        assertFalse(Manifest.permission.INTERNET in requestedPermissions())
    }

    @Test
    fun `pide lo necesario para sonar con la pantalla apagada`() {
        val requested = requestedPermissions()
        assertTrue(Manifest.permission.FOREGROUND_SERVICE in requested)
        assertTrue(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK in requested)
        assertTrue(Manifest.permission.WAKE_LOCK in requested)
    }

    @Test
    fun `el servicio de reproducción está declarado como sesión multimedia`() {
        val component = ComponentName(context, "com.zenith.player.playback.PlaybackService")
        val info = context.packageManager.getServiceInfo(component, 0)
        assertEquals(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK, info.foregroundServiceType)

        val intent = Intent("androidx.media3.session.MediaSessionService").setPackage(context.packageName)
        val found = context.packageManager.queryIntentServices(intent, 0).map { it.serviceInfo.name }
        assertTrue(found.toString(), "com.zenith.player.playback.PlaybackService" in found)
    }
}
