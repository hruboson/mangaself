package dev.hrubos.mangaself.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import dev.hrubos.mangaself.viewmodel.ShelfViewModel

/**
 * Shows library of user content for given profile in grid.
 * Batch actions:
 *  - mark favourite
 *  - remove from library
 *  ?(- delete from device memory)?
 */
@Composable
fun ShelfScreen(viewModel: ShelfViewModel, onSettings: () -> Unit) {
    Surface (modifier = Modifier.fillMaxSize()) {
        FloatingTopMenu(onSettings = onSettings)
    }
}