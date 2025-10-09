package dev.hrubos.mangaself.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.hrubos.mangaself.view.AboutScreen
import dev.hrubos.mangaself.view.AddProfileScreen
import dev.hrubos.mangaself.view.EntryScreen
import dev.hrubos.mangaself.view.SettingsScreen
import dev.hrubos.mangaself.view.ShelfScreen
import dev.hrubos.mangaself.viewmodel.ProfileViewModel
import dev.hrubos.mangaself.viewmodel.ShelfViewModel

@Composable
fun AppNavigation(
    profileViewModel: ProfileViewModel,
    shelfViewModel: ShelfViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "entry") {
        composable("entry") {
            EntryScreen(
                viewModel = profileViewModel,
                onNavigateToAddProfile = { navController.navigate("addProfile") },
                onNavigateToShelf = { profileId ->
                    navController.navigate("shelf/$profileId")
                }
            )
        }

        composable("addProfile") {
            AddProfileScreen(
                viewModel = profileViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        /*composable("profileDetails/{profileId}") { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
            ProfileDetailsScreen(profileId = profileId, viewModel = viewModel)
        }*/

        composable("shelf/{profileId}") { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            LaunchedEffect(profileId) {
                profileViewModel.selectProfile(profileId)
            }

            ShelfScreen(
                shelfViewModel = shelfViewModel,
                onSettings = { navController.navigate("settings") }
            )
        }


        composable("settings") {
            SettingsScreen(
                viewModel = profileViewModel,
                onBack = { navController.popBackStack() },
                onAbout = { navController.navigate("about") }
            )
        }

        composable("about") {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }

    }
}