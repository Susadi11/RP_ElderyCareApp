package com.rp_elderycareapp.services

import androidx.compose.runtime.Composable

/**
 * Creates a PlatformAlarmManager instance for the current platform
 * Android implementation uses LocalContext to get the Android context
 */
@Composable
expect fun rememberPlatformAlarmManager(): PlatformAlarmManager
