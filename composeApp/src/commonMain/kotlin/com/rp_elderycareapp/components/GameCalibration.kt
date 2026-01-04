package com.rp_elderycareapp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

@Composable
fun GameCalibration(
    onCalibrationComplete: (List<Int>) -> Unit,
    onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var calibrationStep by remember { mutableStateOf(0) }
    var tapTimes by remember { mutableStateOf(listOf<Int>()) }
    var showCircle by remember { mutableStateOf(false) }
    var circleAppearTime by remember { mutableStateOf(0L) }
    var isProcessing by remember { mutableStateOf(false) }

    val totalSteps = 12
    val progress = calibrationStep.toFloat() / totalSteps

    LaunchedEffect(calibrationStep) {
        if (calibrationStep > 0 && calibrationStep <= totalSteps && !isProcessing) {
            // Random delay between 0.8-2 seconds (reduced for faster calibration)
            delay((800..2000).random().toLong())
            showCircle = true
            circleAppearTime = Clock.System.now().toEpochMilliseconds()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Motor Calibration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (calibrationStep == 0) {
            // Instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📍 Instructions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• A circle will appear randomly\n" +
                                "• Tap it as quickly as you can\n" +
                                "• Repeat 12 times\n" +
                                "• This helps us measure your response time",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 24.sp
                    )
                }
            }

            Button(
                onClick = { calibrationStep = 1 },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Start Calibration", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            if (onSkip != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Skip Calibration", fontSize = 16.sp)
                }
            }
        } else if (calibrationStep <= totalSteps) {
            // Progress indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Step $calibrationStep of $totalSteps",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tap area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (showCircle && !isProcessing) {
                            val reactionTime = (Clock.System.now().toEpochMilliseconds() - circleAppearTime).toInt()
                            tapTimes = tapTimes + reactionTime
                            showCircle = false
                            isProcessing = true
                            calibrationStep++

                            // Reset for next trial
                            if (calibrationStep <= totalSteps) {
                                isProcessing = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showCircle,
                    enter = scaleIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TAP!",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!showCircle && calibrationStep <= totalSteps) {
                    Text(
                        text = "Wait for the circle...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Completion
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "✓ Calibration Complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Processing your results...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
            }

            LaunchedEffect(Unit) {
                delay(500) // Reduced from 1000ms
                onCalibrationComplete(tapTimes)
            }
        }
    }
}