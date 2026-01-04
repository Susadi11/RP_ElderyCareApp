package com.rp_elderycareapp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Color
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
                    trial = currentTrial,
                    targetIndex = targetIndex,
                    shownAtMs = targetShownTime - gameStartTime,
                    tapIndex = null,
                    tapAtMs = null,
                    rtRawMs = null,
                    result = "miss"
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
            val reactionTime = (tapTime - targetShownTime).toInt()
            val isCorrect = tappedIndex == targetIndex

            val trial = TrialData(
                trial = currentTrial,
                targetIndex = targetIndex,
                shownAtMs = targetShownTime - gameStartTime,
                tapIndex = tappedIndex,
                tapAtMs = tapTime - gameStartTime,
                rtRawMs = reactionTime,
                result = if (isCorrect) "hit" else "wrong"
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header with stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trial counter
                Column {
                    Text(
                        text = "Trial",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$currentTrial/$totalTrials",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                // Streak
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (streak > 0) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$streak",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (streak > 0) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Countdown overlay
        if (!gameStarted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (countdown > 0) "$countdown" else "GO!",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 120.sp
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
                                        elevation = if (isTarget) 12.dp else 4.dp,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isTarget)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = if (isTarget) 4.dp else 2.dp,
                                        color = if (isTarget)
                                            Color.White
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        handleBoxTap(boxIndex)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isTarget,
                                    enter = fadeIn(animationSpec = tween(100)),
                                    exit = fadeOut(animationSpec = tween(100))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = "Tap the highlighted box as quickly as you can!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}