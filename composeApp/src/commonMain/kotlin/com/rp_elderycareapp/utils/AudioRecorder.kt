package com.rp_elderycareapp.utils

// Platform-specific audio recorder
expect class PlatformAudioRecorder() {
    fun startRecording(): String
    fun stopRecording(): String?
    fun cancelRecording()
}
