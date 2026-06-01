package com.example.melofy.data.api

import com.example.melofy.data.model.JamendoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApi {

    @GET("tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String = "51cd2603",
        @Query("format") format: String = "json",
        @Query("namesearch") trackName: String,
        @Query("artist_name") artistName: String = "",
        @Query("limit") limit: Int = 5,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("include") include: String = "musicinfo"
    ): JamendoResponse
}
