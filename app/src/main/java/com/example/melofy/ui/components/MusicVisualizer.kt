package com.example.melofy.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary

@Composable
fun MusicVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 12,
    barWidth: Dp = 4.dp,
    barGap: Dp = 3.dp,
    maxHeight: Dp = 40.dp
) {
    val transition = rememberInfiniteTransition(label = "visualizer")
    
    // We animate different bars with offset durations to create a organic, wave-like movement
    val animations = (0 until barCount).map { index ->
        val duration = 600 + (index * 130) % 500
        transition.animateFloat(
            initialValue = 0.15f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(barGap),
        verticalAlignment = Alignment.Bottom
    ) {
        val colors = listOf(Primary, Secondary, Accent)
        
        animations.forEachIndexed { index, animState ->
            val scale = if (isPlaying) animState.value else 0.15f
            Canvas(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight()
            ) {
                val height = size.height * scale
                val top = size.height - height
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(colors[index % colors.size], colors[(index + 1) % colors.size])
                    ),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, height),
                    cornerRadius = CornerRadius(size.width / 2, size.width / 2)
                )
            }
        }
    }
}
