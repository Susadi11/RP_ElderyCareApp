package com.rp_elderycareapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.rp_elderycareapp.data.reminder.*
import com.rp_elderycareapp.services.ReminderWebSocketService
import com.rp_elderycareapp.services.PlatformAlarmManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// UI States
sealed class ReminderUiState {
    object Loading : ReminderUiState()
    data class Success(val reminders: List<Reminder>) : ReminderUiState()
    data class Error(val message: String) : ReminderUiState()
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val dashboard: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

sealed class AlertUiState {
    object Loading : AlertUiState()
    data class Success(val alerts: List<CaregiverAlert>) : AlertUiState()
    data class Error(val message: String) : AlertUiState()
}

class ReminderViewModel(private val alarmManager: PlatformAlarmManager? = null) {
    private val apiService = ReminderApiService()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var webSocketService: ReminderWebSocketService? = null
    
    var reminderState by mutableStateOf<ReminderUiState>(ReminderUiState.Loading)
        private set
    
    var selectedReminder by mutableStateOf<Reminder?>(null)
        private set
    
    var showResponseDialog by mutableStateOf(false)
        private set
    
    var showCreateDialog by mutableStateOf(false)
        private set
    
    var responseAnalysis by mutableStateOf<ReminderResponseResult?>(null)
        private set
    
    private val _activeAlarm = MutableStateFlow<Reminder?>(null)
    val activeAlarm: StateFlow<Reminder?> = _activeAlarm.asStateFlow()
    
    private val _wsConnectionState = MutableStateFlow<String>("Disconnected")
    val wsConnectionState: StateFlow<String> = _wsConnectionState.asStateFlow()
    
    // Initialize WebSocket connection
    fun initWebSocket(userId: String) {
        webSocketService = ReminderWebSocketService(userId)
        webSocketService?.connect(scope)
        
        // Listen for alarm notifications
        scope.launch {
            webSocketService?.reminderAlarms?.collect { reminder ->
                println("=== 🚨 ALARM RECEIVED: ${reminder.title} ===")
                _activeAlarm.value = reminder
                
                // Trigger platform-specific alarm (music, vibration, notification)
                alarmManager?.triggerAlarm(reminder)
                
                // Auto-show alarm dialog
                selectedReminder = reminder
                showResponseDialog = true
            }
        }
        
        // Monitor connection state
        scope.launch {
            webSocketService?.connectionState?.collect { state ->
                _wsConnectionState.value = when (state) {
                    is ReminderWebSocketService.ConnectionState.Connected -> "Connected"
                    is ReminderWebSocketService.ConnectionState.Connecting -> "Connecting..."
                    is ReminderWebSocketService.ConnectionState.Disconnected -> "Disconnected"
                    is ReminderWebSocketService.ConnectionState.Error -> "Error: ${state.message}"
                }
                println("=== WebSocket state: ${_wsConnectionState.value} ===")
            }
        }
    }
    
    // Load reminders for a user
    fun loadReminders(userId: String, statusFilter: String? = "active") {
        println("=== STARTING loadReminders: user=$userId, filter=$statusFilter ===")
        reminderState = ReminderUiState.Loading
        scope.launch {
            val result = apiService.getUserReminders(userId, statusFilter)
            result
                .onSuccess { reminders ->
                    println("=== LOAD SUCCESS: Got ${reminders.size} reminders ===")
                    reminderState = if (reminders.isEmpty()) {
                        ReminderUiState.Success(emptyList())
                    } else {
                        ReminderUiState.Success(reminders)
                    }
                }
                .onFailure { error ->
                    println("=== LOAD FAILED: ${error.message} ===")
                    println("Error type: ${error::class.simpleName}")
                    error.printStackTrace()
                    reminderState = ReminderUiState.Error(
                        "${error.message}\n\nCheck console logs for details"
                    )
                }
        }
    }
    
    // Create a new reminder
    fun createReminder(request: CreateReminderRequest, onSuccess: () -> Unit = {}) {
        scope.launch {
            println("Creating reminder for user: ${request.userId}")
            apiService.createReminder(request)
                .onSuccess { reminder ->
                    println("Reminder created successfully: ${reminder.id}")
                    // Reload reminders with active filter
                    println("Reloading reminders...")
                    loadReminders(request.userId, "active")
                    println("Calling onSuccess callback")
                    onSuccess()
                }
                .onFailure { error ->
                    println("Failed to create reminder: ${error.message}")
                    error.printStackTrace()
                    reminderState = ReminderUiState.Error(error.message ?: "Failed to create reminder")
                }
        }
    }
    
    // Create reminder from natural language
    fun createReminderFromVoice(request: NaturalLanguageReminderRequest, onSuccess: (Reminder) -> Unit = {}) {
        scope.launch {
            apiService.createReminderFromNaturalLanguage(request)
                .onSuccess { reminder ->
                    loadReminders(request.userId)
                    onSuccess(reminder)
                }
                .onFailure { error ->
                    reminderState = ReminderUiState.Error(error.message ?: "Failed to create reminder")
                }
        }
    }
    
    // Respond to a reminder
    fun respondToReminder(request: ReminderResponseRequest, onComplete: (ReminderResponseResult) -> Unit = {}) {
        scope.launch {
            apiService.respondToReminder(request)
                .onSuccess { result ->
                    responseAnalysis = result
                    onComplete(result)
                    // Reload reminders
                    loadReminders(request.userId)
                }
                .onFailure { error ->
                    reminderState = ReminderUiState.Error(error.message ?: "Failed to process response")
                }
        }
    }
    
    // Snooze a reminder
    fun snoozeReminder(reminderId: String, userId: String, delayMinutes: Int = 15) {
        scope.launch {
            apiService.snoozeReminder(reminderId, delayMinutes)
                .onSuccess {
                    loadReminders(userId)
                }
                .onFailure { error ->
                    reminderState = ReminderUiState.Error(error.message ?: "Failed to snooze reminder")
                }
        }
    }    
    // Complete a reminder (marks as done, creates next occurrence for daily repeats)
    fun completeReminder(reminderId: String, userId: String, onSuccess: () -> Unit = {}) {
        scope.launch {
            println("=== Completing reminder: $reminderId ===")
            apiService.completeReminder(reminderId)
                .onSuccess { nextReminder ->
                    println("=== Reminder completed! Next: ${nextReminder.id} ===")
                    // Clear active alarm
                    _activeAlarm.value = null
                    // Reload reminders to show updated list
                    loadReminders(userId)
                    onSuccess()
                }
                .onFailure { error ->
                    println("=== Failed to complete: ${error.message} ===")
                    reminderState = ReminderUiState.Error(error.message ?: "Failed to complete reminder")
                }
        }
    }
    
    // Dismiss alarm without completing
    fun dismissAlarm() {
        // Stop the alarm sound and vibration
        alarmManager?.dismissAlarm()
        _activeAlarm.value = null
    }    
    // Delete a reminder
    fun deleteReminder(reminderId: String, userId: String) {
        scope.launch {
            apiService.deleteReminder(reminderId)
                .onSuccess {
                    loadReminders(userId)
                }
                .onFailure { error ->
                    reminderState = ReminderUiState.Error(error.message ?: "Failed to delete reminder")
                }
        }
    }
    
    // Update reminder
    fun updateReminder(reminderId: String, request: CreateReminderRequest, onSuccess: () -> Unit = {}) {
        scope.launch {
            apiService.updateReminder(reminderId, request)
                .onSuccess {
                    loadReminders(request.userId)
                    onSuccess()
                }
                .onFailure { error ->
                    reminderState = ReminderUiState.Error(error.message ?: "Failed to update reminder")
                }
        }
    }
    
    // Show response dialog
    fun showResponseDialog(reminder: Reminder) {
        selectedReminder = reminder
        showResponseDialog = true
    }
    
    fun hideResponseDialog() {
        showResponseDialog = false
        responseAnalysis = null
    }
    
    fun showCreateReminderDialog() {
        showCreateDialog = true
    }
    
    fun hideCreateReminderDialog() {
        showCreateDialog = false
    }
    
    fun cleanup() {
        alarmManager?.cleanup()
        scope.cancel()
        apiService.close()
    }
}

class DashboardViewModel {
    private val apiService = ReminderApiService()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    var dashboardState by mutableStateOf<DashboardUiState>(DashboardUiState.Loading)
        private set
    
    var behaviorPattern by mutableStateOf<BehaviorPattern?>(null)
        private set
    
    var weeklyReport by mutableStateOf<WeeklyReportSummary?>(null)
        private set
    
    // Load dashboard data
    fun loadDashboard(userId: String, days: Int = 7) {
        dashboardState = DashboardUiState.Loading
        scope.launch {
            apiService.getDashboardAnalytics(userId, days)
                .onSuccess { dashboard ->
                    dashboardState = DashboardUiState.Success(dashboard)
                }
                .onFailure { error ->
                    dashboardState = DashboardUiState.Error(error.message ?: "Failed to load dashboard")
                }
        }
    }
    
    // Load behavior patterns
    fun loadBehaviorPattern(userId: String, days: Int = 30) {
        scope.launch {
            apiService.getBehaviorPattern(userId, days)
                .onSuccess { pattern ->
                    behaviorPattern = pattern
                }
                .onFailure { error ->
                    // Handle error silently or show notification
                }
        }
    }
    
    // Load weekly report
    fun loadWeeklyReport(userId: String) {
        scope.launch {
            apiService.getWeeklyReportSummary(userId)
                .onSuccess { report ->
                    weeklyReport = report
                }
                .onFailure { error ->
                    // Handle error
                }
        }
    }
    
    fun cleanup() {
        scope.cancel()
        apiService.close()
    }
}

class CaregiverAlertViewModel {
    private val apiService = ReminderApiService()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    var alertState by mutableStateOf<AlertUiState>(AlertUiState.Loading)
        private set
    
    // Load caregiver alerts
    fun loadAlerts(caregiverId: String, activeOnly: Boolean = true) {
        alertState = AlertUiState.Loading
        scope.launch {
            apiService.getCaregiverAlerts(caregiverId, activeOnly)
                .onSuccess { alerts ->
                    alertState = AlertUiState.Success(alerts)
                }
                .onFailure { error ->
                    alertState = AlertUiState.Error(error.message ?: "Failed to load alerts")
                }
        }
    }
    
    // Acknowledge an alert
    fun acknowledgeAlert(alertId: String, caregiverId: String) {
        scope.launch {
            apiService.acknowledgeAlert(alertId, caregiverId)
                .onSuccess {
                    loadAlerts(caregiverId)
                }
                .onFailure { error ->
                    alertState = AlertUiState.Error(error.message ?: "Failed to acknowledge alert")
                }
        }
    }
    
    // Resolve an alert
    fun resolveAlert(alertId: String, caregiverId: String) {
        scope.launch {
            apiService.resolveAlert(alertId, caregiverId)
                .onSuccess {
                    loadAlerts(caregiverId)
                }
                .onFailure { error ->
                    alertState = AlertUiState.Error(error.message ?: "Failed to resolve alert")
                }
        }
    }
    
    fun cleanup() {
        scope.cancel()
        apiService.close()
    }
}
