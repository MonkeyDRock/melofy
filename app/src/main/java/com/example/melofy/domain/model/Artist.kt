package com.example.melofy.domain.model

data class Artist(
    val id: Long,
    val name: String,
    val artworkUrl: String,
    val genre: String
) {
    val highResArtworkUrl: String
        get() = artworkUrl.replace("100x100bb", "600x600bb")
}
