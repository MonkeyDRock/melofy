package com.example.melofy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melofy.domain.model.Artist
import com.example.melofy.domain.model.Song
import com.example.melofy.domain.repository.MusicRepository
import com.example.melofy.domain.repository.AuthRepository
import com.example.melofy.playback.MelofyMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class GenreChip(val name: String, val emoji: String)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val authRepository: AuthRepository,
    private val serviceConnection: MelofyMusicServiceConnection
) : ViewModel() {

    private val _trendingTracks = MutableStateFlow<List<Song>>(emptyList())
    val trendingTracks: StateFlow<List<Song>> = _trendingTracks.asStateFlow()

    private val _recommendedTracks = MutableStateFlow<List<Song>>(emptyList())
    val recommendedTracks: StateFlow<List<Song>> = _recommendedTracks.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    private val _artistSpotlight = MutableStateFlow<List<Artist>>(emptyList())
    val artistSpotlight: StateFlow<List<Artist>> = _artistSpotlight.asStateFlow()

    private val _newReleases = MutableStateFlow<List<Song>>(emptyList())
    val newReleases: StateFlow<List<Song>> = _newReleases.asStateFlow()

    private val _personalizedCountry = MutableStateFlow("")
    val personalizedCountry: StateFlow<String> = _personalizedCountry.asStateFlow()

    private val _personalizedCountryTracks = MutableStateFlow<List<Song>>(emptyList())
    val personalizedCountryTracks: StateFlow<List<Song>> = _personalizedCountryTracks.asStateFlow()

    private val _personalizedArtist = MutableStateFlow("")
    val personalizedArtist: StateFlow<String> = _personalizedArtist.asStateFlow()

    private val _personalizedArtistTracks = MutableStateFlow<List<Song>>(emptyList())
    val personalizedArtistTracks: StateFlow<List<Song>> = _personalizedArtistTracks.asStateFlow()

    private val _personalizedGenre = MutableStateFlow("")
    val personalizedGenre: StateFlow<String> = _personalizedGenre.asStateFlow()

    private val _personalizedGenreTracks = MutableStateFlow<List<Song>>(emptyList())
    val personalizedGenreTracks: StateFlow<List<Song>> = _personalizedGenreTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val playbackState = serviceConnection.playbackState

    val genreChips = listOf(
        GenreChip("Pop", "🎤"),
        GenreChip("Hip-Hop", "🎧"),
        GenreChip("R&B", "🎷"),
        GenreChip("Rock", "🎸"),
        GenreChip("Electronic", "🎹"),
        GenreChip("Indie", "🎻"),
        GenreChip("Jazz", "🎺"),
        GenreChip("Classical", "🎼")
    )

    val greeting: String
        get() {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            }
        }

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        _isLoading.value = true
        viewModelScope.launch {
            // Load recently played
            _recentlyPlayed.value = musicRepository.getRecentlyPlayed()

            // Load trending
            musicRepository.getTrendingTracks()
                .onSuccess { tracks -> _trendingTracks.value = tracks }
            
            // Load recommended
            musicRepository.getRecommendedTracks()
                .onSuccess { tracks -> _recommendedTracks.value = tracks }

            // Load artist spotlight with real iTunes data
            musicRepository.getArtistSpotlight()
                .onSuccess { artists -> _artistSpotlight.value = artists }

            // Load new releases
            musicRepository.getNewReleases()
                .onSuccess { tracks -> _newReleases.value = tracks }

            // Load Personalized feeds based on user choices
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val country = user.country.trim()
                    if (country.isNotBlank()) {
                        _personalizedCountry.value = country
                        musicRepository.searchTracks(query = "$country top hits", entity = "song")
                            .onSuccess { tracks -> _personalizedCountryTracks.value = tracks.take(15) }
                    } else {
                        _personalizedCountryTracks.value = emptyList()
                    }

                    val artistsList = user.favoriteArtists.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    if (artistsList.isNotEmpty()) {
                        val firstArtist = artistsList.first()
                        _personalizedArtist.value = firstArtist
                        musicRepository.searchTracks(query = firstArtist, entity = "song")
                            .onSuccess { tracks -> _personalizedArtistTracks.value = tracks.take(15) }
                    } else {
                        _personalizedArtistTracks.value = emptyList()
                    }

                    val genresList = user.favoriteGenres.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    if (genresList.isNotEmpty()) {
                        val firstGenre = genresList.first()
                        _personalizedGenre.value = firstGenre
                        musicRepository.searchTracks(query = "$firstGenre hits", entity = "song")
                            .onSuccess { tracks -> _personalizedGenreTracks.value = tracks.take(15) }
                    } else {
                        _personalizedGenreTracks.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _isLoading.value = false
        }
    }

    fun playSong(song: Song, queue: List<Song>) {
        viewModelScope.launch {
            // Resolve full stream from Jamendo before playing
            val resolvedSong = musicRepository.resolveFullStream(song)
            musicRepository.saveRecentlyPlayed(resolvedSong)
            serviceConnection.playSong(resolvedSong, queue)
            // Reload recently played to sync immediately
            _recentlyPlayed.value = musicRepository.getRecentlyPlayed()
        }
    }

    fun togglePlayPause() {
        val state = playbackState.value
        if (state.isPlaying) {
            serviceConnection.pause()
        } else {
            serviceConnection.play()
        }
    }
}
