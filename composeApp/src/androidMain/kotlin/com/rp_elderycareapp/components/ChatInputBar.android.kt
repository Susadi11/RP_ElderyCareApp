package com.rp_elderycareapp.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rp_elderycareapp.R

actual fun getMicIconPainter(): Any {
    return R.drawable.outline_mic_24
}

@Composable
actual fun MicIconContent(isRecording: Boolean) {
    if (isRecording) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Stop recording",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    } else {
        Icon(
            painter = painterResource(R.drawable.outline_mic_24),
            contentDescription = "Start voice recording",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
