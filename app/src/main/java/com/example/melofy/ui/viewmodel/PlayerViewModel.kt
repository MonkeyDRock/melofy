package com.example.melofy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melofy.domain.model.Song
import com.example.melofy.domain.repository.MusicRepository
import com.example.melofy.domain.repository.PlaylistRepository
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
class PlayerViewModel @Inject constructor(
    private val serviceConnection: MelofyMusicServiceConnection,
    private val playlistRepository: PlaylistRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val playbackState = serviceConnection.playbackState

    private val _isCurrentSongDownloaded = MutableStateFlow(false)
    val isCurrentSongDownloaded: StateFlow<Boolean> = _isCurrentSongDownloaded.asStateFlow()

    private val _sleepTimerSeconds = MutableStateFlow(0L)
    val sleepTimerSeconds: StateFlow<Long> = _sleepTimerSeconds.asStateFlow()

    private val _moreFromArtist = MutableStateFlow<List<Song>>(emptyList())
    val moreFromArtist: StateFlow<List<Song>> = _moreFromArtist.asStateFlow()

    private var timerJob: Job? = null
    private var lastLoadedArtist: String? = null

    init {
        viewModelScope.launch {
            playbackState.collect { state ->
                val songId = state.currentSong?.id
                _isCurrentSongDownloaded.value = if (songId != null) {
                    playlistRepository.isDownloaded(songId)
                } else {
                    false
                }
                // Load "More from this Artist" when the song changes
                val artist = state.currentSong?.artist
                if (artist != null && artist != lastLoadedArtist) {
                    lastLoadedArtist = artist
                    loadMoreFromArtist(artist, state.currentSong?.id)
                }
            }
        }
    }

    private fun loadMoreFromArtist(artist: String, currentSongId: String?) {
        viewModelScope.launch {
            musicRepository.searchTracks(artist)
                .onSuccess { tracks ->
                    _moreFromArtist.value = tracks
                        .filter { it.id != currentSongId }
                        .take(10)
                }
                .onFailure {
                    _moreFromArtist.value = emptyList()
                }
        }
    }

    val staticLyrics = listOf(
        "Oh, the rhythm is catching on...",
        "I feel every single beat in my soul,",
        "We are dancing through the night in neon colors,",
        "Letting go of all the worries that we hold.",
        "Oh Melofy, you set my heart on fire,",
        "Taking me higher, you're my one desire!",
        "Every lyric is a message in the wind,",
        "A brand new chapter is about to begin.",
        "So let the bass kick in and feel the vibe,",
        "It's a beautiful day to be alive!"
    )

    fun togglePlayPause() {
        val state = playbackState.value
        if (state.isPlaying) {
            serviceConnection.pause()
        } else {
            serviceConnection.play()
        }
    }

    fun skipToNext() {
        serviceConnection.skipToNext()
    }

    fun skipToPrevious() {
        serviceConnection.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        serviceConnection.seekTo(positionMs)
    }

    fun toggleShuffle() {
        serviceConnection.toggleShuffle()
    }

    fun toggleRepeat() {
        serviceConnection.toggleRepeat()
    }

    fun toggleFavoriteActiveSong() {
        val activeSong = playbackState.value.currentSong ?: return
        viewModelScope.launch {
            playlistRepository.toggleFavorite(activeSong)
            // Trigger a minor queue update to update favorited flags
            val currentQueue = playbackState.value.queue
            val updatedQueue = currentQueue.map {
                if (it.id == activeSong.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            // Connection updates automatically in background
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            val resolvedSong = musicRepository.resolveFullStream(song)
            musicRepository.saveRecentlyPlayed(resolvedSong)
            serviceConnection.playSong(resolvedSong, playbackState.value.queue + resolvedSong)
        }
    }

    fun startSleepTimer(minutes: Int) {
        timerJob?.cancel()
        _sleepTimerSeconds.value = minutes * 60L
        timerJob = viewModelScope.launch {
            while (_sleepTimerSeconds.value > 0) {
                delay(1000)
                _sleepTimerSeconds.value -= 1
            }
            // Pauses the player when timer expires
            serviceConnection.pause()
        }
    }

    fun cancelSleepTimer() {
        timerJob?.cancel()
        _sleepTimerSeconds.value = 0L
    }

    fun setPlaybackSpeed(speed: Float) {
        serviceConnection.setPlaybackSpeed(speed)
    }

    fun downloadActiveSong() {
        val activeSong = playbackState.value.currentSong ?: return
        viewModelScope.launch {
            playlistRepository.downloadSong(activeSong)
            _isCurrentSongDownloaded.value = playlistRepository.isDownloaded(activeSong.id)
        }
    }

    fun deleteActiveSongDownload() {
        val activeSong = playbackState.value.currentSong ?: return
        viewModelScope.launch {
            playlistRepository.deleteDownloadedSong(activeSong.id)
            _isCurrentSongDownloaded.value = false
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
