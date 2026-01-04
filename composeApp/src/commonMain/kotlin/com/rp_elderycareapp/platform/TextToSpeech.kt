package com.rp_elderycareapp.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberTextToSpeech(): TextToSpeech

interface TextToSpeech {
    fun speak(text: String)
    fun shutdown()
}
