package dev.hrubos.db

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
        val newPublication = Publication(
            systemPath = path,
            title = title,
            description = description,
            coverPath = "",
            chapters = emptyList(),
            lastChapterRead = 0,
            favourite = false
        )

        return client.post("$baseUrl/profile/$profileId/publications") {
            contentType(ContentType.Application.Json)
            setBody(newPublication)
        }.body()
    }

    override suspend fun addChaptersToPublication(
        profileId: String,
        pubUri: String,
        chapters: List<Chapter>
    ) {
        client.put("$baseUrl/profile/$profileId/publication/chapters") {
            parameter("pubUri", pubUri)
            contentType(ContentType.Application.Json)
            setBody(chapters)
        }
    }

    override suspend fun editPublicationCover(profileId: String, pubUri: String, coverUri: String) {
        val encodedPubUri = URLEncoder.encode(pubUri, StandardCharsets.UTF_8.toString())
        val encodedCoverUri = URLEncoder.encode(coverUri, StandardCharsets.UTF_8.toString())

        client.put("$baseUrl/profile/$profileId/publication/cover?systemPath=$encodedPubUri&coverPath=$encodedCoverUri")
    }

    override suspend fun togglePublicationFavourite(
        profileId: String,
        pubUri: String,
        toggleTo: Boolean
    ) {
        val encodedPubUri = URLEncoder.encode(pubUri, StandardCharsets.UTF_8.toString())

        client.put("$baseUrl/profile/$profileId/publication/favourite") {
            parameter("systemPath", encodedPubUri)
            parameter("toggleTo", toggleTo)
        }
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
        val encodedPath = URLEncoder.encode(systemPath, StandardCharsets.UTF_8.toString()) // encode so it doesn't cause trouble in the query/url
        return client.get("$baseUrl/profile/$profileId/publication?systemPath=$encodedPath")
            .body()
    }

    override suspend fun removePublication(systemPath: String) {
        client.delete("$baseUrl/publications")
    }

    override suspend fun removePublicationFromProfile(profileId: String, systemPath: String) {
        val encodedPath = URLEncoder.encode(systemPath, StandardCharsets.UTF_8.toString()) // encode so it doesn't cause trouble in the query/url
        client.delete("$baseUrl/profile/$profileId/publication?systemPath=$encodedPath")
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
        val encodedPubUri = URLEncoder.encode(pub.systemPath, StandardCharsets.UTF_8.toString())
        val encodedChapterTitle = URLEncoder.encode(chapter.title, StandardCharsets.UTF_8.toString())

        client.put("$baseUrl/profile/$profileId/publication/chapter") {
            parameter("systemPath", encodedPubUri)
            parameter("chapterTitle", encodedChapterTitle)
            parameter("lastRead", lastRead)
        }
    }
}
