package dev.hrubos.mangaself.navigation

import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                    shelfViewModel.handlePickedFolder(uri)

                    // auto navigate to publication detail
                    val publicationPathEncoded = Base64.encodeToString(uri.toString().toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
                    navController.navigate("publicationDetail/$publicationPathEncoded")
                },
                onPublicationClick = { publication ->
                    val encodedUri = Base64.encodeToString(publication.systemPath.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
                    navController.navigate("publicationDetail/$encodedUri")
                }
            )
        }

        composable("publicationDetail/{publicationPath}") { backStackEntry ->
            val publicationPathEncoded = backStackEntry.arguments?.getString("publicationPath") ?: return@composable
            val publicationPath = String(
            Base64.decode(publicationPathEncoded, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING),
                    Charsets.UTF_8
            )

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