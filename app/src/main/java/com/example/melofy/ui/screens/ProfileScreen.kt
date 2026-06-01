package com.example.melofy.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.melofy.R
import com.example.melofy.domain.model.Song
import com.example.melofy.ui.components.GlassmorphicCard
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.CyanAccent
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import com.example.melofy.ui.theme.Surface
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary
import com.example.melofy.ui.viewmodel.AuthViewModel
import com.example.melofy.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

data class NotificationItem(
    val id: String,
    val title: String,
    val text: String,
    val time: String,
    val isUnread: Boolean
)

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    libraryViewModel: LibraryViewModel,
    onSettingsClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    onVoiceSearchClick: (String) -> Unit
) {
    val userState by authViewModel.userState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Library real-time statistics
    val favorites by libraryViewModel.favorites.collectAsState()
    val playlists by libraryViewModel.playlists.collectAsState()
    val downloadedSongs by libraryViewModel.downloadedSongs.collectAsState()

    // Notification states
    var notifications by remember {
        mutableStateOf(
            listOf(
                NotificationItem("1", "Weekly Sonic Report", "Your listening signature shifted 15% towards Synthwave this week!", "5m ago", true),
                NotificationItem("2", "Super fidelity Sound unlocked", "Extreme 320kbps audio profile fully verified on your device.", "2h ago", true),
                NotificationItem("3", "AI DJ recommendation ready", "Tap to tune into your morning mood-focused playlist.", "1d ago", true)
            )
        )
    }
    var showNotificationDialog by remember { mutableStateOf(false) }
    val unreadNotificationsCount = notifications.count { it.isUnread }

    // Read user profile details
    val name = when (val state = userState) {
        is AuthViewModel.UserState.Authenticated -> state.user.name
        else -> "Melofy Listener"
    }

    val email = when (val state = userState) {
        is AuthViewModel.UserState.Authenticated -> state.user.email
        else -> "listener@melofy.com"
    }

    val avatarScheme = when (val state = userState) {
        is AuthViewModel.UserState.Authenticated -> state.user.avatar
        else -> "default"
    }

    // Dynamic gradient mapping for active aura/avatar
    val avatarBrush = when (avatarScheme) {
        "sunset" -> Brush.linearGradient(listOf(Primary, Accent))
        "ocean" -> Brush.linearGradient(listOf(CyanAccent, Primary))
        "electric" -> Brush.linearGradient(listOf(Secondary, Accent))
        "forest" -> Brush.linearGradient(listOf(Color(0xFF00E676), CyanAccent))
        "royal" -> Brush.linearGradient(listOf(Color(0xFFFFD600), Primary))
        else -> Brush.linearGradient(listOf(Primary, Secondary, Accent)) // Default
    }

    val glowColor = when (avatarScheme) {
        "sunset" -> Primary
        "ocean" -> CyanAccent
        "electric" -> Secondary
        "forest" -> Color(0xFF00E676)
        "royal" -> Color(0xFFFFD600)
        else -> Primary
    }

    // Interactive Energy Vibe Slider State
    var vibeEnergy by remember { mutableStateOf(0.5f) }
    val vibeLabel = when {
        vibeEnergy < 0.25f -> "Chill Ambient (Resting Mode)"
        vibeEnergy < 0.5f -> "Calm Acoustic (Relaxing Mode)"
        vibeEnergy < 0.75f -> "Energetic Pop (Active Mode)"
        else -> "Hyper Electronic (Party Mode)"
    }

    // Dynamic Sonic Aura Analysis
    val topGenre = remember(favorites) {
        if (favorites.isEmpty()) "Unknown"
        else {
            favorites.mapNotNull { it.genre }
                .filter { it.isNotBlank() }
                .groupBy { it }
                .maxByOrNull { it.value.size }?.key ?: "Unknown"
        }
    }

    val auraTitle = when {
        topGenre.contains("Synthwave", ignoreCase = true) || topGenre.contains("Retro", ignoreCase = true) -> "Retro Dreamer"
        topGenre.contains("Pop", ignoreCase = true) -> "Vibrant Energizer"
        topGenre.contains("Rock", ignoreCase = true) || topGenre.contains("Metal", ignoreCase = true) -> "Electric Maverick"
        topGenre.contains("Chill", ignoreCase = true) || topGenre.contains("Lofi", ignoreCase = true) -> "Zen Harmonizer"
        topGenre != "Unknown" -> "Sonic Explorer"
        else -> "Curious Explorer"
    }

    val auraDesc = when (auraTitle) {
        "Retro Dreamer" -> "You are deeply aligned with cosmic synths, driving basslines, and late-night neon drives."
        "Vibrant Energizer" -> "Your soul beats to high-fidelity pop frequencies and uptempo melodies."
        "Electric Maverick" -> "You crave raw distortion, driving drums, and energetic rebellion."
        "Zen Harmonizer" -> "You search for soft textures, soothing keys, and absolute peaceful soundscapes."
        "Sonic Explorer" -> "You enjoy $topGenre frequencies and a highly diverse spectrum of sound waves."
        else -> "Start favoriting songs to decode your signature sonic frequency and unlock your Music Aura!"
    }

    val auraColors = when (auraTitle) {
        "Retro Dreamer" -> listOf(Secondary, Accent)
        "Vibrant Energizer" -> listOf(Primary, Accent)
        "Electric Maverick" -> listOf(Primary, Secondary)
        "Zen Harmonizer" -> listOf(CyanAccent, Primary)
        else -> listOf(Primary, Secondary, Accent)
    }

    // Editing Dialog State
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(name) }
    var selectedAvatarScheme by remember { mutableStateOf(avatarScheme) }

    // Speech Recognition Launcher
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                Toast.makeText(context, "Searching: \"$spokenText\"", Toast.LENGTH_SHORT).show()
                onVoiceSearchClick(spokenText)
            }
        }
    }

    fun startVoiceSearch() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a song, album, or artist name...")
            }
            voiceSearchLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice Search is not supported on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    // Glowing animation transitions
    val infiniteTransition = rememberInfiniteTransition(label = "profile_animations")
    val borderRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "border_rotation"
    )

    // Breathing glow avatar scale animation
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_scale"
    )

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(0.08f), Color.Transparent),
                        radius = size.maxDimension * 0.45f
                    ),
                    center = this.center.copy(y = -200f)
                )
            },
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // 1. Header Row (Avatar, Name, Bell icon in top right)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header icons (Settings, Notifications)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bell icon
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        IconButton(
                            onClick = { showNotificationDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Surface.copy(0.4f), CircleShape)
                        ) {
                            if (unreadNotificationsCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = Primary,
                                            contentColor = Color.White
                                        ) {
                                            Text("$unreadNotificationsCount")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Inbox",
                                        tint = Accent
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Inbox",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }

                    // Settings Shortcut
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Surface.copy(0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breathing glow avatar Circle (Tap to Edit)
                Box(
                    modifier = Modifier
                        .size(105.dp * avatarScale)
                        .background(avatarBrush, shape = CircleShape)
                        .clickable {
                            editName = name
                            selectedAvatarScheme = avatarScheme
                            showEditDialog = true
                        }
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Background, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarScheme.startsWith("http://") || avatarScheme.startsWith("https://")) {
                            AsyncImage(
                                model = avatarScheme,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Primary,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        // Floating edit icon indicator
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(Primary, CircleShape)
                                .align(Alignment.BottomEnd)
                                .border(1.5.dp, Background, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        editName = name
                        selectedAvatarScheme = avatarScheme
                        showEditDialog = true
                    }
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = Accent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary
                    )
                )
            }
        }

        // 2. Listener stats dashboard
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stat 1: Favorites
                StatCard(
                    icon = Icons.Default.Favorite,
                    value = "${favorites.size}",
                    label = "Favorites",
                    iconColor = Accent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (favorites.isNotEmpty()) {
                                coroutineScope.launch {
                                    // Smoothly scroll down to Favorites section
                                    listState.animateScrollToItem(7)
                                }
                            } else {
                                Toast.makeText(context, "No favorites yet! Add songs from Home or Search.", Toast.LENGTH_SHORT).show()
                            }
                        }
                )

                // Stat 2: Playlists
                StatCard(
                    icon = Icons.Default.LibraryMusic,
                    value = "${playlists.size}",
                    label = "Playlists",
                    iconColor = Primary,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            Toast.makeText(context, "Check your detailed playlists under the Library tab!", Toast.LENGTH_SHORT).show()
                        }
                )

                // Stat 3: Offline Downloads
                StatCard(
                    icon = Icons.Default.Headphones,
                    value = "${downloadedSongs.size}",
                    label = "Offline",
                    iconColor = CyanAccent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            Toast.makeText(context, "Offline songs can be played without network in your Library!", Toast.LENGTH_SHORT).show()
                        }
                )
            }
        }

        // 3. Dynamic Sonic Aura Card
        item {
            Spacer(modifier = Modifier.height(24.dp))
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(auraColors.first().copy(0.18f), Color.Transparent),
                                    radius = size.maxDimension * 0.7f
                                ),
                                center = this.center
                            )
                        }
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(auraColors),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(auraColors.first().copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = auraColors.first(),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SONIC AURA",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = auraColors.first(),
                                        letterSpacing = 1.5.sp,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = auraTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = auraDesc,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // 4. Listener Achievement Badges Row (Unlockable Gamification)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Listener Achievements",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Horizontal scrolling achievements row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Badge 1: Vibe Master (3+ favorites)
                    val isVibeMaster = favorites.size >= 3
                    BadgeChip(
                        title = "Vibe Master",
                        isUnlocked = isVibeMaster,
                        glowColor = Accent,
                        icon = Icons.Default.Favorite
                    )

                    // Badge 2: Playlist Pioneer (1+ playlists)
                    val isPioneer = playlists.size >= 1
                    BadgeChip(
                        title = "Playlist Pioneer",
                        isUnlocked = isPioneer,
                        glowColor = Primary,
                        icon = Icons.Default.LibraryMusic
                    )

                    // Badge 3: Offline Explorer (3+ downloads)
                    val isExplorer = downloadedSongs.size >= 3
                    BadgeChip(
                        title = "Offline Explorer",
                        isUnlocked = isExplorer,
                        glowColor = CyanAccent,
                        icon = Icons.Default.Headphones
                    )

                    // Badge 4: Elite Listener (Sum >= 5)
                    val isElite = (favorites.size + playlists.size + downloadedSongs.size) >= 5
                    BadgeChip(
                        title = "Elite Listener",
                        isUnlocked = isElite,
                        glowColor = Color(0xFFFFD600),
                        icon = Icons.Default.Star
                    )
                }
            }
        }

        // 5. Interactive Vibe Energy Slider
        item {
            Spacer(modifier = Modifier.height(24.dp))
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Sound Wave Vibe",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            fontSize = 14.sp
                        )
                        Text(
                            text = vibeLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Accent
                            ),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = vibeEnergy,
                        onValueChange = { vibeEnergy = it },
                        colors = SliderDefaults.colors(
                            thumbColor = Accent,
                            activeTrackColor = Accent,
                            inactiveTrackColor = Color.White.copy(0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 6. Speech-to-Text Voice Mic Search Card Launcher
        item {
            Spacer(modifier = Modifier.height(20.dp))
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable { startVoiceSearch() },
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Accent.copy(0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mic Search",
                            tint = Accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Voice Mic Search",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Tap to speak and discover music instantly",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // 7. Premium Upgrade Card (Animated and highly lucrative)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable(onClick = onUpgradeClick),
                shape = RoundedCornerShape(26.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Primary.copy(alpha = 0.2f), Color.Transparent),
                                    radius = size.minDimension * 0.9f
                                ),
                                center = this.center
                            )
                        }
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Primary, Secondary, Accent, Primary),
                                start = androidx.compose.ui.geometry.Offset(borderRotation, 0f),
                                end = androidx.compose.ui.geometry.Offset(borderRotation + 400f, 400f)
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MELOFY PREMIUM",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Accent,
                                        letterSpacing = 2.sp,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Unlock Ultra Fidelity Audio",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Get ad-free listening, 320kbps extreme audio, offline downloads, and unlimited AI DJ generation.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                lineHeight = 18.sp,
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onUpgradeClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "View Plans • From $4.99/mo",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 8. Top Favorite Songs Quick Play section
        if (favorites.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Quick Play Favorites",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Render up to top 3 favorite tracks
                    favorites.take(3).forEach { song ->
                        FavoriteSongRow(
                            song = song,
                            onPlayClick = {
                                libraryViewModel.playSong(song, favorites)
                                Toast.makeText(context, "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // 9. Quick Actions / Menu List
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Surface.copy(alpha = 0.6f))
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.Favorite,
                    title = "My Favorites",
                    subtitle = "Manage favorited tracks",
                    onClick = {
                        if (favorites.isNotEmpty()) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(7)
                            }
                        } else {
                            Toast.makeText(context, "No favorites yet! Go check out the Search page.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                ProfileMenuItem(
                    icon = Icons.Default.History,
                    title = "Listening History",
                    subtitle = "View recently played songs",
                    onClick = { Toast.makeText(context, "Recent plays are listed on Home Screen!", Toast.LENGTH_SHORT).show() }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "App Settings",
                    subtitle = "Customize playback and downloads",
                    onClick = onSettingsClick
                )
            }
        }

        // 10. Support / Info List
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Surface.copy(alpha = 0.6f))
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Help & Feedback",
                    subtitle = "Get support or request features",
                    onClick = { Toast.makeText(context, "Feedback sent! Thank you.", Toast.LENGTH_SHORT).show() }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "Melofy protects your data locally",
                    onClick = { Toast.makeText(context, "Melofy protects your data locally.", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }

    // 11. Edit Profile Beautiful Glassmorphic Dialog
    val firebaseUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val googlePhotoUrl = firebaseUser?.photoUrl?.toString()
    var customAvatarUrl by remember { mutableStateOf(if (avatarScheme.startsWith("http")) avatarScheme else "") }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Surface.copy(alpha = 0.95f),
            title = {
                Text(
                    text = "Edit Profile",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = customAvatarUrl,
                        onValueChange = {
                            customAvatarUrl = it
                            if (it.isNotBlank()) {
                                selectedAvatarScheme = it
                            }
                        },
                        label = { Text("Custom Profile Photo URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!googlePhotoUrl.isNullOrBlank()) {
                        Button(
                            onClick = {
                                selectedAvatarScheme = googlePhotoUrl
                                customAvatarUrl = googlePhotoUrl
                                Toast.makeText(context, "Google profile photo selected!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary.copy(0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Use Google Profile Photo", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text(
                        text = "Or Choose Premium Photo Presets",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val imagePresets = listOf(
                        "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=150&auto=format&fit=crop&q=60", // Synthwave DJ
                        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop&q=60", // Neon Mic
                        "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=150&auto=format&fit=crop&q=60"  // Cassette Tape
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        imagePresets.forEachIndexed { idx, url ->
                            val isSelected = selectedAvatarScheme == url
                            AsyncImage(
                                model = url,
                                contentDescription = "Preset $idx",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(0.05f))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedAvatarScheme = url
                                        customAvatarUrl = url
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Or Choose Glow Theme Presets",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Avatar Color themes grid options
                    val schemes = listOf(
                        Pair("default", Brush.linearGradient(listOf(Primary, Secondary, Accent))),
                        Pair("sunset", Brush.linearGradient(listOf(Primary, Accent))),
                        Pair("ocean", Brush.linearGradient(listOf(CyanAccent, Primary))),
                        Pair("electric", Brush.linearGradient(listOf(Secondary, Accent))),
                        Pair("forest", Brush.linearGradient(listOf(Color(0xFF00E676), CyanAccent))),
                        Pair("royal", Brush.linearGradient(listOf(Color(0xFFFFD600), Primary)))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        schemes.forEach { (schemeName, brush) ->
                            val isSelected = selectedAvatarScheme == schemeName
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(brush, shape = CircleShape)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedAvatarScheme = schemeName
                                        customAvatarUrl = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            authViewModel.updateProfile(editName, selectedAvatarScheme)
                            showEditDialog = false
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEditDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
        )
    }

    // 12. Notification Inbox Glassmorphic Sheet/Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            containerColor = Surface.copy(alpha = 0.95f),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Accent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Updates Inbox",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                notifications = emptyList()
                                Toast.makeText(context, "Inbox Cleared!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All",
                                tint = Accent
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    if (notifications.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Your inbox is empty!",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary
                                )
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(notifications.size) { index ->
                                val notification = notifications[index]
                                GlassmorphicCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Mark read on click
                                            notifications = notifications.map {
                                                if (it.id == notification.id) it.copy(isUnread = false) else it
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = notification.title,
                                                fontWeight = FontWeight.Bold,
                                                color = if (notification.isUnread) Accent else TextPrimary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = notification.time,
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notification.text,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Mark all as read when closing
                        notifications = notifications.map { it.copy(isUnread = false) }
                        showNotificationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", color = Color.White)
                }
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
        )
    }
}

@Composable
fun BadgeChip(
    title: String,
    isUnlocked: Boolean,
    glowColor: Color,
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isUnlocked) glowColor.copy(0.12f) else Color.White.copy(0.02f)
            )
            .border(
                width = 1.dp,
                color = if (isUnlocked) glowColor.copy(0.5f) else Color.White.copy(0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isUnlocked) glowColor else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) TextPrimary else TextSecondary
                ),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                fontSize = 14.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun FavoriteSongRow(
    song: Song,
    onPlayClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art using Coil AsyncImage
            AsyncImage(
                model = song.artworkUrl,
                contentDescription = "Song Artwork",
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(0.05f)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    ),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(34.dp)
                    .background(Primary.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Background.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}
