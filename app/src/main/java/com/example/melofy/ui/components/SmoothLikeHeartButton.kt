package com.example.melofy.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.TextSecondary

@Composable
fun SmoothLikeHeartButton(
    isLiked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp
) {
    var clickedTrigger by remember { mutableStateOf(false) }

    // Trigger the spring pop and ring explosion whenever liked state changes to true
    LaunchedEffect(isLiked) {
        if (isLiked) {
            clickedTrigger = true
            kotlinx.coroutines.delay(350)
            clickedTrigger = false
        }
    }

    // Spring scale bounce animation
    val scale by animateFloatAsState(
        targetValue = if (clickedTrigger) 1.4f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "heart_scale"
    )

    // Fading burst progress
    val burstRadiusProgress by animateFloatAsState(
        targetValue = if (clickedTrigger) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 350),
        label = "burst_progress"
    )

    // Smooth color change
    val heartColor by animateColorAsState(
        targetValue = if (isLiked) Primary else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "heart_color"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
            .drawBehind {
                if (burstRadiusProgress > 0f) {
                    val maxRadius = size.minDimension * 0.95f
                    // Glowing outer ring expanding and fading
                    drawCircle(
                        color = Primary.copy(alpha = 1f - burstRadiusProgress),
                        radius = maxRadius * burstRadiusProgress,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = heartColor,
            modifier = Modifier
                .size(iconSize)
                .scale(scale)
        )
    }
}
