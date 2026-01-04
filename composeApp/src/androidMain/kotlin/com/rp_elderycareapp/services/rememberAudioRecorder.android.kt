package com.rp_elderycareapp.services

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android-specific composable to remember an AudioRecorder instance
 */
@Composable
actual fun rememberAudioRecorder(): AudioRecorder {
    val context = LocalContext.current
    return remember { createAndroidAudioRecorder(context) }
}
