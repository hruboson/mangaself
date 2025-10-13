package dev.hrubos.db

interface Repository {
    suspend fun getProfile(id: String): Profile
    suspend fun getAllProfiles(): List<Profile>
    suspend fun insertProfile(profile: Profile): Profile
    suspend fun deleteProfile(profile: Profile)
    suspend fun clearProfiles()
    suspend fun updateProfile(profile: Profile, name: String, readingMode: String)

    suspend fun addPublication(profileId: String, path: String, title: String, description: String): Publication
    suspend fun addChaptersToPublication(pubUri: String, chapters: List<Chapter>)
    suspend fun editPublicationCover(pubUri: String, coverUri: String)
    suspend fun togglePublicationFavourite(pubUri: String, toggleTo: Boolean)
    suspend fun getAllPublications(): List<Publication>
    suspend fun getAllPublicationsOfProfile(profileId: String): List<Publication>
    suspend fun getPublicationBySystemPath(systemPath: String): Publication
    suspend fun removePublication(systemPath: String)
    suspend fun removePublicationFromProfile(profileId: String, systemPath: String)
    suspend fun clearPublications()
}