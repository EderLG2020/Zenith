package com.zenith.player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Pestañas de la barra flotante, en orden (doc/01-diseno.md §5.4). */
enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Inicio", Icons.Rounded.Home),
    Library("library", "Mi Música", Icons.Rounded.LibraryMusic),
    History("history", "Historial", Icons.Rounded.History),
    Playlists("playlists", "Listas", Icons.Rounded.BookmarkBorder),
    Settings("settings", "Ajustes", Icons.Rounded.Settings),
}
