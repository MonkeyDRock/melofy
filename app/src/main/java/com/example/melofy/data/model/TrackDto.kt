package com.example.melofy.data.model

import com.example.melofy.domain.model.Song
import com.google.gson.annotations.SerializedName

data class TrackDto(
    @SerializedName("trackId") val trackId: Long?,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("collectionName") val collectionName: String?,
    @SerializedName("artworkUrl100") val artworkUrl100: String?,
    @SerializedName("previewUrl") val previewUrl: String?,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long?,
    @SerializedName("collectionId") val collectionId: Long?,
    @SerializedName("artistId") val artistId: Long?,
    @SerializedName("primaryGenreName") val primaryGenreName: String?,
    @SerializedName("wrapperType") val wrapperType: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("collectionPrice") val collectionPrice: Double?,
    @SerializedName("trackPrice") val trackPrice: Double?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("trackViewUrl") val trackViewUrl: String?,
    @SerializedName("collectionViewUrl") val collectionViewUrl: String?,
    @SerializedName("artworkUrl60") val artworkUrl60: String?,
    @SerializedName("artworkUrl30") val artworkUrl30: String?,
    @SerializedName("trackCount") val trackCount: Int?,
    @SerializedName("trackNumber") val trackNumber: Int?,
    @SerializedName("discCount") val discCount: Int?,
    @SerializedName("discNumber") val discNumber: Int?,
    @SerializedName("country") val country: String?,
    @SerializedName("isStreamable") val isStreamable: Boolean?
) {
    fun toSong(isFavorite: Boolean = false): Song {
        return Song(
            id = (trackId ?: collectionId ?: 0L).toString(),
            title = trackName ?: collectionName ?: "Unknown Track",
            artist = artistName ?: "Unknown Artist",
            album = collectionName ?: "Unknown Album",
            artworkUrl = artworkUrl100 ?: "",
            previewUrl = previewUrl ?: "",
            durationMs = trackTimeMillis ?: 0L,
            isFavorite = isFavorite,
            genre = primaryGenreName ?: "",
            releaseDate = releaseDate ?: "",
            trackViewUrl = trackViewUrl ?: ""
        )
    }
}
