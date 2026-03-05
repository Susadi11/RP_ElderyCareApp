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

    companion object {
        /** How long the alarm plays before being temporarily silenced (2 minutes). */
        private const val NO_RESPONSE_MILLIS = 2L * 60 * 1000
        /** Silent gap between repeats (1 minute). */
        private const val RETRY_DELAY_MILLIS  = 1L * 60 * 1000
        /** Maximum number of re-raise cycles before marking as missed. */
        private const val MAX_REPEATS = 3
    }

    /** Job that runs the 2-min → stop → 1-min → repeat timeout cycle. */
    private var alarmTimeoutJob: Job? = null
    /** Keeps a reference to the currently ringing reminder so repeats can re-use it. */
    private var lastAlarmReminder: Reminder? = null
    
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
    
    var stopAlarmResult by mutableStateOf<StopAlarmResponse?>(null)
        private set
    
    var snoozeTrackedResult by mutableStateOf<SnoozeTrackedResponse?>(null)
        private set
    
    var helpRequestResult by mutableStateOf<HelpRequestResponse?>(null)
        private set
    
    private val _activeAlarm = MutableStateFlow<Reminder?>(null)
    val activeAlarm: StateFlow<Reminder?> = _activeAlarm.asStateFlow()

    /** How many times the current alarm has repeated (0 = first ring). */
    private val _alarmRepeatCount = MutableStateFlow(0)
    val alarmRepeatCount: StateFlow<Int> = _alarmRepeatCount.asStateFlow()

    /** Non-null when the backend has marked a reminder as missed. Cleared after UI reads it. */
    private val _missedAlarmMessage = MutableStateFlow<String?>(null)
    val missedAlarmMessage: StateFlow<String?> = _missedAlarmMessage.asStateFlow()
    
    private val _wsConnectionState = MutableStateFlow<String>("Disconnected")
    val wsConnectionState: StateFlow<String> = _wsConnectionState.asStateFlow()
    
    private var pollingJob: Job? = null
    
    // Initialize WebSocket connection AND start polling for due reminders
    fun initWebSocket(userId: String) {
        // Start WebSocket (for future real-time updates)
        initWebSocketOnly(userId)
        
        // Start polling for due reminders (backup mechanism)
        startReminderPolling(userId)
    }
    
    private fun initWebSocketOnly(userId: String) {
        webSocketService = ReminderWebSocketService(userId)
        webSocketService?.connect(scope)
        
        // Listen for initial alarm notifications
        scope.launch {
            webSocketService?.reminderAlarms?.collect { reminder ->
                println("=== 🚨 ALARM RECEIVED: ${reminder.title} ===")
                lastAlarmReminder = reminder
                _alarmRepeatCount.value = 0
                _activeAlarm.value = reminder
                // Trigger platform-specific alarm (music, vibration, notification)
                alarmManager?.triggerAlarm(reminder)
                // Start the 2-min → stop → 1-min → repeat timeout cycle
                startAlarmTimeoutCycle(reminder, userId)
            }
        }

        // Listen for repeat alarms from backend — sync visual count.
        // The frontend timeout cycle already manages the actual re-triggering, so
        // we only update the count here to keep both in sync. If a backend repeat
        // arrives while the alarm is silenced (during the 1-min gap), we also
        // re-show it immediately (the backend may have tighter timing).
        scope.launch {
            webSocketService?.alarmRepeat?.collect { event ->
                println("=== 🔁 REPEAT ALARM #${event.repeatCount}: ${event.reminderId} ===")
                _alarmRepeatCount.value = event.repeatCount
                val last = lastAlarmReminder
                if (last != null && last.id == event.reminderId && _activeAlarm.value == null) {
                    // Alarm was in the silent gap — backend beat the timer, re-show now
                    cancelAlarmTimeout()
                    _activeAlarm.value = last
                    alarmManager?.triggerAlarm(last)
                    startAlarmTimeoutCycle(last, event.reminderId)
                }
            }
        }

        // Listen for alarm acknowledged (backend confirms another device/actor acked it)
        scope.launch {
            webSocketService?.alarmAcknowledged?.collect { event ->
                println("=== ✅ WS ACKNOWLEDGED: ${event.reminderId} ===")
                if (_activeAlarm.value?.id == event.reminderId ||
                    lastAlarmReminder?.id == event.reminderId) {
                    cancelAlarmTimeout()
                    alarmManager?.stopAlarm()
                    _activeAlarm.value = null
                    _alarmRepeatCount.value = 0
                }
            }
        }

        // Listen for missed alarm (backend gives up after max repeats)
        scope.launch {
            webSocketService?.alarmMissed?.collect { event ->
                println("=== ⚠️ ALARM MISSED: ${event.reminderId} ===")
                if (_activeAlarm.value?.id == event.reminderId ||
                    lastAlarmReminder?.id == event.reminderId) {
                    cancelAlarmTimeout()
                    alarmManager?.dismissAlarm()
                    _activeAlarm.value = null
                    _alarmRepeatCount.value = 0
                }
                _missedAlarmMessage.value = event.message ?: "⚠️ You missed a reminder. Caregiver has been notified."
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
            // For active tab, get all non-completed reminders (includes snoozed)
            val actualFilter = if (statusFilter == "active") null else statusFilter
            val result = apiService.getUserReminders(userId, actualFilter)
            result
                .onSuccess { allReminders ->
                    // Safety filter: render only reminders for the requested user.
                    val userScopedReminders = allReminders.filter { it.userId == userId }
                    // Filter client-side for active tab to include both active and snoozed
                    val reminders = if (statusFilter == "active") {
                        userScopedReminders.filter { it.status.lowercase() == "active" || it.status.lowercase() == "snoozed" }
                    } else if (statusFilter == "completed") {
                        userScopedReminders.filter { it.status.lowercase() == "completed" }
                    } else {
                        userScopedReminders
                    }
                    println("=== LOAD SUCCESS: Got ${reminders.size} reminders (${userScopedReminders.size} user-scoped, ${allReminders.size} total) ===")
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
                    if (error.message == "__RELOAD__") {
                        // Server created the reminder but did not echo back the object.
                        // Treat as success: reload the list and continue.
                        println("Reminder created (no echo). Reloading list...")
                        loadReminders(request.userId, "active")
                        onSuccess()
                    } else {
                        println("Failed to create reminder: ${error.message}")
                        error.printStackTrace()
                        reminderState = ReminderUiState.Error(error.message ?: "Failed to create reminder")
                    }
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
    
    // Create reminder from audio file (voice recording)
    fun createReminderFromAudio(
        audioFilePath: String,
        userId: String,
        priority: String? = null,
        onSuccess: (AudioReminderResponse) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        scope.launch {
            try {
                // Read audio file as bytes
                val audioFile = java.io.File(audioFilePath)
                if (!audioFile.exists()) {
                    onError("Audio file not found")
                    reminderState = ReminderUiState.Error("Audio file not found")
                    return@launch
                }
                
                val audioBytes = audioFile.readBytes()
                val fileName = audioFile.name
                
                println("Uploading audio file: $fileName (${audioBytes.size} bytes)")
                
                apiService.createReminderFromAudio(
                    audioFile = audioBytes,
                    fileName = fileName,
                    userId = userId,
                    priority = priority
                )
                    .onSuccess { response ->
                        println("Audio reminder created successfully!")
                        println("Transcription: ${response.transcription}")
                        loadReminders(userId, "active")
                        onSuccess(response)
                    }
                    .onFailure { error ->
                        println("Failed to create audio reminder: ${error.message}")
                        val errorMsg = error.message ?: "Failed to create reminder from audio"
                        reminderState = ReminderUiState.Error(errorMsg)
                        onError(errorMsg)
                    }
            } catch (e: Exception) {
                println("Exception reading audio file: ${e.message}")
                e.printStackTrace()
                val errorMsg = "Failed to read audio file: ${e.message}"
                reminderState = ReminderUiState.Error(errorMsg)
                onError(errorMsg)
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
        cancelAlarmTimeout()
        alarmManager?.dismissAlarm()
        _activeAlarm.value = null
        _alarmRepeatCount.value = 0
    }

    /** Clear the missed alarm message after the UI has shown it. */
    fun clearMissedAlarmMessage() {
        _missedAlarmMessage.value = null
    }

    /**
     * Starts the frontend alarm timeout cycle for the given reminder.
     *
     * Cycle per iteration:
     *  - Wait 2 minutes (NO_RESPONSE_MILLIS): alarm plays + dialog visible
     *  - No response → hide SCREEN ONLY (sound keeps playing in background)
     *  - Wait 1 minute (RETRY_DELAY_MILLIS): sound still playing, no screen
     *  - Re-raise: show dialog again with REPEAT #X badge
     *  - After MAX_REPEATS cycles with no response → mark as missed, stop everything
     *
     * The cycle is cancelled as soon as the user acknowledges, snoozes, or stops the alarm.
     */
    private fun startAlarmTimeoutCycle(reminder: Reminder, userId: String) {
        alarmTimeoutJob?.cancel()
        alarmTimeoutJob = scope.launch {
            var repeats = 0
            while (isActive) {
                // ── Phase 1: wait for the user to respond ─────────────────────────
                delay(NO_RESPONSE_MILLIS)

                // User responded in time — exit
                if (_activeAlarm.value?.id != reminder.id && lastAlarmReminder?.id != reminder.id) {
                    println("=== ⏱️ Timeout cycle: alarm already handled, exiting ===")
                    return@launch
                }

                repeats++
                println("=== ⏱️ No response after 2 min (repeat $repeats/$MAX_REPEATS) for '${reminder.title}' ===")

                if (repeats >= MAX_REPEATS) {
                    // ── All retries exhausted → stop everything and declare missed ──
                    println("=== ❌ '${reminder.title}' MISSED after $repeats repeats ===")
                    alarmManager?.dismissAlarm()   // stop sound + vibration completely
                    _activeAlarm.value = null
                    _alarmRepeatCount.value = 0
                    lastAlarmReminder = null
                    // Notify backend to update status → "missed"
                    scope.launch {
                        apiService.markReminderMissed(reminder.id, userId)
                            .onSuccess { println("=== Backend marked as missed ===") }
                            .onFailure { println("=== Could not reach backend for missed status: ${it.message} ===") }
                        loadReminders(userId)
                    }
                    _missedAlarmMessage.value =
                        "⚠️ You missed: ${reminder.title}.\nYour caregiver has been notified."
                    return@launch
                }

                // ── Phase 2: 1-minute gap — hide screen but KEEP sound playing ─────
                // Only clear the active alarm (hides dialog); do NOT call stopAlarm()
                _activeAlarm.value = null
                println("=== 🔕 Screen hidden — sound still playing for 1 min ===")

                delay(RETRY_DELAY_MILLIS)
                if (!isActive) return@launch

                // ── Phase 3: re-raise — show dialog again with incremented count ───
                println("=== 🔁 Re-raising alarm (repeat #$repeats): '${reminder.title}' ===")
                _alarmRepeatCount.value = repeats
                _activeAlarm.value = reminder
                // Sound is already playing; restart it cleanly so it's fresh
                alarmManager?.triggerAlarm(reminder)
            }
        }
    }

    /** Cancel any running alarm timeout cycle (call on acknowledge / snooze / stop). */
    private fun cancelAlarmTimeout() {
        alarmTimeoutJob?.cancel()
        alarmTimeoutJob = null
        lastAlarmReminder = null
    }


    /**
     * Acknowledge the active alarm — calls POST /api/reminders/acknowledge/{id}.
     * This is the explicit confirmation step after the user presses "Stop" and confirms.
     * On success: stops the alarm sound/vibration and clears the active alarm state.
     */
    fun acknowledgeReminder(
        reminderId: String,
        userId: String,
        acknowledgmentMethod: String = "tap",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        cancelAlarmTimeout()
        scope.launch {
            apiService.acknowledgeReminder(reminderId, userId, acknowledgmentMethod)
                .onSuccess { response ->
                    println("=== ✅ ALARM ACKNOWLEDGED: ${response.message} ===")
                    alarmManager?.stopAlarm()
                    _activeAlarm.value = null
                    _alarmRepeatCount.value = 0
                    loadReminders(userId)
                    onSuccess()
                }
                .onFailure { error ->
                    println("=== ❌ ACKNOWLEDGE FAILED: ${error.message} ===")
                    onError(error.message ?: "Failed to acknowledge reminder")
                }
        }
    }

    // Delete a reminder — optimistic: remove from list immediately, sync server in background
    fun deleteReminder(reminderId: String, userId: String) {
        // Snapshot current list so we can revert on failure
        val previousState = reminderState

        // Optimistically remove from local list right now
        if (previousState is ReminderUiState.Success) {
            val updatedList = previousState.reminders.filter { it.id != reminderId }
            reminderState = ReminderUiState.Success(updatedList)
        }

        scope.launch {
            apiService.deleteReminder(reminderId)
                .onSuccess {
                    // Server confirmed deletion — do a quiet background reload to sync
                    println("Delete confirmed by server, reloading list")
                    loadReminders(userId)
                }
                .onFailure { error ->
                    println("Delete API call failed: ${error.message}")
                    // Revert optimistic removal so the item reappears
                    reminderState = previousState
                }
        }
    }
    
    // Update reminder — optimistic: update list immediately, sync server in background
    fun updateReminder(reminderId: String, request: CreateReminderRequest, onSuccess: () -> Unit = {}) {
        // Snapshot current list so we can revert on failure
        val previousState = reminderState

        // Optimistically update the item in-place so the card reflects the new values instantly
        if (previousState is ReminderUiState.Success) {
            val updatedList = previousState.reminders.map { reminder ->
                if (reminder.id == reminderId) {
                    reminder.copy(
                        title = request.title,
                        description = request.description,
                        category = request.category,
                        priority = request.priority
                    )
                } else reminder
            }
            reminderState = ReminderUiState.Success(updatedList)
        }

        onSuccess() // dismiss the dialog immediately

        scope.launch {
            apiService.updateReminder(reminderId, request)
                .onSuccess {
                    println("Update confirmed by server, reloading list")
                    loadReminders(request.userId)
                }
                .onFailure { error ->
                    if (error.message == "__RELOAD__") {
                        // Server updated but did not echo back — treat as success
                        println("Update confirmed (no echo), reloading list")
                        loadReminders(request.userId)
                    } else {
                        println("Update failed: ${error.message}")
                        // Revert optimistic change
                        reminderState = previousState
                    }
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
    
    // Start polling for due reminders every 30 seconds
    private fun startReminderPolling(userId: String) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                try {
                    println("=== 🔍 Polling for due reminders... ===")
                    val result = apiService.getDueReminders(userId, timeWindowMinutes = 5)
                    result.onSuccess { response ->
                        if (response.shouldTriggerAlarm && response.urgentReminders.isNotEmpty()) {
                            val reminder = response.urgentReminders.first()
                            println("=== 🚨 DUE REMINDER FOUND: ${reminder.title} ===")
                            
                            // Only trigger if not already showing this alarm
                            if (_activeAlarm.value?.id != reminder.id) {
                                lastAlarmReminder = reminder
                                _alarmRepeatCount.value = 0
                                _activeAlarm.value = reminder
                                // Trigger platform-specific alarm (music, vibration, notification)
                                alarmManager?.triggerAlarm(reminder)
                                // Start the 2-min → stop → 1-min → repeat timeout cycle
                                startAlarmTimeoutCycle(reminder, userId)
                            }
                        } else {
                            println("=== ✓ No urgent reminders at this time ===")
                        }
                    }.onFailure { error ->
                        println("=== ⚠️ Polling error: ${error.message} ===")
                    }
                } catch (e: Exception) {
                    println("=== ⚠️ Polling exception: ${e.message} ===")
                }
                
                // Poll every 30 seconds
                delay(30000)
            }
        }
    }
    
    // NEW: Stop alarm with response tracking (replaces completeReminder)
    fun stopAlarmWithResponse(reminderId: String, userId: String, userResponse: String, onComplete: (StopAlarmResponse) -> Unit = {}) {
        cancelAlarmTimeout()
        scope.launch {
            println("=== Stopping alarm with response: $reminderId ===")
            // Always clear the alarm UI immediately so the user is never stuck
            _activeAlarm.value = null
            alarmManager?.dismissAlarm()

            apiService.stopAlarmWithResponse(reminderId, userResponse)
                .onSuccess { response ->
                    println("=== Alarm stopped! Risk score: ${response.cognitiveAnalysis.riskScore} ===")
                    println("=== Caregiver notified: ${response.cognitiveAnalysis.caregiverNotified} ===")
                    stopAlarmResult = response
                    loadReminders(userId, "active")
                    onComplete(response)
                }
                .onFailure { error ->
                    println("=== Failed to stop alarm (alarm already dismissed): ${error.message} ===")
                    // Still reload and navigate – don't leave the user on an error screen
                    loadReminders(userId, "active")
                }
        }
    }
    
    // NEW: Snooze with behavior tracking (replaces snoozeReminder - uses 3 min default)
    fun snoozeReminderTracked(reminderId: String, userId: String, delayMinutes: Int = 3, onComplete: (SnoozeTrackedResponse) -> Unit = {}) {
        cancelAlarmTimeout()
        scope.launch {
            println("=== Snoozing reminder (tracked): $reminderId for $delayMinutes min ===")
            // Always clear the alarm UI immediately so the screen is never stuck
            _activeAlarm.value = null
            alarmManager?.dismissAlarm()

            apiService.snoozeReminderTracked(reminderId, delayMinutes)
                .onSuccess { response ->
                    println("=== SNOOZE SUCCESS ===")
                    println("=== Reminder ID: ${response.reminderId} ===")
                    println("=== New scheduled time from API: '${response.newScheduledTime}' ===")
                    println("=== Count this week: ${response.snoozeCountWeek}, rate: ${response.snoozeRate} ===")
                    println("=== Caregiver alert: ${response.caregiverAlert} ===")
                    
                    snoozeTrackedResult = response
                    
                    // Update the reminder locally with new scheduled time
                    updateReminderScheduledTime(reminderId, response.newScheduledTime)
                    
                    // Don't call loadReminders immediately - let the local update take effect
                    // loadReminders(userId, "active")
                    onComplete(response)
                }
                .onFailure { error ->
                    println("=== Failed to snooze (alarm already dismissed): ${error.message} ===")
                    loadReminders(userId, "active")
                }
        }
    }
    
    // Update reminder's scheduled time locally (for snooze)
    private fun updateReminderScheduledTime(reminderId: String, newScheduledTime: String) {
        println("🔄 UPDATING LOCAL REMINDER:")
        println("   Reminder ID: $reminderId")
        println("   New scheduled time: '$newScheduledTime'")
        
        val currentState = reminderState
        if (currentState is ReminderUiState.Success) {
            val oldReminder = currentState.reminders.find { it.id == reminderId }
            println("   Old scheduled time: '${oldReminder?.scheduledTime}'")
            
            val updatedReminders = currentState.reminders.map { reminder ->
                if (reminder.id == reminderId) {
                    val updated = reminder.copy(
                        scheduledTime = newScheduledTime,
                        status = "snoozed"
                    )
                    println("   ✅ Updated reminder: ID=${updated.id}, time='${updated.scheduledTime}', status='${updated.status}'")
                    updated
                } else {
                    reminder
                }
            }
            reminderState = ReminderUiState.Success(updatedReminders)
            println("   ✅ Local state updated successfully")
        } else {
            println("   ❌ Current state is not Success: ${currentState::class.simpleName}")
        }
    }
    
    // NEW: Request help when confused
    fun requestHelp(reminderId: String, userId: String, helpReason: String = "confused", onComplete: (HelpRequestResponse) -> Unit = {}) {
        scope.launch {
            println("=== Requesting help for reminder: $reminderId, reason: $helpReason ===")
            apiService.requestHelp(reminderId, helpReason)
                .onSuccess { response ->
                    println("=== Help requested! Caregiver notified: ${response.caregiverNotified} ===")
                    println("=== Risk score: ${response.cognitiveRiskScore} ===")
                    helpRequestResult = response
                    
                    // Clear active alarm
                    _activeAlarm.value = null
                    alarmManager?.dismissAlarm()
                    
                    // Reload reminders
                    loadReminders(userId)
                    
                    onComplete(response)
                }
                .onFailure { error ->
                    println("=== Failed to request help: ${error.message} ===")
                    reminderState = ReminderUiState.Error(error.message ?: "Failed to request help")
                }
        }
    }
    
    fun cleanup() {
        pollingJob?.cancel()
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
