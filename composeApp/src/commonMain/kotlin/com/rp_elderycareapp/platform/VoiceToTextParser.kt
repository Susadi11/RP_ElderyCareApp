package com.rp_elderycareapp.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

@Composable
expect fun rememberVoiceToTextParser(): VoiceToTextParser

@Composable
expect fun rememberVoicePermission(): VoicePermissionState

interface VoicePermissionState {
    val hasPermission: Boolean
    fun request()
}

data class VoiceToTextParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null
)

interface VoiceToTextParser {
    val state: StateFlow<VoiceToTextParserState>
    fun startListening(languageCode: String = "en-US")
    fun stopListening()
    fun reset()
}
