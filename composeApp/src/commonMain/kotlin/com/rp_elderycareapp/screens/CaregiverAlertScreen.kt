package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.rp_elderycareapp.data.reminder.CaregiverAlert
import com.rp_elderycareapp.viewmodel.CaregiverAlertViewModel
import com.rp_elderycareapp.viewmodel.AlertUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverAlertScreen(
    caregiverId: String = "caregiver_001",
    onNavigateBack: () -> Unit
) {
    val viewModel = remember { CaregiverAlertViewModel() }
    val scope = rememberCoroutineScope()
    var showActiveOnly by remember { mutableStateOf(true) }
    
    LaunchedEffect(showActiveOnly) {
        viewModel.loadAlerts(caregiverId, showActiveOnly)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanup()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Caregiver Alerts", fontWeight = FontWeight.Bold)
                        Text(
                            "Monitor patient health",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadAlerts(caregiverId, showActiveOnly) }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Show active only",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = showActiveOnly,
                    onCheckedChange = { showActiveOnly = it }
                )
            }
            
            when (val state = viewModel.alertState) {
                is AlertUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is AlertUiState.Success -> {
                    if (state.alerts.isEmpty()) {
                        EmptyAlertsState()
                    } else {
                        AlertsList(
                            alerts = state.alerts,
                            onAcknowledge = { alert ->
                                scope.launch {
                                    viewModel.acknowledgeAlert(alert.id, caregiverId)
                                }
                            },
                            onResolve = { alert ->
                                scope.launch {
                                    viewModel.resolveAlert(alert.id, caregiverId)
                                }
                            }
                        )
                    }
                }
                is AlertUiState.Error -> {
                    ErrorAlertsState(
                        message = state.message,
                        onRetry = { viewModel.loadAlerts(caregiverId, showActiveOnly) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsList(
    alerts: List<CaregiverAlert>,
    onAcknowledge: (CaregiverAlert) -> Unit,
    onResolve: (CaregiverAlert) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(alerts) { alert ->
            AlertCard(
                alert = alert,
                onAcknowledge = { onAcknowledge(alert) },
                onResolve = { onResolve(alert) }
            )
        }
    }
}

@Composable
private fun AlertCard(
    alert: CaregiverAlert,
    onAcknowledge: () -> Unit,
    onResolve: () -> Unit
) {
    val (severityColor, severityIcon) = when (alert.severity.lowercase()) {
        "critical" -> Pair(Color(0xFFD32F2F), Icons.Default.Error)
        "warning" -> Pair(Color(0xFFF57C00), Icons.Default.Warning)
        else -> Pair(Color(0xFF2196F3), Icons.Default.Info)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.acknowledged) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (alert.acknowledged) 2.dp else 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = severityIcon,
                        contentDescription = alert.severity,
                        tint = severityColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = alert.severity.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = severityColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = alert.alertType.replace("_", " ").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (alert.acknowledged) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "✓ Seen",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (alert.resolved) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2196F3).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF2196F3), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "✓ Resolved",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Patient info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    "Patient",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Patient: ${alert.userName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Alert message
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Reminder details if available
            if (!alert.reminderTitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Reminder: ${alert.reminderTitle}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (!alert.responseText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Response: \"${alert.responseText}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
            
            // Risk score if available
            alert.riskScore?.let { score ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        "Risk score",
                        tint = when {
                            score < 0.3 -> Color(0xFF4CAF50)
                            score < 0.6 -> Color(0xFFF57C00)
                            else -> Color(0xFFD32F2F)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Risk Score: ${(score * 100).toInt() / 100.0}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            score < 0.3 -> Color(0xFF4CAF50)
                            score < 0.6 -> Color(0xFFF57C00)
                            else -> Color(0xFFD32F2F)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            if (!alert.resolved) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!alert.acknowledged) {
                        Button(
                            onClick = onAcknowledge,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                "Acknowledge",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Acknowledge")
                        }
                    }
                    
                    Button(
                        onClick = onResolve,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            "Resolve",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resolve")
                    }
                    
                    IconButton(
                        onClick = { /* Call patient */ },
                        modifier = Modifier
                            .background(Color(0xFF2196F3), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            Icons.Default.Call,
                            "Call patient",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAlertsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                "No alerts",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(80.dp)
            )
            Text(
                "All Clear!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "No active alerts at the moment",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorAlertsState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                "Error",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(80.dp)
            )
            Text(
                "Error Loading Alerts",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}
