package dev.hrubos.db

import android.net.Uri

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
            RealmRepository(application)
        } else {
            if (mongoBaseUrl == null) {
                throw IllegalArgumentException("Mongo base URL required for MongoRepository")
            }
            MongoRepository(mongoBaseUrl)
        }
    }

    suspend fun getProfile(id: String): Profile = repository.getProfile(id)
    suspend fun getProfiles(): List<Profile> = repository.getAllProfiles()
    suspend fun addProfile(profile: Profile): Profile = repository.insertProfile(profile)
    suspend fun deleteProfile(profile: Profile) = repository.deleteProfile(profile)
    suspend fun clearProfiles() = repository.clearProfiles()
    suspend fun updateProfile(profile: Profile, name: String, readingMode: String) = repository.updateProfile(profile, name, readingMode)

    suspend fun addPublication(profileId: String, path: Uri, title: String = "", description: String = ""): Publication = repository.addPublication(profileId, path, title, description)
    suspend fun addChapterToPublication(pubUri: Uri, title: String = "", description: String = "", pages: Int = 0, pageLastRead: Int = 0, read: Boolean = false) = repository.addChapterToPublication(pubUri, title, description, pages, pageLastRead, read)
    suspend fun getAllPublications() = repository.getAllPublications()
    suspend fun getAllPublicationsOfProfile(profileId: String) = repository.getAllPublicationsOfProfile(profileId)
    suspend fun getPublicationBySystemPath(systemPath: String) = repository.getPublicationBySystemPath(systemPath)
}