package dev.hrubos.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.setBody
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*

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
}