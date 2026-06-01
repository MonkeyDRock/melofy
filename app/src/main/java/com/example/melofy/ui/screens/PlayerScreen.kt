package com.example.melofy.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.melofy.domain.model.Song
import com.example.melofy.ui.components.GlassmorphicCard
import com.example.melofy.ui.components.MusicVisualizer
import com.example.melofy.ui.components.RotatingArtwork
import com.example.melofy.ui.components.SmoothLikeHeartButton
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import com.example.melofy.ui.theme.Surface
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary
import com.example.melofy.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBackClick: () -> Unit
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerSeconds.collectAsState()
    val isDownloaded by viewModel.isCurrentSongDownloaded.collectAsState()
    val moreFromArtist by viewModel.moreFromArtist.collectAsState()
    val context = LocalContext.current

    val song = playbackState.currentSong ?: return

    var showTimerDialog by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(song.id) {
        showContent = false
        delay(100)
        showContent = true
    }

    // Helper: Formats milliseconds into e.g. "03:45"
    fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    // Sleep Timer Dialog
    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = {
                Text(
                    "Sleep Timer",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    listOf(5, 15, 30, 45, 60).forEach { mins ->
                        TextButton(
                            onClick = {
                                viewModel.startSleepTimer(mins)
                                showTimerDialog = false
                                Toast.makeText(context, "Timer set for $mins minutes", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$mins Minutes", color = Accent)
                                Text(
                                    "${mins}m",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelSleepTimer()
                    showTimerDialog = false
                    Toast.makeText(context, "Timer cancelled", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Cancel Timer", color = Secondary)
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ═══════════════════════════════════════════════════
        // BLURRED BACKGROUND ARTWORK
        // ═══════════════════════════════════════════════════
        AsyncImage(
            model = song.highResArtworkUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp),
            contentScale = ContentScale.Crop
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Background.copy(0.75f),
                            Background.copy(0.88f),
                            Background.copy(0.95f)
                        )
                    )
                )
        )

        // Top gradient accent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Primary.copy(0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ═══════════════════════════════════════════════════
        // MAIN CONTENT
        // ═══════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── Header Bar ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            letterSpacing = 3.sp,
                            fontSize = 10.sp
                        )
                    )
                    if (song.album.isNotBlank() && song.album != "Unknown Album") {
                        Text(
                            text = song.album,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(180.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row {
                    // Sleep timer button
                    IconButton(onClick = { showTimerDialog = true }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Sleep timer",
                                tint = if (sleepTimerRemaining > 0) Primary else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            if (sleepTimerRemaining > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(8.dp)
                                        .background(Primary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Rotating Artwork with Glow ──────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) + slideInVertically(tween(600)) { -30 }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .drawBehind {
                            if (playbackState.isPlaying) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Primary.copy(0.2f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension * 0.75f
                                )
                            }
                        }
                ) {
                    RotatingArtwork(
                        artworkUrl = song.highResArtworkUrl,
                        isPlaying = playbackState.isPlaying,
                        size = 280.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Song Info + Actions ─────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 150))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                    fontSize = 26.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Favorite
                            SmoothLikeHeartButton(
                                isLiked = song.isFavorite,
                                onClick = { viewModel.toggleFavoriteActiveSong() },
                                iconSize = 26.dp
                            )
                            // Download
                            IconButton(onClick = {
                                if (isDownloaded) {
                                    viewModel.deleteActiveSongDownload()
                                    Toast.makeText(context, "Download removed", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.downloadActiveSong()
                                    Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = if (isDownloaded) Primary else TextSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    // ─── Metadata Chips ──────────────────────
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Full track vs Preview indicator
                        if (song.isFullTrack) {
                            MetadataChip(text = "♫ FULL TRACK", color = Primary)
                        } else {
                            MetadataChip(text = "▶ PREVIEW", color = Color(0xFFFF9800))
                        }
                        if (!song.genre.isNullOrBlank()) {
                            MetadataChip(text = song.genre!!, color = Primary)
                        }
                        if (song.releaseYear.isNotBlank()) {
                            MetadataChip(text = song.releaseYear, color = Secondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Visualizer ──────────────────────────────
            MusicVisualizer(
                isPlaying = playbackState.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .height(40.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ─── Progress Slider ─────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Slider(
                    value = playbackState.currentPositionMs.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..(playbackState.durationMs.toFloat().coerceAtLeast(1f)),
                    colors = SliderDefaults.colors(
                        thumbColor = Primary,
                        activeTrackColor = Primary,
                        inactiveTrackColor = Color.White.copy(0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(playbackState.currentPositionMs),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = formatTime(playbackState.durationMs),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Playback Controls ───────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.isShuffleEnabled) Primary else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = { viewModel.skipToPrevious() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play/Pause — Large with glow
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .drawBehind {
                            if (playbackState.isPlaying) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Primary.copy(0.35f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension * 0.9f
                                )
                            }
                        }
                ) {
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(76.dp)
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                            contentDescription = "Play/Pause",
                            tint = Primary,
                            modifier = Modifier.size(76.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = { viewModel.skipToNext() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    val repeatIcon = if (playbackState.repeatMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat
                    val repeatColor = if (playbackState.repeatMode != 0) Primary else TextSecondary
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = repeatColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Speed Controller ────────────────────────
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Speed",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speedVal ->
                            val isSelected = playbackState.playbackSpeed == speedVal
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected)
                                            Brush.horizontalGradient(listOf(Primary, Secondary))
                                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                    )
                                    .clickable { viewModel.setPlaybackSpeed(speedVal) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${speedVal}x",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Lyrics Card ─────────────────────────────
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .height(160.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LYRICS",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                letterSpacing = 3.sp,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.staticLyrics[((playbackState.currentPositionMs / 4000) % viewModel.staticLyrics.size).toInt()],
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            lineHeight = 26.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ─── Sleep Timer Status ──────────────────────
            if (sleepTimerRemaining > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                GlassmorphicCard(
                    modifier = Modifier
                        .padding(horizontal = 28.dp)
                        .clickable { showTimerDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sleep in ${sleepTimerRemaining / 60}m ${sleepTimerRemaining % 60}s",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            // ─── Queue Preview ───────────────────────────
            val upNext = playbackState.queue
                .dropWhile { it.id != song.id }
                .drop(1)
                .take(3)

            if (upNext.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UP NEXT",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                letterSpacing = 2.sp,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            upNext.forEach { queueSong ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.playSong(queueSong) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = queueSong.artworkUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = queueSong.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 13.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = queueSong.artist,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ─── More from this Artist ───────────────────
            if (moreFromArtist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MORE FROM ${song.artist.uppercase()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                letterSpacing = 1.5.sp,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(moreFromArtist) { relatedSong ->
                            Column(
                                modifier = Modifier
                                    .width(110.dp)
                                    .clickable { viewModel.playSong(relatedSong) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                ) {
                                    AsyncImage(
                                        model = relatedSong.artworkUrl,
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
                                                        Color.Black.copy(0.6f)
                                                    ),
                                                    startY = 60f
                                                )
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = relatedSong.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (!relatedSong.genre.isNullOrBlank()) {
                                    Text(
                                        text = relatedSong.genre.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Primary.copy(0.8f),
                                            fontSize = 9.sp
                                        ),
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// METADATA CHIP COMPONENT
// ═══════════════════════════════════════════════════════════
@Composable
private fun MetadataChip(
    text: String,
    color: Color,
    maxWidth: Int = 0
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = if (maxWidth > 0) Modifier.width(maxWidth.dp) else Modifier
        )
    }
}
