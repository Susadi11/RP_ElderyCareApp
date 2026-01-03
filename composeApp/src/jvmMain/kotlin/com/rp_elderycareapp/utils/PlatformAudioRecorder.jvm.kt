package com.rp_elderycareapp.utils

actual class PlatformAudioRecorder {
    actual fun startRecording(): String {
        // TODO: Implement JVM audio recording
        throw UnsupportedOperationException("Audio recording not yet implemented for Desktop")
    }

    actual fun stopRecording(): String? {
        return null
    }

    actual fun cancelRecording() {
        // No-op
    }
}
