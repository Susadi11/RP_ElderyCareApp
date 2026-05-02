package com.rp_elderycareapp.screens

import androidx.compose.foundation.BorderStroke
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
import com.rp_elderycareapp.components.reminder.AlarmResponseDialog
import com.rp_elderycareapp.components.reminder.AudioRecorderDialog
import com.rp_elderycareapp.data.reminder.*
import com.rp_elderycareapp.viewmodel.ReminderViewModel
import com.rp_elderycareapp.viewmodel.ReminderUiState
import com.rp_elderycareapp.services.rememberPlatformAlarmManager
import com.rp_elderycareapp.services.rememberAudioRecorder
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(userId: String) {
    val alarmManager = rememberPlatformAlarmManager()
    val audioRecorder = rememberAudioRecorder()
    val viewModel = remember { ReminderViewModel(alarmManager) }
    val scope = rememberCoroutineScope()

    val activeAlarm by viewModel.activeAlarm.collectAsState()
    val alarmRepeatCount by viewModel.alarmRepeatCount.collectAsState()
    val missedAlarmMessage by viewModel.missedAlarmMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }
    var showReminderOptionsDialog by remember { mutableStateOf(false) }
    var showAudioRecorderDialog by remember { mutableStateOf(false) }
    var showAlarmResponseDialog by remember { mutableStateOf(false) }
    var reminderForResponse by remember { mutableStateOf<Reminder?>(null) }
    var showMissedAlarmDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var activeFilter by remember { mutableStateOf("all") }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadReminders(userId)
        viewModel.initWebSocket(userId)
    }

    LaunchedEffect(missedAlarmMessage) {
        if (missedAlarmMessage != null) showMissedAlarmDialog = true
    }

    DisposableEffect(Unit) { onDispose { viewModel.cleanup() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Reminders", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A9FFF),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showReminderOptionsDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add reminder", tint = Color.White)
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(colors = listOf(Color(0xFFE8F4F8), Color(0xFFF5F9FB))))
        ) {
            // Pill tabs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .background(Color(0xFFD6ECFF), RoundedCornerShape(50.dp))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Active" to 0, "Completed" to 1, "All" to 2).forEach { (label, index) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (selectedTab == index) Color.White else Color.Transparent,
                                    RoundedCornerShape(50.dp)
                                )
                                .clickable {
                                    selectedTab = index
                                    when (index) {
                                        0 -> viewModel.loadReminders(userId, "active")
                                        1 -> viewModel.loadReminders(userId, "completed")
                                        2 -> viewModel.loadReminders(userId, null)
                                    }
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 15.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF4A9FFF) else Color(0xFF7BA8C4)
                            )
                        }
                    }
                }
            }

            when (val state = viewModel.reminderState) {
                is ReminderUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4A9FFF))
                    }
                }
                is ReminderUiState.Success -> {
                    if (selectedTab == 0) {
                        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        val filtered = remember(state.reminders, searchQuery, selectedDate, activeFilter) {
                            state.reminders.filter { reminder ->
                                val cardMatch = when (activeFilter) {
                                    "today" -> reminder.scheduledTime.take(10) == today
                                    "high_priority" -> reminder.priority.lowercase() in listOf("high", "critical")
                                    else -> true
                                }
                                val dateMatch = selectedDate?.let { reminder.scheduledTime.take(10) == it.toString() } ?: true
                                val searchMatch = searchQuery.isEmpty() ||
                                    reminder.title.contains(searchQuery, ignoreCase = true) ||
                                    (reminder.description?.contains(searchQuery, ignoreCase = true) == true)
                                cardMatch && dateMatch && searchMatch
                            }
                        }
                        Column(modifier = Modifier.fillMaxSize()) {
                            SummaryCards(reminders = state.reminders, activeFilter = activeFilter, onFilterChange = { filter ->
                                activeFilter = filter; selectedDate = null
                            })
                            SearchAndFilterBar(
                                searchQuery = searchQuery, onSearchChange = { searchQuery = it },
                                selectedDate = selectedDate, onDateClick = { showDatePicker = true },
                                onClearDate = { selectedDate = null }
                            )
                            if (filtered.isEmpty()) EmptyState()
                            else ReminderList(
                                reminders = filtered,
                                onEdit = { reminder -> reminderToEdit = reminder; showEditDialog = true },
                                onDelete = { reminder -> scope.launch { viewModel.deleteReminder(reminder.id, userId) } }
                            )
                        }
                    } else {
                        if (state.reminders.isEmpty()) EmptyState()
                        else ReminderList(
                            reminders = state.reminders,
                            onEdit = { reminder -> reminderToEdit = reminder; showEditDialog = true },
                            onDelete = { reminder -> scope.launch { viewModel.deleteReminder(reminder.id, userId) } }
                        )
                    }
                }
                is ReminderUiState.Error -> ErrorState(message = state.message) { viewModel.loadReminders(userId) }
            }
        }

        if (viewModel.showResponseDialog && viewModel.selectedReminder != null) {
            ReminderResponseDialog(
                reminder = viewModel.selectedReminder!!,
                onDismiss = { viewModel.hideResponseDialog() },
                onSubmitResponse = { responseText ->
                    viewModel.respondToReminder(ReminderResponseRequest(
                        reminderId = viewModel.selectedReminder!!.id, userId = userId, responseText = responseText
                    ))
                },
                onComplete = { viewModel.completeReminder(viewModel.selectedReminder!!.id, userId) { viewModel.hideResponseDialog() } },
                onSnooze = { delayMinutes ->
                    val reminderId = viewModel.selectedReminder?.id
                    if (reminderId != null) {
                        scope.launch {
                            viewModel.snoozeReminderTracked(reminderId, userId, delayMinutes) { response ->
                                selectedTab = 0
                                viewModel.hideResponseDialog()
                                if (response.caregiverAlert) println("⚠️ Frequent snoozing detected")
                            }
                        }
                    }
                },
                responseResult = viewModel.responseAnalysis
            )
        }

        if (showReminderOptionsDialog) {
            ReminderOptionsDialog(
                onDismiss = { showReminderOptionsDialog = false },
                onManualCreate = { showReminderOptionsDialog = false; showCreateDialog = true },
                onVoiceRecording = { showReminderOptionsDialog = false; showAudioRecorderDialog = true }
            )
        }

        if (showAudioRecorderDialog) {
            AudioRecorderDialog(
                audioRecorder = audioRecorder,
                onDismiss = { showAudioRecorderDialog = false },
                onSubmit = { audioFilePath, onComplete ->
                    scope.launch {
                        viewModel.createReminderFromAudio(
                            audioFilePath = audioFilePath, userId = userId, priority = "medium",
                            onSuccess = { response ->
                                println("✅ Reminder created from audio: ${response.transcription}")
                                showAudioRecorderDialog = false
                                onComplete(true, "Reminder created successfully!")
                            },
                            onError = { error -> println("❌ Error: $error"); onComplete(false, error) }
                        )
                    }
                },
                userId = userId
            )
        }

        if (showCreateDialog) {
            CreateReminderDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { request ->
                    showCreateDialog = false
                    selectedTab = 0
                    viewModel.createReminder(request) { println("Reminder created") }
                },
                userId = userId
            )
        }

        if (showEditDialog && reminderToEdit != null) {
            EditReminderDialog(
                reminder = reminderToEdit!!,
                onDismiss = { showEditDialog = false; reminderToEdit = null },
                onUpdate = { request ->
                    val idToUpdate = reminderToEdit?.id
                    showEditDialog = false; reminderToEdit = null
                    if (idToUpdate != null) viewModel.updateReminder(idToUpdate, request) { println("Reminder updated") }
                },
                userId = userId
            )
        }

        if (activeAlarm != null) {
            AlarmDialog(
                reminder = activeAlarm!!,
                repeatCount = alarmRepeatCount,
                onDismiss = {},
                onSnooze = { delayMinutes ->
                    val alarmId = activeAlarm?.id ?: return@AlarmDialog
                    scope.launch {
                        viewModel.snoozeReminderTracked(alarmId, userId, delayMinutes) { response ->
                            selectedTab = 0
                            if (response.caregiverAlert) println("⚠️ Frequent snoozing detected")
                        }
                    }
                },
                onStopAlarm = { reminderForResponse = activeAlarm; showAlarmResponseDialog = true },
                onNeedHelp = {
                    scope.launch {
                        viewModel.requestHelp(activeAlarm!!.id, userId, "confused") { response ->
                            println("💙 Help requested: ${response.caregiverNotified}")
                        }
                    }
                }
            )
        }

        if (showAlarmResponseDialog && reminderForResponse != null) {
            AlarmResponseDialog(
                reminder = reminderForResponse!!,
                onDismiss = { showAlarmResponseDialog = false; reminderForResponse = null },
                onSubmit = { userResponse ->
                    val reminderId = reminderForResponse?.id
                    showAlarmResponseDialog = false; reminderForResponse = null
                    if (reminderId != null) {
                        scope.launch {
                            viewModel.acknowledgeReminder(reminderId = reminderId, userId = userId, acknowledgmentMethod = "tap")
                            viewModel.stopAlarmWithResponse(reminderId, userId, userResponse) { response ->
                                selectedTab = 0
                                if (response.cognitiveAnalysis.riskScore > 0.7) println("⚠️ High cognitive risk")
                            }
                        }
                    }
                }
            )
        }

        if (showMissedAlarmDialog && missedAlarmMessage != null) {
            AlertDialog(
                onDismissRequest = { showMissedAlarmDialog = false; viewModel.clearMissedAlarmMessage(); selectedTab = 0 },
                icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp)) },
                title = { Text("Missed Reminder", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
                text = { Text(missedAlarmMessage!!, style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    Button(
                        onClick = { showMissedAlarmDialog = false; viewModel.clearMissedAlarmMessage(); selectedTab = 0 },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) { Text("OK") }
                }
            )
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                        }
                        showDatePicker = false; activeFilter = "all"
                    }) { Text("OK", color = Color(0xFF4A9FFF), fontWeight = FontWeight.Bold) }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
                colors = DatePickerDefaults.colors(containerColor = Color.White)
            ) {
                DatePicker(state = datePickerState, colors = styledDatePickerColors())
            }
        }
    }
}

// ─── Options Dialog ──────────────────────────────────────────────────────────

@Composable
private fun ReminderOptionsDialog(
    onDismiss: () -> Unit,
    onManualCreate: () -> Unit,
    onVoiceRecording: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {
            Column {
                // Gradient header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF4A9FFF), Color(0xFF0EA5E9))),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text("Add New Reminder", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Choose how to create it", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White)
                        }
                    }
                }
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReminderOptionCard(
                        icon = Icons.Default.Mic, iconColor = Color(0xFF4A9FFF), iconBg = Color(0xFFDCEFFE),
                        accentColor = Color(0xFF4A9FFF), title = "Voice Recording", subtitle = "Speak your reminder aloud",
                        onClick = onVoiceRecording
                    )
                    ReminderOptionCard(
                        icon = Icons.Default.Edit, iconColor = Color(0xFF10B981), iconBg = Color(0xFFD1FAE5),
                        accentColor = Color(0xFF10B981), title = "Manual Entry", subtitle = "Type in the reminder details",
                        onClick = onManualCreate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun ReminderOptionCard(
    icon: ImageVector, iconColor: Color, iconBg: Color,
    accentColor: Color, title: String, subtitle: String, onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(52.dp).background(iconBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF7BA8C4))
            }
            Icon(Icons.Default.ArrowForward, null, tint = accentColor.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Summary + Filter ────────────────────────────────────────────────────────

@Composable
private fun SummaryCards(reminders: List<Reminder>, activeFilter: String, onFilterChange: (String) -> Unit) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    val todayCount = reminders.count { it.scheduledTime.take(10) == today }
    val allCount = reminders.size
    val highCount = reminders.count { it.priority.lowercase() in listOf("high", "critical") }

    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GradientSummaryCard(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Today", count = todayCount,
            gradient = listOf(Color(0xFF4A9FFF), Color(0xFF1E88E5)), isSelected = activeFilter == "today", onClick = { onFilterChange("today") })
        GradientSummaryCard(modifier = Modifier.weight(1f).fillMaxHeight(), label = "All", count = allCount,
            gradient = listOf(Color(0xFF10B981), Color(0xFF059669)), isSelected = activeFilter == "all", onClick = { onFilterChange("all") })
        GradientSummaryCard(modifier = Modifier.weight(1f).fillMaxHeight(), label = "High", count = highCount,
            gradient = listOf(Color(0xFFEF4444), Color(0xFFF97316)), isSelected = activeFilter == "high_priority", onClick = { onFilterChange("high_priority") })
    }
}

@Composable
private fun GradientSummaryCard(modifier: Modifier = Modifier, label: String, count: Int, gradient: List<Color>, isSelected: Boolean, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(gradient)).padding(vertical = 18.dp, horizontal = 12.dp)) {
            Column(modifier = Modifier.fillMaxWidth().align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(count.toString(), fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.92f), textAlign = TextAlign.Center)
            }
            if (isSelected) Box(modifier = Modifier.align(Alignment.TopEnd).size(10.dp).background(Color.White, CircleShape))
        }
    }
}

@Composable
private fun SearchAndFilterBar(searchQuery: String, onSearchChange: (String) -> Unit, selectedDate: LocalDate?, onDateClick: () -> Unit, onClearDate: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = searchQuery, onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search reminders...", fontSize = 16.sp, color = Color(0xFFABC8D9)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF7BA8C4), modifier = Modifier.size(22.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, "Clear", tint = Color(0xFF7BA8C4), modifier = Modifier.size(20.dp))
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF4A9FFF),
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
            Surface(onClick = onDateClick, shape = RoundedCornerShape(14.dp), color = Color(0xFF4A9FFF), modifier = Modifier.size(56.dp)) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.DateRange, "Filter by date", tint = Color.White, modifier = Modifier.size(22.dp))
                    Text("Date", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (selectedDate != null) {
            Spacer(modifier = Modifier.height(8.dp))
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            val label = "${months[selectedDate.monthNumber - 1]} ${selectedDate.dayOfMonth}, ${selectedDate.year}"
            Surface(onClick = onClearDate, shape = RoundedCornerShape(50.dp), color = Color(0xFFD6ECFF), modifier = Modifier.height(36.dp)) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.DateRange, null, tint = Color(0xFF4A9FFF), modifier = Modifier.size(16.dp))
                    Text(label, fontSize = 14.sp, color = Color(0xFF4A9FFF), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.Close, "Clear date", tint = Color(0xFF4A9FFF), modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun ReminderList(reminders: List<Reminder>, onEdit: (Reminder) -> Unit, onDelete: (Reminder) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)) {
        items(reminders) { reminder ->
            ReminderCard(reminder = reminder, onEdit = { onEdit(reminder) }, onDelete = { onDelete(reminder) })
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(110.dp).background(Color(0xFFD6ECFF), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Notifications, null, tint = Color(0xFF4A9FFF), modifier = Modifier.size(54.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("No Reminders Found", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        Spacer(modifier = Modifier.height(10.dp))
        Text("Tap + to add a new reminder", fontSize = 16.sp, color = Color(0xFF7BA8C4), textAlign = TextAlign.Center)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.ErrorOutline, "Error", tint = Color(0xFFD32F2F), modifier = Modifier.size(80.dp))
            Text("Oops! Something went wrong", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Refresh, "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

// ─── Create Reminder Dialog ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateReminderDialog(onDismiss: () -> Unit, onCreate: (CreateReminderRequest) -> Unit, userId: String) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("medication") }
    var priority by remember { mutableStateOf("medium") }
    val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    var selectedDate by remember {
        mutableStateOf("${todayDate.year}-${todayDate.monthNumber.toString().padStart(2,'0')}-${todayDate.dayOfMonth.toString().padStart(2,'0')}")
    }
    var selectedTime by remember { mutableStateOf("13:00") }
    var repeatPattern by remember { mutableStateOf<String?>(null) }
    var repeatIntervalMinutes by remember { mutableStateOf("") }
    var notifyCaregiverOnMiss by remember { mutableStateOf(true) }
    var escalationThresholdMinutes by remember { mutableStateOf("30") }
    var showDatePickerForm by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds())

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF5F9FB),
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ReminderFormHeader(title = "New Reminder", subtitle = "Fill in all required fields", onClose = onDismiss)
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FormCard("Reminder Title") {
                        OutlinedTextField(
                            value = title, onValueChange = { title = it },
                            placeholder = { Text("e.g., Take blood pressure medication", color = Color(0xFFB0C4D4)) },
                            leadingIcon = { Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF4A9FFF), modifier = Modifier.size(20.dp)) },
                            singleLine = true, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp), colors = formFieldColors()
                        )
                    }
                    FormCard("Description (Optional)") {
                        OutlinedTextField(
                            value = description, onValueChange = { description = it },
                            placeholder = { Text("Add more details...", color = Color(0xFFB0C4D4)) },
                            minLines = 2, maxLines = 3, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp), colors = formFieldColors()
                        )
                    }
                    FormCard("Schedule") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                onClick = { showDatePickerForm = true },
                                shape = RoundedCornerShape(14.dp), color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2EAF0)),
                                modifier = Modifier.weight(1.4f).height(56.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF4A9FFF), modifier = Modifier.size(18.dp))
                                    Text(formatDisplayDate(selectedDate), fontSize = 13.sp, color = Color(0xFF1A1A2E), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            OutlinedTextField(
                                value = selectedTime,
                                onValueChange = { v -> val f = v.filter { it.isDigit() || it == ':' }; if (f.length <= 5) selectedTime = f },
                                placeholder = { Text("HH:MM") },
                                leadingIcon = { Icon(Icons.Default.Schedule, null, tint = Color(0xFF4A9FFF), modifier = Modifier.size(18.dp)) },
                                singleLine = true, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp), colors = formFieldColors()
                            )
                        }
                    }
                    FormCard("Category") { CategorySelector(selected = category, onSelect = { category = it }) }
                    FormCard("Priority") { PrioritySelector(selected = priority, onSelect = { priority = it }) }
                    FormCard("Repeat Pattern") {
                        RepeatSelector(selected = repeatPattern, onSelect = { repeatPattern = it }, customInterval = repeatIntervalMinutes, onIntervalChange = { repeatIntervalMinutes = it })
                    }
                    FormCard("Caregiver Notifications") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Notify caregiver if missed", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A2E))
                                    Text("Alert caregiver when skipped", fontSize = 12.sp, color = Color(0xFF7BA8C4))
                                }
                                Switch(checked = notifyCaregiverOnMiss, onCheckedChange = { notifyCaregiverOnMiss = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF4A9FFF)))
                            }
                            if (notifyCaregiverOnMiss) {
                                OutlinedTextField(
                                    value = escalationThresholdMinutes,
                                    onValueChange = { escalationThresholdMinutes = it.filter { c -> c.isDigit() } },
                                    label = { Text("Alert after (minutes)") }, placeholder = { Text("30") },
                                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp), colors = formFieldColors()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                // Bottom action bar
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFE2EAF0))
                        ) { Text("Cancel", fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B)) }
                        Button(
                            onClick = {
                                val validTime = normalizeTime(selectedTime)
                                val utcScheduledTime = LocalDateTime.parse("${selectedDate}T${validTime}:00")
                                    .toInstant(TimeZone.currentSystemDefault()).toString()
                                onCreate(CreateReminderRequest(
                                    userId = userId, title = title, description = description.ifBlank { null },
                                    scheduledTime = utcScheduledTime, category = category, priority = priority,
                                    repeat_pattern = repeatPattern,
                                    repeatIntervalMinutes = if (repeatPattern == "custom" && repeatIntervalMinutes.isNotBlank()) repeatIntervalMinutes.toIntOrNull() else null,
                                    notifyCaregiverOnMiss = notifyCaregiverOnMiss,
                                    escalationThresholdMinutes = if (notifyCaregiverOnMiss && escalationThresholdMinutes.isNotBlank()) escalationThresholdMinutes.toIntOrNull() ?: 30 else 30
                                ))
                            },
                            modifier = Modifier.weight(2f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A9FFF)),
                            enabled = title.isNotBlank() && isValidTime(selectedTime)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Reminder", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDatePickerForm) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerForm = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
                        selectedDate = "${dt.year}-${dt.monthNumber.toString().padStart(2,'0')}-${dt.dayOfMonth.toString().padStart(2,'0')}"
                    }
                    showDatePickerForm = false
                }) { Text("OK", color = Color(0xFF4A9FFF), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDatePickerForm = false }) { Text("Cancel") } },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(state = datePickerState, colors = styledDatePickerColors())
        }
    }
}

// ─── Edit Reminder Dialog ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditReminderDialog(reminder: Reminder, onDismiss: () -> Unit, onUpdate: (CreateReminderRequest) -> Unit, userId: String) {
    val existingDateTime = remember {
        try {
            val instant = Instant.parse(reminder.scheduledTime.replace("+00:00", "Z").replace(".000", ""))
            instant.toLocalDateTime(TimeZone.UTC)
        } catch (e: Exception) {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
    val existingDateMillis = remember {
        try { Instant.parse(reminder.scheduledTime.replace("+00:00","Z").replace(".000","")).toEpochMilliseconds() }
        catch (e: Exception) { Clock.System.now().toEpochMilliseconds() }
    }

    var title by remember { mutableStateOf(reminder.title) }
    var description by remember { mutableStateOf(reminder.description ?: "") }
    var category by remember { mutableStateOf(reminder.category) }
    var priority by remember { mutableStateOf(reminder.priority) }
    var selectedDate by remember {
        mutableStateOf("${existingDateTime.year}-${existingDateTime.monthNumber.toString().padStart(2,'0')}-${existingDateTime.dayOfMonth.toString().padStart(2,'0')}")
    }
    var selectedTime by remember {
        mutableStateOf("${existingDateTime.hour.toString().padStart(2,'0')}:${existingDateTime.minute.toString().padStart(2,'0')}")
    }
    var repeatPattern by remember { mutableStateOf(reminder.repeat_pattern) }
    var repeatIntervalMinutes by remember { mutableStateOf(reminder.repeatIntervalMinutes?.toString() ?: "") }
    var notifyCaregiverOnMiss by remember { mutableStateOf(reminder.notifyCaregiverOnMiss) }
    var escalationThresholdMinutes by remember { mutableStateOf(reminder.escalationThresholdMinutes.toString()) }
    var showDatePickerForm by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = existingDateMillis)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp), color = Color(0xFFF5F9FB), tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ReminderFormHeader(title = "Edit Reminder", subtitle = "Update the reminder details", onClose = onDismiss, isEdit = true)
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FormCard("Reminder Title") {
                        OutlinedTextField(
                            value = title, onValueChange = { title = it },
                            leadingIcon = { Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF4A9FFF), modifier = Modifier.size(20.dp)) },
                            singleLine = true, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp), colors = formFieldColors()
                        )
                    }
                    FormCard("Description (Optional)") {
                        OutlinedTextField(
                            value = description, onValueChange = { description = it },
                            placeholder = { Text("Add more details...", color = Color(0xFFB0C4D4)) },
                            minLines = 2, maxLines = 3, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp), colors = formFieldColors()
                        )
                    }
                    FormCard("Schedule") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                onClick = { showDatePickerForm = true },
                                shape = RoundedCornerShape(14.dp), color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2EAF0)),
                                modifier = Modifier.weight(1.4f).height(56.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF4A9FFF), modifier = Modifier.size(18.dp))
                                    Text(formatDisplayDate(selectedDate), fontSize = 13.sp, color = Color(0xFF1A1A2E), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            OutlinedTextField(
                                value = selectedTime,
                                onValueChange = { v -> val f = v.filter { it.isDigit() || it == ':' }; if (f.length <= 5) selectedTime = f },
                                placeholder = { Text("HH:MM") },
                                leadingIcon = { Icon(Icons.Default.Schedule, null, tint = Color(0xFF4A9FFF), modifier = Modifier.size(18.dp)) },
                                singleLine = true, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp), colors = formFieldColors()
                            )
                        }
                    }
                    FormCard("Category") { CategorySelector(selected = category, onSelect = { category = it }) }
                    FormCard("Priority") { PrioritySelector(selected = priority, onSelect = { priority = it }) }
                    FormCard("Repeat Pattern") {
                        RepeatSelector(selected = repeatPattern, onSelect = { repeatPattern = it }, customInterval = repeatIntervalMinutes, onIntervalChange = { repeatIntervalMinutes = it })
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFE2EAF0))
                        ) { Text("Cancel", fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B)) }
                        Button(
                            onClick = {
                                val validTime = normalizeTime(selectedTime)
                                val utcScheduledTime = LocalDateTime.parse("${selectedDate}T${validTime}:00")
                                    .toInstant(TimeZone.currentSystemDefault()).toString()
                                onUpdate(CreateReminderRequest(
                                    userId = userId, title = title, description = description.ifBlank { null },
                                    scheduledTime = utcScheduledTime, category = category, priority = priority,
                                    repeat_pattern = repeatPattern,
                                    repeatIntervalMinutes = if (repeatPattern == "custom" && repeatIntervalMinutes.isNotBlank()) repeatIntervalMinutes.toIntOrNull() else null,
                                    notifyCaregiverOnMiss = notifyCaregiverOnMiss,
                                    escalationThresholdMinutes = if (escalationThresholdMinutes.isNotBlank()) escalationThresholdMinutes.toIntOrNull() ?: 30 else 30
                                ))
                            },
                            modifier = Modifier.weight(2f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            enabled = title.isNotBlank() && isValidTime(selectedTime)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDatePickerForm) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerForm = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
                        selectedDate = "${dt.year}-${dt.monthNumber.toString().padStart(2,'0')}-${dt.dayOfMonth.toString().padStart(2,'0')}"
                    }
                    showDatePickerForm = false
                }) { Text("OK", color = Color(0xFF4A9FFF), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDatePickerForm = false }) { Text("Cancel") } },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(state = datePickerState, colors = styledDatePickerColors())
        }
    }
}

// ─── Form Helper Composables ──────────────────────────────────────────────────

@Composable
private fun ReminderFormHeader(title: String, subtitle: String, onClose: () -> Unit, isEdit: Boolean = false) {
    val gradientColors = if (isEdit) listOf(Color(0xFF10B981), Color(0xFF059669)) else listOf(Color(0xFF4A9FFF), Color(0xFF0EA5E9))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(gradientColors), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isEdit) Icons.Default.Edit else Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(subtitle, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close", tint = Color.White) }
        }
    }
}

@Composable
private fun FormCard(label: String, content: @Composable () -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7BA8C4), modifier = Modifier.padding(bottom = 6.dp, start = 2.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(14.dp)) { content() }
        }
    }
}

@Composable
private fun CategorySelector(selected: String, onSelect: (String) -> Unit) {
    val categories = listOf(
        Triple("medication",  Icons.Default.MedicalServices, Color(0xFF6366F1)),
        Triple("appointment", Icons.Default.CalendarToday,   Color(0xFF0EA5E9)),
        Triple("meal",        Icons.Default.Restaurant,      Color(0xFFF97316)),
        Triple("exercise",    Icons.Default.FitnessCenter,   Color(0xFF22C55E)),
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { (cat, icon, color) ->
            val isSel = selected == cat
            Surface(
                onClick = { onSelect(cat) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSel) color.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                border = if (isSel) BorderStroke(1.5.dp, color) else BorderStroke(1.dp, Color.Transparent),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(icon, null, tint = if (isSel) color else Color(0xFFAFC8D6), modifier = Modifier.size(20.dp))
                    Text(
                        when(cat) { "medication" -> "Meds"; "appointment" -> "Appt"; else -> cat.replaceFirstChar { it.uppercase() } },
                        fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) color else Color(0xFF94A3B8), textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PrioritySelector(selected: String, onSelect: (String) -> Unit) {
    val priorities = listOf(
        Triple("low",      Color(0xFF22C55E), "Low"),
        Triple("medium",   Color(0xFFF59E0B), "Medium"),
        Triple("high",     Color(0xFFF97316), "High"),
        Triple("critical", Color(0xFFEF4444), "Critical"),
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        priorities.forEach { (pri, color, label) ->
            val isSel = selected == pri
            Surface(
                onClick = { onSelect(pri) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSel) color.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                border = if (isSel) BorderStroke(1.5.dp, color) else null,
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) color else Color(0xFF94A3B8), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun RepeatSelector(selected: String?, onSelect: (String?) -> Unit, customInterval: String, onIntervalChange: (String) -> Unit) {
    val options = listOf(Pair("Once", null), Pair("Daily", "daily"), Pair("Weekly", "weekly"), Pair("Custom", "custom"))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (label, value) ->
            val isSel = selected == value
            Surface(
                onClick = { onSelect(value) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSel) Color(0xFF4A9FFF).copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                border = if (isSel) BorderStroke(1.5.dp, Color(0xFF4A9FFF)) else null,
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) Color(0xFF4A9FFF) else Color(0xFF94A3B8))
                }
            }
        }
    }
    if (selected == "custom") {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = customInterval,
            onValueChange = { onIntervalChange(it.filter { c -> c.isDigit() }) },
            label = { Text("Repeat every (minutes)") },
            placeholder = { Text("e.g., 120 = every 2 hours") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp), colors = formFieldColors()
        )
    }
}

// ─── Shared Helpers ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun styledDatePickerColors() = DatePickerDefaults.colors(
    containerColor = Color.White,
    titleContentColor = Color(0xFF1A1A2E),
    headlineContentColor = Color(0xFF4A9FFF),
    weekdayContentColor = Color(0xFF64748B),
    subheadContentColor = Color(0xFF1A1A2E),
    navigationContentColor = Color(0xFF4A9FFF),
    yearContentColor = Color(0xFF1A1A2E),
    currentYearContentColor = Color(0xFF4A9FFF),
    selectedYearContentColor = Color.White,
    selectedYearContainerColor = Color(0xFF4A9FFF),
    dayContentColor = Color(0xFF1A1A2E),
    selectedDayContentColor = Color.White,
    selectedDayContainerColor = Color(0xFF4A9FFF),
    todayContentColor = Color(0xFF4A9FFF),
    todayDateBorderColor = Color(0xFF4A9FFF),
    disabledDayContentColor = Color(0xFFCBD5E1),
    disabledSelectedDayContentColor = Color.White,
    disabledSelectedDayContainerColor = Color(0xFFBFDBFE)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Color(0xFFE2EAF0), focusedBorderColor = Color(0xFF4A9FFF),
    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
)

private fun normalizeTime(time: String): String {
    if (time.length == 5 && isValidTime(time)) return time
    val digits = time.filter { it.isDigit() }
    return when (digits.length) {
        3 -> "${digits[0]}:${digits[1]}${digits[2]}"
        4 -> "${digits.substring(0, 2)}:${digits.substring(2)}"
        else -> time
    }
}

private fun formatDisplayDate(dateStr: String): String {
    if (dateStr.length < 10) return dateStr
    return try {
        val parts = dateStr.split("-")
        if (parts.size != 3) return dateStr
        val month = parts[1].toIntOrNull() ?: return dateStr
        val day = parts[2].toIntOrNull() ?: return dateStr
        val monthName = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec").getOrNull(month - 1) ?: return dateStr
        "$monthName $day, ${parts[0]}"
    } catch (e: Exception) { dateStr }
}

private fun isValidTime(time: String): Boolean {
    if (time.length != 5 || time[2] != ':') return false
    val parts = time.split(":")
    if (parts.size != 2) return false
    val hours = parts[0].toIntOrNull() ?: return false
    val minutes = parts[1].toIntOrNull() ?: return false
    return hours in 0..23 && minutes in 0..59
}

private fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
