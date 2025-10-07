package dev.hrubos.mangaself

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dev.hrubos.mangaself.navigation.AppNavigation
import dev.hrubos.mangaself.ui.theme.MangaselfTheme
import dev.hrubos.mangaself.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MangaselfTheme {
                AppNavigation(viewModel)
            }
        }
    }
}