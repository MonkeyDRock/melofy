package com.example.melofy.domain.model

import java.util.Objects

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val previewUrl: String,
    val durationMs: Long,
    val isFavorite: Boolean = false,
    val genre: String? = "",
    val releaseDate: String? = "",
    val trackViewUrl: String? = "",
    val fullStreamUrl: String? = ""
) {
    val highResArtworkUrl: String
        get() = (artworkUrl as String? ?: "").replace("100x100bb", "600x600bb")
    
    val medResArtworkUrl: String
        get() = (artworkUrl as String? ?: "").replace("100x100bb", "300x300bb")
    
    val releaseYear: String
        get() {
            val date = releaseDate ?: ""
            return if (date.length >= 4) date.substring(0, 4) else ""
        }

    /** Returns the best available playback URL: Jamendo full stream > iTunes preview */
    val playbackUrl: String
        get() {
            val stream = fullStreamUrl ?: ""
            return stream.ifBlank { previewUrl ?: "" }
        }

    /** True if a full-length Jamendo stream is available */
    val isFullTrack: Boolean
        get() = !fullStreamUrl.isNullOrBlank()

    /**
     * Returns a sanitized copy where null String fields are replaced with empty strings.
     * Needed because Gson bypasses Kotlin default values during deserialization.
     */
    fun sanitize(): Song = copy(
        genre = genre ?: "",
        releaseDate = releaseDate ?: "",
        trackViewUrl = trackViewUrl ?: "",
        fullStreamUrl = fullStreamUrl ?: ""
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Song) return false

        return (id as String?) == (other.id as String?) &&
                (title as String?) == (other.title as String?) &&
                (artist as String?) == (other.artist as String?) &&
                (album as String?) == (other.album as String?) &&
                (artworkUrl as String?) == (other.artworkUrl as String?) &&
                (previewUrl as String?) == (other.previewUrl as String?) &&
                durationMs == other.durationMs &&
                isFavorite == other.isFavorite &&
                genre == other.genre &&
                releaseDate == other.releaseDate &&
                trackViewUrl == other.trackViewUrl &&
                fullStreamUrl == other.fullStreamUrl
    }

    override fun hashCode(): Int {
        return Objects.hash(
            id, title, artist, album, artworkUrl, previewUrl,
            durationMs, isFavorite, genre, releaseDate, trackViewUrl, fullStreamUrl
        )
    }
}
