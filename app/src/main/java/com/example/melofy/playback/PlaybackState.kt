package com.example.melofy.playback

import com.example.melofy.domain.model.Song

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val isRepeatEnabled: Boolean = false,
    val repeatMode: Int = 0,
    val queue: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val playbackSpeed: Float = 1.0f
)
