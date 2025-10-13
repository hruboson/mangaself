package dev.hrubos.db

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.gson.gson

/**
 *
 *
 *  TODO THIS WHOLE CLASS, THE REQUESTS ARE GIBBERISH NOW
 *
 *
 */

class MongoRepository(private val baseUrl: String) : Repository {

    private val client = HttpClient {
        install(ContentNegotiation) { gson() }
    }

    override suspend fun getProfile(id: String): Profile {
        return client.get("$baseUrl/profile/$id").body()
    }

    override suspend fun getAllProfiles(): List<Profile> {
        return client.get("$baseUrl/profiles").body()
    }

    override suspend fun insertProfile(profile: Profile): Profile {
        return client.post("$baseUrl/users") {
            setBody(profile)
        }.body()
    }

    override suspend fun deleteProfile(profile: Profile) {
        return client.post("$baseUrl/deleteProfile/").body()
    }

    override suspend fun clearProfiles() {
        return client.post("$baseUrl/clearProfiles").body()
    }

    override suspend fun updateProfile(profile: Profile, name: String, readingMode: String) {
        return client.post("$baseUrl/updateProfile") {
            setBody(profile)
        }.body()
    }

    override suspend fun addPublication(
        profileId: String,
        path: String,
        title: String,
        description: String
    ): Publication {
        TODO("Not yet implemented")
    }

    override suspend fun addChaptersToPublication(
        pubUri: String,
        chapters: List<Chapter>
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun editPublicationCover(pubUri: String, coverUri: String) {
        TODO("Not yet implemented")
    }

    override suspend fun togglePublicationFavourite(
        pubUri: String,
        toggleTo: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllPublications(): List<Publication> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllPublicationsOfProfile(profileId: String): List<Publication> {
        TODO("Not yet implemented")
    }

    override suspend fun getPublicationBySystemPath(systemPath: String): Publication {
        TODO("Not yet implemented")
    }

    override suspend fun removePublication(systemPath: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removePublicationFromProfile(
        profileId: String,
        systemPath: String
    ) {
        TODO("Not yet implemented")
    }


    override suspend fun clearPublications() {
        TODO("Not yet implemented")
    }
}