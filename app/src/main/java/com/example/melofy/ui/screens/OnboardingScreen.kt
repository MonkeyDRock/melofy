package com.example.melofy.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateNext: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Feel Every Beat",
            description = "Explore millions of high-definition streamable tracks, curated albums, and complete discographies fetched directly from global charts."
        ),
        OnboardingPage(
            title = "Personalized Picks",
            description = "Enjoy dynamic recommended songs tailored precisely to your listening history, alongside trending billboard hits customized for you."
        ),
        OnboardingPage(
            title = "Offline Playback & Sync",
            description = "Download your favorite tracks to listen completely offline, with immediate background syncing to your secure Cloud Firestore profile."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Soft backdrop glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(0.08f), Color.Transparent),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Upper Title Header
            Text(
                text = "Melofy",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 1.5.sp,
                    color = TextPrimary
                )
            )

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]

                // Calculate real-time scroll offset fraction for this page
                val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
                val absOffset = kotlin.math.abs(pageOffset)

                // Animate properties based on page offset
                val scale = 1f - (absOffset * 0.15f).coerceIn(0f, 1f)
                val alpha = 1f - (absOffset * 0.75f).coerceIn(0f, 1f)
                val translationX = pageOffset * 180.dp.value // smooth parallax shift

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Page specific premium Canvas Animations
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                this.translationX = translationX
                            }
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(0.06f),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .background(Color.White.copy(0.02f), RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (pageIndex) {
                            0 -> SpinningVinylRecordAnimation()
                            1 -> DancingEqualizerAnimation()
                            2 -> PulsingSyncShieldAnimation()
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = page.title,
                        modifier = Modifier.graphicsLayer {
                            this.alpha = alpha
                            this.translationX = translationX * 1.4f // Faster slide for distinct parallax depth
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 32.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = page.description,
                        modifier = Modifier
                            .graphicsLayer {
                                this.alpha = alpha
                                this.translationX = translationX * 1.8f // Even faster slide for description depth
                            }
                            .padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = TextSecondary,
                            lineHeight = 26.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Navigation Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Primary else Color.White.copy(0.2f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.lastIndex) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onNavigateNext()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (pagerState.currentPage == pages.lastIndex) "Get Started" else "Next",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Forward arrow",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpinningVinylRecordAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = Modifier.size(120.dp)) {
        val center = center
        val radius = size.minDimension / 2

        // Draw Vinyl Black Base Circle
        drawCircle(
            color = Color(0xFF141414),
            radius = radius
        )

        // Draw Grooved rings
        for (i in 1..4) {
            drawCircle(
                color = Color.White.copy(0.08f),
                radius = radius * (0.25f + 0.14f * i),
                style = Stroke(width = 1.5f)
            )
        }

        // Draw Center Label Circle (Primary Green)
        rotate(rotation) {
            drawCircle(
                color = Primary,
                radius = radius * 0.32f
            )

            // Draw a detail spot on the green label to visually show rotation
            drawCircle(
                color = Color.White.copy(0.40f),
                radius = radius * 0.08f,
                center = androidx.compose.ui.geometry.Offset(center.x - radius * 0.16f, center.y)
            )
        }

        // Center Spindle Hole
        drawCircle(
            color = Background,
            radius = radius * 0.06f
        )
    }
}

@Composable
fun DancingEqualizerAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val heights = listOf(
        infiniteTransition.animateFloat(
            initialValue = 0.2f, targetValue = 0.85f,
            animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h1"
        ),
        infiniteTransition.animateFloat(
            initialValue = 0.35f, targetValue = 0.65f,
            animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h2"
        ),
        infiniteTransition.animateFloat(
            initialValue = 0.15f, targetValue = 0.95f,
            animationSpec = infiniteRepeatable(tween(850, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h3"
        ),
        infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 0.75f,
            animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h4"
        ),
        infiniteTransition.animateFloat(
            initialValue = 0.45f, targetValue = 0.55f,
            animationSpec = infiniteRepeatable(tween(750, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "h5"
        )
    )

    Row(
        modifier = Modifier
            .size(120.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { heightVal ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightVal.value)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Primary)
            )
        }
    }
}

@Composable
fun PulsingSyncShieldAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing breathing halo
        Box(
            modifier = Modifier
                .size((100 * scale).dp)
                .background(Primary.copy(alpha), CircleShape)
        )

        // Circular Shield Base
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.OfflinePin,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}
