package com.example.melofy.data.model

import com.example.melofy.domain.model.Artist
import com.google.gson.annotations.SerializedName

data class ArtistDto(
    @SerializedName("artistId") val artistId: Long?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("artworkUrl100") val artworkUrl100: String?,
    @SerializedName("primaryGenreName") val primaryGenreName: String?,
    @SerializedName("artistLinkUrl") val artistLinkUrl: String?,
    @SerializedName("wrapperType") val wrapperType: String?
) {
    fun toArtist(): Artist {
        return Artist(
            id = artistId ?: 0L,
            name = artistName ?: "Unknown Artist",
            artworkUrl = artworkUrl100 ?: "",
            genre = primaryGenreName ?: "Music"
        )
    }
}
