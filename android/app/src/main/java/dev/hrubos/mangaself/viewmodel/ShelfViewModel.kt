package dev.hrubos.mangaself.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dev.hrubos.db.Chapter
import dev.hrubos.db.Database
import dev.hrubos.db.Publication
import dev.hrubos.mangaself.model.ApiResult
import dev.hrubos.mangaself.model.Configuration
import dev.hrubos.mangaself.model.MangaDexApi
import dev.hrubos.mangaself.model.filterAndSortChapters
import dev.hrubos.mangaself.model.filterAndSortChaptersIncludingUnnumbered
import dev.hrubos.mangaself.model.padNumbers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// NOTES
// manifest - android.permission.INTERNET
// launched effect - with content - url, response=URL(...).readText()!, json, ...
// exception handling - try catch / responseCode - if code ... else if code ... else ...
// api key security - do not save directly in code, save in local.properties (equal to .env)
//      BuildConfig.API_KEY
// optimize API calls - caching (HTTP cache, in-memory cache, App cache), background loading, debounce, throttle, e-tag

class ShelfViewModel(application: Application): AndroidViewModel(application) {

    private var _db: Database = Database(
        useLocal = Configuration.useLocalDB,
        application = application,
        mongoBaseUrl = Configuration.apiURL ?: ""
    )
    val db: Database get() = _db

    fun reinitializeDatabase() {
        _db = Database(
            useLocal = Configuration.useLocalDB,
            application = application,
            mongoBaseUrl = Configuration.apiURL ?: ""
        )
    }

    private val _publications = MutableStateFlow<List<Publication>>(emptyList())
    val publications: StateFlow<List<Publication>> = _publications.asStateFlow()

    // live field publication --- better for editing, just don't forget to call loadPublication
    private val _publication = MutableStateFlow<Publication?>(null)
    val publication: StateFlow<Publication?> = _publication

    private val _showFavourites = MutableStateFlow(false)
    val showFavourites: StateFlow<Boolean> = _showFavourites

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _descriptionState = MutableStateFlow<ApiResult<String>>(ApiResult.Idle)
    val descriptionState: StateFlow<ApiResult<String>> = _descriptionState

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage
    fun showStatusMessage(message: String) {
        viewModelScope.launch {
            _statusMessage.value = message
            delay(3000)
            _statusMessage.value = null
        }
    }


    // combined publications flow: favourites + search filter
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
            val pub = db.getPublicationBySystemPath(Configuration.selectedProfileId, systemPath)
            _publication.value = pub
        }
    }

    suspend fun getPublicationBySystemPath(systemPath: String): Publication? {
        Log.d("ShelfViewModel", "Loading publication with path $systemPath")
        return db.getPublicationBySystemPath(Configuration.selectedProfileId, systemPath)
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
                if(pub.systemPath.isNotBlank()){
                    scanChapters(pub)
                }else {
                Log.e("ShelfViewModel", "Cannot determine system path of added publication: ${rawUri}")
                }

                val description = MangaDexApi.fetchMangaDescription(pub.title)
                _descriptionState.value = description

                when (description) {
                    is ApiResult.Success -> {
                        editPublicationDescription(description.data)
                    }
                    is ApiResult.NotFound -> {
                        Log.w("Shelf", "Description not found")
                    }
                    is ApiResult.NetworkError -> {
                        Log.e("Shelf", "No internet", description.exception)
                    }
                    is ApiResult.HttpError -> {
                        Log.e("Shelf", "HTTP ${description.code}")
                    }
                    is ApiResult.UnknownError -> {
                        Log.e("Shelf", "Unknown error", description.exception)
                    }
                    else -> {}
                }

                val coverUri = findFirstImageInFirstChapter(uri)
                if (coverUri != null) {
                    db.editPublicationCover(Configuration.selectedProfileId, pub.systemPath, coverUri.toString())
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
                Log.e("ShelfViewModel", "Failed to delete all publications", e)
            }
        }
    }

    fun editPublicationTitle(title: String){
        val currentPub = _publication.value ?: return
        viewModelScope.launch {
            try {
                db.editPublication(
                    profileId = Configuration.selectedProfileId,
                    pubUri = currentPub.systemPath,
                    title = title,
                    description = currentPub.description
                )

                // refresh publication
                val updatedPub = db.getPublicationBySystemPath(Configuration.selectedProfileId, currentPub.systemPath)
                _publication.value = updatedPub

                _publications.value = _publications.value.map {
                    if (it.systemPath == updatedPub?.systemPath) updatedPub else it
                }
            } catch (e: Exception) {
                Log.e("ShelfViewModel", "Failed to update publication title", e)
            }
        }
    }

    fun editPublicationDescription(description: String){
        val currentPub = _publication.value ?: return
        viewModelScope.launch {
            try {
                db.editPublication(
                    profileId = Configuration.selectedProfileId,
                    pubUri = currentPub.systemPath,
                    title = currentPub.title,
                    description = description
                )

                // refresh publication
                val updatedPub = db.getPublicationBySystemPath(Configuration.selectedProfileId, currentPub.systemPath)
                _publication.value = updatedPub

                _publications.value = _publications.value.map {
                    if (it.systemPath == updatedPub?.systemPath) updatedPub else it
                }
            } catch (e: Exception) {
                Log.e("ShelfViewModel", "Failed to update publication description", e)
            }
        }
    }

    fun togglePublicationFavourite(pub: Publication){
        viewModelScope.launch {
            db.togglePublicationFavourite(Configuration.selectedProfileId, pub.systemPath, !pub.favourite)

            // update value immediately
            val updatedPub = db.getPublicationBySystemPath(Configuration.selectedProfileId, pub.systemPath)
            // meh this might be super inefficient ---> look into this
            _publications.value = _publications.value.map { (if(it.systemPath == pub.systemPath) updatedPub else it) as Publication }
        }
    }

    fun toggleShowFavourites() {
        _showFavourites.value = !_showFavourites.value
    }

    // Update search query
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateChapterLastRead(pub: Publication, chapter: Chapter, lastPage: Int){
        viewModelScope.launch {
            try {
                db.updateChapter(Configuration.selectedProfileId, pub, chapter, lastPage)

                val updatedPub = db.getPublicationBySystemPath(Configuration.selectedProfileId, pub.systemPath)
                _publication.value = updatedPub
            } catch (e: Exception) {
                Log.e("ShelfViewModel", "Failed to update chapter ${pub.title}/${chapter.title}", e)
            }
        }
    }

    /*********************
     * Scanner functions *
     ********************/

    private fun scanChapters(pub: Publication){
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
                .toList().filterAndSortChaptersIncludingUnnumbered() // TODO add user settings to either filter out unnumbered or keep them

            val chaptersList = mutableListOf<Chapter>()
            var position = 0

            for (dir in sortedDirs) {
                val pageFiles = dir.listFiles().filter { it.isFile } // only count files
                val pagesCount = pageFiles.size

                if (pagesCount == 0) {
                    Log.d("ChapterScanner", "Skipping empty chapter: ${dir.name}")
                    continue
                }

                val chapter = Chapter(
                    title = dir.name ?: "Untitled Chapter",
                    systemPath = dir.uri.toString(),
                    description = "",
                    position = position++,
                    pages = pagesCount,
                    pageLastRead = 0,
                    read = false
                )

                chaptersList.add(chapter)
            }

            try {
                db.setChaptersToPublication(Configuration.selectedProfileId, pub.systemPath, chaptersList)
                Log.d("ChapterScanner", "Finished scanning chapters: ${chaptersList.size} found")

                // refresh publication after DB write
                val updated = db.getPublicationBySystemPath(Configuration.selectedProfileId, pub.systemPath)
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

    fun rescanChapters(pub: Publication) {
        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            Log.d("ChapterScanner", "Rescanning chapters for ${pub.systemPath}")

            val folderUri = pub.systemPath.toUri()
            val docFolder = DocumentFile.fromTreeUri(getApplication(), folderUri)

            if (docFolder == null || !docFolder.isDirectory) {
                Log.e("ChapterScanner", "Invalid publication folder")
                _isScanning.value = false
                return@launch
            }

            val existingChapters = pub.chapters
            val existingSystemPaths = existingChapters.map { it.systemPath }.toSet()

            val sortedDirs = docFolder.listFiles()
                .toList()
                .filterAndSortChaptersIncludingUnnumbered() // TODO add user settings to either filter out unnumbered or keep them

            val currentPaths = sortedDirs.map { it.uri.toString() }.toSet()

            // remove invalid
            val chaptersToRemove = existingChapters.filter { it.systemPath !in currentPaths }
            if (chaptersToRemove.isNotEmpty()) {
                try {
                    db.removeChaptersOfPublication(Configuration.selectedProfileId, pub.systemPath, chaptersToRemove)
                    Log.d("ChapterScanner", "Removed ${chaptersToRemove.size} missing chapters from ${pub.systemPath}")
                } catch (e: Exception) {
                    Log.e("ChapterScanner", "Failed to remove missing chapters for ${pub.systemPath}", e)
                }
            }

            // add new
            val newChapters = mutableListOf<Chapter>()
            var position = pub.chapters.size // start after existing chapters

            for (dir in sortedDirs) {
                if (existingSystemPaths.contains(dir.uri.toString())) continue

                val pageFiles = dir.listFiles().filter { it.isFile }
                if (pageFiles.isEmpty()) {
                    Log.d("ChapterScanner", "Skipping empty chapter: ${dir.name}")
                    continue
                }

                val chapter = Chapter(
                    title = dir.name ?: "Untitled Chapter",
                    systemPath = dir.uri.toString(),
                    description = "",
                    position = position++,
                    pages = pageFiles.size,
                    pageLastRead = 0,
                    read = false
                )

                newChapters.add(chapter)
            }

            if (newChapters.isEmpty()) {
                Log.d("ChapterScanner", "No new chapters found for ${pub.systemPath}")
            } else {
                try {
                    db.addNewChaptersToPublication(Configuration.selectedProfileId, pub.systemPath, newChapters)
                    Log.d("ChapterScanner", "Added ${newChapters.size} new chapters to ${pub.systemPath}")
                } catch (e: Exception) {
                    Log.e("ChapterScanner", "Failed to add new chapters for ${pub.systemPath}", e)
                }
            }

            // refresh publication
            val updated = db.getPublicationBySystemPath(Configuration.selectedProfileId, pub.systemPath)
            withContext(Dispatchers.Main) {
                _publication.value = updated
            }

            _isScanning.value = false
        }
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
            .filter { it.isFile && it.name?.matches(Regex(".*\\.(jpg|jpeg|png|webp)$", RegexOption.IGNORE_CASE)) == true }
            .sortedBy { it.name?.padNumbers() }
            .firstOrNull()

        return firstImage?.uri
    }

    private fun updateChapterTitles(pub: Publication, mdChapters: List<Chapter>) {
        viewModelScope.launch {
            try {
                val titleMap = mdChapters.associateBy({ it.position }, { it.title })

                pub.chapters.forEach { localChapter ->
                    val newTitle = titleMap[localChapter.position]
                    if (!newTitle.isNullOrBlank() && newTitle != localChapter.title) {
                        db.updateChapter(
                            profileId = Configuration.selectedProfileId,
                            pub = pub,
                            chapter = localChapter.copy(title = newTitle),
                            lastRead = localChapter.pageLastRead // preserve lastRead
                        )
                    }
                }

                // refresh publication
                val updatedPub = db.getPublicationBySystemPath(Configuration.selectedProfileId, pub.systemPath)
                withContext(Dispatchers.Main) {
                    _publication.value = updatedPub
                    _publications.value = _publications.value.map { (if (it.systemPath == pub.systemPath) updatedPub else it)!! }
                }

            } catch (e: Exception) {
                Log.e("ShelfViewModel", "Failed to update chapter titles", e)
            }
        }
    }

    public fun fetchMetadata(pub: Publication){
        viewModelScope.launch {
            _descriptionState.value = ApiResult.Loading

            when (val result = MangaDexApi.fetchMangaByTitle(pub.title)) {
                is ApiResult.Idle -> { }
                is ApiResult.Success -> {
                    val attributes = result.data.optJSONObject("attributes")
                    val description = attributes?.optJSONObject("description")?.optString("en") ?: ""

                    _descriptionState.value = ApiResult.Success(description)
                    editPublicationDescription(description)

                    showStatusMessage("Metadata updated successfully.")
                }

                is ApiResult.NotFound -> {
                    _descriptionState.value = result
                    showStatusMessage("Manga not found on MangaDex. Try changing the Manga title")
                }

                is ApiResult.NetworkError -> {
                    _descriptionState.value = result
                    showStatusMessage("Network error. Check your internet connection.")
                }

                is ApiResult.HttpError -> {
                    _descriptionState.value = result
                    showStatusMessage("Server error (${result.code}). Try again later.")
                }

                is ApiResult.UnknownError -> {
                    _descriptionState.value = result
                    showStatusMessage("An unknown error occurred.")
                }

                ApiResult.Loading -> {}
            }
        }
    }

    public fun handlePickedFolder(uri: Uri){
        getApplication<Application>().contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        addPublication(Configuration.selectedProfileId, uri)
    }
}