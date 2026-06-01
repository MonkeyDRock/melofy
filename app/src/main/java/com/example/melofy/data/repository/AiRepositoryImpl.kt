package com.example.melofy.data.repository

import com.example.melofy.data.api.GroqApi
import com.example.melofy.data.model.AiDjResponse
import com.example.melofy.data.model.GroqChatRequest
import com.example.melofy.data.model.GroqMessage
import com.example.melofy.data.model.GroqResponseFormat
import com.example.melofy.di.IoDispatcher
import com.example.melofy.domain.model.Song
import com.example.melofy.domain.repository.AiRepository
import com.example.melofy.domain.repository.MusicRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val groqApi: GroqApi,
    private val musicRepository: MusicRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AiRepository {

    private val gson = Gson()
    private val apiKey = "Bearer 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"

    private val systemPrompt = """
        You are Melofy AI DJ, a highly empathetic, expressive, and fun personal music curator inside the Melofy player.
        You have high emotional intelligence: analyze the user's sentiment, mood, and tone carefully.
        
        Guidelines for your 'reply':
        1. Adapt your tone and energy to match the user's mood. If they are excited, match their hype! If they are sad, be warm, comforting, and supportive. If they are stressed, be soothing.
        2. Express real emotion, use music-related metaphors, and use highly descriptive, encouraging words.
        3. Keep your response engaging, personal, and concise (approx. 2 to 3 sentences), ending with a warm signature vibe or question.
        4. Include relevant emojis that match the emotional tone.
        
        Guidelines for 'searchQueries':
        Generate 1 to 3 relevant, specific search queries (e.g. specific artists, track names, or targeted genre/mood search terms like "lofi sad slow", "party dance pop electro") to fetch actual playable tracks from the iTunes API.
        
        You MUST respond in JSON format with the following schema:
        {
          "reply": "Your emotionally resonant, empathetic response to the user.",
          "searchQueries": ["query 1", "query 2"]
        }
    """.trimIndent()

    override suspend fun getAiSuggestions(
        messages: List<Pair<String, String>>
    ): Result<Pair<String, List<Song>>> = withContext(ioDispatcher) {
        runCatching {
            // Build message list starting with system prompt
            val groqMessages = mutableListOf<GroqMessage>()
            groqMessages.add(GroqMessage(role = "system", content = systemPrompt))
            
            messages.forEach { (role, content) ->
                groqMessages.add(GroqMessage(role = role, content = content))
            }

            // Create request with json_object response format
            val request = GroqChatRequest(
                model = "llama-3.3-70b-versatile",
                messages = groqMessages,
                responseFormat = GroqResponseFormat(type = "json_object")
            )

            val response = groqApi.getChatCompletion(
                authorization = apiKey,
                request = request
            )

            val rawJson = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("No response from Groq AI DJ")

            val parsedResponse = gson.fromJson(rawJson, AiDjResponse::class.java)
                ?: throw Exception("Failed to parse Groq AI DJ response")

            val reply = parsedResponse.reply
            val queries = parsedResponse.searchQueries

            // Fetch songs in parallel for each query
            val searchResults = queries.map { query ->
                async {
                    musicRepository.searchTracks(query)
                        .getOrDefault(emptyList())
                }
            }.awaitAll()

            // Flatten, deduplicate by ID, and shuffle/limit results
            val songs = searchResults.flatten()
                .distinctBy { it.id }
                .shuffled()
                .take(15)

            Pair(reply, songs)
        }.onFailure { err ->
            android.util.Log.e("AiRepository", "Error getting suggestions from Groq AI", err)
        }
    }
}
