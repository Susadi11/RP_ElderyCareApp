package com.rp_elderycareapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rp_elderycareapp.R

// Ash shade color for all icons
private val AshColor = Color(0xFF6B7280)

@Composable
actual fun SettingsNotificationIcon() {
    Image(
        painter = painterResource(R.drawable.outline_notifications_active_24),
        contentDescription = "Notifications",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsVolumeIcon() {
    Image(
        painter = painterResource(R.drawable.outline_volume_up_24),
        contentDescription = "Volume",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsTextIcon() {
    Image(
        painter = painterResource(R.drawable.outline_text_fields_24),
        contentDescription = "Text",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsDarkModeIcon() {
    Image(
        painter = painterResource(R.drawable.outline_dark_mode_24),
        contentDescription = "Dark Mode",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsActivityIcon() {
    Image(
        painter = painterResource(R.drawable.outline_progress_activity_24),
        contentDescription = "Activity",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsHeartIcon() {
    Image(
        painter = painterResource(R.drawable.outline_ecg_heart_24),
        contentDescription = "Heart",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsHealthMetricsIcon() {
    Image(
        painter = painterResource(R.drawable.outline_health_metrics_24),
        contentDescription = "Health Metrics",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsHelpIcon() {
    Image(
        painter = painterResource(R.drawable.outline_help_24),
        contentDescription = "Help",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsShieldIcon() {
    Image(
        painter = painterResource(R.drawable.outline_shield_lock_24),
        contentDescription = "Shield",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun SettingsInfoIcon() {
    Image(
        painter = painterResource(R.drawable.outline_info_24),
        contentDescription = "Info",
        colorFilter = ColorFilter.tint(AshColor),
        modifier = Modifier.size(22.dp)
    )
}
