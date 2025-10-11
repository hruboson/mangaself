package dev.hrubos.mangaself.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.hrubos.mangaself.view.AboutScreen
import dev.hrubos.mangaself.view.AddProfileScreen
import dev.hrubos.mangaself.view.EntryScreen
import dev.hrubos.mangaself.view.SettingsScreen
import dev.hrubos.mangaself.view.ShelfWrapper
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

            LaunchedEffect(Unit) {
                val restored = profileViewModel.restoreLastSelectedProfile()
                restored?.let { profile ->
                    // If a profile was restored, skip to shelf
                    navController.navigate("shelf/${profile.id}") {
                        popUpTo("entry") { inclusive = true }
                    }
                }
            }
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

            ShelfWrapper(
                shelfViewModel = shelfViewModel,
                onSettings = { navController.navigate("settings") },
                onFolderSelected = { uri -> Log.v("FOLDER SELECTED:", uri.path ?: "None selected") }
            )
        }


        composable("settings") {
            SettingsScreen(
                profileViewModel = profileViewModel,
                onBack = { navController.popBackStack() },
                onAbout = { navController.navigate("about") },
                onLogout = {
                    navController.navigate("entry") {
                        popUpTo("entry") { inclusive = true } // clear backstack
                    }
                }
            )
        }

        composable("about") {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }

    }
}