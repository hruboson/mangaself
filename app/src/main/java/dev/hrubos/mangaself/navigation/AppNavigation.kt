package dev.hrubos.mangaself.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.hrubos.mangaself.view.AddProfileScreen
import dev.hrubos.mangaself.view.EntryScreen
import dev.hrubos.mangaself.viewmodel.ProfileViewModel

@Composable
fun AppNavigation(viewModel: ProfileViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "entry") {
        composable("entry") {
            EntryScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate("addProfile") }
            )
        }

        composable("addProfile") {
            AddProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        /*composable("profileDetails/{profileId}") { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: ""
            ProfileDetailsScreen(profileId = profileId, viewModel = viewModel)
        }*/

        /*
        composable("settings") { SettingsScreen(viewModel) }
        composable("about") { AboutScreen() }
        */
    }
}