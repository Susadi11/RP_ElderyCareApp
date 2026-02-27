package com.rp_elderycareapp.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

@Composable
expect fun rememberAudioRecorder(): AudioRecorder

interface AudioRecorder {
    val isRecording: StateFlow<Boolean>
    val outputFilePath: StateFlow<String?>
    fun startRecording(fileName: String)
    fun stopRecording()
}
