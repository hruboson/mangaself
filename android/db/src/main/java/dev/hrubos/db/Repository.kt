package dev.hrubos.db

interface Repository {
    suspend fun getProfile(id: String): Profile?
    suspend fun getAllProfiles(): List<Profile>
    suspend fun insertProfile(profile: Profile): Profile
    suspend fun deleteProfile(profile: Profile)
    suspend fun clearProfiles()
    suspend fun updateProfile(profile: Profile, name: String, readingMode: String)

    suspend fun addPublication(profileId: String, path: String, title: String, description: String): Publication
    suspend fun setChaptersToPublication(profileId: String, pubUri: String, chapters: List<Chapter>)
    suspend fun addNewChaptersToPublication(profileId: String, pubUri: String, chapters: List<Chapter>)
    suspend fun removeChaptersOfPublication(profileId: String, pubUri: String, chapters: List<Chapter>)

    suspend fun editPublicationCover(profileId: String, pubUri: String, coverUri: String)
    suspend fun editPublication(profileId: String, pubUri: String, description: String, title: String)
    suspend fun togglePublicationFavourite(profileId: String, pubUri: String, toggleTo: Boolean)
    suspend fun getAllPublications(): List<Publication>
    suspend fun getAllPublicationsOfProfile(profileId: String): List<Publication>
    suspend fun getPublicationBySystemPath(profileId: String, systemPath: String): Publication?
    suspend fun removePublication(systemPath: String)
    suspend fun removePublicationFromProfile(profileId: String, systemPath: String)
    suspend fun clearPublications()

    suspend fun updateChapter(profileId: String, pub: Publication, chapter: Chapter, lastRead: Int)
}