package com.rp_elderycareapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.rp_elderycareapp.components.ChatInputBar
import com.rp_elderycareapp.components.MessageBubble
import com.rp_elderycareapp.data.ChatMessage
import com.rp_elderycareapp.data.MessageSender
import com.rp_elderycareapp.data.MessageType
import com.rp_elderycareapp.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit = {}
) {
    var messages by remember { mutableStateOf(getInitialMessages()) }
    var currentMessage by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat header with glass effect
        ChatHeaderContent(
            isTyping = isTyping,
            onNavigateBack = onNavigateBack
        )

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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

        // Input bar
        ChatInputBar(
            message = currentMessage,
            onMessageChange = { currentMessage = it },
            onSendMessage = {
                if (currentMessage.trim().isNotEmpty()) {
                    // Add user message
                    val userMessage = ChatMessage(
                        id = "msg_${System.currentTimeMillis()}",
                        content = currentMessage.trim(),
                        sender = MessageSender.USER,
                        type = MessageType.TEXT
                    )
                    messages = messages + userMessage
                    currentMessage = ""
                    
                    // Simulate AI response
                    coroutineScope.launch {
                        isTyping = true
                        delay(1500) // Simulate thinking time
                        
                        val aiResponse = ChatMessage(
                            id = "ai_${System.currentTimeMillis()}",
                            content = generateAIResponse(userMessage.content),
                            sender = MessageSender.AI_COMPANION,
                            type = MessageType.TEXT
                        )
                        messages = messages + aiResponse
                        isTyping = false
                    }
                }
            },
            onStartVoiceRecording = {
                isRecording = true
                // TODO: Implement actual voice recording
            },
            onStopVoiceRecording = {
                isRecording = false
                // Add voice message
                val voiceMessage = ChatMessage(
                    id = "voice_${System.currentTimeMillis()}",
                    content = "Voice message recorded",
                    sender = MessageSender.USER,
                    type = MessageType.VOICE
                )
                messages = messages + voiceMessage
                
                // Simulate AI voice response
                coroutineScope.launch {
                    isTyping = true
                    delay(2000)
                    
                    val aiVoiceResponse = ChatMessage(
                        id = "ai_voice_${System.currentTimeMillis()}",
                        content = "I heard your voice message! Let me help you with that.",
                        sender = MessageSender.AI_COMPANION,
                        type = MessageType.TEXT
                    )
                    messages = messages + aiVoiceResponse
                    isTyping = false
                }
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
            timestamp = System.currentTimeMillis() - 5000
        )
    )
}

@Composable
expect fun ChatHeaderContent(
    isTyping: Boolean,
    onNavigateBack: () -> Unit
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
