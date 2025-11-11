package com.rp_elderycareapp.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier
) {
    QuickActionButton(
        text = "Settings",
        icon = Icons.Default.Settings,
        backgroundColor = Color(0xFF9CA3AF), // Gray
        onClick = onClick,
        modifier = modifier
    )
}
