package com.example.melofy.data.api

import com.example.melofy.data.model.GroqChatRequest
import com.example.melofy.data.model.GroqChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {

    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqChatRequest
    ): GroqChatResponse
}
