package com.example.melofy.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.melofy.ui.components.GlassmorphicCard
import com.example.melofy.ui.theme.Accent
import com.example.melofy.ui.theme.Background
import com.example.melofy.ui.theme.CyanAccent
import com.example.melofy.ui.theme.Primary
import com.example.melofy.ui.theme.Secondary
import com.example.melofy.ui.theme.Surface
import com.example.melofy.ui.theme.TextPrimary
import com.example.melofy.ui.theme.TextSecondary

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: String,
    val period: String,
    val description: String,
    val benefits: List<String>,
    val accentColor: Color,
    val isPopular: Boolean = false
)

@Composable
fun SubscriptionScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedPlanId by remember { mutableStateOf("premium") }

    val plans = listOf(
        SubscriptionPlan(
            id = "free",
            name = "Melofy Free",
            price = "$0",
            period = "always",
            description = "Standard music listening with audio ads.",
            benefits = listOf(
                "Access to millions of songs",
                "Standard 160kbps audio quality",
                "Online streaming only",
                "Ad-supported playback"
            ),
            accentColor = TextSecondary
        ),
        SubscriptionPlan(
            id = "premium",
            name = "Melofy Premium",
            price = "$4.99",
            period = "month",
            description = "High-fidelity audio with total offline control.",
            benefits = listOf(
                "Ad-free music streaming",
                "Ultra High-Fidelity 320kbps audio",
                "Unlimited offline song downloads",
                "Unlimited prompts for Melofy AI DJ",
                "Cancel subscription anytime"
            ),
            accentColor = Accent,
            isPopular = true
        ),
        SubscriptionPlan(
            id = "family",
            name = "Melofy Family",
            price = "$7.99",
            period = "month",
            description = "Premium audio for the whole household.",
            benefits = listOf(
                "Up to 6 unique premium profiles",
                "Shared family music playlists",
                "Explicit lyrics filter controls",
                "All Melofy Premium benefits included",
                "Ad-free listening for everyone"
            ),
            accentColor = CyanAccent
        )
    )

    // Animated glow background
    val infiniteTransition = rememberInfiniteTransition(label = "sub_glow")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(0.12f), Color.Transparent),
                        radius = size.maxDimension * 0.45f
                    ),
                    center = this.center.copy(x = this.center.x + glowOffset, y = this.center.y - 400f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Accent.copy(0.08f), Color.Transparent),
                        radius = size.maxDimension * 0.45f
                    ),
                    center = this.center.copy(x = this.center.x - glowOffset, y = this.center.y + 400f)
                )
            }
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
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
                Text(
                    text = "Melofy Subscription",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Choose Your Rhythm",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 28.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Unlock high-fidelity streaming, offline listening, and full AI DJ capabilities.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 14.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Render Plan Cards
                items(plans) { plan ->
                    PlanItemCard(
                        plan = plan,
                        isSelected = selectedPlanId == plan.id,
                        onClick = { selectedPlanId = plan.id }
                    )
                }

                // Action checkout section
                item {
                    val activePlan = plans.first { it.id == selectedPlanId }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            if (activePlan.id == "free") {
                                Toast.makeText(context, "You are currently enjoying Melofy Free!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Welcome to ${activePlan.name}! Subscription complete.", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activePlan.id == "free") Surface else Primary
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (activePlan.id == "free") Icons.Default.Check else Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (activePlan.id == "free") "Keep Current Free Plan" else "Subscribe to ${activePlan.name}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Secured checkout. Cancel or modify plans anytime directly in Google Play settings.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun PlanItemCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "plan_border")
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_border"
    )

    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(plan.accentColor, Primary, plan.accentColor),
                start = androidx.compose.ui.geometry.Offset(pulseBorder, 0f),
                end = androidx.compose.ui.geometry.Offset(pulseBorder + 300f, 300f)
            ),
            shape = RoundedCornerShape(24.dp)
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = Color.White.copy(0.08f),
            shape = RoundedCornerShape(24.dp)
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Surface.copy(0.95f) else Surface.copy(0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Info: Plan Name, Price, Popular badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (plan.isPopular) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = plan.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = plan.price,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = plan.accentColor,
                            fontSize = 24.sp
                        )
                    )
                    Text(
                        text = if (plan.period == "always") "forever" else "/${plan.period}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.06f)))
            Spacer(modifier = Modifier.height(16.dp))

            // Benefit List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plan.benefits.forEach { benefit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(plan.accentColor.copy(0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = plan.accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = benefit,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
