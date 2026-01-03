package com.rp_elderycareapp.utils

actual class PlatformAudioRecorder {
    actual fun startRecording(): String {
        // TODO: Implement Web audio recording
        throw UnsupportedOperationException("Audio recording not yet implemented for Web")
    }

    actual fun stopRecording(): String? {
        return null
    }

    actual fun cancelRecording() {
        // No-op
    }
}
