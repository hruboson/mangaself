package dev.hrubos.db

import android.net.Uri
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
        val profile = realm.query<Profile>("id == $0", id).first().find()

        return profile ?: throw NoSuchElementException("Profile with id $id not found")
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
        path: Uri,
        title: String,
        description: String
    ) {
        realm.writeBlocking {
            val profile = query<Profile>("id == $0", profileId).first().find()

            if (profile != null) {
                val newPublication = copyToRealm(
                    Publication().apply {
                        systemPath = path.toString() // store URI as string
                        this.title = title
                        this.description = description
                        favourite = false
                    }
                )

                profile.associatedPublications.add(newPublication)
            } else {
                throw IllegalArgumentException("Profile not found")
            }
        }
    }

    override suspend fun addChapterToPublication(
        pubUri: Uri,
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
}