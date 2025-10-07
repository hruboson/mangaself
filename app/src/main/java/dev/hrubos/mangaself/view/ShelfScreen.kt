package dev.hrubos.mangaself.view

import androidx.compose.runtime.Composable
import dev.hrubos.mangaself.viewmodel.ProfileViewModel

/**
 * Shows library of user content for given profile in grid.
 * Batch actions:
 *  - mark favourite
 *  - remove from library
 *  ?(- delete from device memory)?
 */
@Composable
fun ShelfScreen(viewModel: ProfileViewModel, onNavigateToAdd: () -> Unit) {

}