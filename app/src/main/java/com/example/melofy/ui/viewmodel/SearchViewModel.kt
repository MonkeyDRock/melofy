package com.example.melofy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melofy.domain.model.Song
import com.example.melofy.domain.repository.MusicRepository
import com.example.melofy.playback.MelofyMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val serviceConnection: MelofyMusicServiceConnection
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchType = MutableStateFlow("song") // song, album, artist
    val searchType: StateFlow<String> = _searchType.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Song?>(null)
    val selectedAlbum: StateFlow<Song?> = _selectedAlbum.asStateFlow()

    private val _albumTracks = MutableStateFlow<List<Song>>(emptyList())
    val albumTracks: StateFlow<List<Song>> = _albumTracks.asStateFlow()

    private val _isLoadingAlbumTracks = MutableStateFlow(false)
    val isLoadingAlbumTracks: StateFlow<Boolean> = _isLoadingAlbumTracks.asStateFlow()

    val playbackState = serviceConnection.playbackState

    val popularArtists = listOf(
        "Arijit Singh", "Taylor Swift", "The Weeknd", "Billie Eilish", 
        "Drake", "Post Malone", "Ed Sheeran", "Justin Bieber"
    )

    val searchSuggestions = listOf(
        "Lofi study beats", "Summer dance party", "Gym motivation workout", "Chill acoustic guitar"
    )

    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        
        if (newQuery.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce for 400ms to avoid slamming the iTunes API
            delay(400)
            executeSearch(newQuery)
        }
    }

    fun setSearchType(type: String) {
        _searchType.value = type
        if (_query.value.isNotBlank()) {
            executeSearch(_query.value)
        }
    }

    fun executeSearch(searchQuery: String) {
        _query.value = searchQuery
        _isLoading.value = true
        viewModelScope.launch {
            val entity = when (_searchType.value) {
                "album" -> "album"
                "artist" -> "musicArtist"
                else -> "song"
            }
            musicRepository.searchTracks(searchQuery, entity)
                .onSuccess { results ->
                    _searchResults.value = results
                }
                .onFailure {
                    _searchResults.value = emptyList()
                }
            _isLoading.value = false
        }
    }

    fun loadAlbumTracks(album: Song) {
        _selectedAlbum.value = album
        _albumTracks.value = emptyList()
        _isLoadingAlbumTracks.value = true
        viewModelScope.launch {
            val collectionId = album.id.toLongOrNull()
            if (collectionId != null) {
                musicRepository.getAlbumTracks(collectionId)
                    .onSuccess { tracks ->
                        _albumTracks.value = tracks
                    }
                    .onFailure {
                        _albumTracks.value = emptyList()
                    }
            }
            _isLoadingAlbumTracks.value = false
        }
    }

    fun clearSelectedAlbum() {
        _selectedAlbum.value = null
        _albumTracks.value = emptyList()
    }

    fun playSong(song: Song, queue: List<Song>) {
        viewModelScope.launch {
            val resolvedSong = musicRepository.resolveFullStream(song)
            musicRepository.saveRecentlyPlayed(resolvedSong)
            serviceConnection.playSong(resolvedSong, queue)
        }
    }
}
