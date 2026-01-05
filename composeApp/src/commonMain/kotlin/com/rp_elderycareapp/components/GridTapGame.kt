package com.rp_elderycareapp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.api.TrialData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.random.Random
import kotlinx.datetime.Clock

@Composable
fun GridTapGame(
    totalTrials: Int = 50,
    onTrialComplete: (TrialData) -> Unit,
    onGameComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTrial by remember { mutableStateOf(1) }
    var targetIndex by remember { mutableStateOf(-1) }
    var showTarget by remember { mutableStateOf(false) }
    var targetShownTime by remember { mutableStateOf(0L) }
    var gameStartTime by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var maxStreak by remember { mutableStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(3) }
    var gameStarted by remember { mutableStateOf(false) }
    var needsNextTrial by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Countdown before game starts (reduced to 500ms per count for faster start)
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(500) // Reduced from 1000ms to 500ms
            countdown--
        }
        gameStarted = true
    }

    // Show new target
    LaunchedEffect(currentTrial, gameStarted) {
        if (gameStarted && currentTrial <= totalTrials && !isProcessing) {
            delay((800..1500).random().toLong())
            targetIndex = Random.nextInt(9)
            showTarget = true
            targetShownTime = Clock.System.now().toEpochMilliseconds()

            // Auto-miss after timeout
            delay(2500)
            if (showTarget) {
                // Timeout - record miss
                val trial = TrialData(
                    trialNumber = currentTrial,
                    targetPosition = targetIndex,
                    reactionTime = 2.5,  // Max timeout in seconds
                    correct = false,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
                onTrialComplete(trial)
                showTarget = false
                isProcessing = true
                streak = 0

                delay(300)
                isProcessing = false

                if (currentTrial >= totalTrials) {
                    onGameComplete()
                } else {
                    currentTrial++
                }
            }
        }
    }

    fun handleBoxTap(tappedIndex: Int) {
        if (showTarget && !isProcessing) {
            val tapTime = Clock.System.now().toEpochMilliseconds()
            val reactionTimeMs = (tapTime - targetShownTime)
            val reactionTimeSec = reactionTimeMs / 1000.0  // Convert to seconds
            val isCorrect = tappedIndex == targetIndex

            val trial = TrialData(
                trialNumber = currentTrial,
                targetPosition = targetIndex,
                reactionTime = reactionTimeSec,
                correct = isCorrect,
                timestamp = tapTime
            )

            onTrialComplete(trial)
            showTarget = false
            isProcessing = true

            if (isCorrect) {
                score++
                streak++
                if (streak > maxStreak) maxStreak = streak
            } else {
                streak = 0
            }

            // Set flag to trigger next trial
            needsNextTrial = true
        }
    }

    // Handle next trial progression
    LaunchedEffect(needsNextTrial) {
        if (needsNextTrial) {
            delay(300)
            isProcessing = false
            needsNextTrial = false

            if (currentTrial >= totalTrials) {
                onGameComplete()
            } else {
                currentTrial++
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F9FF),
                        Color(0xFFE0F2FE)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Modern Header with stats - glassmorphism card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color(0xFF0EA5E9).copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trial counter with gradient
                Column {
                    Text(
                        text = "Trial",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$currentTrial/$totalTrials",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0EA5E9)
                    )
                }

                // Score with animation
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10B981)
                    )
                }

                // Streak with animated star
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val starRotation by infiniteTransition.animateFloat(
                        initialValue = if (streak > 0) -10f else 0f,
                        targetValue = if (streak > 0) 10f else 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (streak > 0) Color(0xFFFFD700) else Color(0xFFCBD5E1),
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer {
                                rotationZ = if (streak > 0) starRotation else 0f
                            }
                    )
                    Column {
                        Text(
                            text = "Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$streak",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (streak > 0) Color(0xFFFFD700) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Countdown overlay with modern animation
        if (!gameStarted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Text(
                    text = if (countdown > 0) "$countdown" else "GO!",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0EA5E9),
                    fontSize = 120.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                )
            }
        } else {
            // 3x3 Grid
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { row ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(3) { col ->
                            val boxIndex = row * 3 + col
                            val isTarget = boxIndex == targetIndex && showTarget

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .shadow(
                                        elevation = if (isTarget) 20.dp else 8.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = if (isTarget) 
                                            Color(0xFF0EA5E9).copy(alpha = 0.6f) 
                                        else 
                                            Color.Black.copy(alpha = 0.1f)
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isTarget)
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF0EA5E9),
                                                    Color(0xFF0284C7)
                                                )
                                            )
                                        else
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.White,
                                                    Color(0xFFF8FAFC)
                                                )
                                            )
                                    )
                                    .border(
                                        width = if (isTarget) 4.dp else 3.dp,
                                        color = if (isTarget)
                                            Color.White
                                        else
                                            Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        handleBoxTap(boxIndex)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Pulsing glow for target
                                if (isTarget) {
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val glowAlpha by infiniteTransition.animateFloat(
                                        initialValue = 0.3f,
                                        targetValue = 0.7f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(600),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Color.White.copy(alpha = glowAlpha)
                                            )
                                    )
                                }
                                
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isTarget,
                                    enter = fadeIn(animationSpec = tween(100)) + scaleIn(initialScale = 0.3f),
                                    exit = fadeOut(animationSpec = tween(100)) + scaleOut(targetScale = 0.3f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Modern Instructions with gradient text effect
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = "Tap the highlighted box as quickly as you can!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}