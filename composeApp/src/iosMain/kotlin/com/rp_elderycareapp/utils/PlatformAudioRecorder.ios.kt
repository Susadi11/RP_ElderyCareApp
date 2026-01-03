package com.rp_elderycareapp.utils

actual class PlatformAudioRecorder {
    actual fun startRecording(): String {
        // TODO: Implement iOS audio recording
        throw UnsupportedOperationException("Audio recording not yet implemented for iOS")
    }

    actual fun stopRecording(): String? {
        return null
    }

    actual fun cancelRecording() {
        // No-op
    }
}
