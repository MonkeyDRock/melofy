package com.example.melofy.data.api

import com.example.melofy.data.model.ITunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesSearchApi {

    @GET("search")
    suspend fun searchTracks(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 40
    ): ITunesSearchResponse

    @GET("lookup")
    suspend fun lookupAlbumSongs(
        @Query("id") collectionId: Long,
        @Query("entity") entity: String = "song"
    ): ITunesSearchResponse

    @GET("search")
    suspend fun searchArtists(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "musicArtist",
        @Query("limit") limit: Int = 1
    ): ITunesSearchResponse
}
