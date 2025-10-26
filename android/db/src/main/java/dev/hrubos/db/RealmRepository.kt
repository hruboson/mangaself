package dev.hrubos.db

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query

class RealmRepository(application: android.app.Application) : Repository {

    private val realm: Realm

    init {
        val config = RealmConfiguration.Builder(
            schema = setOf(
                ProfileRO::class,
                PublicationRO::class,
                ChapterRO::class
            )
        ).name("app.realm")
            .deleteRealmIfMigrationNeeded()
            .build()
        realm = Realm.open(config)
    }

    override suspend fun getProfile(id: String): Profile  {
        val result = realm.query<ProfileRO>("id == $0", id).first().find()
            ?: throw NoSuchElementException("Profile with id $id not found")
        return result.toProfile()
    }

    override suspend fun getAllProfiles(): List<Profile> {
        return realm.query<ProfileRO>().find().map { it.toProfile() }
    }

    override suspend fun insertProfile(profile: Profile): Profile {
        realm.write {
            copyToRealm(profile.toRealmObject())
        }
        return profile
    }

    override suspend fun deleteProfile(profile: Profile) {
        realm.write {
            val managedProfile = findLatest(profile.toRealmObject()) ?: return@write
            delete(managedProfile)
        }
    }

    override suspend fun clearProfiles() {
        realm.write {
            val allProfiles = query<ProfileRO>().find()
            delete(allProfiles)
        }
    }

    override suspend fun updateProfile(profile: Profile, name: String, readingMode: String) {
        realm.write {
            val managedProfile = findLatest(profile.toRealmObject()) ?: return@write

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
            val profile = query<ProfileRO>("id == $0", profileId).first().find()
                ?: throw IllegalArgumentException("Profile not found")

            val existing = profile.associatedPublications
                .firstOrNull { it.systemPath == path }

            if (existing != null) {
                return@writeBlocking existing.toPublication()
            }

            // create new record
            val newPubRO = PublicationRO().apply {
                systemPath = path
                this.title = title
                this.description = description
                favourite = false
            }

            val newPub = copyToRealm(newPubRO)
            profile.associatedPublications.add(newPub)
            newPub.toPublication() // return publication
        }
    }

    override suspend fun addChaptersToPublication(
        profileId: String,
        pubUri: String,
        chapters: List<Chapter>
    ) {
        realm.write {
            val profile = query<ProfileRO>("id == $0", profileId).first().find()
                ?: return@write

            val publication = profile.associatedPublications
                .firstOrNull { it.systemPath == pubUri } ?: return@write

            publication.chapters.clear()
            chapters.forEach { ch ->
                val unmanagedChapter = ch.toRealmObject()
                val managedChapter = copyToRealm(unmanagedChapter)

                publication.chapters.add(managedChapter)
            }
        }
    }

    override suspend fun editPublicationCover(profileId: String, pubUri: String, coverUri: String) {
        realm.write {
            val profile = query<ProfileRO>("id == $0", profileId).first().find()
                ?: return@write

            val publication = profile.associatedPublications
                .firstOrNull { it.systemPath == pubUri } ?: return@write

            publication.coverPath = coverUri
        }
    }

    override suspend fun togglePublicationFavourite(
        profileId: String,
        pubUri: String,
        toggleTo: Boolean
    ) {
        realm.write {
            val profile = query<ProfileRO>("id == $0", profileId).first().find()
                ?: return@write

            val publication = profile.associatedPublications
                .firstOrNull { it.systemPath == pubUri } ?: return@write

            publication.favourite = toggleTo
        }
    }

    override suspend fun getAllPublications(): List<Publication> {
        return realm.query<PublicationRO>().find().map { it.toPublication() }
    }

    override suspend fun getAllPublicationsOfProfile(profileId: String): List<Publication> {
        val profile = realm.query<ProfileRO>("id == $0", profileId).first().find()
        return profile?.associatedPublications?.map { it.toPublication() } ?: emptyList()
    }

    override suspend fun getPublicationBySystemPath(profileId: String, systemPath: String): Publication? {
        val profile = realm.query<ProfileRO>("id == $0", profileId).first().find()
            ?: throw IllegalArgumentException("Profile not found")

        return profile.associatedPublications
            .firstOrNull { it.systemPath == systemPath }
            ?.toPublication()
    }

    override suspend fun removePublication(systemPath: String) {
        realm.write {
            val publication = query<PublicationRO>("systemPath == $0", systemPath).first().find()
            if (publication != null) {
                delete(publication)
            }
        }
    }

    override suspend fun removePublicationFromProfile(profileId: String, systemPath: String) {
        realm.write {
            val profile = query<ProfileRO>("id == $0", profileId).first().find()
                ?: throw IllegalArgumentException("Profile not found")

            val publication = profile.associatedPublications
                .firstOrNull { it.systemPath == systemPath }

            if (publication != null) {
                profile.associatedPublications.remove(publication)
                delete(publication)
            }
        }
    }

    override suspend fun clearPublications(){
        realm.write {
            val all = query<PublicationRO>().find()
            delete(all)
        }
    }

    override suspend fun updateChapter(
        profileId: String,
        pub: Publication,
        chapter: Chapter,
        lastRead: Int
    ) {
        realm.write {
            val profile = query<ProfileRO>("id == $0", profileId).first().find() ?: return@write
            val managedPub = profile.associatedPublications.firstOrNull { it.systemPath == pub.systemPath } ?: return@write
            val managedChapter = managedPub.chapters.firstOrNull { it.title == chapter.title } ?: return@write

            managedChapter.pageLastRead = lastRead
            managedChapter.read = lastRead >= managedChapter.pages
            managedPub.lastChapterRead = managedChapter.position + 1
        }
    }
}