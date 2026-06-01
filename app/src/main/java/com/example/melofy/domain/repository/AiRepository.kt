package com.example.melofy.domain.repository

import com.example.melofy.domain.model.Song

interface AiRepository {
    /**
     * Sends the chat history of Pair(role, message) to Groq,
     * parses the response, executes iTunes searches in parallel,
     * and returns the AI's reply and a list of matching playable Songs.
     */
    suspend fun getAiSuggestions(
        messages: List<Pair<String, String>>
    ): Result<Pair<String, List<Song>>>
}
