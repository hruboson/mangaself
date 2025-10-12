package dev.hrubos.db

import android.net.Uri

interface Repository {
    suspend fun getProfile(id: String): Profile
    suspend fun getAllProfiles(): List<Profile>
    suspend fun insertProfile(profile: Profile): Profile
    suspend fun deleteProfile(profile: Profile)
    suspend fun clearProfiles()
    suspend fun updateProfile(profile: Profile, name: String, readingMode: String)

    suspend fun addPublication(profileId: String, path: Uri, title: String, description: String): Publication
    suspend fun addChapterToPublication(pubUri: Uri, title: String, description: String, pages: Int, pageLastRead: Int = 0, read: Boolean = false)
    suspend fun getAllPublications(): List<Publication>
    suspend fun getAllPublicationsOfProfile(profileId: String): List<Publication>
    suspend fun getPublicationBySystemPath(systemPath: String): Publication
}