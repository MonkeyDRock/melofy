package com.example.melofy.data.model

import com.google.gson.annotations.SerializedName

data class JamendoResponse(
    @SerializedName("headers") val headers: JamendoHeaders,
    @SerializedName("results") val results: List<JamendoTrackDto>
)

data class JamendoHeaders(
    @SerializedName("status") val status: String,
    @SerializedName("code") val code: Int,
    @SerializedName("error_message") val errorMessage: String,
    @SerializedName("results_count") val resultsCount: Int
)

data class JamendoTrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("artist_id") val artistId: String,
    @SerializedName("artist_name") val artistName: String,
    @SerializedName("artist_idstr") val artistIdStr: String?,
    @SerializedName("album_name") val albumName: String?,
    @SerializedName("album_id") val albumId: String?,
    @SerializedName("album_image") val albumImage: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("audio") val audio: String,
    @SerializedName("audiodownload") val audioDownload: String?,
    @SerializedName("audiodownload_allowed") val audioDownloadAllowed: Boolean?,
    @SerializedName("releasedate") val releaseDate: String?,
    @SerializedName("license_ccurl") val licenseCcUrl: String?
)
