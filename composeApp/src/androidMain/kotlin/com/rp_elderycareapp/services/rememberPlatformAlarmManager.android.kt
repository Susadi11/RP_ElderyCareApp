package com.rp_elderycareapp.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of rememberPlatformAlarmManager
 */
@Composable
actual fun rememberPlatformAlarmManager(): PlatformAlarmManager {
    val context = LocalContext.current
    return remember { PlatformAlarmManager(context.applicationContext) }
}
