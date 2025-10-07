package dev.hrubos.mangaself

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dev.hrubos.mangaself.navigation.AppNavigation
import dev.hrubos.mangaself.ui.theme.MangaselfTheme
import dev.hrubos.mangaself.viewmodel.ProfileViewModel
import dev.hrubos.mangaself.viewmodel.ShelfViewModel

class MainActivity : ComponentActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private val shelfViewModel: ShelfViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO remove after testing
        profileViewModel.clearProfiles {
            Log.d("MainActivity", "Profiles cleared successfully")
        }

        setContent {
            MangaselfTheme {
                AppNavigation(profileViewModel, shelfViewModel)
            }
        }
    }
}