package com.example.melofy.data.model

import com.google.gson.annotations.SerializedName

data class ITunesSearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<TrackDto>
)
