package com.rp_elderycareapp.services

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
                
                // Parse host and port from baseUrl (e.g., "ws://172.28.2.178:8000")
                val url = baseUrl.removePrefix("ws://").removePrefix("wss://")
                val (host, portStr) = if (":" in url) {
                    url.split(":", limit = 2)
                } else {
                    listOf(url, "80")
                }
                val port = portStr.toIntOrNull() ?: 8000
                
                println("=== Connecting to WebSocket: $host:$port/ws/user/$userId ===")
                
                client.webSocket(
                    host = host,
                    port = port,
                    path = "/ws/user/$userId"
                ) {
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
                    // Parse the reminder from the data field
                    val reminder = json.decodeFromString<Reminder>(wsMessage.data)
                    println("=== 🔔 ALARM: ${reminder.title} at ${reminder.scheduledTime} ===")
                    _reminderAlarms.emit(reminder)
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
