package com.zenith.player

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class ManifestTest {

    private fun requestedPermissions(): List<String> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions.orEmpty().toList()
    }

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
}
