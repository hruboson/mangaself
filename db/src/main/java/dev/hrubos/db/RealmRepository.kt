package dev.hrubos.db

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query

class RealmRepository(application: android.app.Application) : Repository {

    private val realm: Realm

    init {
        val config = RealmConfiguration.Builder(schema = setOf(Profile::class))
            .name("app.realm")
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
}