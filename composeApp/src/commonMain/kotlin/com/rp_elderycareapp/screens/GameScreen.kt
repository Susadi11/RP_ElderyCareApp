package com.rp_elderycareapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rp_elderycareapp.api.GameApi
import com.rp_elderycareapp.components.GameCalibration
import com.rp_elderycareapp.components.GridTapGame
import com.rp_elderycareapp.data.GameRepository
import com.rp_elderycareapp.viewmodel.AuthViewModel
import com.rp_elderycareapp.viewmodels.DifficultyConfig
import com.rp_elderycareapp.viewmodels.GameState
import com.rp_elderycareapp.viewmodels.GameViewModel
import com.rp_elderycareapp.viewmodels.difficultyForRisk
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    authViewModel: AuthViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToTrends: () -> Unit = {}
) {
    val userId = authViewModel?.currentUser?.value?.user_id ?: ""
    val token  = authViewModel?.getAccessToken() ?: ""
    val repository = remember { GameRepository(GameApi()) }
    val viewModel: GameViewModel = remember(userId, token) {
        GameViewModel(repository, userId, token)
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
                        onCalibrationComplete = { tapTimes -> viewModel.submitCalibration(tapTimes) },
                        onSkip = { viewModel.skipCalibration() }
                    )
                }
                is GameState.Calibrating -> {
                    LoadingScreen("Calibrating your response time...")
                }
                is GameState.Ready -> {
                    GameReadyScreen(
                        stats = uiState.stats,
                        motorBaseline = uiState.motorBaseline,
                        difficulty = uiState.difficulty,
                        onStartGame = {
                            gameStartTime = Clock.System.now().toEpochMilliseconds()
                            viewModel.startGame()
                        },
                        onRecalibrate = { viewModel.forceRecalibration() },
                        onViewTrends = onNavigateToTrends
                    )
                }
                is GameState.Playing -> {
                    val diff = uiState.difficulty
                    GridTapGame(
                        totalTrials     = diff.totalTrials,
                        targetTimeoutMs = diff.targetTimeoutMs,
                        minIsiMs        = diff.minIsiMs,
                        maxIsiMs        = diff.maxIsiMs,
                        hintThreshold   = diff.hintThreshold,
                        onTrialComplete = { trial -> viewModel.recordTrial(trial) },
                        onGameComplete  = {
                            val endTime = Clock.System.now().toEpochMilliseconds()
                            viewModel.submitGameSession(gameStartTime, endTime)
                        }
                    )
                }
                is GameState.Results -> {
                    ResultsScreen(
                        response    = state.response,
                        totalTrials = uiState.totalTrials,
                        score       = uiState.score,
                        streak      = uiState.streak,
                        onPlayAgain = { viewModel.resetToReady() },
                        onViewTrends = onNavigateToTrends,
                        onExit      = onNavigateBack
                    )
                }
                is GameState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.resetToReady() },
                        onExit  = onNavigateBack
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

// ── Loading ──────────────────────────────────────────────────────────────────

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
            CircularProgressIndicator(color = Color(0xFF0EA5E9))
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// ── Ready ─────────────────────────────────────────────────────────────────────

@Composable
fun GameReadyScreen(
    stats: com.rp_elderycareapp.api.UserStatsResponse?,
    motorBaseline: Double?,
    difficulty: DifficultyConfig = difficultyForRisk(null),
    onStartGame: () -> Unit,
    onRecalibrate: () -> Unit,
    onViewTrends: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.06f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFFBAE6FD))
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
            Spacer(Modifier.height(24.dp))

            // Icon header — replaces 🎮 emoji
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                    .shadow(20.dp, CircleShape, ambientColor = Color(0xFF0EA5E9).copy(0.4f))
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector    = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint           = Color.White,
                    modifier       = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text  = "Ready to Play!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0C4A6E)
            )

            Spacer(Modifier.height(28.dp))

            // Progress / stats card
            stats?.let { s ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF0EA5E9).copy(0.25f)),
                    shape  = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.95f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text  = "Your Progress",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C4A6E)
                        )
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ModernStatItem("Sessions", "${s.totalSessions}",               Color(0xFF0EA5E9), Icons.Default.SportsEsports)
                            ModernStatItem("Avg SAC",  "${(s.avgSAC * 1000).toInt() / 1000.0}", Color(0xFF10B981), Icons.Default.Speed)
                            ModernStatItem("Avg IES",  "${(s.avgIES * 1000).toInt() / 1000.0}", Color(0xFFF59E0B), Icons.Default.ShowChart)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            // Risk badge
                            val riskColor = when (s.currentRiskLevel) {
                                "LOW"    -> Color(0xFF10B981)
                                "MEDIUM" -> Color(0xFFF59E0B)
                                "HIGH"   -> Color(0xFFEF4444)
                                else     -> Color(0xFF64748B)
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = riskColor.copy(0.1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(riskColor, CircleShape)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text  = "Risk: ${s.currentRiskLevel}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = riskColor
                                    )
                                }
                            }
                            // Difficulty badge
                            val diffColor = when (difficulty.level) {
                                1    -> Color(0xFF10B981)
                                3    -> Color(0xFFEF4444)
                                else -> Color(0xFF0EA5E9)
                            }
                            val diffLabel = when (difficulty.level) {
                                1    -> "Easy"
                                3    -> "Challenge"
                                else -> "Standard"
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = diffColor.copy(0.1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(diffColor, CircleShape)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text  = diffLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = diffColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // How to Play card — replaces 📖 emoji header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.9f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF0EA5E9).copy(0.12f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint     = Color(0xFF0EA5E9),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text  = "How to Play",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C4A6E)
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    val steps = listOf(
                        "Watch the 3×3 grid carefully",
                        "A box will light up randomly",
                        "Tap it as quickly as you can",
                        "Complete ${difficulty.totalTrials} trials (${difficulty.targetTimeoutMs / 1000.0}s per target)",
                        "Try to build a streak!"
                    )
                    steps.forEachIndexed { i, step ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(Color(0xFF0EA5E9).copy(0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text  = "${i + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0EA5E9)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text  = step,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF475569),
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(28.dp))

            // Start Game button
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color(0xFF0EA5E9).copy(0.5f)),
                shape  = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4), Color(0xFF14B8A6))
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
                            modifier = Modifier.size(32.dp),
                            tint     = Color.White
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Start Game", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // View Trends button — replaces 📈 emoji
            OutlinedButton(
                onClick = onViewTrends,
                modifier = Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0EA5E9)),
                border = BorderStroke(1.5.dp, Color(0xFF0EA5E9).copy(0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Cognitive Trends", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Recalibrate button — replaces ⚙️ emoji
            motorBaseline?.let { baseline ->
                OutlinedButton(
                    onClick = onRecalibrate,
                    modifier = Modifier.fillMaxWidth(),
                    shape  = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Tune, null, modifier = Modifier.size(18.dp), tint = Color(0xFF64748B))
                        Spacer(Modifier.width(8.dp))
                        Text("Recalibrate", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(6.dp))
                        Text("(Current: ${(baseline * 1000).toInt()} ms)", fontSize = 13.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Stat item (used in GameReadyScreen) ──────────────────────────────────────

@Composable
fun ModernStatItem(label: String, value: String, color: Color, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = color.copy(0.3f))
                .background(color.copy(0.08f), RoundedCornerShape(16.dp))
                .border(1.dp, color.copy(0.18f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(2.dp))
                Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
    }
}

// ── Results ───────────────────────────────────────────────────────────────────

@Composable
fun ResultsScreen(
    response: com.rp_elderycareapp.api.GameSessionResponse,
    totalTrials: Int,
    score: Int,
    streak: Int,
    onPlayAgain: () -> Unit,
    onViewTrends: () -> Unit = {},
    onExit: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val celebrationScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF0F9FF), Color(0xFFDCFCE7), Color(0xFFFEF3C7))
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
            Spacer(Modifier.height(24.dp))

            // Trophy icon — replaces 🎉 emoji
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer { scaleX = celebrationScale; scaleY = celebrationScale }
                    .shadow(20.dp, CircleShape, ambientColor = Color(0xFF0EA5E9).copy(0.4f))
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text  = "Game Complete!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0C4A6E),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // Score card (gradient)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, RoundedCornerShape(28.dp), ambientColor = Color(0xFF0EA5E9).copy(0.4f)),
                shape  = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4), Color(0xFF14B8A6))
                            )
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = "Your Score",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(0.9f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text  = "$score / $totalTrials",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(0.2f)) {
                            Text(
                                text  = "${(response.features.accuracy * 100).toInt()}% Accuracy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Risk assessment card — replaces ✅⚠️🔴ℹ️ emojis
            val riskColor = when (response.prediction.riskLevel) {
                "LOW"    -> Color(0xFF10B981)
                "MEDIUM" -> Color(0xFFF59E0B)
                "HIGH"   -> Color(0xFFEF4444)
                else     -> Color(0xFF64748B)
            }
            val riskIcon = when (response.prediction.riskLevel) {
                "LOW"    -> Icons.Default.CheckCircle
                "MEDIUM" -> Icons.Default.Warning
                "HIGH"   -> Icons.Default.Error
                else     -> Icons.Default.Info
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = riskColor.copy(0.07f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(riskColor.copy(0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(riskIcon, null, tint = riskColor, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text  = "Risk Assessment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C4A6E)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = riskColor.copy(0.15f)) {
                        Text(
                            text  = response.prediction.riskLevel,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = riskColor,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text  = "Score: ${response.prediction.riskScore0_100.toInt()} / 100",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Performance metrics card — replaces 📊 and metric emojis
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.95f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF0EA5E9).copy(0.12f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text  = "Performance Metrics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C4A6E)
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    // Thin divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                    Spacer(Modifier.height(8.dp))

                    ModernMetricRow("Response Time",    "${(response.features.rtAdjMedian * 1000).toInt()} ms",    Color(0xFF0EA5E9), Icons.Default.Timer)
                    ModernMetricRow("Speed-Accuracy",   "${(response.features.sac * 1000).toInt() / 1000.0}",      Color(0xFF10B981), Icons.Default.Speed)
                    ModernMetricRow("Efficiency (IES)", "${(response.features.ies * 1000).toInt() / 1000.0}",      Color(0xFFF59E0B), Icons.Default.ShowChart)
                    ModernMetricRow("RT Variability",   "${(response.features.variability * 1000).toInt()} ms",    Color(0xFF8B5CF6), Icons.Default.BarChart)
                    ModernMetricRow("Best Streak",      "$streak hits",                                            Color(0xFFEF4444), Icons.Default.Whatshot)
                    ModernMetricRow(
                        label = "Hint Usage",
                        value = if (response.features.hintDependencyRate > 0)
                            "${(response.features.hintDependencyRate * 100).toInt()}% of trials"
                        else "None",
                        color = Color(0xFFD97706),
                        icon  = Icons.Default.Lightbulb
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Session ID badge — replaces 📋 emoji
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF475569).copy(0.08f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Assignment, null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "Session ID: ${response.sessionId}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(20.dp))

            // View Trends button — replaces 📈 emoji
            OutlinedButton(
                onClick = onViewTrends,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0C4A6E)),
                border = BorderStroke(1.5.dp, Color(0xFF0EA5E9).copy(0.6f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Cognitive Trends", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Play Again button — replaces 🎮 emoji
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color(0xFF0EA5E9).copy(0.5f)),
                shape  = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4), Color(0xFF14B8A6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Play Again", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(28.dp),
                border = BorderStroke(2.dp, Color(0xFF0EA5E9).copy(0.3f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Exit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0EA5E9))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Metric row (with icon, replaces emoji prefix) ─────────────────────────────

@Composable
fun ModernMetricRow(label: String, value: String, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(color.copy(0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
            )
        }
        Surface(shape = RoundedCornerShape(10.dp), color = color.copy(0.1f)) {
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

// Keep legacy overload (no icon) so any other callers still compile
@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val shake by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue  = 5f,
        animationSpec = infiniteRepeatable(tween(100), RepeatMode.Reverse)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFEF2F2), Color(0xFFFEE2E2), Color(0xFFFECDD3))
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
            // Error icon — replaces ⚠️ emoji with animated icon circle
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer { translationX = shake }
                    .background(Color(0xFFEF4444).copy(0.1f), CircleShape)
                    .border(2.dp, Color(0xFFEF4444).copy(0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint     = Color(0xFFEF4444),
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text  = "Something went wrong",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFDC2626),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.95f))
            ) {
                Text(
                    text  = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(Modifier.height(40.dp))

            // Try Again button — replaces 🔄 emoji
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(32.dp), ambientColor = Color(0xFFEF4444).copy(0.5f)),
                shape  = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFFB923C))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Try Again", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(28.dp),
                border = BorderStroke(2.dp, Color(0xFFEF4444).copy(0.3f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Go Back", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }
        }
    }
}
