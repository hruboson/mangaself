package dev.hrubos.mangaself.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.hrubos.db.Database
import dev.hrubos.db.Profile
import dev.hrubos.mangaself.model.Configuration
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application): AndroidViewModel(application) {

    // will be assigned on initialization
    private val db = Database(
        useLocal = Configuration.useLocalDB,
        application = application
    )

    fun getProfiles(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val profiles = db.getProfiles()
                val text = profiles.joinToString("\n") { "ID: ${it.id}, Name: ${it.name}" }
                onResult(text)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to get profiles", e)
                onResult("")
            }
        }
    }

    fun addProfile(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val profile = Profile()
                profile.name = "Test User ${System.currentTimeMillis() % 1000}"

                db.addProfile(profile)
                onComplete()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to add profile", e)
            }
        }
    }
}