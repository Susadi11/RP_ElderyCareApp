package com.rp_elderycareapp.utils

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

/**
 * Composable helper to request audio recording permission on Android
 * Usage:
 * ```
 * val audioPermission = rememberAudioPermission()
 * 
 * Button(onClick = { audioPermission.request() }) {
 *     Text("Request Permission")
 * }
 * 
 * if (audioPermission.hasPermission) {
 *     // Record audio
 * }
 * ```
 */
@Composable
fun rememberAudioPermission(): AudioPermissionState {
    var hasPermission by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }
    
    return remember {
        object : AudioPermissionState {
            override val hasPermission: Boolean get() = hasPermission
            
            override fun request() {
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}

interface AudioPermissionState {
    val hasPermission: Boolean
    fun request()
}
