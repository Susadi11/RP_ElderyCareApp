package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.viewmodel.DashboardViewModel
import com.rp_elderycareapp.viewmodel.DashboardUiState
import com.rp_elderycareapp.data.reminder.DashboardData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    userId: String = "patient_001",
    onNavigateBack: () -> Unit
) {
    val viewModel = remember { DashboardViewModel() }
    var selectedDays by remember { mutableStateOf(7) }
    
    LaunchedEffect(selectedDays) {
        viewModel.loadDashboard(userId, selectedDays)
        viewModel.loadBehaviorPattern(userId, 30)
        viewModel.loadWeeklyReport(userId)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanup()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Dashboard", fontWeight = FontWeight.Bold) },
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
        when (val state = viewModel.dashboardState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    dashboard = state.dashboard,
                    selectedDays = selectedDays,
                    onDaysChange = { selectedDays = it },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(state.message, color = Color(0xFFD32F2F))
                        Button(onClick = { viewModel.loadDashboard(userId, selectedDays) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    dashboard: DashboardData,
    selectedDays: Int,
    onDaysChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(7, 14, 30).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { onDaysChange(days) },
                        label = { Text("$days days") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Completion rate
        item {
            StatCard(
                title = "Reminder Completion",
                value = "${(dashboard.statistics.confirmationRate * 100).toInt()}%",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF4CAF50),
                progress = dashboard.statistics.confirmationRate.toFloat()
            )
        }
        
        // Cognitive health
        item {
            CognitiveHealthCard(dashboard)
        }
        
        // Statistics
        item {
            StatsOverviewCard(dashboard)
        }
        
        // Best times
        item {
            BestTimesCard(dashboard)
        }
        
        // Recommendations
        item {
            RecommendationsCard(dashboard)
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    progress: Float? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            if (progress != null) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun CognitiveHealthCard(dashboard: DashboardData) {
    val health = dashboard.cognitiveHealth
    val (color, icon, status) = when {
        health.avgRiskScore < 0.3 -> Triple(Color(0xFF4CAF50), Icons.Default.SentimentVerySatisfied, "GOOD")
        health.avgRiskScore < 0.6 -> Triple(Color(0xFFFBC02D), Icons.Default.SentimentNeutral, "MODERATE")
        else -> Triple(Color(0xFFD32F2F), Icons.Default.SentimentVeryDissatisfied, "NEEDS ATTENTION")
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Health status",
                    tint = color,
                    modifier = Modifier.size(56.dp)
                )
                Column {
                    Text(
                        "Cognitive Health: $status",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        "Risk Score: ${(health.avgRiskScore * 100).toInt() / 100.0} (Low)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Trend: ${health.trend.uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsOverviewCard(dashboard: DashboardData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Statistics Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val stats = dashboard.statistics
            StatRow("Total Reminders", stats.totalReminders.toString())
            StatRow("Confirmed", stats.confirmed.toString(), Color(0xFF4CAF50))
            StatRow("Ignored", stats.ignored.toString(), Color(0xFFF57C00))
            StatRow("Delayed", stats.delayed.toString(), Color(0xFFFBC02D))
            StatRow("Confused", stats.confused.toString(), Color(0xFFD32F2F))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BestTimesCard(dashboard: DashboardData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    "Time",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Best Times for Reminders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val timing = dashboard.timing
            TimeSlotRow("Best Time", "${timing.optimalHour}:00 - ${timing.optimalHour + 2}:00", Color(0xFF4CAF50))
            TimeSlotRow("Avoid After", "${timing.worstHours.firstOrNull() ?: 20}:00", Color(0xFFD32F2F))
            TimeSlotRow("Avg Response Time", "${timing.avgResponseTimeSeconds}s", MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TimeSlotRow(label: String, time: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(time, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun RecommendationsCard(dashboard: DashboardData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    "Recommendations",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Recommendations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            listOf(
                "Keep morning routine consistent for best results",
                "Avoid reminders during low-response hours",
                "Consider adding visual cues for important tasks"
            ).forEach { recommendation ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("•", fontWeight = FontWeight.Bold)
                    Text(
                        recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
