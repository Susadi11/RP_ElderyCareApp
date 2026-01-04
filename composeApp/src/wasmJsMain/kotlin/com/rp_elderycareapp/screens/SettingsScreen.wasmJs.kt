package com.rp_elderycareapp.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.rp_elderycareapp.ui.theme.AppColors

@Composable
actual fun SettingsNotificationIcon() {
    Icon(
        imageVector = Icons.Default.Notifications,
        contentDescription = "Notifications",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun SettingsVolumeIcon() {
    Icon(
        imageVector = Icons.Default.VolumeUp,
        contentDescription = "Volume",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun SettingsTextIcon() {
    Icon(
        imageVector = Icons.Default.TextFields,
        contentDescription = "Text Settings",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun SettingsDarkModeIcon() {
    Icon(
        imageVector = Icons.Default.DarkMode,
        contentDescription = "Dark Mode",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun SettingsActivityIcon() {
    Icon(
        imageVector = Icons.Default.DirectionsRun,
        contentDescription = "Activity",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun SettingsHeartIcon() {
    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Heart Rate",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun SettingsHealthMetricsIcon() {
    Icon(
        imageVector = Icons.Default.HealthAndSafety,
        contentDescription = "Health Metrics",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun SettingsHelpIcon() {
    Icon(
        imageVector = Icons.Default.Help,
        contentDescription = "Help",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun SettingsShieldIcon() {
    Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = "Privacy",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun SettingsInfoIcon() {
    Icon(
        imageVector = Icons.Default.Info,
        contentDescription = "About",
        tint = AppColors.PrimaryText
    )
}