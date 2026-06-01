package com.example.melofy.data.model

import com.google.gson.annotations.SerializedName

data class GroqChatRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7,
    @SerializedName("response_format") val responseFormat: GroqResponseFormat? = null
)

data class GroqMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

data class GroqResponseFormat(
    val type: String // "json_object"
)

data class GroqChatResponse(
    val id: String,
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val index: Int,
    val message: GroqMessage,
    @SerializedName("finish_reason") val finishReason: String
)

/**
 * Representation of the structured JSON output returned by the AI DJ.
 */
data class AiDjResponse(
    val reply: String,
    val searchQueries: List<String>
)
