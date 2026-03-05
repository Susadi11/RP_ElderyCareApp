package com.rp_elderycareapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.api.MmseAssessment
import com.rp_elderycareapp.viewmodel.MmseViewModel
import kotlinx.coroutines.launch

@Composable
fun MmseTestScreen(
    userId: String,
    viewModel: MmseViewModel = remember { MmseViewModel() },
    onStartAssessmentClick: () -> Unit = {}
) {
    // Animation for entrance
    val offsetY = remember { Animatable(50f) }
    val alpha = remember { Animatable(0f) }

    // Scroll state for making the content scrollable
    val scrollState = rememberScrollState()

    val assessments by viewModel.mmseScores
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.fetchMmseAssessments(userId)
        }
        
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
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F4F8),
                        Color(0xFFF5F9FB)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .offset(y = offsetY.value.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(38.dp))

            // Main Test Card
            MmseTestCard(alpha = alpha.value, onStartAssessmentClick = onStartAssessmentClick)

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4A9FFF))
                }
            } else if (errorMessage != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.fetchMmseAssessments(userId) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A9FFF))
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                MmseScoreGraph(scores = assessments)
            }

            // Extra bottom spacing for better scrolling experience
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
private fun MmseTestCard(alpha: Float, onStartAssessmentClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            )
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brain Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFFE3F2FD),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Brain Icon",
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF4A9FFF)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Test Title
            Text(
                text = "MMSE Cognitive Test",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Test Description
            Text(
                text = "Voice-based cognitive\nscreening to assess memory,\nattention, and thinking abilities",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Start Assessment Button
            Button(
                onClick = onStartAssessmentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color(0xFF4A9FFF).copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A9FFF)
                )
            ) {
                Text(
                    text = "Start Assessment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MmseScoreGraph(scores: List<MmseAssessment>) {
    val maxScore = 30
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(scores) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MMSE Score History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        text = if (scores.isEmpty()) "No assessment history found" else "Recent assessment trends",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF4A9FFF),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (scores.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (scores.size == 1) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Score: ${scores[0].total_score.toInt()}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A9FFF)
                            )
                            Text(
                                text = "Need at least 2 sessions for trend",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    } else {
                        Text(
                            text = "Take your first assessment to see history",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        // Grid lines (Horizontal)
                        val gridLines = 5
                        for (i in 0..gridLines) {
                            val y = height - (i.toFloat() / gridLines) * height
                            drawLine(
                                color = Color(0xFFF1F5F9),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val spacing = width / (scores.size - 1)
                        val points = scores.mapIndexed { index, assessment ->
                            val x = index * spacing
                            val targetY = height - (assessment.total_score / maxScore) * height
                            // Animate from bottom to targetY
                            val y = height - (height - targetY) * animationProgress.value
                            Offset(x, y)
                        }

                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                // Smooth curve calculation
                                val cp1 = Offset(prev.x + (curr.x - prev.x) / 2f, prev.y)
                                val cp2 = Offset(prev.x + (curr.x - prev.x) / 2f, curr.y)
                                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                            }
                        }

                        // Gradient Fill
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }
                        
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF4A9FFF).copy(alpha = 0.2f),
                                    Color(0xFF4A9FFF).copy(alpha = 0.0f)
                                )
                            )
                        )

                        // Main Line
                        drawPath(
                            path = path,
                            color = Color(0xFF4A9FFF),
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Data Points
                        points.forEach { point ->
                            drawCircle(
                                color = Color.White,
                                radius = 6.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color(0xFF4A9FFF),
                                radius = 4.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // X-axis labels
            if (scores.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    scores.forEachIndexed { index, _ ->
                        Text(
                            text = "S${index + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
