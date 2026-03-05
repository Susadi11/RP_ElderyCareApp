package com.rp_elderycareapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MmseResultScreen(
    score: Int,
    maxScore: Int = 30,
    onNavigateToHome: () -> Unit = {},
    onViewDetails: () -> Unit = {}
) {
    // Determine risk level based on score
    val (riskLevel, riskColor, riskEmoji) = when {
        score >= 26 -> Triple("Normal", Color(0xFF10B981), "🎉")
        score >= 20 -> Triple("Mild", Color(0xFFFBBF24), "⚠️")
        score >= 10 -> Triple("Moderate", Color(0xFFF97316), "⚠️")
        else -> Triple("Severe", Color(0xFFEF4444), "🚨")
    }

    val recommendation = when {
        score >= 26 -> "Score is within normal range. Continue regular monitoring."
        score >= 20 -> "Mild cognitive impairment detected. Consider follow-up assessment."
        score >= 10 -> "Moderate cognitive impairment. Recommend comprehensive medical evaluation."
        else -> "Severe cognitive impairment. Urgent medical evaluation recommended."
    }

    // Animations
    val offsetY = remember { Animatable(50f) }
    val alpha = remember { Animatable(0f) }
    val scoreScale = remember { Animatable(0f) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        launch {
            delay(300)
            scoreScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        if (score >= 26) {
            delay(800)
            showConfetti = true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F4F8),
                        Color(0xFFF5F9FB)
                    )
                )
            ),
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .offset(y = offsetY.value.dp)
                .graphicsLayer { this.alpha = alpha.value },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // Success Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = riskColor.copy(alpha = 0.4f)
                            )
                            .background(
                                color = riskColor.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = riskEmoji,
                            fontSize = 40.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Test Completed!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Great job completing the MMSE",
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Score Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(32.dp)
                        )
                        .graphicsLayer {
                            scaleX = scoreScale.value
                            scaleY = scoreScale.value
                        },
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        riskColor.copy(alpha = 0.15f),
                                        riskColor.copy(alpha = 0.05f),
                                        Color.White
                                    )
                                )
                            )
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your Score",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "$score",
                                    fontSize = 88.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor,
                                    lineHeight = 88.sp
                                )
                                Text(
                                    text = " / $maxScore",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF6B7280),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Animated Progress Bar
                            val progressAnimation = remember { Animatable(0f) }
                            LaunchedEffect(Unit) {
                                delay(600)
                                progressAnimation.animateTo(
                                    targetValue = score.toFloat() / maxScore.toFloat(),
                                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .background(
                                        color = Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressAnimation.value)
                                        .height(16.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    riskColor,
                                                    riskColor.copy(alpha = 0.8f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${(score.toFloat() / maxScore.toFloat() * 100).toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            // Risk Level Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = riskColor.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        score >= 26 -> Icons.Default.CheckCircle
                                        score >= 20 -> Icons.Default.Warning
                                        score >= 10 -> Icons.Default.Info
                                        else -> Icons.Default.Error
                                    },
                                    contentDescription = "Risk Level",
                                    tint = riskColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Risk Assessment",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF6B7280)
                                )
                                Text(
                                    text = riskLevel,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Divider(
                            color = Color(0xFFE5E7EB),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Score Categories
                        ScoreCategoryItem("26-30", "Normal", score >= 26)
                        Spacer(modifier = Modifier.height(12.dp))
                        ScoreCategoryItem("20-25", "Mild", score in 20..25)
                        Spacer(modifier = Modifier.height(12.dp))
                        ScoreCategoryItem("10-19", "Moderate", score in 10..19)
                        Spacer(modifier = Modifier.height(12.dp))
                        ScoreCategoryItem("0-9", "Severe", score in 0..9)
                    }
                }
            }

            // Recommendation Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF3C7)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(end = 16.dp, top = 4.dp)
                        )
                        Column {
                            Text(
                                text = "Recommendation",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = recommendation,
                                fontSize = 15.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // View Detailed Report Button
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(30.dp),
                                ambientColor = Color(0xFF4A9FFF).copy(alpha = 0.5f)
                            ),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF4A9FFF),
                                            Color(0xFF3B82F6)
                                        )
                                    ),
                                    shape = RoundedCornerShape(30.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = "Details",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "View Detailed Report",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Back to Home Button
                    OutlinedButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1A1A2E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = Color(0xFFE5E7EB)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = Color(0xFF1A1A2E),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Back to Home",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ScoreCategoryItem(
    range: String,
    label: String,
    isActive: Boolean
) {
    val backgroundColor = if (isActive) {
        when (label) {
            "Normal" -> Color(0xFFD1FAE5)
            "Mild" -> Color(0xFFFEF3C7)
            "Moderate" -> Color(0xFFFED7AA)
            "Severe" -> Color(0xFFFEE2E2)
            else -> Color(0xFFF3F4F6)
        }
    } else {
        Color(0xFFF3F4F6)
    }

    val textColor = if (isActive) {
        when (label) {
            "Normal" -> Color(0xFF065F46)
            "Mild" -> Color(0xFF78350F)
            "Moderate" -> Color(0xFF9A3412)
            "Severe" -> Color(0xFF991B1B)
            else -> Color(0xFF6B7280)
        }
    } else {
        Color(0xFF9CA3AF)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = "Score $range",
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }

        if (isActive) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = textColor,
                        shape = CircleShape
                    )
            )
        }
    }
}