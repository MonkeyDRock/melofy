package com.example.melofy.domain.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val avatar: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val country: String = "",
    val favoriteArtists: String = "",
    val favoriteGenres: String = ""
)
