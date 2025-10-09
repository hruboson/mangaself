package dev.hrubos.mangaself.viewmodel

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.hrubos.db.Database
import dev.hrubos.db.Profile
import dev.hrubos.mangaself.model.Configuration
import dev.hrubos.mangaself.model.dataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application): AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val SELECTED_PROFILE_ID = stringPreferencesKey("selected_profile_id")

    // will be assigned on initialization
    private val db = Database(
        useLocal = Configuration.useLocalDB,
        application = application
    )

    init {
        viewModelScope.launch {
            dataStore.data.map { it[SELECTED_PROFILE_ID] }
                .collect { id ->
                    Configuration.selectedProfileId = id
                }
        }
    }

    fun selectProfile(profileId: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[SELECTED_PROFILE_ID] = profileId
            }
            Configuration.selectedProfileId = profileId
            Log.d("ProfileViewModel", "Selected profile: $profileId")
        }
    }

    fun logout(onComplete: () -> Unit = {}){
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[SELECTED_PROFILE_ID] = ""
            }
            Configuration.selectedProfileId = ""
            Log.d("ProfileViewModel", "Logged out of profile")
            onComplete()
        }
    }

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