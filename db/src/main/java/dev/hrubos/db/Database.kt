package dev.hrubos.db

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

    suspend fun addPublication(profileId: String, path: String, title: String = "", description: String = ""): Publication = repository.addPublication(profileId, path, title, description)
    suspend fun addChaptersToPublication(pubUri: String, chapters: List<Chapter>) = repository.addChaptersToPublication(pubUri, chapters)
    suspend fun editPublicationCover(pubUri: String, coverUri: String) = repository.editPublicationCover(pubUri, coverUri)
    suspend fun togglePublicationFavourite(pubUri: String, toggleTo: Boolean) = repository.togglePublicationFavourite(pubUri, toggleTo)
    suspend fun getAllPublications() = repository.getAllPublications()
    suspend fun getAllPublicationsOfProfile(profileId: String) = repository.getAllPublicationsOfProfile(profileId)
    suspend fun getPublicationBySystemPath(systemPath: String) = repository.getPublicationBySystemPath(systemPath)
    suspend fun removePublication(systemPath: String) = repository.removePublication(systemPath)
    suspend fun removePublicationFromProfile(profileId: String, systemPath: String) = repository.removePublicationFromProfile(profileId, systemPath)
    suspend fun clearPublications() = repository.clearPublications()
}