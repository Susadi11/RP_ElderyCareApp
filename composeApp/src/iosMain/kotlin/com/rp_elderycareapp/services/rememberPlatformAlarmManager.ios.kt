package com.rp_elderycareapp.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS implementation of rememberPlatformAlarmManager
 */
@Composable
actual fun rememberPlatformAlarmManager(): PlatformAlarmManager {
    return remember { PlatformAlarmManager() }
}
