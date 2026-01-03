package com.rp_elderycareapp.api

import kotlinx.serialization.Serializable

@Serializable
data class ChatTextRequest(
    val user_id: String,
    val message: String,
    val session_id: String? = null,
    val max_tokens: Int = 150,
    val temperature: Double = 0.7,
    val use_history: Boolean = true
)

@Serializable
data class ChatTextResponse(
    val response: String,
    val session_id: String,
    val user_id: String,
    val timestamp: String,
    val metadata: ChatMetadata? = null,
    val safety_warnings: List<String>? = null
)

@Serializable
data class ChatMetadata(
    val model: String,
    val adapter: String,
    val temperature: Double,
    val max_tokens: Int,
    val conversation_length: Int
)

@Serializable
data class VoiceResponse(
    val response: String,
    val session_id: String,
    val user_id: String,
    val timestamp: String,
    val transcription: String,
    val metadata: ChatMetadata? = null,
    val safety_warnings: List<String>? = null
)
