package dev.hrubos.mangaself.viewmodel

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dev.hrubos.db.Database
import dev.hrubos.db.Profile
import dev.hrubos.mangaself.model.Configuration
import dev.hrubos.mangaself.model.ReadingMode
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

    private val _apiStatusMessage = MutableStateFlow<String?>(null)
    val apiStatusMessage: StateFlow<String?> = _apiStatusMessage.asStateFlow()

    fun clearApiStatusMessage() {
        _apiStatusMessage.value = null
    }

    private var _db: Database = Database(
        useLocal = Configuration.useLocalDB,
        application = application,
        mongoBaseUrl = Configuration.apiURL ?: ""
    )
    val db: Database get() = _db

    fun reinitializeDatabase() {
        _db = Database(
            useLocal = Configuration.useLocalDB,
            application = application,
            mongoBaseUrl = Configuration.apiURL ?: ""
        )
    }

    init {
        viewModelScope.launch {
            dataStore.data.map { it[LAST_SELECTED_PROFILE_ID] }
                .collect { id ->
                    Configuration.selectedProfileId = id ?: ""
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
                Log.d("ProfileViewModel", "Selected profile: ${profile?.name}")
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

        if (id == null || id == "") {
            _selectedProfile.value = null
            return null
        }

        return try {
            val profile = db.getProfile(id)
            if(profile == null){
                Log.e("ProfileViewModel", "Failed to restore profile $id")
                _selectedProfile.value = null
                null
            }
            _selectedProfile.value = profile
            Configuration.selectedProfileId = id
            Log.d("ProfileViewModel", "Restored last selected profile: ${profile?.name}")
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
                _apiStatusMessage.value = null // Clear any previous error on success
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to get profiles", e)
                onResult(emptyList())

                _apiStatusMessage.value = "Failed to connect to API: ${e.message ?: "Unknown error"}. Check the URL and your network connection."
            }
        }
    }

    fun getCurrentReadingMode(): ReadingMode {
        val profile = _selectedProfile.value
        return profile?.readingMode?.let { text ->
            ReadingMode.values().firstOrNull { it.text == text }
        } ?: ReadingMode.LEFTTORIGHT
    }

    fun addProfile(name: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // by default profile is marked as local
                val profile = Profile()
                profile.name = name
                profile.readingMode = ReadingMode.LEFTTORIGHT.text // not optimal to store the whole string but w/e :-)

                db.addProfile(profile)
                onComplete()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to add profile", e)
            }
        }
    }

    fun updateProfileName(newName: String) {
        updateProfile(name = newName)
    }

    fun updateProfileReadingMode(newReadingMode: String) {
        updateProfile(readingMode = newReadingMode)
    }

    private fun updateProfile(name:String = "", readingMode:String = ""){
        val current = _selectedProfile.value ?: return
        var newName = current.name
        var newReadingMode = current.readingMode
        if(name != ""){
            newName = name
        }
        if(readingMode != ""){
            newReadingMode = readingMode
        }

        viewModelScope.launch {
            try {
                db.updateProfile(current, newName, newReadingMode)

                val updated = db.getProfile(current.id)
                _selectedProfile.value = updated
                Log.d("ProfileViewModel", "Profile updated: $newName, $newReadingMode")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to update profile", e)
            }
        }
    }

    fun deleteProfile(onComplete: () -> Unit = {}) { // deletes currently selected profile
        val current = _selectedProfile.value ?: return

        viewModelScope.launch {
            try {
                db.deleteProfile(current)
                Log.d("ProfileViewModel", "Deleted profile")
                onComplete()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to delete profile", e)
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