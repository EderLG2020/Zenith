package com.zenith.player.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zenith.player.ui.theme.ZenithColors

/** Pantalla provisional (fase 1): solo muestra su título centrado. */
@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = ZenithColors.TextPrimary)
    }
}
