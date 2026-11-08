package com.zenith.player.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenith.player.ui.theme.ZenithColors
import com.zenith.player.ui.theme.ZenithShapes
import com.zenith.player.ui.theme.ZenithTheme

/** Barra de navegación flotante tipo píldora; la pestaña activa va en un círculo lima. */
@Composable
fun FloatingBottomBar(
    current: Destination,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(68.dp)
            .clip(ZenithShapes.Pill)
            .background(ZenithColors.SurfaceHigh.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Destination.entries.forEach { destination ->
            val selected = destination == current
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (selected) ZenithColors.Accent else Color.Transparent)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(destination) }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    tint = if (selected) ZenithColors.OnAccent else ZenithColors.TextPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Preview(backgroundColor = 0xFF0E0E12, showBackground = true, widthDp = 380)
@Composable
private fun FloatingBottomBarPreview() {
    ZenithTheme { FloatingBottomBar(current = Destination.Home, onSelect = {}) }
}
