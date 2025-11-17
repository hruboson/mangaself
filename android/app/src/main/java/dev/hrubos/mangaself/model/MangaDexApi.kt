package dev.hrubos.mangaself.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object MangaDexApi {

    private const val BASE_URL = "https://api.mangadex.org"

    private suspend fun get(endpoint: String, query: Map<String, String> = emptyMap()): JSONObject? {
        return withContext(Dispatchers.IO) {
            try {
                val queryString = if (query.isNotEmpty()) {
                    query.entries.joinToString("&") { "${it.key}=${it.value.replace(" ", "+")}" }
                } else ""
                val url = URL("$BASE_URL$endpoint?$queryString")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode != 200) {
                    connection.disconnect()
                    return@withContext null
                }

                val responseText = connection.inputStream.bufferedReader().readText()
                connection.disconnect()
                JSONObject(responseText)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /** Fetch manga by title, returns first match or null */
    suspend fun fetchMangaByTitle(title: String): JSONObject? {
        val response = get("/manga", mapOf("title" to title, "limit" to "1")) ?: return null
        val dataArray = response.optJSONArray("data") ?: return null
        if (dataArray.length() == 0) return null
        return dataArray.getJSONObject(0)
    }

    /** Fetch manga description (keeps your existing functionality) */
    suspend fun fetchMangaDescription(title: String): String? {
        val manga = fetchMangaByTitle(title) ?: return null
        val attributes = manga.optJSONObject("attributes") ?: return null
        val descriptionObj = attributes.optJSONObject("description") ?: return null
        return descriptionObj.optString("en") ?: descriptionObj.keys().asSequence().firstOrNull()?.let { descriptionObj.getString(it) }
    }
}