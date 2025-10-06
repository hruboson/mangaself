package dev.hrubos.mangaself

import dev.hrubos.db.Database

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import dev.hrubos.mangaself.ui.components.FloatingTopMenu
import dev.hrubos.mangaself.ui.theme.MangaselfTheme
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