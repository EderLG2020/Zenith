package com.zenith.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zenith.player.ui.history.HistoryScreen
import com.zenith.player.ui.home.HomeScreen
import com.zenith.player.ui.library.LibraryRoute
import com.zenith.player.ui.playlists.PlaylistsScreen
import com.zenith.player.ui.settings.SettingsScreen

@Composable
fun ZenithNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Destination.Home.route, modifier = modifier) {
        composable(Destination.Home.route) { HomeScreen() }
        composable(Destination.Library.route) { LibraryRoute() }
        composable(Destination.History.route) { HistoryScreen() }
        composable(Destination.Playlists.route) { PlaylistsScreen() }
        composable(Destination.Settings.route) { SettingsScreen() }
    }
}

/**
 * Cambia de pestaña. Solo Inicio queda debajo en la pila: atrás desde cualquier
 * pestaña vuelve a Inicio, y atrás desde Inicio cierra la app.
 */
fun NavController.navigateToTab(destination: Destination) {
    navigate(destination.route) {
        popUpTo(Destination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
