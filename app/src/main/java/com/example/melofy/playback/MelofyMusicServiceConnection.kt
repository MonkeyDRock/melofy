package com.example.melofy.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.melofy.domain.model.Song
import com.example.melofy.domain.repository.PlaylistRepository
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MelofyMusicServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistRepository: PlaylistRepository
) {
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var mediaController: MediaController? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MelofyPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get().apply {
                    addListener(PlayerListener())
                    updatePlaybackState()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    fun playSong(song: Song, queue: List<Song>) {
        val controller = mediaController ?: return
        
        // Prepare list of media items
        val mediaItems = queue.map { item ->
            val metadata = MediaMetadata.Builder()
                .setTitle(item.title)
                .setArtist(item.artist)
                .setAlbumTitle(item.album)
                .setArtworkUri(android.net.Uri.parse(item.highResArtworkUrl))
                .build()

            val playbackUri = if (playlistRepository.isDownloaded(item.id)) {
                playlistRepository.getDownloadedSongFileUri(item.id) ?: item.playbackUrl
            } else {
                item.playbackUrl
            }

            MediaItem.Builder()
                .setMediaId(item.id)
                .setUri(android.net.Uri.parse(playbackUri))
                .setMediaMetadata(metadata)
                .build()
        }

        controller.setMediaItems(mediaItems)
        val targetIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        
        // Set state queue immediately to avoid UI lags
        _playbackState.update { it.copy(queue = queue, currentSong = song) }

        controller.seekTo(targetIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val currentMode = controller.repeatMode
        controller.repeatMode = when (currentMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        updatePlaybackState()
    }

    fun setPlaybackSpeed(speed: Float) {
        val controller = mediaController ?: return
        controller.setPlaybackSpeed(speed)
        updatePlaybackState()
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _playbackState.update {
                            it.copy(
                                currentPositionMs = controller.currentPosition,
                                durationMs = controller.duration.coerceAtLeast(0L)
                            )
                        }
                    }
                }
                delay(400)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    private fun updatePlaybackState() {
        val controller = mediaController ?: return
        val currentMediaId = controller.currentMediaItem?.mediaId
        
        val activeSong = _playbackState.value.queue.find { it.id == currentMediaId } 
            ?: controller.currentMediaItem?.let { item ->
                Song(
                    id = item.mediaId,
                    title = item.mediaMetadata.title?.toString() ?: "Unknown Title",
                    artist = item.mediaMetadata.artist?.toString() ?: "Unknown Artist",
                    album = item.mediaMetadata.albumTitle?.toString() ?: "Unknown Album",
                    artworkUrl = item.mediaMetadata.artworkUri?.toString() ?: "",
                    previewUrl = "", // Set in playSong
                    durationMs = controller.duration
                )
            }

        _playbackState.update {
            it.copy(
                isPlaying = controller.isPlaying,
                currentSong = activeSong,
                durationMs = controller.duration.coerceAtLeast(0L),
                currentPositionMs = controller.currentPosition,
                isShuffleEnabled = controller.shuffleModeEnabled,
                isRepeatEnabled = controller.repeatMode != Player.REPEAT_MODE_OFF,
                repeatMode = controller.repeatMode,
                isLoading = controller.playbackState == Player.STATE_BUFFERING,
                playbackSpeed = controller.playbackParameters.speed
            )
        }

        if (controller.isPlaying) {
            startProgressUpdate()
        } else {
            stopProgressUpdate()
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlaybackState()
        }

        override fun onPlaybackStateChanged(state: Int) {
            updatePlaybackState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updatePlaybackState()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            updatePlaybackState()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            updatePlaybackState()
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            updatePlaybackState()
        }
    }
}
