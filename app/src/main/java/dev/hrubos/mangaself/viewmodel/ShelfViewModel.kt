package dev.hrubos.mangaself.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.hrubos.db.Database
import dev.hrubos.db.Publication
import dev.hrubos.mangaself.model.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        val rawUri = "${uri.scheme}://${uri.authority}${uri.path}" // needed because uri.path is not enough and uri.toString() encodes
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
        Log.d("ShelfViewModel", "Scanning chapters for ${pub.systemPath}")
    }

    fun fetchMetadata(pub: Publication){

    }
}