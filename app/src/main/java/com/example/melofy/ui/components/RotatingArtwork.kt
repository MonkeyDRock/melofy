package com.example.melofy.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary

@Composable
fun RotatingArtwork(
    artworkUrl: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "artwork_rotation")
    
    val rotationAngleState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val rotationAngle = if (isPlaying) rotationAngleState.value else 0f

    Box(
        modifier = modifier
            .size(size)
            .border(
                width = 3.dp,
                brush = Brush.sweepGradient(listOf(Primary, Secondary, Accent, Primary)),
                shape = CircleShape
            )
            .padding(6.dp)
    ) {
        AsyncImage(
            model = artworkUrl,
            contentDescription = "Album Artwork",
            modifier = Modifier
                .size(size - 12.dp)
                .rotate(rotationAngle)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
