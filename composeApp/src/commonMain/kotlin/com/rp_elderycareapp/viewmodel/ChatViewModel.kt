package com.rp_elderycareapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.rp_elderycareapp.api.ChatApi
import com.rp_elderycareapp.data.ChatMessage
import com.rp_elderycareapp.data.MessageSender
import com.rp_elderycareapp.data.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ChatViewModel {
    private val chatApi = ChatApi()
    // Own scope so coroutines survive navigation (not tied to composable lifecycle)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val messages = mutableStateOf(getInitialMessages())
    val sessionId = mutableStateOf<String?>(null)
    val isTyping = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun sendTextMessage(userId: String, messageText: String) {
        messages.value = messages.value + ChatMessage(
            id = "msg_${Clock.System.now().toEpochMilliseconds()}",
            content = messageText,
            sender = MessageSender.USER,
            type = MessageType.TEXT
        )
        errorMessage.value = null

        scope.launch {
            isTyping.value = true
            try {
                val result = chatApi.sendTextMessage(
                    userId = userId,
                    message = messageText,
                    sessionId = sessionId.value
                )
                result.onSuccess { response ->
                    sessionId.value = response.session_id
                    messages.value = messages.value + ChatMessage(
                        id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                        content = response.response,
                        sender = MessageSender.AI_COMPANION,
                        type = MessageType.TEXT
                    )
                }.onFailure { error ->
                    errorMessage.value = "Connection error: ${error.message}"
                    messages.value = messages.value + ChatMessage(
                        id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                        content = "I'm having trouble connecting to the server. Please make sure the backend is running on localhost:8080",
                        sender = MessageSender.AI_COMPANION,
                        type = MessageType.TEXT
                    )
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
            } finally {
                isTyping.value = false
            }
        }
    }

    fun sendVoiceMessage(userId: String, audioFilePath: String) {
        val voiceMsgId = "voice_${Clock.System.now().toEpochMilliseconds()}"
        messages.value = messages.value + ChatMessage(
            id = voiceMsgId,
            content = "",
            sender = MessageSender.USER,
            type = MessageType.VOICE
        )

        scope.launch {
            isTyping.value = true
            errorMessage.value = null
            try {
                val result = chatApi.sendVoiceMessage(
                    userId = userId,
                    audioFilePath = audioFilePath,
                    sessionId = sessionId.value
                )
                result.onSuccess { voiceResponse ->
                    sessionId.value = voiceResponse.session_id
                    messages.value = messages.value.map { msg ->
                        if (msg.id == voiceMsgId) msg.copy(content = voiceResponse.transcription) else msg
                    }
                    messages.value = messages.value + ChatMessage(
                        id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                        content = voiceResponse.response,
                        sender = MessageSender.AI_COMPANION,
                        type = MessageType.TEXT
                    )
                }.onFailure { error ->
                    errorMessage.value = "Voice error: ${error.message}"
                    messages.value = messages.value + ChatMessage(
                        id = "ai_${Clock.System.now().toEpochMilliseconds()}",
                        content = "I had trouble processing your voice message. Please try again or type your message.",
                        sender = MessageSender.AI_COMPANION,
                        type = MessageType.TEXT
                    )
                }
            } catch (e: Exception) {
                errorMessage.value = "Voice error: ${e.message}"
            } finally {
                isTyping.value = false
            }
        }
    }

    fun toggleVoicePlayback(messageId: String) {
        messages.value = messages.value.map { msg ->
            if (msg.id == messageId && msg.type == MessageType.VOICE) {
                msg.copy(isPlaying = !msg.isPlaying)
            } else {
                msg.copy(isPlaying = false)
            }
        }
    }

    private fun getInitialMessages(): List<ChatMessage> = listOf(
        ChatMessage(
            id = "welcome_1",
            content = "Hello! I'm Hale, your AI Care Companion. I'm here to help you with daily tasks, remind you about medications, and have friendly conversations. How are you feeling today?",
            sender = MessageSender.AI_COMPANION,
            type = MessageType.TEXT,
            timestamp = Clock.System.now().toEpochMilliseconds() - 5000
        )
    )
}
