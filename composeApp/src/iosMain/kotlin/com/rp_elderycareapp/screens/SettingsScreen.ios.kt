package com.rp_elderycareapp.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Ash shade color for all icons
private val AshColor = Color(0xFF6B7280)

@Composable
actual fun SettingsNotificationIcon() {
    Icon(
        imageVector = Icons.Default.Notifications,
        contentDescription = "Notifications",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsVolumeIcon() {
    Icon(
        imageVector = Icons.Default.VolumeUp,
        contentDescription = "Volume",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsTextIcon() {
    Icon(
        imageVector = Icons.Default.TextFields,
        contentDescription = "Text",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsDarkModeIcon() {
    Icon(
        imageVector = Icons.Default.DarkMode,
        contentDescription = "Dark Mode",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsActivityIcon() {
    Icon(
        imageVector = Icons.Default.Timeline,
        contentDescription = "Activity",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsHeartIcon() {
    Icon(
        imageVector = Icons.Default.FavoriteBorder,
        contentDescription = "Heart",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsHealthMetricsIcon() {
    Icon(
        imageVector = Icons.Default.ShowChart,
        contentDescription = "Health Metrics",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsHelpIcon() {
    Icon(
        imageVector = Icons.Default.Help,
        contentDescription = "Help",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsShieldIcon() {
    Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = "Shield",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsInfoIcon() {
    Icon(
        imageVector = Icons.Default.Info,
        contentDescription = "Info",
        tint = AshColor,
        modifier = Modifier.size(22.dp)
    )
}
