package com.example.melofy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melofy.domain.model.Playlist
import com.example.melofy.domain.model.Song
import com.example.melofy.domain.repository.MusicRepository
import com.example.melofy.domain.repository.PlaylistRepository
import com.example.melofy.playback.MelofyMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val musicRepository: MusicRepository,
    private val serviceConnection: MelofyMusicServiceConnection
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<Song>>(emptyList())
    val favorites: StateFlow<List<Song>> = _favorites.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _downloadedSongs = MutableStateFlow<List<Song>>(emptyList())
    val downloadedSongs: StateFlow<List<Song>> = _downloadedSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val playbackState = serviceConnection.playbackState

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        _isLoading.value = true
        viewModelScope.launch {
            // Load favorites
            playlistRepository.getFavorites()
                .onSuccess { list ->
                    _favorites.value = list
                }
            
            // Load playlists
            playlistRepository.getPlaylists()
                .onSuccess { list ->
                    _playlists.value = list
                }

            // Load downloaded songs
            playlistRepository.getDownloadedSongs()
                .onSuccess { list ->
                    _downloadedSongs.value = list
                }
            
            _isLoading.value = false
        }
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name, description)
                .onSuccess {
                    loadLibrary()
                }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
                .onSuccess {
                    loadLibrary()
                }
        }
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, song)
                .onSuccess {
                    loadLibrary()
                }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
                .onSuccess {
                    loadLibrary()
                }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            playlistRepository.toggleFavorite(song)
            loadLibrary()
        }
    }

    fun playSong(song: Song, queue: List<Song>) {
        viewModelScope.launch {
            val resolvedSong = musicRepository.resolveFullStream(song)
            serviceConnection.playSong(resolvedSong, queue)
        }
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch {
            playlistRepository.downloadSong(song)
            loadLibrary()
        }
    }

    fun deleteDownloadedSong(songId: String) {
        viewModelScope.launch {
            playlistRepository.deleteDownloadedSong(songId)
            loadLibrary()
        }
    }
}
