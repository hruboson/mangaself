package dev.hrubos.mangaself.model

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "app_preferences")

object Configuration {
    var useLocalDB: Boolean = true
    var readingMode: ReadingMode = ReadingMode.LONGSTRIP
    var selectedProfileId: String? = ""
}