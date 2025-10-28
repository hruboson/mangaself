package dev.hrubos.mangaself.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "app_preferences")

object Configuration {
    var useLocalDB: Boolean = true
    var apiURL: String? = null

    var readingMode: ReadingMode = ReadingMode.LONGSTRIP
    var selectedProfileId: String = ""

    private val USE_LOCAL_DB = booleanPreferencesKey("use_local_db")
    private val API_URL = stringPreferencesKey("api_url")

    suspend fun load(context: Context) {
        val prefs = context.dataStore.data.first() // read current preferences
        useLocalDB = prefs[USE_LOCAL_DB] ?: true
        apiURL = prefs[API_URL]
    }

    suspend fun save(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[USE_LOCAL_DB] = useLocalDB
            prefs[API_URL] = apiURL ?: ""
        }
    }
}