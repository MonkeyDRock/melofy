package com.example.melofy.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.melofy.ui.components.GlassmorphicCard
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Surface
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary
import com.example.melofy.ui.viewmodel.PodcastViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PodcastPlayerScreen(
    viewModel: PodcastViewModel,
    onBackClick: () -> Unit
) {
    val activeEpisode by viewModel.activeEpisode.collectAsState()
    val episode = activeEpisode ?: return

    var selectedSpeed by remember { mutableStateOf("1.0x") }
    var isLiked by remember { mutableStateOf(false) }

    // Official YouTube Iframe API HTML - guaranteed to run natively and autoplay on Android WebViews!
    val htmlData = remember(episode.id) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body, html {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    background-color: #000000;
                    overflow: hidden;
                }
                #player {
                    width: 100%;
                    height: 100%;
                    border: none;
                }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        height: '100%',
                        width: '100%',
                        videoId: '${episode.id}',
                        playerVars: {
                            'autoplay': 1,
                            'playsinline': 1,
                            'controls': 1,
                            'modestbranding': 1,
                            'rel': 0,
                            'showinfo': 0,
                            'iv_load_policy': 3,
                            'fs': 1
                        },
                        events: {
                            'onReady': onPlayerReady
                        }
                    });
                }
                function onPlayerReady(event) {
                    event.target.playVideo();
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Header Bar ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Accent,
                                letterSpacing = 2.sp,
                                fontSize = 9.sp
                            )
                        )
                    }
                    Text(
                        text = "Melofy Podcast Show",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            // ─── Cyberpunk Video Container ────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(230.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(Accent, Primary)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            // Enable Hardware Layer rendering explicitly for high-perf HTML5 video playback
                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                            
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                mediaPlaybackRequiresUserGesture = false
                                javaScriptCanOpenWindowsAutomatically = true
                                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                
                                // Set standard Chrome User Agent to satisfy YouTube security / bot detection policies
                                userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                            }
                            webViewClient = WebViewClient()
                            webChromeClient = WebChromeClient()
                            
                            // Load using YouTube BaseURL origin to bypass any domain restriction blocks
                            loadDataWithBaseURL("https://www.youtube.com", htmlData, "text/html", "UTF-8", null)
                            tag = episode.id
                        }
                    },
                    update = { webView ->
                        if (webView.tag != episode.id) {
                            webView.loadDataWithBaseURL("https://www.youtube.com", htmlData, "text/html", "UTF-8", null)
                            webView.tag = episode.id
                        }
                    }
                )
            }

            // ─── Interactive Player Control Desk ─────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Surface.copy(0.6f), RoundedCornerShape(12.dp))
                        .clickable { isLiked = !isLiked }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = if (isLiked) Accent else TextSecondary.copy(0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLiked) "Liked" else "Like",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isLiked) Accent else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Speed Selector Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1.0x", "1.25x", "1.5x", "2.0x").forEach { speed ->
                        val isActive = selectedSpeed == speed
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isActive) Accent else Surface.copy(0.6f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedSpeed = speed }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = speed,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isActive) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── Episode Details & Metadata Card ──────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Beautiful Host Avatar & Metadata section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface.copy(0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Host initials placeholder avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Accent, Primary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = episode.host.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = episode.host,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Main Host & Moderator",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }

                    // Category badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Primary.copy(0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = episode.category.uppercase(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Scrollable Description Panel
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EPISODE SUMMARY",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Accent,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = episode.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    DisposableEffect(episode.id) {
        onDispose {
            // Automatic clean-up
        }
    }
}
