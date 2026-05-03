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
    targetTimeoutMs: Long = 2500L,
    minIsiMs: Long = 800L,
    maxIsiMs: Long = 1500L,
    hintThreshold: Int = 3,
    onTrialComplete: (TrialData) -> Unit,
    onGameComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTrial by remember { mutableStateOf(1) }
    var targetIndex by remember { mutableStateOf(-1) }
    var showTarget by remember { mutableStateOf(false) }
    var targetShownTime by remember { mutableStateOf(0L) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var maxStreak by remember { mutableStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(3) }
    var gameStarted by remember { mutableStateOf(false) }
    var needsNextTrial by remember { mutableStateOf(false) }

    // Hint system state
    var consecutiveErrors by remember { mutableStateOf(0) }
    var showHintForCurrentTrial by remember { mutableStateOf(false) }
    var hintCount by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    // Countdown before game starts
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(500)
            countdown--
        }
        gameStarted = true
    }

    // Show new target for each trial
    LaunchedEffect(currentTrial, gameStarted) {
        if (gameStarted && currentTrial <= totalTrials && !isProcessing) {
            delay((minIsiMs..maxIsiMs).random())
            targetIndex = Random.nextInt(9)

            // Determine hint: trigger after hintThreshold consecutive errors
            showHintForCurrentTrial = consecutiveErrors >= hintThreshold
            if (showHintForCurrentTrial) hintCount++

            showTarget = true
            targetShownTime = Clock.System.now().toEpochMilliseconds()

            // Auto-miss after timeout
            delay(targetTimeoutMs)
            if (showTarget) {
                val hintUsed = if (showHintForCurrentTrial) 1 else 0
                val trial = TrialData(
                    trialNumber = currentTrial,
                    targetPosition = targetIndex,
                    rt_raw = 2.5,
                    correct = 0,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    hint_used = hintUsed
                )
                onTrialComplete(trial)
                showTarget = false
                showHintForCurrentTrial = false
                isProcessing = true
                streak = 0
                consecutiveErrors++

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
            val reactionTimeSec = (tapTime - targetShownTime) / 1000.0
            val isCorrect = tappedIndex == targetIndex
            val hintUsed = if (showHintForCurrentTrial) 1 else 0

            val trial = TrialData(
                trialNumber = currentTrial,
                targetPosition = targetIndex,
                rt_raw = reactionTimeSec,
                correct = if (isCorrect) 1 else 0,
                timestamp = tapTime,
                hint_used = hintUsed
            )

            onTrialComplete(trial)
            showTarget = false
            showHintForCurrentTrial = false
            isProcessing = true

            if (isCorrect) {
                score++
                streak++
                if (streak > maxStreak) maxStreak = streak
                consecutiveErrors = 0
            } else {
                streak = 0
                consecutiveErrors++
            }

            needsNextTrial = true
        }
    }

    // Progress to next trial
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
        // Header card — trial, score, streak, hints
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color(0xFF0EA5E9).copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trial counter
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

                // Score
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
                            .size(24.dp)
                            .graphicsLayer { rotationZ = if (streak > 0) starRotation else 0f }
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

                // Hint counter badge — only shown once hints have been used
                if (hintCount > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Hints",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$hintCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Countdown overlay
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
            // Hint banner — visible when hint is active and target is on screen
            AnimatedVisibility(
                visible = showHintForCurrentTrial && showTarget,
                enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.9f),
                exit = fadeOut(animationSpec = tween(150))
            ) {
                val pulseAlpha by rememberInfiniteTransition().animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .graphicsLayer { alpha = pulseAlpha },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF3C7)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        Color(0xFFF59E0B).copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "💡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hint — tap the golden box!",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            // 3×3 Grid
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
                            val isHint = isTarget && showHintForCurrentTrial

                            // Animate the hint cell with an extra pulsing border
                            val infiniteTransition = rememberInfiniteTransition()
                            val hintBorderWidth by infiniteTransition.animateFloat(
                                initialValue = 4f,
                                targetValue = 8f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(400),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            val hintGlowAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 0.9f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(400),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            val normalGlowAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 0.7f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .shadow(
                                        elevation = if (isTarget) 20.dp else 8.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = when {
                                            isHint -> Color(0xFFF59E0B).copy(alpha = 0.7f)
                                            isTarget -> Color(0xFF0EA5E9).copy(alpha = 0.6f)
                                            else -> Color.Black.copy(alpha = 0.1f)
                                        }
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        when {
                                            isHint -> Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFBBF24),
                                                    Color(0xFFF59E0B)
                                                )
                                            )
                                            isTarget -> Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF0EA5E9),
                                                    Color(0xFF0284C7)
                                                )
                                            )
                                            else -> Brush.linearGradient(
                                                colors = listOf(
                                                    Color.White,
                                                    Color(0xFFF8FAFC)
                                                )
                                            )
                                        }
                                    )
                                    .border(
                                        width = if (isHint) hintBorderWidth.dp else if (isTarget) 4.dp else 3.dp,
                                        color = when {
                                            isHint -> Color(0xFFD97706).copy(alpha = hintGlowAlpha)
                                            isTarget -> Color.White
                                            else -> Color(0xFFE2E8F0)
                                        },
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
                                // Inner glow overlay
                                if (isTarget) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Color.White.copy(
                                                    alpha = if (isHint) hintGlowAlpha * 0.3f
                                                    else normalGlowAlpha
                                                )
                                            )
                                    )
                                }

                                // Icon — lamp for hint, star for normal target
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isTarget,
                                    enter = fadeIn(animationSpec = tween(100)) + scaleIn(initialScale = 0.3f),
                                    exit = fadeOut(animationSpec = tween(100)) + scaleOut(targetScale = 0.3f)
                                ) {
                                    if (isHint) {
                                        Text(
                                            text = "💡",
                                            fontSize = 48.sp
                                        )
                                    } else {
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom instruction card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = if (consecutiveErrors >= hintThreshold)
                        "Tap the golden 💡 box as quickly as you can!"
                    else
                        "Tap the highlighted box as quickly as you can!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (consecutiveErrors >= hintThreshold)
                        Color(0xFF92400E)
                    else
                        Color(0xFF475569),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
