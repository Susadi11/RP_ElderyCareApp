package com.rp_elderycareapp.services

import androidx.compose.runtime.Composable

/**
 * iOS stub for rememberAudioRecorder
 */
@Composable
actual fun rememberAudioRecorder(): AudioRecorder {
    return createAudioRecorder()
}
