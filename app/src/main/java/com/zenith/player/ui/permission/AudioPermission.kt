package com.zenith.player.ui.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Permiso para leer la música: Android 13 lo separó por tipo de archivo (`READ_MEDIA_AUDIO`). */
fun audioPermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

fun Context.hasAudioPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, audioPermission()) == PackageManager.PERMISSION_GRANTED
