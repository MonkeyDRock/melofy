package com.example.melofy.domain.model

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val songs: List<Song> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val songCount: Int
        get() = songs.size
}
