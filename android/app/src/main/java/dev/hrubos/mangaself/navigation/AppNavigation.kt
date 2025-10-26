package dev.hrubos.mangaself.navigation

import android.util.Base64
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.hrubos.db.Chapter
import dev.hrubos.db.Publication
import dev.hrubos.mangaself.model.Configuration
import dev.hrubos.mangaself.view.AboutScreen
import dev.hrubos.mangaself.view.AddProfileScreen
import dev.hrubos.mangaself.view.EntryScreen
import dev.hrubos.mangaself.view.PublicationDetail
import dev.hrubos.mangaself.view.ReaderScreen
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
                },
                onToggleFavourite = { publication ->
                   shelfViewModel.togglePublicationFavourite(publication)
                },
                onContinueReading = { publication, _ ->
                    // find chapter to continue
                    val sortedChapters = publication.chapters.sortedBy { it.position }
                    val chapterToContinue = sortedChapters.firstOrNull { it.pageLastRead < it.pages }
                        ?: sortedChapters.lastOrNull()
                    chapterToContinue?.let { navigateToReader(navController, publication, it) }
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
                onBack = { navController.navigate("shelf/${Configuration.selectedProfileId}") },
                onChapterClick = { pub, chapter ->
                    navigateToReader(navController, pub, chapter)
                }
            )
        }

        composable(
            route = "reader/{publicationPath}/{chapterPath}"
        ) { backStackEntry ->
            val pubEncoded = backStackEntry.arguments?.getString("publicationPath") ?: return@composable
            val chapterEncoded = backStackEntry.arguments?.getString("chapterPath") ?: return@composable

            val pubPath = String(
                Base64.decode(pubEncoded, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING),
                Charsets.UTF_8
            )
            val chapterPath = String(
                Base64.decode(chapterEncoded, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING),
                Charsets.UTF_8
            )

            var publication by remember { mutableStateOf<Publication?>(null) }
            var chapter by remember { mutableStateOf<Chapter?>(null) }

            LaunchedEffect(pubPath) {
                val pub = shelfViewModel.getPublicationBySystemPath(pubPath)
                publication = pub
                if(pub == null) { navController.popBackStack() }
                chapter = pub?.chapters?.firstOrNull { it.systemPath == chapterPath }
            }

            if (publication != null && chapter != null) {
                val pub = publication!!   // local non-null copy
                val chap = chapter!!

                ReaderScreen(
                    profileViewModel = profileViewModel,
                    shelfViewModel = shelfViewModel,
                    publication = publication!!,
                    chapter = chapter!!,
                    onBack = { navigateToPublication(navController, pub) },
                    onPageChanged = { pageNum -> },
                    onChapterChange = { newChapter ->
                        navigateToReader(navController, pub, newChapter)
                    }
                )
            } else {
                // loading UI while DB fetch completes
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading chapter...")
                }
            }
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


fun navigateToReader(navController: NavController, publication: Publication, chapter: Chapter) {
    val pubEncoded = Base64.encodeToString(publication.systemPath.toByteArray(Charsets.UTF_8),
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    val chapterEncoded = Base64.encodeToString(chapter.systemPath.toByteArray(Charsets.UTF_8),
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)

    navController.navigate("reader/$pubEncoded/$chapterEncoded")
}


fun navigateToPublication(navController: NavController, publication: Publication) {
    val pubEncoded = Base64.encodeToString(
        publication.systemPath.toByteArray(Charsets.UTF_8),
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
    )
    navController.navigate("publicationDetail/$pubEncoded") {
        popUpTo("shelf/${Configuration.selectedProfileId}") { inclusive = false }
        launchSingleTop = true
    }
}