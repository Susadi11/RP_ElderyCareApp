package com.rp_elderycareapp.data

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
    val timestamp: Long = System.currentTimeMillis(),
    val isPlaying: Boolean = false // For voice messages
)
