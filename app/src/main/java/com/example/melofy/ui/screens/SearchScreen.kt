package com.example.melofy.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.melofy.domain.model.Song
import com.example.melofy.ui.components.GlassmorphicCard
import com.example.melofy.ui.components.SmoothLikeHeartButton
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary
import com.example.melofy.ui.viewmodel.SearchViewModel
import com.example.melofy.ui.viewmodel.LibraryViewModel

data class GenreItem(
    val name: String,
    val searchKeyword: String,
    val icon: String,
    val brush: Brush
)

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    libraryViewModel: LibraryViewModel,
    onSongClick: (Song, List<Song>) -> Unit
) {
    val query by viewModel.query.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchType by viewModel.searchType.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val albumTracks by viewModel.albumTracks.collectAsState()
    val isLoadingAlbumTracks by viewModel.isLoadingAlbumTracks.collectAsState()
    val context = LocalContext.current

    // Collect library favorites for instant Heart binding
    val favorites by libraryViewModel.favorites.collectAsState()
    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }

    // Collect active playback state to highlight "now playing" track
    val playbackState by viewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong
    val isPlaying = playbackState.isPlaying

    // Speech Recognition Activity Launcher
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.onQueryChanged(spokenText)
                viewModel.executeSearch(spokenText)
                Toast.makeText(context, "Searching for spoken: \"$spokenText\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startVoiceSearch() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak song, artist, or album...")
            }
            voiceSearchLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice speech search is not supported on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    // Curated high-fidelity Spotify-style browse genres list
    val browseGenres = listOf(
        GenreItem("Synthwave Cosmic", "Synthwave", "💿", Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFF9C27B0)))),
        GenreItem("Chillout Lofi", "Lofi study beats", "☕", Brush.linearGradient(listOf(Color(0xFF2196F3), Color(0xFF00BCD4)))),
        GenreItem("Workout Power", "Gym workout music", "⚡", Brush.linearGradient(listOf(Color(0xFFFF5722), Color(0xFFFFC107)))),
        GenreItem("Acoustic Sunset", "Acoustic guitar", "🌅", Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)))),
        GenreItem("AI DJ Electronic", "Electronic dance", "👽", Brush.linearGradient(listOf(Color(0xFFFF3D00), Color(0xFFFF9100)))),
        GenreItem("Classical Grace", "Classical piano", "🎻", Brush.linearGradient(listOf(Color(0xFF607D8B), Color(0xFF9E9E9E))))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // 1. Search Bar (Fully functional Speech triggers)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        label = { Text("Search songs, albums, creators...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Primary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = Accent,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { startVoiceSearch() }
                                    .padding(8.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(0.12f),
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // 1b. Search Category Toggle Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        Triple("song", "Songs", "🎵"),
                        Triple("album", "Albums", "💿"),
                        Triple("artist", "Creators", "🎙️")
                    ).forEach { (type, label, emoji) ->
                        val isSelected = searchType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Accent else Color.White.copy(0.06f))
                                .clickable { viewModel.setSearchType(type) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$emoji $label",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) Background else TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // 2. Default state (Empty Query - Spotify-style Browse Grid)
            if (query.isBlank()) {
                // Popular Creators horizontal deck
                item {
                    Text(
                        text = "Popular Creators",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(viewModel.popularArtists) { artist ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(0.06f))
                                    .clickable {
                                        viewModel.setSearchType("song")
                                        viewModel.executeSearch(artist)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = artist,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Spotify-style browse all genres grid
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Browse All Music Genres",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simple Compose Grid within LazyColumn
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val pairs = browseGenres.chunked(2)
                        pairs.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { genre ->
                                    GenreCard(
                                        genre = genre,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.setSearchType("song")
                                            viewModel.executeSearch(genre.searchKeyword)
                                        }
                                    )
                                }
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                // Results list
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found for \"$query\"",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                } else if (searchType == "song") {
                    items(searchResults) { song ->
                        val isCurrentTrack = currentSong?.id == song.id
                        val titleColor = if (isCurrentTrack) Accent else TextPrimary
                        val favorited = favoriteIds.contains(song.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .clickable { onSongClick(song, searchResults) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = song.artworkUrl,
                                contentDescription = "Artwork",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCurrentTrack && isPlaying) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Now Playing",
                                            tint = Accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = titleColor
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            // Dynamic Library Heart favorite button
                            SmoothLikeHeartButton(
                                isLiked = favorited,
                                onClick = {
                                    libraryViewModel.toggleFavorite(song)
                                    Toast.makeText(
                                        context,
                                        if (favorited) "Removed from Favorites" else "Added to Favorites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                iconSize = 20.dp
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else if (searchType == "album") {
                    val albumPairs = searchResults.chunked(2)
                    items(albumPairs) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            pair.forEach { album ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(0.04f))
                                        .clickable { viewModel.loadAlbumTracks(album) }
                                        .padding(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = album.artworkUrl,
                                            contentDescription = "Album Artwork",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = album.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = album.artist,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (pair.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else if (searchType == "artist") {
                    items(searchResults) { artist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                                .clickable {
                                    viewModel.setSearchType("song")
                                    viewModel.executeSearch(artist.artist)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(Primary, Secondary))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(
                                    text = artist.artist,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Artist • Verified Melofy Creator",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Accent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Sliding album details glass overlay sheet
        AnimatedVisibility(
            visible = selectedAlbum != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            val album = selectedAlbum ?: return@AnimatedVisibility

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background.copy(0.96f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { viewModel.clearSelectedAlbum() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Close",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Album Tracks",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Album Card Header Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(0.04f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = album.artworkUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = album.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = album.artist,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Apple Music Release",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Accent,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tracklist Listing
                    if (isLoadingAlbumTracks) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    } else if (albumTracks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No playable preview tracks in this album",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            items(albumTracks) { song ->
                                val isCurrentTrack = currentSong?.id == song.id
                                val titleColor = if (isCurrentTrack) Accent else TextPrimary
                                val favorited = favoriteIds.contains(song.id)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp)
                                        .clickable { onSongClick(song, albumTracks) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(0.04f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isCurrentTrack && isPlaying) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Now Playing",
                                                    tint = Accent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = titleColor
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = song.artist,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    SmoothLikeHeartButton(
                                        isLiked = favorited,
                                        onClick = {
                                            libraryViewModel.toggleFavorite(song)
                                            Toast.makeText(
                                                context,
                                                if (favorited) "Removed from Favorites" else "Added to Favorites",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        iconSize = 18.dp
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenreCard(
    genre: GenreItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = modifier
            .height(95.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(genre.brush)
                .padding(14.dp)
        ) {
            Text(
                text = genre.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                ),
                modifier = Modifier.align(Alignment.TopStart)
            )

            Text(
                text = genre.icon,
                fontSize = 32.sp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}
