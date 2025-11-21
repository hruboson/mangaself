package dev.hrubos.mangaself

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import dev.hrubos.mangaself.model.Configuration
import dev.hrubos.mangaself.model.ThemeStyle
import dev.hrubos.mangaself.navigation.AppNavigation
import dev.hrubos.mangaself.ui.theme.MangaselfTheme
import dev.hrubos.mangaself.viewmodel.ProfileViewModel
import dev.hrubos.mangaself.viewmodel.ShelfViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private val shelfViewModel: ShelfViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // load configuration from DataStore
        lifecycleScope.launch {
            Configuration.load(applicationContext)

            // this is asynchronous so this should always be called
            // its a bit inefficient but w/e \_(-.-)_/ shouldn't really affect the performance
            profileViewModel.reinitializeDatabase()
            shelfViewModel.reinitializeDatabase()
        }

        // disable edge-to-edge on newer phones
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val currentTheme by Configuration.themeFlow(applicationContext)
                .collectAsState(initial = ThemeStyle.AUTO)

            MangaselfTheme (darkTheme = when(currentTheme) {
                ThemeStyle.AUTO -> isSystemInDarkTheme()
                ThemeStyle.DARK -> true
                ThemeStyle.LIGHT -> false
            } ) { // put this logic inside of MangaselfTheme class
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                ) {
                    AppNavigation(profileViewModel, shelfViewModel, onThemeChange = { newTheme ->
                        lifecycleScope.launch {
                            Configuration.setTheme(applicationContext, newTheme)
                        }
                    })
                }
            }
        }
    }
}