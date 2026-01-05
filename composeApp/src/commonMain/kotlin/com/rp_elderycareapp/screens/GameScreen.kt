package com.rp_elderycareapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rp_elderycareapp.api.GameApi
import com.rp_elderycareapp.components.GameCalibration
import com.rp_elderycareapp.components.GridTapGame
import com.rp_elderycareapp.data.GameRepository
import com.rp_elderycareapp.viewmodels.GameState
import com.rp_elderycareapp.viewmodels.GameViewModel
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    userId: String = "user123", // Replace with actual user ID from your auth system
    onNavigateBack: () -> Unit = {}
) {
    val repository = remember { GameRepository(GameApi()) }
    val viewModel: GameViewModel = remember {
        GameViewModel(repository, userId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var gameStartTime by remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cognitive Game", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState.gameState) {
                is GameState.Idle, is GameState.NeedCalibration -> {
                    GameCalibration(
                        onCalibrationComplete = { tapTimes ->
                            viewModel.submitCalibration(tapTimes)
                        },
                        onSkip = {
                            viewModel.skipCalibration()
                        }
                    )
                }

                is GameState.Calibrating -> {
                    LoadingScreen("Calibrating your response time...")
                }

                is GameState.Ready -> {
                    GameReadyScreen(
                        stats = uiState.stats,
                        onStartGame = {
                            gameStartTime = Clock.System.now().toEpochMilliseconds()
                            viewModel.startGame()
                        }
                    )
                }

                is GameState.Playing -> {
                    GridTapGame(
                        totalTrials = uiState.totalTrials,
                        onTrialComplete = { trial ->
                            viewModel.recordTrial(trial)
                        },
                        onGameComplete = {
                            val endTime = Clock.System.now().toEpochMilliseconds()
                            viewModel.submitGameSession(gameStartTime, endTime)
                        }
                    )
                }

                is GameState.Results -> {
                    ResultsScreen(
                        response = state.response,
                        totalTrials = uiState.totalTrials,
                        score = uiState.score,
                        streak = uiState.streak,
                        onPlayAgain = { viewModel.resetToReady() },
                        onExit = onNavigateBack
                    )
                }

                is GameState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.resetToReady() },
                        onExit = onNavigateBack
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun GameReadyScreen(
    stats: com.rp_elderycareapp.api.UserStatsResponse?,
    onStartGame: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F9FF),
                        Color(0xFFE0F2FE),
                        Color(0xFFBAE6FD)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Animated header
            Text(
                text = "🎮",
                fontSize = 64.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ready to Play!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0C4A6E)
            )

            Spacer(modifier = Modifier.height(32.dp))

// Modern Stats Card
            stats?.let {
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
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Progress",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C4A6E)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ModernStatItem("Sessions", "${it.totalSessions}", Color(0xFF0EA5E9))
                            ModernStatItem("Avg SAC", "${(it.avgSAC * 1000).toInt() / 1000.0}", Color(0xFF10B981))
                            ModernStatItem("Avg IES", "${(it.avgIES * 1000).toInt() / 1000.0}", Color(0xFFF59E0B))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Risk level badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (it.currentRiskLevel) {
                                "LOW" -> Color(0xFF10B981).copy(alpha = 0.1f)
                                "MEDIUM" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                "HIGH" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                                else -> Color(0xFF94A3B8).copy(alpha = 0.1f)
                            }
                        ) {
                            Text(
                                text = "Risk Level: ${it.currentRiskLevel}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = when (it.currentRiskLevel) {
                                    "LOW" -> Color(0xFF10B981)
                                    "MEDIUM" -> Color(0xFFF59E0B)
                                    "HIGH" -> Color(0xFFEF4444)
                                    else -> Color(0xFF64748B)
                                },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Modern Instructions Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📖",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "How to Play",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0C4A6E)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "• Watch the 3×3 grid carefully\n" +
                            "• A box will light up randomly\n" +
                            "• Tap it as quickly as you can\n" +
                            "• Complete 50 trials\n" +
                            "• Try to build a streak!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF475569),
                    lineHeight = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // Modern gradient button
        Button(
            onClick = onStartGame,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(36.dp),
                    ambientColor = Color(0xFF0EA5E9).copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(36.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0EA5E9),
                                Color(0xFF06B6D4),
                                Color(0xFF14B8A6)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Start Game",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
    }
}

@Composable
fun ModernStatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = color.copy(alpha = 0.3f)
                )
                .background(
                    color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ResultsScreen(
    response: com.rp_elderycareapp.api.GameSessionResponse,
    totalTrials: Int,
    score: Int,
    streak: Int,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    // Celebration animation
    val infiniteTransition = rememberInfiniteTransition()
    val celebrationScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF0F9FF),
                            Color(0xFFDCFCE7),
                            Color(0xFFFEF3C7)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Animated celebration emoji
            Text(
                text = "🎉",
                fontSize = 80.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = celebrationScale
                    scaleY = celebrationScale
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Game Complete!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0C4A6E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Modern Score Card with gradient
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color(0xFF0EA5E9).copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0EA5E9),
                                    Color(0xFF06B6D4),
                                    Color(0xFF14B8A6)
                                )
                            )
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Score",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "$score / $totalTrials",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${(response.features.accuracy * 100).toInt()}% Accuracy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

        Spacer(modifier = Modifier.height(24.dp))

        // Modern Risk Assessment Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (response.prediction.riskLevel) {
                    "LOW" -> Color(0xFF10B981).copy(alpha = 0.1f)
                    "MEDIUM" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                    "HIGH" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                    else -> Color(0xFFF8FAFC)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (response.prediction.riskLevel) {
                            "LOW" -> "✅"
                            "MEDIUM" -> "⚠️"
                            "HIGH" -> "🔴"
                            else -> "ℹ️"
                        },
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Risk Assessment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0C4A6E)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (response.prediction.riskLevel) {
                        "LOW" -> Color(0xFF10B981).copy(alpha = 0.15f)
                        "MEDIUM" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        "HIGH" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        else -> Color(0xFF94A3B8).copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = response.prediction.riskLevel,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (response.prediction.riskLevel) {
                            "LOW" -> Color(0xFF10B981)
                            "MEDIUM" -> Color(0xFFF59E0B)
                            "HIGH" -> Color(0xFFEF4444)
                            else -> Color(0xFF64748B)
                        },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Score: ${response.prediction.riskScore0_100.toInt()}/100",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Modern Performance Metrics Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Performance Metrics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0C4A6E)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                ModernMetricRow("⚡ Response Time", "${(response.features.avgReactionTime * 1000).toInt()} ms", Color(0xFF0EA5E9))
                ModernMetricRow("🎯 Speed-Accuracy", "${(response.features.sac * 1000).toInt() / 1000.0}", Color(0xFF10B981))
                ModernMetricRow("⚙️ Efficiency (IES)", "${(response.features.ies * 1000).toInt() / 1000.0}", Color(0xFFF59E0B))
                ModernMetricRow("📈 RT Variability", "${(response.features.rtVariability * 1000).toInt()} ms", Color(0xFF8B5CF6))
                ModernMetricRow("🔥 Best Streak", "$streak hits", Color(0xFFEF4444))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Session Info Badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF475569).copy(alpha = 0.1f)
        ) {
            Text(
                text = "📋 Session ID: ${response.sessionId}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Modern gradient action button
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0xFF0EA5E9).copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0EA5E9),
                                Color(0xFF06B6D4),
                                Color(0xFF14B8A6)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🎮 Play Again",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(2.dp, Color(0xFF0EA5E9).copy(alpha = 0.3f))
        ) {
            Text(
                "← Exit",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0EA5E9)
            )
        }
            }
        }
    }

@Composable
fun ModernMetricRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF475569)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    // Shaking animation for error
    val infiniteTransition = rememberInfiniteTransition()
    val shake by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(100),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFEF2F2),
                            Color(0xFFFEE2E2),
                            Color(0xFFFECDD3)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated error icon
            Text(
                text = "⚠️",
                fontSize = 80.sp,
                modifier = Modifier.graphicsLayer {
                    translationX = shake
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFDC2626),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = Color(0xFFEF4444).copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFEF4444),
                                    Color(0xFFF97316),
                                    Color(0xFFFB923C)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🔄 Try Again",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(2.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
            ) {
                Text(
                    "← Go Back",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}