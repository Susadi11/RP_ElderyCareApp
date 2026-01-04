package com.rp_elderycareapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberTextToSpeech(): TextToSpeech {
    return remember {
        object : TextToSpeech {
            override fun speak(text: String) {
                // No-op
            }

            override fun shutdown() {
                // No-op
            }
        }
    }
}
