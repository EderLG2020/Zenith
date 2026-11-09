package com.zenith.player.ui.permission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.zenith.player.ui.theme.ZenithColors
import com.zenith.player.ui.theme.ZenithShapes
import com.zenith.player.ui.theme.ZenithTheme

/**
 * Muestra [content] solo si la app puede leer la música. Si no, explica por qué hace falta el permiso.
 * Vuelve a comprobarlo cada vez que la app pasa a primer plano, por si se concedió desde Ajustes.
 */
@Composable
fun AudioPermissionGate(screenTitle: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasAudioPermission()) }
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        granted = result
        // Tras negarlo, si el sistema ya no quiere mostrar la explicación es que no volverá a mostrar el diálogo.
        permanentlyDenied = !result &&
            context.findActivity()?.shouldShowRequestPermissionRationale(audioPermission()) == false
    }

    LifecycleResumeEffect(Unit) {
        granted = context.hasAudioPermission()
        onPauseOrDispose {}
    }

    if (granted) {
        content()
    } else {
        PermissionRequiredScreen(
            screenTitle = screenTitle,
            permanentlyDenied = permanentlyDenied,
            onAllow = { launcher.launch(audioPermission()) },
            onOpenSettings = { context.openAppSettings() },
        )
    }
}

@Composable
fun PermissionRequiredScreen(
    screenTitle: String,
    permanentlyDenied: Boolean,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            screenTitle,
            style = MaterialTheme.typography.titleLarge,
            color = ZenithColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp).padding(bottom = 96.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(72.dp).background(ZenithColors.Surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.LibraryMusic, contentDescription = null, tint = ZenithColors.Accent)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Zenith necesita acceso a tu música",
                style = MaterialTheme.typography.titleMedium,
                color = ZenithColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (permanentlyDenied) {
                    "El permiso está desactivado. Actívalo en Ajustes → Permisos → Música y audio."
                } else {
                    "Solo lee las canciones guardadas en tu teléfono. No usa internet ni comparte nada."
                },
                style = MaterialTheme.typography.bodySmall,
                color = ZenithColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = if (permanentlyDenied) onOpenSettings else onAllow,
                shape = ZenithShapes.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZenithColors.Accent,
                    contentColor = ZenithColors.OnAccent,
                ),
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(if (permanentlyDenied) "Abrir Ajustes" else "Permitir acceso")
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

@Preview(backgroundColor = 0xFF0E0E12, showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun PermissionRequiredScreenPreview() {
    ZenithTheme {
        PermissionRequiredScreen("Mi Música", permanentlyDenied = false, onAllow = {}, onOpenSettings = {})
    }
}
