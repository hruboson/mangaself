package dev.hrubos.mangaself

import dev.hrubos.api.Database

import android.os.Bundle
import android.util.Log
import android.widget.TextView
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        profilesTextView = findViewById(R.id.profilesTextView)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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