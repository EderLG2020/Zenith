package com.zenith.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zenith.player.ui.navigation.Destination
import com.zenith.player.ui.navigation.FloatingBottomBar
import com.zenith.player.ui.navigation.ZenithNavHost
import com.zenith.player.ui.navigation.navigateToTab
import com.zenith.player.ui.navigation.showsFloatingBar
import com.zenith.player.ui.theme.ZenithColors

/** Raíz de la interfaz: pantallas + barra flotante encima de la barra de gestos. */
@Composable
fun ZenithRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = Destination.entries.firstOrNull { it.route == backStackEntry?.destination?.route }
        ?: Destination.Home

    Box(
        Modifier
            .fillMaxSize()
            .background(ZenithColors.Background),
    ) {
        ZenithNavHost(navController, Modifier.fillMaxSize())
        if (showsFloatingBar(backStackEntry?.destination?.route)) {
            FloatingBottomBar(
                current = current,
                onSelect = navController::navigateToTab,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
            )
        }
    }
}
