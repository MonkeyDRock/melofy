package com.example.melofy.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.melofy.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isReady: Boolean = true,
    onNavigateNext: () -> Unit
) {
    val scale = remember { Animatable(0.2f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = { t -> t * (2 - t) } // Decelerate interpolation
            )
        )
    }

    LaunchedEffect(key1 = isReady) {
        if (isReady) {
            delay(1200)
            onNavigateNext()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Primary.copy(0.12f), Background),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Melofy Logo",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Melofy",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 46.sp,
                    brush = Brush.linearGradient(listOf(Color.White, Color.White.copy(0.7f)))
                )
            )

            Text(
                text = "Feel Every Beat",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}
