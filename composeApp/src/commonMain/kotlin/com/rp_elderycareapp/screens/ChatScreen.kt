package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rp_elderycareapp.api.ChatApi
import com.rp_elderycareapp.components.ChatInputBar
import com.rp_elderycareapp.components.MessageBubble
import com.rp_elderycareapp.data.ChatMessage
import com.rp_elderycareapp.data.MessageSender
import com.rp_elderycareapp.data.MessageType
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun ChatScreen(
    authViewModel: AuthViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    var messages by remember { mutableStateOf(getInitialMessages()) }
    var currentMessage by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    var sessionId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Get the actual user ID from authViewModel
    val currentUser = authViewModel?.currentUser?.value
    val userId = currentUser?.user_id ?: "guest_user"

    val chatApi = remember { ChatApi() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Initialize platform-specific voice recorder
    InitializeVoiceRecorderIfNeeded()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                try {
                    delay(100) // Small delay to ensure layout is complete
                    listState.animateScrollToItem(messages.size - 1)
                } catch (e: Exception) {
                    listState.scrollToItem(messages.size - 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat header with glass effect - fixed at top
        ChatHeaderContent(
            isTyping = isTyping,
            onNavigateBack = onNavigateBack
        )

        // Messages list - takes remaining space
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Take all available space between header and input
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp // Extra padding at bottom for better spacing
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    onVoicePlayPause = { messageId ->
                        messages = messages.map { msg ->
                            if (msg.id == messageId && msg.type == MessageType.VOICE) {
                                msg.copy(isPlaying = !msg.isPlaying)
                            } else {
                                msg.copy(isPlaying = false)
                            }
                        }
                    }
                )
            }
        }

        // Input bar at the bottom - will move with keyboard
        ChatInputBar(
            message = currentMessage,
            onMessageChange = { currentMessage = it },
            onSendMessage = {
                if (currentMessage.trim().isNotEmpty()) {
                    // Add user message
                    val userMessage = ChatMessage(
                        id = "msg_${Clock.System.now().toEpochMilliseconds()}",
                        content = currentMessage.trim(),
                        sender = MessageSender.USER,
                        type = MessageType.TEXT
                    )
                    messages = messages + userMessage
                    val messageText = currentMessage.trim()
                    currentMessage = ""

                    // Call real backend API
                    coroutineScope.launch {
                        isTyping = true
                        errorMessage = null

                        try {
                            val result = chatApi.sendTextMessage(
                                userId = userId, // Use actual logged-in user ID
                                message = messageText,
                                sessionId = sessionId
                            )

                            result.onSuccess { response ->
                                // Update session ID from response
                                sessionId = response.session_id

                                val aiResponse = ChatMessage(
                                    id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                                    content = response.response,
                                    sender = MessageSender.AI_COMPANION,
                                    type = MessageType.TEXT
                                )
                                messages = messages + aiResponse
                            }.onFailure { error ->
                                errorMessage = "Connection error: ${error.message}"
                                // Fallback to local response on error
                                val aiResponse = ChatMessage(
                                    id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                                    content = "I'm having trouble connecting to the server. Please make sure the backend is running on localhost:8000",
                                    sender = MessageSender.AI_COMPANION,
                                    type = MessageType.TEXT
                                )
                                messages = messages + aiResponse
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                        } finally {
                            isTyping = false
                        }
                    }
                }
            },
            onStartVoiceRecording = {
                startVoiceRecording(
                    onStarted = { isRecording = true },
                    onError = { error ->
                        isRecording = false
                        errorMessage = error
                    }
                )
            },
            onStopVoiceRecording = {
                stopVoiceRecording(
                    onStopped = { audioFilePath ->
                        isRecording = false
                        if (audioFilePath != null) {
                            // Call real backend API with voice
                            coroutineScope.launch {
                                isTyping = true
                                errorMessage = null

                                try {
                                    val result = chatApi.sendVoiceMessage(
                                        userId = userId, // Use actual logged-in user ID
                                        audioFilePath = audioFilePath,
                                        sessionId = sessionId
                                    )

                                    result.onSuccess { voiceResponse ->
                                        // Update session ID
                                        sessionId = voiceResponse.session_id

                                        // Add voice message as VOICE type (shows transcription in voice bubble)
                                        val userVoiceMessage = ChatMessage(
                                            id = "voice_${Clock.System.now().toEpochMilliseconds()}",
                                            content = voiceResponse.transcription,
                                            sender = MessageSender.USER,
                                            type = MessageType.VOICE
                                        )
                                        messages = messages + userVoiceMessage

                                        // Add AI response as TEXT
                                        val aiResponse = ChatMessage(
                                            id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                                            content = voiceResponse.response,
                                            sender = MessageSender.AI_COMPANION,
                                            type = MessageType.TEXT
                                        )
                                        messages = messages + aiResponse
                                    }.onFailure { error ->
                                        errorMessage = "Voice error: ${error.message}"
                                        val aiResponse = ChatMessage(
                                            id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                                            content = "I had trouble processing your voice message. Please try again or type your message.",
                                            sender = MessageSender.AI_COMPANION,
                                            type = MessageType.TEXT
                                        )
                                        messages = messages + aiResponse
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Voice error: ${e.message}"
                                } finally {
                                    isTyping = false
                                }
                            }
                        }
                    }
                )
            },
            onCancelVoiceRecording = {
                cancelVoiceRecording(
                    onCancelled = {
                        isRecording = false
                    }
                )
            },
            isRecording = isRecording
        )
    }
}

private fun getInitialMessages(): List<ChatMessage> {
    return listOf(
        ChatMessage(
            id = "welcome_1",
            content = "Hello! I'm Hale, your AI Care Companion. I'm here to help you with daily tasks, remind you about medications, and have friendly conversations. How are you feeling today?",
            sender = MessageSender.AI_COMPANION,
            type = MessageType.TEXT,
            timestamp = Clock.System.now().toEpochMilliseconds() - 5000
        )
    )
}

@Composable
expect fun ChatHeaderContent(
    isTyping: Boolean,
    onNavigateBack: () -> Unit
)

// Platform-specific voice recorder initialization
@Composable
expect fun InitializeVoiceRecorderIfNeeded()

// Platform-specific voice recording
expect fun startVoiceRecording(
    onStarted: () -> Unit,
    onError: (String) -> Unit
)

expect fun stopVoiceRecording(
    onStopped: (String?) -> Unit
)

expect fun cancelVoiceRecording(
    onCancelled: () -> Unit
)

private fun generateAIResponse(userMessage: String): String {
    val responses = when {
        userMessage.lowercase().contains("hello") || userMessage.lowercase().contains("hi") -> 
            listOf(
                "Hello there! It's wonderful to see you today. How can I assist you?",
                "Hi! I'm so glad you're here. What would you like to talk about today?"
            )
        userMessage.lowercase().contains("help") -> 
            listOf(
                "I'm here to help! I can assist with reminders, answer questions, or just have a friendly chat. What do you need?",
                "Of course! I'd be happy to help you with anything you need. What's on your mind?"
            )
        userMessage.lowercase().contains("medication") || userMessage.lowercase().contains("medicine") -> 
            listOf(
                "I can help you keep track of your medications. Would you like me to set up reminders for you?",
                "Let's talk about your medications. Do you need help remembering when to take them?"
            )
        userMessage.lowercase().contains("feeling") || userMessage.lowercase().contains("feel") -> 
            listOf(
                "Thank you for sharing how you're feeling. It's important to check in with ourselves. Tell me more about it.",
                "I'm here to listen. How you feel matters to me. Would you like to talk about it?"
            )
        else -> listOf(
            "That's interesting! Tell me more about that.",
            "I understand. How can I help you with this?",
            "Thank you for sharing that with me. What would you like to know more about?",
            "I'm here to listen and help. What else is on your mind?"
        )
    }
    return responses.random()
}
