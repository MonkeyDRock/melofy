package com.example.melofy.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.melofy.ui.components.GlassmorphicCard
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import com.example.melofy.ui.theme.Surface
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary
import com.example.melofy.ui.viewmodel.AuthViewModel

data class CountryOption(val name: String, val flag: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    authViewModel: AuthViewModel,
    onPreferencesSaved: () -> Unit
) {
    val userState by authViewModel.userState.collectAsState()
    val isLoading = userState is AuthViewModel.UserState.Loading

    // Available preferences options
    val countries = listOf(
        CountryOption("India", "🇮🇳"),
        CountryOption("United States", "🇺🇸"),
        CountryOption("United Kingdom", "🇬🇧"),
        CountryOption("Japan", "🇯🇵"),
        CountryOption("South Korea", "🇰🇷"),
        CountryOption("France", "🇫🇷")
    )

    val artists = listOf(
        "Taylor Swift", "Arijit Singh", "Ed Sheeran",
        "Billie Eilish", "The Weeknd", "BTS",
        "Bruno Mars", "Ariana Grande", "Drake"
    )

    val genres = listOf(
        "Pop", "Synthwave", "Lofi", "Rock",
        "Hip-Hop", "Electronic", "R&B", "Jazz", "Classical"
    )

    // Selections state
    var selectedCountry by remember { mutableStateOf("") }
    val selectedArtists = remember { mutableStateListOf<String>() }
    val selectedGenres = remember { mutableStateListOf<String>() }

    // Pulsing and breathing animations for background ambient glows
    val infiniteTransition = rememberInfiniteTransition(label = "glows")
    val ambientGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_glow"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .drawBehind {
                // Large cyan glowing blob in the bottom right corner
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Accent.copy(alpha = ambientGlowAlpha * 0.5f), Color.Transparent),
                        radius = size.maxDimension * 0.6f
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width, size.height)
                )
                // Emerald glowing blob in the top left corner
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = ambientGlowAlpha * 0.4f), Color.Transparent),
                        radius = size.maxDimension * 0.5f
                    ),
                    center = androidx.compose.ui.geometry.Offset(0f, 0f)
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 60.dp, bottom = 120.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // ═══════════════════════════════════════════════════
            // HEADER SECTION
            // ═══════════════════════════════════════════════════
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.linearGradient(listOf(Primary, Accent)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Customize Your Vibe",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Choose your favorites so we can curate the perfect music dashboard for you.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // ═══════════════════════════════════════════════════
            // COUNTRY SECTION (Grid)
            // ═══════════════════════════════════════════════════
            item {
                Column {
                    Text(
                        text = "Select your country",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Row-based grid implementation for countries
                    countries.chunked(2).forEach { rowCountries ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowCountries.forEach { country ->
                                val isSelected = selectedCountry == country.name
                                GlassmorphicCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedCountry = country.name }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            brush = if (isSelected) Brush.linearGradient(listOf(Primary, Accent))
                                            else Brush.linearGradient(listOf(Color.White.copy(0.1f), Color.White.copy(0.02f))),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    borderWidth = 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isSelected) Primary.copy(0.12f) else Surface.copy(0.4f))
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = country.flag,
                                            fontSize = 22.sp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = country.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) TextPrimary else TextSecondary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(16.dp)
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
            // FAVORITE ARTISTS SECTION (Flow Chips)
            // ═══════════════════════════════════════════════════
            item {
                Column {
                    Text(
                        text = "Who are your favorite artists?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose one or more of your favorites",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        artists.forEach { artist ->
                            val isSelected = selectedArtists.contains(artist)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Accent.copy(0.15f)
                                        else Surface.copy(0.5f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Accent else Color.White.copy(0.12f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            selectedArtists.remove(artist)
                                        } else {
                                            selectedArtists.add(artist)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Accent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = artist,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) TextPrimary else TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════
            // FAVORITE GENRES SECTION (Flow Chips)
            // ═══════════════════════════════════════════════════
            item {
                Column {
                    Text(
                        text = "What genres do you love?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose genres matching your style",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        genres.forEach { genre ->
                            val isSelected = selectedGenres.contains(genre)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Primary.copy(0.15f)
                                        else Surface.copy(0.5f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Primary else Color.White.copy(0.12f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            selectedGenres.remove(genre)
                                        } else {
                                            selectedGenres.add(genre)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) TextPrimary else TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════
        // SAVE BUTTON (Sticky bottom neon overlay)
        // ═══════════════════════════════════════════════════
        val isButtonEnabled = selectedCountry.isNotBlank() && selectedArtists.isNotEmpty() && selectedGenres.isNotEmpty() && !isLoading
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Background.copy(alpha = 0.95f), Background)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Button(
                onClick = {
                    if (isButtonEnabled) {
                        val artistsStr = selectedArtists.joinToString(",")
                        val genresStr = selectedGenres.joinToString(",")
                        authViewModel.updateUserPreferences(
                            country = selectedCountry,
                            favoriteArtists = artistsStr,
                            favoriteGenres = genresStr,
                            onSuccess = onPreferencesSaved
                        )
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .graphicsLayer {
                        if (isButtonEnabled) {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                    }
                    .drawBehind {
                        if (isButtonEnabled) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Primary.copy(0.4f), Color.Transparent),
                                    radius = size.minDimension * 0.9f
                                ),
                                center = this.center
                            )
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Color.White.copy(0.1f),
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(0.3f)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "LET'S GET GROOVING",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
        }
    }
}
