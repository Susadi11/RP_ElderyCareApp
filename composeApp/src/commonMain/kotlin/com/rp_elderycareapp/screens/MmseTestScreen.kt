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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
    val offsetY = remember { Animatable(50f) }
    val alpha = remember { Animatable(0f) }
    val scrollState = rememberScrollState()

    val assessments by viewModel.mmseScores
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.fetchMmseAssessments(userId)
        }
        launch {
            offsetY.animateTo(0f, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            alpha.animateTo(1f, tween(600))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F4F8), Color(0xFFF5F9FB))
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
                    Text(text = errorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchMmseAssessments(userId) }) { Text("Retry") }
                }
            } else {
                // Bento Grid Insights
                BentoGridInsights(viewModel = viewModel)

                Spacer(modifier = Modifier.height(32.dp))

                // Progress Graph
                MmseScoreGraph(scores = assessments)

                Spacer(modifier = Modifier.height(32.dp))

                // Detailed History
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Detailed History", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    Text(text = "${assessments.size} tests", fontSize = 14.sp, color = Color(0xFF6B7280))
                }

                Spacer(modifier = Modifier.height(16.dp))

                assessments.forEach { assessment ->
                    AssessmentHistoryItem(assessment = assessment)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private const val RAW_MAX_SCORE = 20f
private const val DISPLAY_MAX_SCORE = 30f

private fun scaleScore(rawScore: Float): Float {
    return (rawScore / RAW_MAX_SCORE) * DISPLAY_MAX_SCORE
}

@Composable
fun BentoGridInsights(viewModel: MmseViewModel) {
    val latestScore by viewModel.latestScore
    val trendStatus by viewModel.trendStatus
    val completionRate by viewModel.completionRate

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Cognitive Insights",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isSmallScreen = maxWidth < 340.dp
            val gridHeight = if (isSmallScreen) 280.dp else 200.dp
            
            if (isSmallScreen) {
                // Stacked layout for very small screens
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                        InsightCard(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            title = "Latest Score",
                            value = latestScore?.let { scaleScore(it.toFloat()).toInt().toString() } ?: "--",
                            subtitle = "Total: 30 pts",
                            icon = Icons.Default.Assessment,
                            containerColor = Color(0xFF4A9FFF),
                            contentColor = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        InsightCard(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            title = "Trend",
                            value = trendStatus,
                            icon = when(trendStatus) {
                                "Improving" -> Icons.Default.TrendingUp
                                "Declining" -> Icons.Default.TrendingDown
                                else -> Icons.Default.Sync
                            },
                            containerColor = Color.White,
                            iconColor = when(trendStatus) {
                                "Improving" -> Color(0xFF10B981)
                                "Declining" -> Color(0xFFEF4444)
                                else -> Color(0xFF4A9FFF)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    InsightCard(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        title = "Streak",
                        value = "$completionRate%",
                        icon = Icons.Default.Whatshot,
                        containerColor = Color.White,
                        iconColor = Color(0xFFF59E0B)
                    )
                }
            } else {
                // Bento Layout
                Row(modifier = Modifier.fillMaxWidth().height(gridHeight)) {
                    // Main Card: Latest Score
                    InsightCard(
                        modifier = Modifier.weight(1.3f).fillMaxHeight(),
                        title = "Latest Score",
                        value = latestScore?.toString() ?: "--",
                        subtitle = "Total: 30 pts",
                        icon = Icons.Default.Assessment,
                        containerColor = Color(0xFF4A9FFF),
                        contentColor = Color.White
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        // Secondary Card: Trend
                        InsightCard(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            title = "Trend",
                            value = trendStatus,
                            icon = when(trendStatus) {
                                "Improving" -> Icons.Default.TrendingUp
                                "Declining" -> Icons.Default.TrendingDown
                                else -> Icons.Default.Sync
                            },
                            containerColor = Color.White,
                            iconColor = when(trendStatus) {
                                "Improving" -> Color(0xFF10B981)
                                "Declining" -> Color(0xFFEF4444)
                                else -> Color(0xFF4A9FFF)
                            }
                        )
                        
//                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Tertiary Card: Completion
//                        InsightCard(
//                            modifier = Modifier.weight(1f).fillMaxWidth(),
//                            title = "Streak",
//                            value = "$completionRate%",
//                            icon = Icons.Default.Whatshot,
//                            containerColor = Color.White,
//                            iconColor = Color(0xFFF59E0B)
//                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsightCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    containerColor: Color = Color.White,
    contentColor: Color = Color(0xFF1A1A2E),
    iconColor: Color? = null
) {
    Card(
        modifier = modifier.shadow(
            elevation = 8.dp, 
            shape = RoundedCornerShape(24.dp), 
            ambientColor = Color.Black.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor ?: (if (containerColor == Color.White) Color(0xFF4A9FFF) else Color.White.copy(alpha = 0.8f)),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title, 
                    fontSize = 16.sp,
                    color = contentColor.copy(alpha = 0.7f), 
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            
            Column {
                Text(
                    text = value, 
                    fontSize = if (value.length > 8) 24.sp else 28.sp,
                    fontWeight = FontWeight.Bold, 
                    color = contentColor,
                    maxLines = 1
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle, 
                        fontSize = 18.sp,
                        color = contentColor.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun MmseTestCard(alpha: Float, onStartAssessmentClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = 0.08f)).graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(80.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = "Brain Icon", modifier = Modifier.size(40.dp), tint = Color(0xFF4A9FFF))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "MMSE Cognitive Test", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Voice-based cognitive screening", fontSize = 16.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStartAssessmentClick,
                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(28.dp), ambientColor = Color(0xFF4A9FFF).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A9FFF))
            ) {
                Text(text = "Start Assessment", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun AssessmentHistoryItem(assessment: MmseAssessment) {
    val scaledScore = scaleScore(assessment.total_score)
    val scorePercentage = scaledScore / DISPLAY_MAX_SCORE
    val scoreColor = when {
        scaledScore >= 26 -> Color(0xFF10B981) // Normal
        scaledScore >= 20 -> Color(0xFFF59E0B) // Mild
        scaledScore >= 10 -> Color(0xFFFF8C00) // Moderate
        else -> Color(0xFFEF4444)              // Severe
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).background(scoreColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Text(text = scaledScore.toInt().toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = scoreColor)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Assessment Score", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = formatDate(assessment.assessment_date), fontSize = 13.sp, color = Color(0xFF6B7280))
                }
            }
            assessment.ml_summary?.ml_risk_label?.let { label ->
                Box(modifier = Modifier.background(if (label == "Control") Color(0xFFE0F2F1) else Color(0xFFFFEBEE), RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (label == "Control") Color(0xFF00796B) else Color(0xFFC62828))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFD1D5DB))
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val parts = dateStr.split("T")[0].split("-")
        val month = when (parts[1]) {
            "01" -> "Jan" "02" -> "Feb" "03" -> "Mar" "04" -> "Apr" "05" -> "May" "06" -> "Jun"
            "07" -> "Jul" "08" -> "Aug" "09" -> "Sep" "10" -> "Oct" "11" -> "Nov" "12" -> "Dec"
            else -> parts[1]
        }
        "$month ${parts[2]}, ${parts[0]}"
    } catch (e: Exception) { dateStr }
}

@Composable
fun MmseScoreGraph(scores: List<MmseAssessment>) {
    val chronologicalScores = scores.sortedBy { it.assessment_date }
    val maxScore = DISPLAY_MAX_SCORE
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(chronologicalScores) {
        animationProgress.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Score Trend", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    Text(text = "Progress over time", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
                Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = Color(0xFF4A9FFF), modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (chronologicalScores.size < 2) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text(text = "More sessions needed for trend", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val spacing = width / (chronologicalScores.size - 1)
                        val points = chronologicalScores.mapIndexed { index, assessment ->
                            val x = index * spacing
                            val scaled = scaleScore(assessment.total_score)
                            val targetY = height - (scaled / maxScore) * height
                            Offset(x, height - (height - targetY) * animationProgress.value)
                        }

                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                cubicTo(prev.x + (curr.x - prev.x) / 2f, prev.y, prev.x + (curr.x - prev.x) / 2f, curr.y, curr.x, curr.y)
                            }
                        }

                        drawPath(path = path, color = Color(0xFF4A9FFF), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(
                            path = Path().apply { addPath(path); lineTo(points.last().x, height); lineTo(points.first().x, height); close() },
                            brush = Brush.verticalGradient(colors = listOf(Color(0xFF4A9FFF).copy(alpha = 0.2f), Color.Transparent))
                        )
                        points.forEach { drawCircle(Color.White, radius = 6.dp.toPx(), center = it); drawCircle(Color(0xFF4A9FFF), radius = 4.dp.toPx(), center = it) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chronologicalScores.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    chronologicalScores.forEachIndexed { index, _ ->
                        Text(text = "S${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
