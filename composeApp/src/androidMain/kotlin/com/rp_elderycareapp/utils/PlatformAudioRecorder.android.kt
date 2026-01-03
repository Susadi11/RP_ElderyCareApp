package com.rp_elderycareapp.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class PlatformAudioRecorder {
    private var audioRecorder: AudioRecorder? = null
    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context
        this.audioRecorder = AudioRecorder(context)
    }

    actual fun startRecording(): String {
        return audioRecorder?.startRecording()
            ?: throw IllegalStateException("AudioRecorder not initialized. Call initialize() first.")
    }

    actual fun stopRecording(): String? {
        return audioRecorder?.stopRecording()
    }

    actual fun cancelRecording() {
        audioRecorder?.cancelRecording()
    }
}

@Composable
fun rememberAudioRecorder(): PlatformAudioRecorder {
    val context = LocalContext.current
    return androidx.compose.runtime.remember {
        PlatformAudioRecorder().apply {
            initialize(context.applicationContext)
        }
    }
}
