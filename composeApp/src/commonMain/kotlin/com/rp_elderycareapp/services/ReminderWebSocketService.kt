package com.rp_elderycareapp.services

import com.rp_elderycareapp.data.reminder.AlarmEvent
import com.rp_elderycareapp.data.reminder.Reminder
import com.rp_elderycareapp.data.reminder.WebSocketMessage
import com.rp_elderycareapp.getApiBaseUrl
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class ReminderWebSocketService(
    private val userId: String
) {
    // Use base URL from Constants.kt - automatically configured
    private val baseUrl: String = getApiBaseUrl().replace("http://", "ws://").replace("https://", "wss://")
    private val client = HttpClient {
        install(WebSockets)
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private val _reminderAlarms = MutableSharedFlow<Reminder>()
    val reminderAlarms: SharedFlow<Reminder> = _reminderAlarms.asSharedFlow()

    /** Emits when a repeat alarm fires. Carries repeat count and reminder id. */
    private val _alarmRepeat = MutableSharedFlow<AlarmEvent>()
    val alarmRepeat: SharedFlow<AlarmEvent> = _alarmRepeat.asSharedFlow()

    /** Emits when backend confirms the alarm was acknowledged. */
    private val _alarmAcknowledged = MutableSharedFlow<AlarmEvent>()
    val alarmAcknowledged: SharedFlow<AlarmEvent> = _alarmAcknowledged.asSharedFlow()

    /** Emits when the backend marks a reminder as missed (after max repeats). */
    private val _alarmMissed = MutableSharedFlow<AlarmEvent>()
    val alarmMissed: SharedFlow<AlarmEvent> = _alarmMissed.asSharedFlow()
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var websocketJob: Job? = null
    private var session: DefaultClientWebSocketSession? = null
    
    sealed class ConnectionState {
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
    
    fun connect(scope: CoroutineScope) {
        websocketJob?.cancel()
        websocketJob = scope.launch {
            try {
                _connectionState.value = ConnectionState.Connecting

                val wsUrl = "$baseUrl/ws/user/$userId"
                println("=== Connecting to WebSocket: $wsUrl ===")

                client.webSocket(wsUrl) {
                    session = this
                    _connectionState.value = ConnectionState.Connected
                    println("=== WebSocket Connected! ===")
                    
                    // Listen for incoming messages
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                println("=== WebSocket received: $text ===")
                                handleMessage(text)
                            }
                            is Frame.Close -> {
                                println("=== WebSocket closed ===")
                                _connectionState.value = ConnectionState.Disconnected
                            }
                            else -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                println("=== WebSocket error: ${e.message} ===")
                e.printStackTrace()
                _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
                // Reconnect after 5 seconds
                delay(5000)
                if (isActive) {
                    connect(scope)
                }
            }
        }
    }
    
    private suspend fun handleMessage(message: String) {
        try {
            val wsMessage = json.decodeFromString<WebSocketMessage>(message)
            println("=== WebSocket message type: ${wsMessage.type} ===")
            
            when (wsMessage.type) {
                "reminder" -> {
                    // Backend may send reminder either in `data` JSON string or flat fields.
                    val reminder = if (!wsMessage.data.isNullOrBlank()) {
                        json.decodeFromString<Reminder>(wsMessage.data)
                    } else {
                        val reminderId = wsMessage.reminderId ?: return
                        val title = wsMessage.title ?: return
                        val scheduledTime = wsMessage.scheduledTime ?: wsMessage.timestamp ?: return
                        Reminder(
                            id = reminderId,
                            userId = wsMessage.userId ?: userId,
                            title = title,
                            description = wsMessage.description,
                            scheduledTime = scheduledTime,
                            priority = wsMessage.priority ?: "medium",
                            category = wsMessage.category ?: "other",
                            escalationEnabled = wsMessage.escalationEnabled ?: true,
                            timeoutSeconds = wsMessage.timeoutSeconds,
                            requiresAcknowledgment = wsMessage.requiresAcknowledgment ?: true
                        )
                    }
                    println("=== 🔔 ALARM: ${reminder.title} at ${reminder.scheduledTime} ===")
                    _reminderAlarms.emit(reminder)
                }
                "reminder_repeat" -> {
                    val reminderId = wsMessage.reminderId ?: return
                    val repeatCount = wsMessage.repeatCount ?: 1
                    println("=== 🔁 REPEAT ALARM #$repeatCount for $reminderId ===")
                    _alarmRepeat.emit(
                        AlarmEvent(
                            type = "reminder_repeat",
                            reminderId = reminderId,
                            repeatCount = repeatCount,
                            totalAttempts = wsMessage.totalAttempts,
                            timeoutSeconds = wsMessage.timeoutSeconds,
                            escalationEnabled = wsMessage.escalationEnabled,
                            message = wsMessage.message
                        )
                    )
                }
                "alarm_acknowledged" -> {
                    val reminderId = wsMessage.reminderId ?: return
                    println("=== ✅ ALARM ACKNOWLEDGED: $reminderId ===")
                    _alarmAcknowledged.emit(
                        AlarmEvent(
                            type = "alarm_acknowledged",
                            reminderId = reminderId,
                            message = wsMessage.message
                        )
                    )
                }
                "alarm_missed" -> {
                    val reminderId = wsMessage.reminderId ?: return
                    println("=== ⚠️ ALARM MISSED: $reminderId ===")
                    _alarmMissed.emit(
                        AlarmEvent(
                            type = "alarm_missed",
                            reminderId = reminderId,
                            totalAttempts = wsMessage.totalAttempts,
                            caregiverNotified = wsMessage.caregiverNotified,
                            message = wsMessage.message
                        )
                    )
                }
                "alert" -> {
                    println("=== Alert received ===")
                    // Handle caregiver alerts
                }
                "update" -> {
                    println("=== Update received ===")
                    // Handle reminder updates
                }
            }
        } catch (e: Exception) {
            println("=== Error parsing WebSocket message: ${e.message} ===")
            e.printStackTrace()
        }
    }
    
    fun disconnect() {
        websocketJob?.cancel()
        session = null
        _connectionState.value = ConnectionState.Disconnected
        println("=== WebSocket disconnected ===")
    }
    
    fun close() {
        disconnect()
        client.close()
    }
}
