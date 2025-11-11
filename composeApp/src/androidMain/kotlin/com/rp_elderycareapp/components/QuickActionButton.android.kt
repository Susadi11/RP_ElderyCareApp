package com.rp_elderycareapp.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.rp_elderycareapp.R

@Composable
actual fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier
) {
    QuickActionButton(
        text = "Settings",
        icon = painterResource(R.drawable.outline_settings_24),
        backgroundColor = Color(0xFF9CA3AF), // Gray
        onClick = onClick,
        modifier = modifier
    )
}
