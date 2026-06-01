package com.example.melofy.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.melofy.domain.model.Artist
import com.example.melofy.domain.model.Song
import com.example.melofy.ui.components.GlassmorphicCard
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import com.example.melofy.ui.theme.Surface
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary
import com.example.melofy.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSearchBannerClick: () -> Unit,
    onMoodClick: (String) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onAiDjClick: () -> Unit,
    onPodcastClick: () -> Unit
) {
    val trendingTracks by viewModel.trendingTracks.collectAsState()
    val recommendedTracks by viewModel.recommendedTracks.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val artistSpotlight by viewModel.artistSpotlight.collectAsState()
    val newReleases by viewModel.newReleases.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    
    val personalizedCountry by viewModel.personalizedCountry.collectAsState()
    val personalizedCountryTracks by viewModel.personalizedCountryTracks.collectAsState()
    val personalizedArtist by viewModel.personalizedArtist.collectAsState()
    val personalizedArtistTracks by viewModel.personalizedArtistTracks.collectAsState()
    val personalizedGenre by viewModel.personalizedGenre.collectAsState()
    val personalizedGenreTracks by viewModel.personalizedGenreTracks.collectAsState()
    
    val featuredTrack = recommendedTracks.firstOrNull()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    // Section visibility animations
    var showGreeting by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showHero by remember { mutableStateOf(false) }
    var showSections by remember { mutableStateOf(false) }

    LaunchedEffect(recommendedTracks) {
        if (recommendedTracks.isNotEmpty()) {
            showGreeting = true
            delay(150)
            showSearch = true
            delay(150)
            showHero = true
            delay(200)
            showSections = true
            // Play song on launch if not already playing
            if (playbackState.currentSong == null) {
                delay(2000)
                viewModel.playSong(recommendedTracks.first(), recommendedTracks)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ═══════════════════════════════════════════════════
        // 1. GREETING HEADER with animated glow
        // ═══════════════════════════════════════════════════
        item {
            AnimatedVisibility(
                visible = showGreeting,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = viewModel.greeting,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 30.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Let's feel every beat \uD83C\uDFB5",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 15.sp
                            )
                        )
                    }

                    // Animated glow profile icon
                    val glowTransition = rememberInfiniteTransition(label = "glow")
                    val glowAlpha by glowTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "glow_alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Primary.copy(alpha = glowAlpha),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension
                                )
                            }
                            .background(
                                Brush.linearGradient(listOf(Primary, Secondary)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // 1.5. MELOFY DJ AI MAIN CTA (NEW & IMPROVED)
        // ═══════════════════════════════════════════════════
        item {
            AnimatedVisibility(
                visible = showGreeting,
                enter = fadeIn(tween(700, delayMillis = 100)) + slideInVertically(tween(700, delayMillis = 100)) { 40 }
            ) {
                // Infinite transitions for rotating gradient and pulsing scale
                val infiniteTransition = rememberInfiniteTransition(label = "ai_dj_cta")
                
                val glowAlphaState by infiniteTransition.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 0.85f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow_alpha"
                )

                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.99f,
                    targetValue = 1.01f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                val borderRotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1200f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(8000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "border_rotation"
                )

                // Rotating vibes suggestions
                val sampleVibes = listOf(
                    "\"Late night study lofi\" 📚",
                    "\"High energy gym session\" ⚡",
                    "\"Acoustic coffee shop vibes\" ☕",
                    "\"Chill beach party mix\" 🌊",
                    "\"Relaxing jazz for a rainy day\" 🌧️"
                )
                var currentVibeIndex by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    while(true) {
                        delay(3500)
                        currentVibeIndex = (currentVibeIndex + 1) % sampleVibes.size
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onAiDjClick),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    // Deep background glowing gradient
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Primary.copy(alpha = glowAlphaState * 0.4f), Color.Transparent),
                                            radius = size.maxDimension * 0.9f
                                        ),
                                        center = this.center
                                    )
                                }
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Primary, Secondary, Accent, Primary),
                                        start = androidx.compose.ui.geometry.Offset(borderRotation, 0f),
                                        end = androidx.compose.ui.geometry.Offset(borderRotation + 500f, 500f)
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column {
                                // Sparkles + New Badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "MELOFY DJ AI",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Accent,
                                                letterSpacing = 2.sp,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    
                                    // Pulsing "NEW" Tag
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Brush.linearGradient(listOf(Secondary, Primary)),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "NEW FEATURE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 8.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Main Catchy Title
                                Text(
                                    text = "Mix Your Moods & Vibes",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary,
                                        fontSize = 18.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Narrative Description
                                Text(
                                    text = "Describe any vibe or choose a mood to generate custom playlists instantly.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondary,
                                        lineHeight = 16.sp,
                                        fontSize = 12.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Try / Sample suggestions
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Surface.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Crossfade(
                                        targetState = currentVibeIndex,
                                        animationSpec = tween(400),
                                        label = "vibe_crossfade"
                                    ) { index ->
                                        Text(
                                            text = "Try: ${sampleVibes[index]}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextPrimary.copy(alpha = 0.9f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Shimmering Launch Button
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .background(
                                            Brush.linearGradient(listOf(Primary, Secondary)),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "TRY DJ AI NOW",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // 2. SEARCH BANNER
        // ═══════════════════════════════════════════════════
        item {
            AnimatedVisibility(
                visible = showSearch,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 30 }
            ) {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clickable(onClick = onSearchBannerClick),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search songs, artists, albums...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Old Promo Card removed

        // ═══════════════════════════════════════════════════
        // 3. HERO FEATURED BANNER
        // ═══════════════════════════════════════════════════
        if (featuredTrack != null) {
            item {
                AnimatedVisibility(
                    visible = showHero,
                    enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { 50 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionHeader(
                            title = "Featured for You",
                            subtitle = "Handpicked just for you",
                            icon = Icons.Default.AutoAwesome
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { onSongClick(featuredTrack, recommendedTracks) }
                        ) {
                            // Background artwork
                            AsyncImage(
                                model = featuredTrack.highResArtworkUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(0.3f),
                                                Color.Black.copy(0.85f)
                                            )
                                        )
                                    )
                            )
                            // Left side gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Primary.copy(0.25f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            // Content overlay
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (!featuredTrack.genre.isNullOrBlank()) {
                                        Text(
                                            text = featuredTrack.genre.orEmpty().uppercase(),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Primary,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    Text(
                                        text = featuredTrack.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary,
                                            fontSize = 22.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = featuredTrack.artist,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextSecondary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                // Play button with glow
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .drawBehind {
                                            drawCircle(
                                                color = Primary.copy(0.4f),
                                                radius = size.minDimension * 0.8f
                                            )
                                        }
                                        .background(Primary, shape = CircleShape)
                                        .clickable { onSongClick(featuredTrack, recommendedTracks) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val isPlaying = playbackState.currentSong?.id == featuredTrack.id && playbackState.isPlaying
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // 4. GENRE QUICK PICKS
        // ═══════════════════════════════════════════════════
        item {
            AnimatedVisibility(
                visible = showSections,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(500, delayMillis = 100)) { 40 }
            ) {
                Column {
                    Spacer(modifier = Modifier.height(28.dp))
                    SectionHeader(
                        title = "Quick Picks",
                        subtitle = "Jump into a genre"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(viewModel.genreChips) { chip ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Surface, Primary.copy(0.15f))
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(Primary.copy(0.3f), Color.Transparent)
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { onMoodClick(chip.name) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "${chip.emoji} ${chip.name}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // 5. LISTENING HISTORY
        // ═══════════════════════════════════════════════════
        if (recentlyPlayed.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = showSections,
                    enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200)) { 40 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(28.dp))
                        SectionHeader(
                            title = "Listening History",
                            subtitle = "Pick up where you left off"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(recentlyPlayed) { song ->
                                GlassmorphicCard(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .clickable { onSongClick(song, recentlyPlayed) },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(114.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        ) {
                                            AsyncImage(
                                                model = song.medResArtworkUrl,
                                                contentDescription = "Artwork",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            // Duration badge
                                            if (song.durationMs > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(4.dp)
                                                        .background(
                                                            Color.Black.copy(0.7f),
                                                            shape = RoundedCornerShape(6.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    val mins = (song.durationMs / 60000).toInt()
                                                    val secs = ((song.durationMs % 60000) / 1000).toInt()
                                                    Text(
                                                        text = "${mins}:${String.format("%02d", secs)}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = TextPrimary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                            }
                                            // Play indicator for current song
                                            if (playbackState.currentSong?.id == song.id && playbackState.isPlaying) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.Center)
                                                        .size(32.dp)
                                                        .background(Primary.copy(0.9f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Pause,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = song.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            text = song.artist,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // PERSONALIZED COUNTRY HITS (NEW)
        // ═══════════════════════════════════════════════════
        if (personalizedCountryTracks.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = showSections,
                    enter = fadeIn(tween(500, delayMillis = 250)) + slideInVertically(tween(500, delayMillis = 250)) { 40 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(28.dp))
                        SectionHeader(
                            title = "Top Chart in $personalizedCountry",
                            subtitle = "Most trending tracks in your country \uD83C\uDF1F",
                            icon = Icons.Default.Album
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(personalizedCountryTracks) { song ->
                                RecommendedSongCard(
                                    song = song,
                                    isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                                    onClick = { onSongClick(song, personalizedCountryTracks) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // PERSONALIZED ARTIST HITS (NEW)
        // ═══════════════════════════════════════════════════
        if (personalizedArtistTracks.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = showSections,
                    enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300)) { 40 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(28.dp))
                        SectionHeader(
                            title = "Designed For You: Best of $personalizedArtist",
                            subtitle = "Fresh handpicked tracks from your favorite artist ✨",
                            icon = Icons.Default.AutoAwesome
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(personalizedArtistTracks) { song ->
                                RecommendedSongCard(
                                    song = song,
                                    isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                                    onClick = { onSongClick(song, personalizedArtistTracks) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // PERSONALIZED GENRE HITS (NEW)
        // ═══════════════════════════════════════════════════
        if (personalizedGenreTracks.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = showSections,
                    enter = fadeIn(tween(500, delayMillis = 350)) + slideInVertically(tween(500, delayMillis = 350)) { 40 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(28.dp))
                        SectionHeader(
                            title = "Designed For You: Top $personalizedGenre Hits",
                            subtitle = "Tuned exactly to your listening genres \uD83C\uDFB5",
                            icon = Icons.Default.MusicNote
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(personalizedGenreTracks) { song ->
                                RecommendedSongCard(
                                    song = song,
                                    isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                                    onClick = { onSongClick(song, personalizedGenreTracks) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // 6. ARTIST SPOTLIGHT (Real iTunes Data)
        // ═══════════════════════════════════════════════════
        if (artistSpotlight.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = showSections,
                    enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300)) { 40 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(28.dp))
                        SectionHeader(
                            title = "Artist Spotlight",
                            subtitle = "Discover iconic artists"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(artistSpotlight) { artist ->
                                ArtistSpotlightCard(
                                    artist = artist,
                                    onClick = { onMoodClick(artist.name) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // 7. RECOMMENDED FOR YOU
        // ═══════════════════════════════════════════════════
        if (recommendedTracks.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = showSections,
                    enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400)) { 40 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(28.dp))
                        SectionHeader(
                            title = "Recommended for You",
                            subtitle = "Based on your listening history",
                            icon = Icons.Default.AutoAwesome
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(recommendedTracks.take(12)) { song ->
                                RecommendedSongCard(
                                    song = song,
                                    isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                                    onClick = { onSongClick(song, recommendedTracks) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // 8. NEW RELEASES
        // ═══════════════════════════════════════════════════
        if (newReleases.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = showSections,
                    enter = fadeIn(tween(500, delayMillis = 500)) + slideInVertically(tween(500, delayMillis = 500)) { 40 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(28.dp))
                        SectionHeader(
                            title = "New Releases",
                            subtitle = "Fresh tracks just dropped",
                            icon = Icons.Default.FiberNew
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(newReleases.take(10)) { song ->
                                NewReleaseCard(
                                    song = song,
                                    onClick = { onSongClick(song, newReleases) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // 9. HOT RIGHT NOW — Numbered Trending List
        // ═══════════════════════════════════════════════════
        item {
            AnimatedVisibility(
                visible = showSections,
                enter = fadeIn(tween(500, delayMillis = 600)) + slideInVertically(tween(500, delayMillis = 600)) { 40 }
            ) {
                Column {
                    Spacer(modifier = Modifier.height(28.dp))
                    SectionHeader(
                        title = "Hot Right Now",
                        subtitle = "What everyone's listening to",
                        icon = Icons.Default.LocalFireDepartment
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (isLoading && trendingTracks.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading trending tracks...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary
                        )
                    )
                }
            }
        } else {
            itemsIndexed(trendingTracks.take(8)) { index, song ->
                TrendingTrackRow(
                    index = index + 1,
                    song = song,
                    isCurrentlyPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                    onClick = { onSongClick(song, trendingTracks) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// REUSABLE COMPONENTS
// ═══════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    fontSize = 21.sp,
                    letterSpacing = (-0.3).sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 13.sp
            ),
            modifier = if (icon != null) Modifier.padding(start = 30.dp) else Modifier
        )
    }
}

@Composable
private fun ArtistSpotlightCard(
    artist: Artist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(95.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated border
        val borderTransition = rememberInfiniteTransition(label = "artist_border")
        val borderRotation by borderTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer { rotationZ = borderRotation }
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(Primary, Secondary, Primary.copy(0.3f), Secondary, Primary)
                    ),
                    shape = CircleShape
                )
                .padding(3.dp)
                .graphicsLayer { rotationZ = -borderRotation },
            contentAlignment = Alignment.Center
        ) {
            if (artist.artworkUrl.isNotBlank()) {
                AsyncImage(
                    model = artist.highResArtworkUrl,
                    contentDescription = artist.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Gradient fallback
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Primary.copy(0.5f), Secondary.copy(0.5f))),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = artist.name.take(1),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (artist.genre.isNotBlank() && artist.genre != "Music") {
            Text(
                text = artist.genre,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecommendedSongCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            AsyncImage(
                model = song.medResArtworkUrl,
                contentDescription = "Artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(0.75f)),
                            startY = 100f
                        )
                    )
            )
            // Genre tag at top
            if (!song.genre.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            Primary.copy(0.85f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = song.genre.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            // Play button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(30.dp)
                    .background(
                        if (isPlaying) Primary else Background.copy(0.85f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = if (isPlaying) Color.White else Accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 13.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun NewReleaseCard(
    song: Song,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = song.medResArtworkUrl,
                    contentDescription = "Artwork",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // NEW badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Primary, Secondary)),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "NEW",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (song.releaseYear.isNotBlank()) {
                    Text(
                        text = song.releaseYear,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendingTrackRow(
    index: Int,
    song: Song,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isCurrentlyPlaying) Primary.copy(0.12f)
                else Surface.copy(0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank number
        Text(
            text = String.format("%02d", index),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (index <= 3) Primary else TextSecondary.copy(0.5f),
                fontSize = if (index <= 3) 24.sp else 20.sp
            ),
            modifier = Modifier.width(36.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Artwork
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = song.artworkUrl,
                contentDescription = "Artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (isCurrentlyPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Song info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentlyPlaying) Primary else TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!song.genre.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .background(
                                Primary.copy(0.15f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = song.genre.orEmpty(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // Trending indicator
        if (index <= 3) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
