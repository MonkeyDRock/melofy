package com.example.melofy.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.melofy.di.IoDispatcher
import com.example.melofy.domain.model.Playlist
import com.example.melofy.domain.model.Song
import com.example.melofy.domain.repository.PlaylistRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences,
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlaylistRepository {

    private val gson = Gson()
    private val localFavsKey = "local_favorites"
    private val localPlaylistsKey = "local_playlists"
    private val localDownloadsKey = "local_downloads"

    // Fallback in-memory/SharedPreferences sets if Firebase throws/is not configured
    private var localFavorites: MutableSet<Song> = mutableSetOf()
    private var localPlaylists: MutableList<Playlist> = mutableListOf()
    private var localDownloads: MutableSet<Song> = mutableSetOf()

    init {
        loadLocalData()
    }

    private fun loadLocalData() {
        try {
            val favsJson = sharedPreferences.getString(localFavsKey, null)
            if (favsJson != null) {
                val type = object : TypeToken<List<Song>>() {}.type
                val favsList: List<Song>? = gson.fromJson(favsJson, type)
                localFavorites = favsList?.map { it.sanitize() }?.toMutableSet() ?: mutableSetOf()
            }

            val playlistsJson = sharedPreferences.getString(localPlaylistsKey, null)
            if (playlistsJson != null) {
                val type = object : TypeToken<List<Playlist>>() {}.type
                localPlaylists = gson.fromJson(playlistsJson, type) ?: mutableListOf()
            }

            val downloadsJson = sharedPreferences.getString(localDownloadsKey, null)
            if (downloadsJson != null) {
                val type = object : TypeToken<List<Song>>() {}.type
                val dlList: List<Song>? = gson.fromJson(downloadsJson, type)
                localDownloads = dlList?.map { it.sanitize() }?.toMutableSet() ?: mutableSetOf()
            }
        } catch (e: Exception) {
            // Old/corrupted data — clear it and start fresh
            e.printStackTrace()
            localFavorites = mutableSetOf()
            localPlaylists = mutableListOf()
            localDownloads = mutableSetOf()
            sharedPreferences.edit().apply {
                remove(localFavsKey)
                remove(localPlaylistsKey)
                remove(localDownloadsKey)
                apply()
            }
        }
    }

    private fun saveLocalData() {
        sharedPreferences.edit().apply {
            putString(localFavsKey, gson.toJson(localFavorites))
            putString(localPlaylistsKey, gson.toJson(localPlaylists))
            putString(localDownloadsKey, gson.toJson(localDownloads))
            apply()
        }
    }

    override suspend fun getPlaylists(): Result<List<Playlist>> = withContext(ioDispatcher) {
        runCatching {
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                try {
                    val snapshot = firestore.collection("users").document(uid).collection("playlists").get().await()
                    val remotePlaylists = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: "Unnamed"
                        val description = doc.getString("description") ?: ""
                        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        
                        // Fetch songs from nested collection or sub-field
                        val songsList = doc.get("songs") as? List<Map<String, Any>> ?: emptyList()
                        val songs = songsList.map { map ->
                            Song(
                                id = map["id"]?.toString() ?: "",
                                title = map["title"]?.toString() ?: "",
                                artist = map["artist"]?.toString() ?: "",
                                album = map["album"]?.toString() ?: "",
                                artworkUrl = map["artworkUrl"]?.toString() ?: "",
                                previewUrl = map["previewUrl"]?.toString() ?: "",
                                durationMs = (map["durationMs"] as? Number)?.toLong() ?: 0L,
                                isFavorite = isFavorite(map["id"]?.toString() ?: "")
                            )
                        }
                        Playlist(id, name, description, songs, createdAt)
                    }
                    
                    // Sync to local
                    localPlaylists = remotePlaylists.toMutableList()
                    saveLocalData()
                    
                    remotePlaylists
                } catch (e: Exception) {
                    localPlaylists
                }
            } else {
                localPlaylists
            }
        }
    }

    override suspend fun createPlaylist(name: String, description: String): Result<Playlist> = withContext(ioDispatcher) {
        runCatching {
            val uid = firebaseAuth.currentUser?.uid
            val newId = UUID.randomUUID().toString()
            val newPlaylist = Playlist(id = newId, name = name, description = description)
            
            localPlaylists.add(newPlaylist)
            saveLocalData()

            if (uid != null) {
                try {
                    firestore.collection("users").document(uid).collection("playlists").document(newId).set(newPlaylist).await()
                } catch (e: Exception) {
                    // Fail silently, use local cache
                }
            }
            newPlaylist
        }
    }

    override suspend fun deletePlaylist(playlistId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val uid = firebaseAuth.currentUser?.uid
            localPlaylists.removeAll { it.id == playlistId }
            saveLocalData()

            if (uid != null) {
                try {
                    firestore.collection("users").document(uid).collection("playlists").document(playlistId).delete().await()
                } catch (e: Exception) {
                    // Fail silently
                }
            }
        }
    }

    override suspend fun addSongToPlaylist(playlistId: String, song: Song): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val uid = firebaseAuth.currentUser?.uid
            val index = localPlaylists.indexOfFirst { it.id == playlistId }
            if (index != -1) {
                val playlist = localPlaylists[index]
                if (!playlist.songs.any { it.id == song.id }) {
                    val updatedSongs = playlist.songs + song.copy(isFavorite = isFavorite(song.id))
                    val updatedPlaylist = playlist.copy(songs = updatedSongs)
                    localPlaylists[index] = updatedPlaylist
                    saveLocalData()

                    if (uid != null) {
                        try {
                            firestore.collection("users").document(uid).collection("playlists").document(playlistId).set(updatedPlaylist).await()
                        } catch (e: Exception) {
                            // Fail silently
                        }
                    }
                }
            }
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val uid = firebaseAuth.currentUser?.uid
            val index = localPlaylists.indexOfFirst { it.id == playlistId }
            if (index != -1) {
                val playlist = localPlaylists[index]
                val updatedSongs = playlist.songs.filterNot { it.id == songId }
                val updatedPlaylist = playlist.copy(songs = updatedSongs)
                localPlaylists[index] = updatedPlaylist
                saveLocalData()

                if (uid != null) {
                    try {
                        firestore.collection("users").document(uid).collection("playlists").document(playlistId).set(updatedPlaylist).await()
                    } catch (e: Exception) {
                        // Fail silently
                    }
                }
            }
        }
    }

    override suspend fun getFavorites(): Result<List<Song>> = withContext(ioDispatcher) {
        runCatching {
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                try {
                    val snapshot = firestore.collection("users").document(uid).collection("favorites").get().await()
                    val remoteFavs = snapshot.documents.mapNotNull { doc ->
                        Song(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            artist = doc.getString("artist") ?: "",
                            album = doc.getString("album") ?: "",
                            artworkUrl = doc.getString("artworkUrl") ?: "",
                            previewUrl = doc.getString("previewUrl") ?: "",
                            durationMs = doc.getLong("durationMs") ?: 0L,
                            isFavorite = true
                        )
                    }
                    localFavorites = remoteFavs.toMutableSet()
                    saveLocalData()
                    remoteFavs
                } catch (e: Exception) {
                    localFavorites.toList()
                }
            } else {
                localFavorites.toList()
            }
        }
    }

    override suspend fun toggleFavorite(song: Song): Result<Boolean> = withContext(ioDispatcher) {
        runCatching {
            val uid = firebaseAuth.currentUser?.uid
            val currentlyFav = isFavorite(song.id)
            val nextState = !currentlyFav

            if (nextState) {
                localFavorites.add(song.copy(isFavorite = true))
            } else {
                localFavorites.removeAll { it.id == song.id }
            }
            saveLocalData()

            if (uid != null) {
                try {
                    val docRef = firestore.collection("users").document(uid).collection("favorites").document(song.id)
                    if (nextState) {
                        docRef.set(song.copy(isFavorite = true)).await()
                    } else {
                        docRef.delete().await()
                    }
                } catch (e: Exception) {
                    // Fail silently
                }
            }
            nextState
        }
    }

    override fun isFavorite(songId: String): Boolean {
        return localFavorites.any { it.id == songId }
    }

    override suspend fun getDownloadedSongs(): Result<List<Song>> = withContext(ioDispatcher) {
        runCatching {
            localDownloads.toList()
        }
    }

    override suspend fun downloadSong(song: Song): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            if (isDownloaded(song.id)) return@runCatching

            val request = Request.Builder().url(song.previewUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Failed to download: $response")

            val fileName = "download_${song.id}.mp3"
            val file = File(context.filesDir, fileName)
            response.body?.byteStream()?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            localDownloads.add(song)
            saveLocalData()
        }
    }

    override suspend fun deleteDownloadedSong(songId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val fileName = "download_${songId}.mp3"
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.delete()
            }
            localDownloads.removeAll { it.id == songId }
            saveLocalData()
        }
    }

    override fun isDownloaded(songId: String): Boolean {
        val fileName = "download_${songId}.mp3"
        val file = File(context.filesDir, fileName)
        return file.exists() && localDownloads.any { it.id == songId }
    }

    override fun getDownloadedSongFileUri(songId: String): String? {
        val fileName = "download_${songId}.mp3"
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            "file://${file.absolutePath}"
        } else {
            null
        }
    }
}
