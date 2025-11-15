package com.rp_elderycareapp.data

import kotlinx.datetime.Clock

enum class MessageType {
    TEXT,
    VOICE
}

enum class MessageSender {
    USER,
    AI_COMPANION
}

data class ChatMessage(
    val id: String,
    val content: String,
    val sender: MessageSender,
    val type: MessageType,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val isPlaying: Boolean = false // For voice messages
)
