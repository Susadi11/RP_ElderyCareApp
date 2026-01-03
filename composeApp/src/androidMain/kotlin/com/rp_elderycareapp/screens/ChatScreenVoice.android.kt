package com.rp_elderycareapp.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.rp_elderycareapp.api.ChatApi
import com.rp_elderycareapp.data.ChatMessage
import com.rp_elderycareapp.data.MessageSender
import com.rp_elderycareapp.data.MessageType
import com.rp_elderycareapp.utils.rememberAudioRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun VoiceRecordingHandler(
    isRecording: Boolean,
    onRecordingStarted: () -> Unit,
    onRecordingFailed: (String) -> Unit,
    onRecordingStopped: (String?) -> Unit
): VoiceRecorder {
    val context = LocalContext.current
    val audioRecorder = rememberAudioRecorder()
    var recordingFilePath by remember { mutableStateOf<String?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val filePath = audioRecorder.startRecording()
                recordingFilePath = filePath
                onRecordingStarted()
            } catch (e: Exception) {
                onRecordingFailed("Failed to start recording: ${e.message}")
            }
        } else {
            onRecordingFailed("Microphone permission denied")
        }
    }

    return object : VoiceRecorder {
        override fun start() {
            // Check permission first
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) -> {
                    try {
                        val filePath = audioRecorder.startRecording()
                        recordingFilePath = filePath
                        onRecordingStarted()
                    } catch (e: Exception) {
                        onRecordingFailed("Failed to start recording: ${e.message}")
                    }
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }

        override fun stop() {
            val filePath = audioRecorder.stopRecording()
            recordingFilePath = null
            onRecordingStopped(filePath)
        }

        override fun cancel() {
            audioRecorder.cancelRecording()
            recordingFilePath = null
        }
    }
}

interface VoiceRecorder {
    fun start()
    fun stop()
    fun cancel()
}

// Helper function to handle voice API call
suspend fun sendVoiceToBackend(
    chatApi: ChatApi,
    audioFilePath: String,
    userId: String,
    sessionId: String?,
    onSuccess: (String, String, String) -> Unit, // (transcription, response, newSessionId)
    onFailure: (String) -> Unit
) {
    val result = chatApi.sendVoiceMessage(
        userId = userId,
        audioFilePath = audioFilePath,
        sessionId = sessionId
    )

    result.onSuccess { voiceResponse ->
        onSuccess(
            voiceResponse.transcription,
            voiceResponse.response,
            voiceResponse.session_id
        )
    }.onFailure { error ->
        onFailure(error.message ?: "Unknown error")
    }
}
