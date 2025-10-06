package dev.hrubos.mangaself.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import dev.hrubos.db.Database
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FloatingTopMenu()
        }

        // --- Test connection with local database ---
        val db = Database(useLocal = true, application = application)

        lifecycleScope.launch {
            try {
                val profiles = db.getProfiles()
                val text = profiles.joinToString(separator = "\n") { "ID: ${it.id}, Name: ${it.name}" }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to get profiles", e)
            }
        }
    }
}