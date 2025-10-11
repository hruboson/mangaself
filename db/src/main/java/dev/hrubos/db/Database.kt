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
    suspend fun clearProfiles() = repository.clearProfiles()

    suspend fun updateProfile(profile: Profile, name: String) = repository.updateProfile(profile, name)
}