package com.rp_elderycareapp.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

actual fun getMicIconPainter(): Any {
    return Icons.Default.KeyboardVoice
}

@Composable
actual fun MicIconContent(isRecording: Boolean) {
    Icon(
        imageVector = if (isRecording) Icons.Default.Close else Icons.Default.KeyboardVoice,
        contentDescription = if (isRecording) "Stop recording" else "Start voice recording",
        tint = Color.White,
        modifier = Modifier.size(24.dp)
    )
}
