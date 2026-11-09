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
import com.zenith.player.ui.nowplaying.NowPlayingRoute
import com.zenith.player.ui.playlists.PlaylistsScreen
import com.zenith.player.ui.settings.SettingsScreen

/** Reproduciendo no es una pestaña: ocupa toda la pantalla y se abre encima de la actual. */
const val NOW_PLAYING_ROUTE = "now_playing"

/** La barra flotante se ve en las pestañas y se oculta en Reproduciendo. */
fun showsFloatingBar(route: String?): Boolean = route != NOW_PLAYING_ROUTE

@Composable
fun ZenithNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Destination.Home.route, modifier = modifier) {
        composable(Destination.Home.route) { HomeScreen() }
        composable(Destination.Library.route) {
            LibraryRoute(onOpenNowPlaying = { navController.navigate(NOW_PLAYING_ROUTE) { launchSingleTop = true } })
        }
        composable(Destination.History.route) { HistoryScreen() }
        composable(Destination.Playlists.route) { PlaylistsScreen() }
        composable(Destination.Settings.route) { SettingsScreen() }
        composable(NOW_PLAYING_ROUTE) { NowPlayingRoute(onBack = { navController.popBackStack() }) }
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
