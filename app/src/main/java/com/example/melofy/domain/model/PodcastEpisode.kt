package com.example.melofy.domain.model

data class PodcastEpisode(
    val id: String,          // YouTube Video ID (e.g. "Ff4fRgnuFgQ")
    val title: String,
    val description: String,
    val host: String,
    val category: String,
    val thumbnailUrl: String
)
