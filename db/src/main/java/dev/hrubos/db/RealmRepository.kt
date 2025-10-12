package dev.hrubos.db

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query

class RealmRepository(application: android.app.Application) : Repository {

    private val realm: Realm

    init {
        val config = RealmConfiguration.Builder(
            schema = setOf(
                Profile::class,
                Publication::class,
                Chapter::class
            )
        ).name("app.realm")
            .deleteRealmIfMigrationNeeded()
            .build()
        realm = Realm.open(config)
    }

    override suspend fun getProfile(id: String): Profile  {
        return realm.query<Profile>("id == $0", id).first().find()
            ?: throw NoSuchElementException("Profile with id $id not found")
    }

    override suspend fun getAllProfiles(): List<Profile> {
        return realm.query<Profile>().find().toList()
    }

    override suspend fun insertProfile(profile: Profile): Profile {
        realm.write {
            copyToRealm(profile)
        }
        return profile
    }

    override suspend fun deleteProfile(profile: Profile) {
        realm.write {
            val managedProfile = findLatest(profile) ?: return@write
            delete(managedProfile)
        }
    }

    override suspend fun clearProfiles() {
        realm.write {
            val allProfiles = query<Profile>().find()
            delete(allProfiles)
        }
    }

    override suspend fun updateProfile(profile: Profile, name: String, readingMode: String) {
        realm.write {
            val managedProfile = findLatest(profile) ?: return@write

            if(name != "") {
                managedProfile.name = name
            }

            if(readingMode != ""){
                managedProfile.readingMode = readingMode
            }
        }
    }

    override suspend fun addPublication(
        profileId: String,
        path: String,
        title: String,
        description: String
    ): Publication {
        return realm.writeBlocking {
            val profile = query<Profile>("id == $0", profileId).first().find()
                ?: throw IllegalArgumentException("Profile not found")

            val existingPublication = query<Publication>("systemPath == $0", path).first().find()

            val publicationToAdd = if (existingPublication != null) {
                // reuse existing record
                existingPublication
            } else {
                // create new record
                copyToRealm(
                    Publication().apply {
                        systemPath = path
                        this.title = title
                        this.description = description
                        favourite = false
                    }
                )
            }

            if (!profile.associatedPublications.contains(publicationToAdd)) {
                profile.associatedPublications.add(publicationToAdd)
            }

            publicationToAdd // return publication
        }
    }

    override suspend fun addChapterToPublication(
        pubUri: String,
        title: String,
        description: String,
        pages: Int,
        pageLastRead: Int,
        read: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllPublications(): List<Publication> {
        return realm.query<Publication>().find().toList()
    }

    override suspend fun getAllPublicationsOfProfile(profileId: String): List<Publication> {
        val profile = realm.query<Profile>("id == $0", profileId).first().find()
        return profile?.associatedPublications?.toList() ?: emptyList()
    }

    override suspend fun getPublicationBySystemPath(systemPath: String): Publication {
        return realm.query<Publication>("systemPath == $0", systemPath).first().find()
            ?: throw NoSuchElementException("Publication with systemPath '$systemPath' not found")
    }

    override suspend fun removePublication(systemPath: String) {
        realm.write {
            val publication = query<Publication>("systemPath == $0", systemPath).first().find()
            if (publication != null) {
                delete(publication)
            }
        }
    }

    override suspend fun clearPublications(){
        realm.write {
            val all = query<Publication>().find()
            delete(all)
        }
    }
}