package com.example.melofy.domain.repository

import com.example.melofy.domain.model.Playlist
import com.example.melofy.domain.model.Song

interface PlaylistRepository {
    suspend fun getPlaylists(): Result<List<Playlist>>
    suspend fun createPlaylist(name: String, description: String): Result<Playlist>
    suspend fun deletePlaylist(playlistId: String): Result<Unit>
    suspend fun addSongToPlaylist(playlistId: String, song: Song): Result<Unit>
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit>
    suspend fun getFavorites(): Result<List<Song>>
    suspend fun toggleFavorite(song: Song): Result<Boolean>
    fun isFavorite(songId: String): Boolean
    suspend fun getDownloadedSongs(): Result<List<Song>>
    suspend fun downloadSong(song: Song): Result<Unit>
    suspend fun deleteDownloadedSong(songId: String): Result<Unit>
    fun isDownloaded(songId: String): Boolean
    fun getDownloadedSongFileUri(songId: String): String?
}
