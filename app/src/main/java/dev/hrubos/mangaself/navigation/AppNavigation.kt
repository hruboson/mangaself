package dev.hrubos.mangaself.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.hrubos.mangaself.model.Configuration
import dev.hrubos.mangaself.view.AboutScreen
import dev.hrubos.mangaself.view.AddProfileScreen
import dev.hrubos.mangaself.view.EntryScreen
import dev.hrubos.mangaself.view.PublicationDetail
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

        composable("shelf/{profileId}") { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId") ?: return@composable
            LaunchedEffect(profileId) {
                profileViewModel.selectProfile(profileId)
            }

            ShelfWrapper(
                shelfViewModel = shelfViewModel,
                profileViewModel = profileViewModel,
                onSettings = { navController.navigate("settings") },
                onFolderSelected = { uri ->
                    val publicationPathEncoded = Uri.encode(uri.toString())
                    shelfViewModel.addPublication(Configuration.selectedProfileId ?: "", uri)
                    navController.navigate("publicationDetail/$publicationPathEncoded")
                },
                onPublicationClick = { publication ->
                    val encodedUri = Uri.encode(publication.systemPath)
                    navController.navigate("publicationDetail/$encodedUri")
                }
            )
        }

        composable("publicationDetail/{publicationPath}") { backStackEntry ->
            val publicationPathEncoded = backStackEntry.arguments?.getString("publicationPath") ?: return@composable
            val publicationPath = Uri.decode(publicationPathEncoded)

            PublicationDetail(
                shelfViewModel,
                publicationPath,
                onBack = { navController.popBackStack() },
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