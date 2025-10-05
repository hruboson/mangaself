package dev.hrubos.mangaself

import FloatingTopMenu
import dev.hrubos.api.Database

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var profilesTextView: TextView

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
                profilesTextView.text = text
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to get profiles", e)
            }
        }
    }
}