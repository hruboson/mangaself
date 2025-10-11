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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application): AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val LAST_SELECTED_PROFILE_ID = stringPreferencesKey("selected_profile_id")

    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()

    // will be assigned on initialization
    private val db = Database(
        useLocal = Configuration.useLocalDB,
        application = application
    )

    init {
        viewModelScope.launch {
            dataStore.data.map { it[LAST_SELECTED_PROFILE_ID] }
                .collect { id ->
                    Configuration.selectedProfileId = id
                }
        }
    }

    fun selectProfile(profileId: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[LAST_SELECTED_PROFILE_ID] = profileId
            }

            runCatching {
                db.getProfile(profileId)
            }.onSuccess { profile ->
                _selectedProfile.value = profile
                Configuration.selectedProfileId = profileId
                Log.d("ProfileViewModel", "Selected profile: ${profile.name}")
            }.onFailure {
                Log.e("ProfileViewModel", "Profile not found: $profileId")
            }
        }
    }

    fun logout(onComplete: () -> Unit = {}){
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[LAST_SELECTED_PROFILE_ID] = ""
            }
            Configuration.selectedProfileId = ""
            _selectedProfile.value = null

            Log.d("ProfileViewModel", "Logged out of profile")
            onComplete()
        }
    }

    suspend fun restoreLastSelectedProfile(): Profile? {
        val id = dataStore.data.first()[LAST_SELECTED_PROFILE_ID]

        if (id == null) {
            _selectedProfile.value = null
            return null
        }

        return try {
            val profile = db.getProfile(id)
            _selectedProfile.value = profile
            Configuration.selectedProfileId = id
            Log.d("ProfileViewModel", "Restored last selected profile: ${profile.name}")
            profile
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Failed to restore profile $id: ${e.message}", e)
            _selectedProfile.value = null
            null
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

    fun updateProfileName(newName: String) {
        val current = _selectedProfile.value ?: return

        viewModelScope.launch {
            try {
                db.updateProfileName(current, newName)
                _selectedProfile.value = current
                Log.d("ProfileViewModel", "Profile name updated: $newName")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to update profile", e)
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