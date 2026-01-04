package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rp_elderycareapp.components.reminder.ReminderCard
import com.rp_elderycareapp.components.reminder.ReminderResponseDialog
import com.rp_elderycareapp.components.reminder.AlarmDialog
import com.rp_elderycareapp.components.reminder.AudioRecorderDialog
import com.rp_elderycareapp.data.reminder.*
import com.rp_elderycareapp.viewmodel.ReminderViewModel
import com.rp_elderycareapp.viewmodel.ReminderUiState
import com.rp_elderycareapp.services.rememberPlatformAlarmManager
import com.rp_elderycareapp.services.rememberAudioRecorder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen() {
    val alarmManager = rememberPlatformAlarmManager()
    val audioRecorder = rememberAudioRecorder()
    val viewModel = remember { ReminderViewModel(alarmManager) }
    val userId = "patient_001" // In production, get from auth/session
    val scope = rememberCoroutineScope()
    
    val activeAlarm by viewModel.activeAlarm.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showAudioRecorderDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadReminders(userId)
        viewModel.initWebSocket(userId)  // Initialize WebSocket for real-time alarms
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
                    Text(
                        "Smart Reminders",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showAudioRecorderDialog = true }) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Record voice reminder",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showVoiceDialog = true }) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = "Type voice command",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create reminder",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Navigate to dashboard */ },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Analytics, contentDescription = "Dashboard")
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Analytics")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0
                        viewModel.loadReminders(userId, "active")
                    },
                    text = { Text("Active") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        viewModel.loadReminders(userId, "completed")
                    },
                    text = { Text("Completed") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        viewModel.loadReminders(userId, null)
                    },
                    text = { Text("All") }
                )
            }
            
            // Content
            when (val state = viewModel.reminderState) {
                is ReminderUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ReminderUiState.Success -> {
                    if (state.reminders.isEmpty()) {
                        EmptyState()
                    } else {
                        ReminderList(
                            reminders = state.reminders,
                            onSnooze = { reminder ->
                                scope.launch {
                                    viewModel.snoozeReminder(reminder.id, userId, 15)
                                }
                            },
                            onRespond = { reminder ->
                                viewModel.showResponseDialog(reminder)
                            }
                        )
                    }
                }
                is ReminderUiState.Error -> {
                    ErrorState(message = state.message) {
                        viewModel.loadReminders(userId)
                    }
                }
            }
        }
        
        // Response dialog
        if (viewModel.showResponseDialog && viewModel.selectedReminder != null) {
            ReminderResponseDialog(
                reminder = viewModel.selectedReminder!!,
                onDismiss = { viewModel.hideResponseDialog() },
                onSubmitResponse = { responseText ->
                    viewModel.respondToReminder(
                        ReminderResponseRequest(
                            reminderId = viewModel.selectedReminder!!.id,
                            userId = userId,
                            responseText = responseText
                        )
                    )
                },
                onComplete = {
                    // Complete the reminder and create next occurrence for daily repeats
                    viewModel.completeReminder(viewModel.selectedReminder!!.id, userId) {
                        viewModel.hideResponseDialog()
                    }
                },
                responseResult = viewModel.responseAnalysis
            )
        }
        
        // Create reminder dialog
        if (showCreateDialog) {
            CreateReminderDialog(
                onDismiss = { 
                    println("Dialog dismissed")
                    showCreateDialog = false 
                },
                onCreate = { request ->
                    println("Create button clicked")
                    showCreateDialog = false  // Close immediately
                    viewModel.createReminder(request) {
                        println("Reminder creation completed")
                        // Reload to ensure we have latest
                        viewModel.loadReminders(userId, "active")
                    }
                },
                userId = userId
            )
        }
        
        // Voice command dialog
        if (showVoiceDialog) {
            VoiceCommandDialog(
                onDismiss = { showVoiceDialog = false },
                onCreate = { command ->
                    scope.launch {
                        viewModel.createReminderFromVoice(
                            NaturalLanguageReminderRequest(
                                userId = userId,
                                commandText = command
                            )
                        ) { reminder ->
                            showVoiceDialog = false
                        }
                    }
                }
            )
        }
        
        // Audio recorder dialog
        if (showAudioRecorderDialog) {
            AudioRecorderDialog(
                audioRecorder = audioRecorder,
                onDismiss = { showAudioRecorderDialog = false },
                onSubmit = { audioFilePath ->
                    scope.launch {
                        viewModel.createReminderFromAudio(
                            audioFilePath = audioFilePath,
                            userId = userId,
                            priority = "medium",
                            onSuccess = { response ->
                                println("✅ Reminder created from audio!")
                                println("Transcription: ${response.transcription}")
                                showAudioRecorderDialog = false
                            },
                            onError = { error ->
                                println("❌ Error: $error")
                                // Keep dialog open to show error
                            }
                        )
                    }
                },
                userId = userId
            )
        }
        
        // Alarm dialog - shows full-screen when alarm triggers
        if (activeAlarm != null) {
            AlarmDialog(
                reminder = activeAlarm!!,
                onDismiss = {
                    viewModel.dismissAlarm()
                },
                onSnooze = { delayMinutes ->
                    scope.launch {
                        viewModel.snoozeReminder(activeAlarm!!.id, userId, delayMinutes)
                        viewModel.dismissAlarm()
                    }
                },
                onComplete = {
                    viewModel.completeReminder(activeAlarm!!.id, userId) {
                        viewModel.dismissAlarm()
                    }
                }
            )
        }
    }
}

@Composable
private fun ReminderList(
    reminders: List<Reminder>,
    onSnooze: (Reminder) -> Unit,
    onRespond: (Reminder) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(reminders) { reminder ->
            ReminderCard(
                reminder = reminder,
                onSnooze = { onSnooze(reminder) },
                onComplete = { onRespond(reminder) },
                onRespond = { onRespond(reminder) }
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "No reminders",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "No reminders yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Create a reminder to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
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
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Debug: Check console/logcat for API response details",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateReminderDialog(
    onDismiss: () -> Unit,
    onCreate: (CreateReminderRequest) -> Unit,
    userId: String
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("medication") }
    var priority by remember { mutableStateOf("medium") }
    
    // Date and Time for scheduled_time
    var selectedDate by remember { mutableStateOf("2026-01-05") } // Tomorrow as default
    var selectedTime by remember { mutableStateOf("08:00") }
    
    // Repeat pattern
    var repeatPattern by remember { mutableStateOf<String?>(null) } // null = one-time
    var repeatIntervalMinutes by remember { mutableStateOf("") }
    
    // Caregiver notification settings
    var notifyCaregiverOnMiss by remember { mutableStateOf(true) }
    var escalationThresholdMinutes by remember { mutableStateOf("30") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Reminder") },
        containerColor = Color.White,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                // DATE PICKER
                Column {
                    Text(
                        "Scheduled Date *",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Calendar Date Picker
                    var showDatePicker by remember { mutableStateOf(false) }
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = System.currentTimeMillis() + 86400000L // Tomorrow
                    )
                    
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = selectedDate,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Pick date"
                            )
                        }
                    }
                    
                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = java.time.Instant.ofEpochMilli(millis)
                                                .atZone(java.time.ZoneId.systemDefault())
                                                .toLocalDate()
                                            selectedDate = date.toString()
                                        }
                                        showDatePicker = false
                                    }
                                ) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }
                
                // TIME PICKER - Direct Input
                Column {
                    Text(
                        "Scheduled Time *",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = selectedTime,
                        onValueChange = { newValue ->
                            // Allow only digits and colon, format as HH:MM
                            val filtered = newValue.filter { it.isDigit() || it == ':' }
                            if (filtered.length <= 5) {
                                selectedTime = filtered
                            }
                        },
                        label = { Text("Time (HH:MM)") },
                        placeholder = { Text("e.g., 08:30, 14:15, 19:45") },
                        leadingIcon = { Icon(Icons.Default.Schedule, "Time") },
                        supportingText = { 
                            Text(
                                "Enter time in 24-hour format (00:00 to 23:59)",
                                style = MaterialTheme.typography.bodySmall
                            ) 
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Column {
                    Text(
                        "Category",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("medication", "appointment", "meal", "exercise").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.capitalize()) }
                            )
                        }
                    }
                }
                
                Column {
                    Text(
                        "Priority",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("low", "medium", "high", "critical").forEach { pri ->
                            FilterChip(
                                selected = priority == pri,
                                onClick = { priority = pri },
                                label = { Text(pri.capitalize()) }
                            )
                        }
                    }
                }
                
                // REPEAT PATTERN
                Column {
                    Text(
                        "Repeat Pattern",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "One-time" to null,
                            "Daily" to "daily",
                            "Weekly" to "weekly",
                            "Custom" to "custom"
                        ).forEach { (label, pattern) ->
                            FilterChip(
                                selected = repeatPattern == pattern,
                                onClick = { repeatPattern = pattern },
                                label = { Text(label) }
                            )
                        }
                    }
                    
                    // Show interval input for custom repeat
                    if (repeatPattern == "custom") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = repeatIntervalMinutes,
                            onValueChange = { repeatIntervalMinutes = it.filter { c -> c.isDigit() } },
                            label = { Text("Repeat every (minutes)") },
                            placeholder = { Text("e.g., 120 for every 2 hours") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // CAREGIVER NOTIFICATION
                Column {
                    Text(
                        "Caregiver Notification",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notify caregiver if missed")
                        Switch(
                            checked = notifyCaregiverOnMiss,
                            onCheckedChange = { notifyCaregiverOnMiss = it }
                        )
                    }
                    
                    if (notifyCaregiverOnMiss) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = escalationThresholdMinutes,
                            onValueChange = { escalationThresholdMinutes = it.filter { c -> c.isDigit() } },
                            label = { Text("Alert caregiver after (minutes)") },
                            placeholder = { Text("30") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate and format time
                    val validTime = if (selectedTime.length == 5 && isValidTime(selectedTime)) {
                        selectedTime
                    } else {
                        // Try to auto-format if only digits (e.g., "830" -> "08:30")
                        val digitsOnly = selectedTime.filter { it.isDigit() }
                        when (digitsOnly.length) {
                            3 -> "${digitsOnly[0]}:${digitsOnly[1]}${digitsOnly[2]}" // "830" -> "8:30"
                            4 -> "${digitsOnly.substring(0, 2)}:${digitsOnly.substring(2)}" // "0830" -> "08:30"
                            else -> selectedTime
                        }
                    }
                    
                    // Format scheduled_time properly
                    val scheduledTime = "${selectedDate}T${validTime}:00"
                    
                    onCreate(
                        CreateReminderRequest(
                            userId = userId,
                            title = title,
                            description = description.ifBlank { null },
                            scheduledTime = scheduledTime,
                            category = category,
                            priority = priority,
                            repeat_pattern = repeatPattern,
                            repeatIntervalMinutes = if (repeatPattern == "custom" && repeatIntervalMinutes.isNotBlank()) 
                                repeatIntervalMinutes.toIntOrNull() else null,
                            notifyCaregiverOnMiss = notifyCaregiverOnMiss,
                            escalationThresholdMinutes = if (notifyCaregiverOnMiss && escalationThresholdMinutes.isNotBlank())
                                escalationThresholdMinutes.toIntOrNull() ?: 30 else 30
                        )
                    )
                },
                enabled = title.isNotBlank() && selectedDate.isNotBlank() && selectedTime.isNotBlank() && isValidTime(selectedTime)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun VoiceCommandDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var command by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Mic, contentDescription = "Voice") },
        title = { Text("Voice Command") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Say or type your reminder command:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    label = { Text("Command") },
                    placeholder = { Text("e.g., Remind me to take medicine at 8 AM daily") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(command) },
                enabled = command.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun String.capitalize() = this.replaceFirstChar { 
    if (it.isLowerCase()) it.titlecase() else it.toString() 
}

/**
 * Validates time format (HH:MM in 24-hour format)
 * Returns true if valid, false otherwise
 */
private fun isValidTime(time: String): Boolean {
    if (time.length != 5 || time[2] != ':') return false
    
    val parts = time.split(":")
    if (parts.size != 2) return false
    
    val hours = parts[0].toIntOrNull() ?: return false
    val minutes = parts[1].toIntOrNull() ?: return false
    
    return hours in 0..23 && minutes in 0..59
}
