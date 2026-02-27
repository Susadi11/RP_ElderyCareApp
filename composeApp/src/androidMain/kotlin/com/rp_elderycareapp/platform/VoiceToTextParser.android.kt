package com.rp_elderycareapp.platform

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberVoiceToTextParser(): VoiceToTextParser {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    return remember {
        AndroidVoiceToTextParser(app)
    }
}

@Composable
actual fun rememberVoicePermission(): VoicePermissionState {
    var isGranted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
    }
    
    return remember {
        object : VoicePermissionState {
            override val hasPermission: Boolean get() = isGranted
            override fun request() {
                launcher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}
