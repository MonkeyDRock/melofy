package com.example.melofy.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import com.example.melofy.ui.components.MusicVisualizer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.melofy.domain.model.Song
import com.example.melofy.ui.components.GlassmorphicCard
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import com.example.melofy.ui.theme.Surface
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary
import com.example.melofy.ui.viewmodel.AiDjViewModel
import com.example.melofy.ui.viewmodel.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDjScreen(
    viewModel: AiDjViewModel,
    onBackClick: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Glowing animation for background elements
    val glowTransition = rememberInfiniteTransition(label = "screen_glow")
    val glowOffset by glowTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .drawBehind {
                // Neon glow background vibes
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(0.12f), Color.Transparent),
                        radius = size.maxDimension * 0.5f
                    ),
                    center = this.center.copy(x = this.center.x + glowOffset, y = this.center.y - 200f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Secondary.copy(0.10f), Color.Transparent),
                        radius = size.maxDimension * 0.5f
                    ),
                    center = this.center.copy(x = this.center.x - glowOffset, y = this.center.y + 300f)
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ═══════════════════════════════════════════════════
            // 1. HEADER
            // ═══════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 4.dp)
                )
                
                Column {
                    Text(
                        text = "Melofy AI DJ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "Powered by Groq Llama 3 AI",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Accent,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // ═══════════════════════════════════════════════════
            // 2. CONVERSATION MESSAGES LIST
            // ═══════════════════════════════════════════════════
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        currentSongId = playbackState.currentSong?.id,
                        isPlaying = playbackState.isPlaying,
                        onPlaySong = { song -> viewModel.playSong(song, msg.songs) }
                    )
                }

                if (isThinking) {
                    item {
                        ThinkingBubble()
                    }
                }
            }

            // ═══════════════════════════════════════════════════
            // 3. BOTTOM PANEL: SUGGESTIONS + INPUT
            // ═══════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(bottom = 16.dp)
            ) {
                // Suggestion chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.suggestionChips) { chip ->
                        SuggestionChipItem(
                            text = chip,
                            onClick = {
                                viewModel.sendMessage(chip)
                            }
                        )
                    }
                }

                // Input bar
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                            cursorBrush = SolidColor(Accent),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "Describe your vibe, mood, or artist...",
                                        style = TextStyle(color = TextSecondary, fontSize = 14.sp)
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val sendEnabled = inputText.isNotBlank()
                        IconButton(
                            onClick = {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            },
                            enabled = sendEnabled,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (sendEnabled) Brush.linearGradient(listOf(Primary, Secondary))
                                    else SolidColor(Surface),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (sendEnabled) Color.White else TextSecondary.copy(0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    currentSongId: String?,
    isPlaying: Boolean,
    onPlaySong: (Song) -> Unit
) {
    val bubbleBg = if (message.isUser) {
        Brush.linearGradient(listOf(Primary, Secondary))
    } else {
        SolidColor(Surface.copy(alpha = 0.8f))
    }
    
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        // Row containing Avatars and Chat Bubble Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!message.isUser) {
                // AI DJ Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(listOf(Primary, Secondary)),
                            shape = CircleShape
                        )
                        .border(1.dp, Primary.copy(0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI DJ",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Bubble Card (wraps content up to 260.dp)
            Card(
                shape = bubbleShape,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .border(
                        width = 1.dp,
                        brush = if (message.isUser) Brush.linearGradient(listOf(Accent.copy(0.3f), Accent.copy(0.3f)))
                                else Brush.horizontalGradient(listOf(Primary.copy(alpha = 0.3f), Color.Transparent)),
                        shape = bubbleShape
                    )
                    .background(bubbleBg, shape = bubbleShape)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 19.sp
                        )
                    )

                    if (message.songs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MELOFY DJ SELECTIONS",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Accent,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }

            if (message.isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Surface.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                        .border(1.dp, Accent.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = Accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Song recommendations carousel (underneath the Row, offset to align with the bubble)
        if (message.songs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (message.isUser) 0.dp else 44.dp, end = if (message.isUser) 44.dp else 0.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(message.songs) { song ->
                        AiRecommendedSongCard(
                            song = song,
                            isCurrentSong = song.id == currentSongId,
                            isPlaying = isPlaying,
                            onClick = { onPlaySong(song) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiRecommendedSongCard(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.65f)),
        modifier = Modifier
            .width(180.dp)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isCurrentSong) Accent else Primary.copy(0.25f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = song.highResArtworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.6f))
                            )
                        )
                )

                // If currently playing, show the visualizer in the center of the image!
                if (isCurrentSong && isPlaying) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MusicVisualizer(
                            isPlaying = true,
                            barCount = 4,
                            barWidth = 4.dp,
                            barGap = 2.dp,
                            maxHeight = 24.dp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 6.dp, end = 6.dp)
                        .background(Primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCurrentSong && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = song.artist,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ThinkingBubble() {
    val glowTransition = rememberInfiniteTransition(label = "thinking_glow")
    val sizePulse by glowTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "size_pulse"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .drawBehind {
                    drawCircle(
                        color = Primary.copy(0.4f * sizePulse),
                        radius = size.minDimension * 0.5f * sizePulse
                    )
                }
                .background(Brush.linearGradient(listOf(Primary, Secondary)), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.6f)),
            modifier = Modifier.border(
                width = 1.dp,
                color = Primary.copy(0.2f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
            )
        ) {
            Text(
                text = "AI DJ is mixing some beats... 💿",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
fun SuggestionChipItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface.copy(alpha = 0.7f))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(Primary.copy(alpha = 0.25f), Color.Transparent)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
