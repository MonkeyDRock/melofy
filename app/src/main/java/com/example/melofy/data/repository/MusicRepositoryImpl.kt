package com.example.melofy.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.melofy.data.api.ITunesSearchApi
import com.example.melofy.data.api.JamendoApi
import com.example.melofy.di.IoDispatcher
import com.example.melofy.domain.model.Artist
import com.example.melofy.domain.model.Song
import com.example.melofy.domain.repository.MusicRepository
import com.example.melofy.domain.repository.PlaylistRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val api: ITunesSearchApi,
    private val jamendoApi: JamendoApi,
    private val playlistRepository: PlaylistRepository,
    private val sharedPreferences: SharedPreferences,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MusicRepository {

    private val gson = Gson()
    private val recentlyPlayedKey = "recently_played_songs"

    // In-memory cache for resolved Jamendo URLs to avoid redundant API calls
    private val jamendoCache = mutableMapOf<String, String>()

    private val spotlightArtistNames = listOf(
        "Taylor Swift", "Ed Sheeran", "The Weeknd",
        "Billie Eilish", "Drake", "Bruno Mars",
        "Ariana Grande", "Dua Lipa", "Post Malone",
        "Bad Bunny", "Arijit Singh", "BTS"
    )

    override suspend fun searchTracks(query: String, entity: String): Result<List<Song>> = withContext(ioDispatcher) {
        runCatching {
            val response = api.searchTracks(term = query, entity = entity)
            response.results
                .filter { 
                    (entity == "song" && it.trackId != null && !it.previewUrl.isNullOrEmpty()) || 
                    (entity != "song" && (it.collectionId != null || it.artistId != null))
                }
                .map { dto ->
                    dto.toSong(isFavorite = playlistRepository.isFavorite((dto.trackId ?: dto.collectionId ?: dto.artistId ?: 0L).toString()))
                }
        }
    }

    override suspend fun getTrendingTracks(): Result<List<Song>> = withContext(ioDispatcher) {
        runCatching {
            // Search popular current billboard/trending terms
            val response = api.searchTracks(term = "trending pop hits", limit = 25)
            response.results
                .filter { it.trackId != null && !it.previewUrl.isNullOrEmpty() }
                .map { dto ->
                    dto.toSong(isFavorite = playlistRepository.isFavorite(dto.trackId.toString()))
                }
        }
    }

    override suspend fun getRecommendedTracks(): Result<List<Song>> = withContext(ioDispatcher) {
        runCatching {
            val recent = getRecentlyPlayed()
            val seedTerm = if (recent.isNotEmpty()) {
                recent.take(5).map { it.artist }.filter { it.isNotBlank() }.shuffled().firstOrNull() ?: "acoustic chill"
            } else {
                "acoustic chill"
            }
            val response = api.searchTracks(term = seedTerm, limit = 20)
            response.results
                .filter { it.trackId != null && !it.previewUrl.isNullOrEmpty() }
                .map { dto ->
                    dto.toSong(isFavorite = playlistRepository.isFavorite(dto.trackId.toString()))
                }
        }
    }

    override suspend fun getTracksByMood(mood: String): Result<List<Song>> = withContext(ioDispatcher) {
        runCatching {
            val searchTerm = when (mood.lowercase()) {
                "chill" -> "lofi chill"
                "workout" -> "workout energy"
                "party" -> "dance electro"
                "focus" -> "ambient instrumental"
                "romance" -> "love acoustic"
                else -> mood
            }
            val response = api.searchTracks(term = searchTerm, limit = 20)
            response.results
                .filter { it.trackId != null && !it.previewUrl.isNullOrEmpty() }
                .map { dto ->
                    dto.toSong(isFavorite = playlistRepository.isFavorite(dto.trackId.toString()))
                }
        }
    }

    override suspend fun getAlbumTracks(collectionId: Long): Result<List<Song>> = withContext(ioDispatcher) {
        runCatching {
            val response = api.lookupAlbumSongs(collectionId = collectionId)
            response.results
                .filter { (it.wrapperType == "track" || it.trackId != null) && !it.previewUrl.isNullOrEmpty() }
                .map { dto ->
                    dto.toSong(isFavorite = playlistRepository.isFavorite(dto.trackId.toString()))
                }
        }
    }

    override suspend fun getArtistSpotlight(): Result<List<Artist>> = withContext(ioDispatcher) {
        runCatching {
            coroutineScope {
                val deferredArtists = spotlightArtistNames.map { artistName ->
                    async {
                        try {
                            val response = api.searchTracks(term = artistName, limit = 1)
                            val dto = response.results.firstOrNull()
                            if (dto != null && dto.artistId != null) {
                                Artist(
                                    id = dto.artistId,
                                    name = dto.artistName ?: artistName,
                                    artworkUrl = dto.artworkUrl100 ?: "",
                                    genre = dto.primaryGenreName ?: "Music"
                                )
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                deferredArtists.awaitAll().filterNotNull()
            }
        }
    }

    override suspend fun getNewReleases(): Result<List<Song>> = withContext(ioDispatcher) {
        runCatching {
            val response = api.searchTracks(term = "new music 2025", limit = 15)
            response.results
                .filter { it.trackId != null && !it.previewUrl.isNullOrEmpty() }
                .map { dto ->
                    dto.toSong(isFavorite = playlistRepository.isFavorite(dto.trackId.toString()))
                }
        }
    }

    override suspend fun getRecentlyPlayed(): List<Song> = withContext(ioDispatcher) {
        val json = sharedPreferences.getString(recentlyPlayedKey, null) ?: ""
        val type = object : TypeToken<List<Song>>() {}.type
        val localList: List<Song> = try {
            if (json.isNotBlank()) {
                (gson.fromJson<List<Song>>(json, type) ?: emptyList()).map { it.sanitize() }
            } else emptyList()
        } catch (e: Exception) {
            // Clear corrupted data
            sharedPreferences.edit().remove(recentlyPlayedKey).apply()
            emptyList()
        }
        
        try {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("recently_played")
                    .orderBy("playedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .await()
                val firestoreSongs = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: return@mapNotNull null
                    Song(
                        id = id,
                        title = doc.getString("title") ?: "",
                        artist = doc.getString("artist") ?: "",
                        album = doc.getString("album") ?: "",
                        artworkUrl = doc.getString("artworkUrl") ?: "",
                        previewUrl = doc.getString("previewUrl") ?: "",
                        durationMs = doc.getLong("durationMs") ?: 0L,
                        isFavorite = playlistRepository.isFavorite(id)
                    )
                }
                if (firestoreSongs.isNotEmpty()) {
                    val updatedJson = gson.toJson(firestoreSongs)
                    sharedPreferences.edit().putString(recentlyPlayedKey, updatedJson).apply()
                    return@withContext firestoreSongs
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        localList.map { it.copy(isFavorite = playlistRepository.isFavorite(it.id)) }
    }

    override suspend fun saveRecentlyPlayed(song: Song) = withContext(ioDispatcher) {
        val currentList = getRecentlyPlayed().toMutableList()
        currentList.removeAll { it.id == song.id }
        currentList.add(0, song)
        if (currentList.size > 20) {
            currentList.removeAt(currentList.lastIndex)
        }
        val json = gson.toJson(currentList)
        sharedPreferences.edit().putString(recentlyPlayedKey, json).apply()

        try {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                val songMap = mapOf(
                    "id" to song.id,
                    "title" to song.title,
                    "artist" to song.artist,
                    "album" to song.album,
                    "artworkUrl" to song.artworkUrl,
                    "previewUrl" to song.previewUrl,
                    "durationMs" to song.durationMs,
                    "playedAt" to System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(userId)
                    .collection("recently_played")
                    .document(song.id)
                    .set(songMap)
                    .await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun resolveFullStream(song: Song): Song = withContext(ioDispatcher) {
        // Return immediately if already resolved
        if (!song.fullStreamUrl.isNullOrBlank()) return@withContext song

        // Check cache first
        val cachedUrl = jamendoCache[song.id]
        if (cachedUrl != null) {
            return@withContext song.copy(fullStreamUrl = cachedUrl)
        }

        try {
            val response = jamendoApi.searchTracks(
                trackName = song.title,
                artistName = song.artist,
                limit = 5
            )

            if (response.headers.status == "success" && response.results.isNotEmpty()) {
                // Find the best matching track using fuzzy matching
                val match = response.results.firstOrNull { jamendoTrack ->
                    val titleMatch = jamendoTrack.name.contains(song.title, ignoreCase = true) ||
                            song.title.contains(jamendoTrack.name, ignoreCase = true)
                    val artistMatch = jamendoTrack.artistName.contains(song.artist, ignoreCase = true) ||
                            song.artist.contains(jamendoTrack.artistName, ignoreCase = true)
                    titleMatch && artistMatch
                } ?: response.results.firstOrNull { jamendoTrack ->
                    // Fallback: just match on track name
                    jamendoTrack.name.contains(song.title, ignoreCase = true) ||
                            song.title.contains(jamendoTrack.name, ignoreCase = true)
                }

                if (match != null && match.audio.isNotBlank()) {
                    val streamUrl = match.audio
                    jamendoCache[song.id] = streamUrl
                    Log.d("MusicRepo", "Jamendo match found for '${song.title}': ${match.name} by ${match.artistName}")
                    return@withContext song.copy(
                        fullStreamUrl = streamUrl,
                        durationMs = if (match.duration > 0) match.duration * 1000L else song.durationMs
                    )
                }
            }

            Log.d("MusicRepo", "No Jamendo match for '${song.title}' — using iTunes preview")
        } catch (e: Exception) {
            Log.e("MusicRepo", "Jamendo lookup failed for '${song.title}': ${e.message}")
        }

        song
    }
}
