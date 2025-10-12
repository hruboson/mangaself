package dev.hrubos.mangaself.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.hrubos.db.Chapter
import dev.hrubos.db.Database
import dev.hrubos.db.Publication
import dev.hrubos.mangaself.model.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ShelfViewModel(application: Application): AndroidViewModel(application) {

    // will be assigned on initialization
    private val db = Database(
        useLocal = Configuration.useLocalDB,
        application = application
    )

    private val _publications = MutableStateFlow<List<Publication>>(emptyList())
    val publications: StateFlow<List<Publication>> = _publications.asStateFlow()

    // live field publication --- better for editing, just don't forget to call loadPublication
    private val _publication = MutableStateFlow<Publication?>(null)
    val publication: StateFlow<Publication?> = _publication

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    fun loadAllPublications() {
        viewModelScope.launch {
            try {
                val pubs = db.getAllPublications()
                _publications.value = pubs
            } catch (e: Exception) {
                Log.e("ShelfViewModel", "Failed to load publications", e)
            }
        }
    }

    fun loadPublicationsOfProfile(profileId: String){
        viewModelScope.launch {
            try {
                val pubs = db.getAllPublicationsOfProfile(profileId)
                _publications.value = pubs
            } catch (e: Exception) {
                Log.e("ShelfViewModel", "Failed to load profile publications", e)
                _publications.value = emptyList()
            }
        }
    }

    fun loadPublication(systemPath: String){
        Log.d("ShelfViewModel", "Loading publication with path $systemPath")
        viewModelScope.launch {
            val pub = db.getPublicationBySystemPath(systemPath)
            _publication.value = pub
        }
    }

    fun addPublication(profileId: String = "", uri: Uri){
        if(profileId == "") return
        val rawUri = uri.toString() // needed because uri.path is not enough and uri.toString() encodes
        viewModelScope.launch {
            try {
                Log.d("ShelfViewModel", "Adding publication with path ${rawUri}")
                val pub = db.addPublication(profileId, rawUri)
                scanChapters(pub)
            } catch (e: Exception) {
                Log.e("ShelfViewModel", "Failed to add publication with path ${rawUri}", e)
            }
        }
    }

    fun removePublication(profileId: String?){
        val systemPath = _publication.value?.systemPath ?: return
        viewModelScope.launch {
            if(profileId == null){
                // remove from all profiles
                db.removePublication(systemPath)
            }else {
                // remove from specified profile
                db.removePublicationFromProfile(profileId, systemPath)
            }
            _publication.value = null
        }
    }

    fun clearPublications(onComplete: () -> Unit = {}){
        viewModelScope.launch {
            try {
                db.clearPublications()
                onComplete()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to delete all publications", e)
            }
        }
    }

    fun editPublicationTitle(title: String){

    }

    fun editPublicationDescription(description: String){

    }

    /*********************
     * Scanner functions *
     ********************/

    fun scanChapters(pub: Publication){
        viewModelScope.launch(Dispatchers.IO){ // Dispatcher.IO moves it off of the main thread
            _isScanning.value = true
            Log.d("ChapterScanner", "Scanning chapters for ${pub.systemPath}")

            val folderUri = pub.systemPath.toUri()
            val docFolder = DocumentFile.fromTreeUri(getApplication(), folderUri)

            if (docFolder == null || !docFolder.isDirectory) {
                Log.e("ChapterScanner", "Invalid publication folder")
                return@launch
            }

            val sortedDirs = docFolder.listFiles()
                .filter { it.isDirectory }
                .sortedWith(compareBy {
                    it.name?.let { name -> name.lowercase(Locale.ROOT) } ?: ""
                })

            val chaptersList = mutableListOf<Chapter>()
            var position = 0

            for (dir in sortedDirs) {
                val pageFiles = dir.listFiles().filter { it.isFile } // only count files
                val pagesCount = pageFiles.size

                if (pagesCount == 0) {
                    Log.d("ChapterScanner", "Skipping empty chapter: ${dir.name}")
                    continue
                }

                val chapter = Chapter().apply {
                    title = dir.name ?: "Untitled Chapter"
                    description = ""
                    pages = pagesCount
                    pageLastRead = 0
                    read = false
                    this.position = position++
                }

                chaptersList.add(chapter)
            }

            try {
                db.addChaptersToPublication(pub.systemPath, chaptersList)
                Log.d("ChapterScanner", "Finished scanning chapters: ${chaptersList.size} found")

                // refresh publication after DB write
                val updated = db.getPublicationBySystemPath(pub.systemPath)
                withContext(Dispatchers.Main) {
                    _publication.value = updated
                }
            } catch (e: Exception) {
                Log.e("ChapterScanner", "Failed to add chapters for ${pub.systemPath}", e)
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun fetchMetadata(pub: Publication){

    }

    fun handlePickedFolder(uri: Uri){
        getApplication<Application>().contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        addPublication(Configuration.selectedProfileId ?: "", uri)
    }
}