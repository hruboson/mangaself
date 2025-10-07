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

    fun getProfiles(onResult: (List<Profile>) -> Unit) {
        viewModelScope.launch {
            try {
                val profiles = db.getProfiles()
                onResult(profiles)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to get profiles", e)
                onResult(emptyList())
            }
        }
    }

    fun addProfile(name: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // by default profile is marked as local
                val profile = Profile()
                profile.name = name

                db.addProfile(profile)
                onComplete()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to add profile", e)
            }
        }
    }

    fun clearProfiles(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                db.clearProfiles()
                onComplete()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to delete all profiles", e)
            }
        }
    }
}