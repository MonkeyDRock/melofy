package com.example.melofy.domain.repository

import com.example.melofy.domain.model.Artist
import com.example.melofy.domain.model.Song

interface MusicRepository {
    suspend fun searchTracks(query: String, entity: String = "song"): Result<List<Song>>
    suspend fun getTrendingTracks(): Result<List<Song>>
    suspend fun getRecommendedTracks(): Result<List<Song>>
    suspend fun getTracksByMood(mood: String): Result<List<Song>>
    suspend fun getRecentlyPlayed(): List<Song>
    suspend fun saveRecentlyPlayed(song: Song)
    suspend fun getAlbumTracks(collectionId: Long): Result<List<Song>>
    suspend fun getArtistSpotlight(): Result<List<Artist>>
    suspend fun getNewReleases(): Result<List<Song>>

    /**
     * Checks Jamendo for a matching full-length stream.
     * Returns the song with [Song.fullStreamUrl] populated if found,
     * or the original song unchanged if no Jamendo match exists.
     */
    suspend fun resolveFullStream(song: Song): Song
}
