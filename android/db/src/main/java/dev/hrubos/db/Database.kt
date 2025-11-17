package dev.hrubos.db

import android.util.Log

class Database(
    useLocal: Boolean,
    application: android.app.Application? = null, // only needed for Realm
    mongoBaseUrl: String? = null                  // only needed for Mongo
) {

    private val repository: Repository

    init {
        repository = if (useLocal) {
            if (application == null) {
                throw IllegalArgumentException("Application instance required for RealmRepository")
            }
            Log.d("Database", "Using local Realm repository")
            RealmRepository(application)
        } else {
            if (mongoBaseUrl == null) {
                throw IllegalArgumentException("Mongo base URL required for MongoRepository")
            }
            Log.d("Database", "Using API with MongoDB")
            MongoRepository(mongoBaseUrl)
        }
    }

    suspend fun getProfile(id: String): Profile? = repository.getProfile(id)
    suspend fun getProfiles(): List<Profile> = repository.getAllProfiles()
    suspend fun addProfile(profile: Profile): Profile = repository.insertProfile(profile)
    suspend fun deleteProfile(profile: Profile) = repository.deleteProfile(profile)
    suspend fun clearProfiles() = repository.clearProfiles()
    suspend fun updateProfile(profile: Profile, name: String, readingMode: String) = repository.updateProfile(profile, name, readingMode)

    suspend fun addPublication(profileId: String, path: String, title: String = "", description: String = ""): Publication =
        repository.addPublication(profileId, path, title, description)

    suspend fun addChaptersToPublication(profileId: String, pubUri: String, chapters: List<Chapter>) =
        repository.addChaptersToPublication(profileId, pubUri, chapters)

    suspend fun editPublicationCover(profileId: String, pubUri: String, coverUri: String) =
        repository.editPublicationCover(profileId, pubUri, coverUri)

    suspend fun editPublication(profileId: String, pubUri: String, description: String, title: String) =
        repository.editPublication(profileId, pubUri, description, title)

    suspend fun togglePublicationFavourite(profileId: String, pubUri: String, toggleTo: Boolean) =
        repository.togglePublicationFavourite(profileId, pubUri, toggleTo)

    suspend fun getAllPublications() = repository.getAllPublications()

    suspend fun getAllPublicationsOfProfile(profileId: String) =
        repository.getAllPublicationsOfProfile(profileId)

    suspend fun getPublicationBySystemPath(profileId: String, systemPath: String) =
        repository.getPublicationBySystemPath(profileId, systemPath)

    suspend fun removePublication(systemPath: String) =
        repository.removePublication(systemPath)

    suspend fun removePublicationFromProfile(profileId: String, systemPath: String) =
        repository.removePublicationFromProfile(profileId, systemPath)

    suspend fun clearPublications() =
        repository.clearPublications()

    suspend fun updateChapter(profileId: String, pub: Publication, chapter: Chapter, lastRead: Int) =
        repository.updateChapter(profileId, pub, chapter, lastRead)
}