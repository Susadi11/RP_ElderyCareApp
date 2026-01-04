package com.rp_elderycareapp.services

import androidx.compose.runtime.Composable

/**
 * Platform-agnostic composable to get an AudioRecorder
 * Platform-specific implementations are provided for each target
 */
@Composable
expect fun rememberAudioRecorder(): AudioRecorder
