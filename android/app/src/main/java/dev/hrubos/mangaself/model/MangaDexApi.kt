package dev.hrubos.mangaself.model

import android.util.Log
import dev.hrubos.db.Chapter
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

    /** Fetch chapters for a given manga ID */
    /**
     * For now not used because Mangadex does not seem as a good source for chapter numbering
     */
    suspend fun fetchChapters(mangaId: String): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        var offset = 0
        val limit = 100
        var counter = 0

        while (true) {
            val response = get("/chapter", mapOf(
                "manga" to mangaId,
                "limit" to limit.toString(),
                "offset" to offset.toString(),
                "translatedLanguage[]" to "en",
                "order[chapter]" to "asc"
            )) ?: break

            val dataArray = response.optJSONArray("data") ?: break
            if (dataArray.length() == 0) break

            for (i in 0 until dataArray.length()) {
                val chapterObj = dataArray.getJSONObject(i)
                val attributes = chapterObj.optJSONObject("attributes") ?: continue
                val title = attributes.optString("title", "Chapter ${attributes.optString("chapter", "")}")
                val chapterNumber = attributes.optString("chapter", "") // keep as string

                Log.d("MangaDexApiCaller", title + " (" + chapterNumber + ")" )

                chapters.add(
                    Chapter(
                        title = title,
                        description = "",
                        position = counter++
                    )
                )
            }

            offset += limit
        }

        return chapters
    }
}