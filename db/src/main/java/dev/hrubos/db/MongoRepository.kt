package dev.hrubos.db

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.gson.gson

class MongoRepository(private val baseUrl: String) : Repository {

    private val client = HttpClient {
        install(ContentNegotiation) { gson() }
    }

    override suspend fun getAllProfiles(): List<Profile> {
        return client.get("$baseUrl/profiles").body()
    }

    override suspend fun insertProfile(profile: Profile): Profile {
        return client.post("$baseUrl/users") {
            setBody(profile)
        }.body()
    }

    override suspend fun clearProfiles() {
        return client.post("$baseUrl/clearProfiles").body()
    }
}