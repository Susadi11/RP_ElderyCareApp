package com.rp_elderycareapp.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.rp_elderycareapp.R
import com.rp_elderycareapp.ui.theme.AppColors
import com.rp_elderycareapp.utils.rememberAudioRecorder

@Composable
actual fun ChatHeaderContent(
    isTyping: Boolean,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppColors.LightBlue.copy(alpha = 0.6f),
                        AppColors.LightBlue.copy(alpha = 0.4f)
                    )
                )
            )
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AppColors.DeepBlue.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                    contentDescription = "Back",
                    tint = AppColors.DeepBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "Chat with Hale",
                    color = AppColors.DeepBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isTyping) "Typing..." else "Online • Ready to help",
                    color = AppColors.DeepBlue.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Simple audio recorder holder
private object VoiceRecorderManager {
    var recorder: com.rp_elderycareapp.utils.PlatformAudioRecorder? = null
    var currentPath: String? = null

    fun initialize(context: android.content.Context) {
        if (recorder == null) {
            recorder = com.rp_elderycareapp.utils.PlatformAudioRecorder().apply {
                initialize(context)
            }
        }
    }
}

actual fun startVoiceRecording(
    onStarted: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val recorder = VoiceRecorderManager.recorder
        if (recorder == null) {
            onError("Audio recorder not initialized")
            return
        }

        val path = recorder.startRecording()
        VoiceRecorderManager.currentPath = path
        onStarted()
    } catch (e: Exception) {
        onError("Failed to start recording: ${e.message}")
    }
}

actual fun stopVoiceRecording(
    onStopped: (String?) -> Unit
) {
    try {
        val recorder = VoiceRecorderManager.recorder
        val path = recorder?.stopRecording()
        VoiceRecorderManager.currentPath = null
        onStopped(path)
    } catch (e: Exception) {
        onStopped(null)
    }
}

actual fun cancelVoiceRecording(
    onCancelled: () -> Unit
) {
    try {
        val recorder = VoiceRecorderManager.recorder
        recorder?.cancelRecording()
        VoiceRecorderManager.currentPath = null
        onCancelled()
    } catch (e: Exception) {
        onCancelled()
    }
}

// Initialize on first composition with permission handling
@Composable
actual fun InitializeVoiceRecorderIfNeeded() {
    val context = LocalContext.current
    var hasPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            VoiceRecorderManager.initialize(context.applicationContext)
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermission) {
            VoiceRecorderManager.initialize(context.applicationContext)
        } else {
            // Request permission on first launch
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
