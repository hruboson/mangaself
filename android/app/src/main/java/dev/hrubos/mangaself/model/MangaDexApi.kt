package dev.hrubos.mangaself.model

import android.util.Log
import dev.hrubos.db.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class ApiResult<out T> {
    data object Idle : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
    data class Success<T>(val data: T) : ApiResult<T>()
    data class HttpError(val code: Int, val message: String) : ApiResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ApiResult<Nothing>()
    data class NotFound(val message: String = "Not found") : ApiResult<Nothing>()
    data class UnknownError(val exception: Throwable) : ApiResult<Nothing>()
}

object MangaDexApi {

    private const val BASE_URL = "https://api.mangadex.org"

    private suspend fun get(endpoint: String, query: Map<String, String> = emptyMap()): ApiResult<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val qs = query.entries.joinToString("&") {
                "${it.key}=${it.value.replace(" ", "+")}"
            }
            val url = URL("$BASE_URL$endpoint?$qs")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream

            val text = stream?.bufferedReader()?.readText().orEmpty()

            connection.disconnect()

            when (code) {
                200 -> ApiResult.Success(JSONObject(text))
                404 -> ApiResult.NotFound("Resource not found")
                else -> ApiResult.HttpError(code, "HTTP error $code")
            }

        } catch (e: java.net.UnknownHostException) {
            ApiResult.NetworkError(e) // No internet
        } catch (e: Exception) {
            ApiResult.UnknownError(e)
        }
    }

    /** Fetch manga by title, returns first match or null */
    suspend fun fetchMangaByTitle(title: String): ApiResult<JSONObject> {
        return when (val result = get("/manga", mapOf("title" to title, "limit" to "1"))) {

            is ApiResult.Success -> {
                val data = result.data.optJSONArray("data")
                if (data == null || data.length() == 0)
                    ApiResult.NotFound("No manga found")
                else
                    ApiResult.Success(data.getJSONObject(0))
            }

            else -> result
        }
    }

    /** Fetch manga description (keeps your existing functionality) */
    suspend fun fetchMangaDescription(title: String): ApiResult<String> {
        return when (val result = fetchMangaByTitle(title)) {
            is ApiResult.Idle -> { return ApiResult.Idle }
            is ApiResult.Success -> {
                val attributes = result.data.optJSONObject("attributes")
                val descObj = attributes?.optJSONObject("description")
                val desc = descObj?.optString("en")
                    ?: descObj?.keys()?.asSequence()?.firstOrNull()?.let { descObj.getString(it) }

                if (desc.isNullOrBlank())
                    ApiResult.NotFound("Description missing")
                else
                    ApiResult.Success(desc)
            }

            is ApiResult.NotFound -> result
            is ApiResult.NetworkError -> result
            is ApiResult.HttpError -> result
            is ApiResult.UnknownError -> result

            ApiResult.Loading -> ApiResult.Loading // propagate
        }
    }

    /** Fetch chapters for a given manga ID */
    /**
     * For now not used because Mangadex does not seem as a good source for chapter numbering
     */
    suspend fun fetchChapters(mangaId: String): ApiResult<List<Chapter>> {
        val chapters = mutableListOf<Chapter>()
        var offset = 0
        val limit = 100
        var counter = 0

        while (true) {

            when (val result = get(
                "/chapter",
                mapOf(
                    "manga" to mangaId,
                    "limit" to limit.toString(),
                    "offset" to offset.toString(),
                    "translatedLanguage[]" to "en",
                    "order[chapter]" to "asc"
                )
            )) {
                is ApiResult.Idle -> { return ApiResult.Idle }
                is ApiResult.Success -> {
                    val dataArray = result.data.optJSONArray("data") ?: return ApiResult.Success(chapters)

                    if (dataArray.length() == 0) {
                        return ApiResult.Success(chapters)
                    }

                    for (i in 0 until dataArray.length()) {
                        val chapterObj = dataArray.getJSONObject(i)
                        val attributes = chapterObj.optJSONObject("attributes") ?: continue

                        val title = attributes.optString(
                            "title",
                            "Chapter ${attributes.optString("chapter", "")}"
                        )

                        val chapterNumber = attributes.optString("chapter", "")

                        Log.d("MangaDexApiCaller", "$title ($chapterNumber)")

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

                is ApiResult.NotFound -> return result
                is ApiResult.NetworkError -> return result
                is ApiResult.HttpError -> return result
                is ApiResult.UnknownError -> return result

                ApiResult.Loading -> ApiResult.Loading // propagate
            }
        }
    }
}