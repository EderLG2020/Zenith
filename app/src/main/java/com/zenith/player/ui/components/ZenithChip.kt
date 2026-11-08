package com.zenith.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenith.player.ui.theme.ZenithColors
import com.zenith.player.ui.theme.ZenithShapes
import com.zenith.player.ui.theme.ZenithTheme

/** Chip tipo píldora: lima cuando está seleccionado, gris oscuro si no. Crece con el tamaño de letra. */
@Composable
fun ZenithChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(ZenithShapes.Pill)
            .background(if (selected) ZenithColors.Accent else ZenithColors.Surface)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) ZenithColors.OnAccent else ZenithColors.TextSecondary,
            maxLines = 1,
        )
    }
}

@Preview(backgroundColor = 0xFF0E0E12, showBackground = true)
@Composable
private fun ZenithChipPreview() {
    ZenithTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ZenithChip("Todo", selected = true, onClick = {})
            ZenithChip("Agregadas recientemente", selected = false, onClick = {})
        }
    }
}
