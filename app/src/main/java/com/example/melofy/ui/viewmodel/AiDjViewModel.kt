package com.example.melofy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melofy.domain.model.Song
import com.example.melofy.domain.repository.AiRepository
import com.example.melofy.domain.repository.MusicRepository
import com.example.melofy.playback.MelofyMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class AiDjViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val musicRepository: MusicRepository,
    private val serviceConnection: MelofyMusicServiceConnection
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hey! I'm your Melofy AI DJ. Tell me what mood, genre, or artist you want to listen to, or ask me for a custom playlist vibe! ✨",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val playbackState = serviceConnection.playbackState

    val suggestionChips = listOf(
        "Late night study lofi 📚",
        "High energy workout mix ⚡",
        "Acoustic coffee shop vibes ☕",
        "Melancholic rainy day indie 🌧️",
        "Neon future synthwave 🌌"
    )

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage(text = text, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        
        _isThinking.value = true
        _error.value = null
        
        viewModelScope.launch {
            // Build conversation history (map to Pair(role, message))
            val history = _chatMessages.value.map { msg ->
                val role = if (msg.isUser) "user" else "assistant"
                role to msg.text
            }
            
            aiRepository.getAiSuggestions(history)
                .onSuccess { (reply, songs) ->
                    val aiMsg = ChatMessage(
                        text = reply,
                        isUser = false,
                        songs = songs
                    )
                    _chatMessages.value = _chatMessages.value + aiMsg
                }
                .onFailure { err ->
                    _error.value = err.message
                    val errorMsg = ChatMessage(
                        text = "Sorry, I ran into an error connecting to my mix tables. Please try again! 🎧",
                        isUser = false
                    )
                    _chatMessages.value = _chatMessages.value + errorMsg
                }
            _isThinking.value = false
        }
    }

    fun playSong(song: Song, queue: List<Song>) {
        viewModelScope.launch {
            val resolvedSong = musicRepository.resolveFullStream(song)
            musicRepository.saveRecentlyPlayed(resolvedSong)
            serviceConnection.playSong(resolvedSong, queue)
        }
    }
}
