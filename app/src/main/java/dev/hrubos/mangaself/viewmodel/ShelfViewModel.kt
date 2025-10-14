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
import dev.hrubos.mangaself.model.filterAndSortChapters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _showFavourites = MutableStateFlow(false)
    val showFavourites: StateFlow<Boolean> = _showFavourites

    private val _searchQuery = MutableStateFlow("") // new: text filter
    val searchQuery: StateFlow<String> = _searchQuery

    // Combined publications flow: favourites + search filter
    val filteredPublications: StateFlow<List<Publication>> = combine(
        _publications,
        _showFavourites,
        _searchQuery
    ) { pubs, favouritesOnly, query ->
        pubs.filter { pub ->
            (!favouritesOnly || pub.favourite) &&
                    (query.isBlank() || pub.title.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
        val rawUri = uri.toString()
        val docFile = DocumentFile.fromTreeUri(getApplication(), uri)
        val dirName = docFile?.name ?: "Unknown"

        viewModelScope.launch {
            try {
                Log.d("ShelfViewModel", "Adding publication with path ${rawUri}")
                val pub = db.addPublication(profileId, rawUri, title = dirName)
                scanChapters(pub)

                val coverUri = findFirstImageInFirstChapter(uri)
                if (coverUri != null) {
                    db.editPublicationCover(pub.systemPath, coverUri.toString())
                    Log.d("ShelfViewModel", "Set cover to $coverUri")
                } else {
                    Log.d("ShelfViewModel", "No cover image found for publication ${pub.systemPath}")
                }
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

    fun togglePublicationFavourite(pub: Publication){
        viewModelScope.launch {
            db.togglePublicationFavourite(pub.systemPath, !pub.favourite)

            // update value immediately
            val updatedPub = db.getPublicationBySystemPath(pub.systemPath)
            // meh this might be super inefficient ---> look into this
            _publications.value = _publications.value.map { if(it.systemPath == pub.systemPath) updatedPub else it }
        }
    }

    fun toggleShowFavourites() {
        _showFavourites.value = !_showFavourites.value
    }

    // Update search query
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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

            fun compareNumberLists(a: List<Int>, b: List<Int>): Int {
                val minLength = minOf(a.size, b.size)
                for (i in 0 until minLength) {
                    if (a[i] != b[i]) return a[i] - b[i]
                }
                return a.size - b.size
            }

            val sortedDirs = docFolder.listFiles()
                .toList().filterAndSortChapters()

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
                    systemPath = dir.uri.toString()
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

    private fun findFirstImageInFirstChapter(pubUri: Uri): Uri? {
        val context = getApplication<Application>()
        val root = DocumentFile.fromTreeUri(context, pubUri) ?: return null

        // sort subfolders
        val chapters = root.listFiles()
            .toList().filterAndSortChapters()

        val firstChapter = chapters.firstOrNull() ?: return null

        // find first image file
        val firstImage = firstChapter.listFiles()
            .filter { it.isFile }
            .firstOrNull { it.name?.endsWith(".jpg", true) == true ||
                    it.name?.endsWith(".jpeg", true) == true ||
                    it.name?.endsWith(".png", true) == true ||
                    it.name?.endsWith(".webp", true) == true }

        return firstImage?.uri
    }



    fun handlePickedFolder(uri: Uri){
        getApplication<Application>().contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        addPublication(Configuration.selectedProfileId ?: "", uri)
    }
}