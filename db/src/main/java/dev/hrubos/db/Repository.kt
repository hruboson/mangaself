package dev.hrubos.db

interface Repository {
    suspend fun getProfile(id: String): Profile
    suspend fun getAllProfiles(): List<Profile>
    suspend fun insertProfile(profile: Profile): Profile
    suspend fun clearProfiles()
    suspend fun updateProfile(profile: Profile, name: String, readingMode: String)
}