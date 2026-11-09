package com.zenith.player.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenith.player.ui.permission.AudioPermissionGate

/**
 * Mi Música conectada a su ViewModel. El ViewModel se crea dentro del permiso concedido:
 * así nunca consulta MediaStore sin permiso (lo que lanzaría SecurityException).
 */
@Composable
fun LibraryRoute() {
    AudioPermissionGate(screenTitle = "Mi Música") {
        val viewModel: LibraryViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        LibraryScreen(state)
    }
}
