package dev.hrubos.db

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson

class MongoRepository(private val baseUrl: String) : Repository {

    private val client = HttpClient {
        install(ContentNegotiation) { gson() }
    }

    override suspend fun getProfile(id: String): Profile? {
        return client.get("$baseUrl/profile/$id").body()
    }

    override suspend fun getAllProfiles(): List<Profile> {
        return client.get("$baseUrl/profiles").body()
    }

    override suspend fun insertProfile(profile: Profile): Profile {
        return client.post("$baseUrl/profile") {
            contentType(ContentType.Application.Json)
            setBody(profile)
        }.body()
    }

    override suspend fun deleteProfile(profile: Profile) {
        client.delete("$baseUrl/profile/${profile.id}")
    }

    override suspend fun clearProfiles() {
        client.delete("$baseUrl/profiles")
    }

    override suspend fun updateProfile(profile: Profile, name: String, readingMode: String) {
        val updated = profile.copy(
            name = if (name.isNotEmpty()) name else profile.name,
            readingMode = if (readingMode.isNotEmpty()) readingMode else profile.readingMode
        )
        client.put("$baseUrl/profile/${profile.id}") {
            contentType(ContentType.Application.Json)
            setBody(updated)
        }
    }

    override suspend fun addPublication(
        profileId: String,
        path: String,
        title: String,
        description: String
    ): Publication {
        return Publication()
    }

    override suspend fun addChaptersToPublication(
        profileId: String,
        pubUri: String,
        chapters: List<Chapter>
    ) {

    }

    override suspend fun editPublicationCover(profileId: String, pubUri: String, coverUri: String) {

    }

    override suspend fun togglePublicationFavourite(
        profileId: String,
        pubUri: String,
        toggleTo: Boolean
    ) {

    }

    override suspend fun getAllPublications(): List<Publication> {
        return client.get("$baseUrl/publications").body()
    }

    override suspend fun getAllPublicationsOfProfile(profileId: String): List<Publication> {
        return client.get("$baseUrl/profile/$profileId/publications").body()
    }

    override suspend fun getPublicationBySystemPath(
        profileId: String,
        systemPath: String
    ): Publication? {
        return null
    }

    override suspend fun removePublication(systemPath: String) {

    }

    override suspend fun removePublicationFromProfile(profileId: String, systemPath: String) {

    }

    override suspend fun clearPublications() {
        client.delete("$baseUrl/publications")
    }

    override suspend fun updateChapter(
        profileId: String,
        pub: Publication,
        chapter: Chapter,
        lastRead: Int
    ) {

    }
}
