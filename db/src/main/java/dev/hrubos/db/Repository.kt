package dev.hrubos.db

interface Repository {
    suspend fun getAllProfiles(): List<Profile>
    suspend fun insertProfile(profile: Profile): Profile
    suspend fun clearProfiles()
}