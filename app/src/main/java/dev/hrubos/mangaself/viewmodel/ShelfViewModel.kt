package dev.hrubos.mangaself.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.hrubos.db.Database
import dev.hrubos.mangaself.model.Configuration

class ShelfViewModel(application: Application): AndroidViewModel(application) {

    // will be assigned on initialization
    private val db = Database(
        useLocal = Configuration.useLocalDB,
        application = application
    )
}