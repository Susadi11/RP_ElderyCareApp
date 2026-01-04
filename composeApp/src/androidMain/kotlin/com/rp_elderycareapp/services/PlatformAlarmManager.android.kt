package com.rp_elderycareapp.services

import android.content.Context
import com.rp_elderycareapp.data.reminder.Reminder

/**
 * Android implementation of PlatformAlarmManager
 */
actual class PlatformAlarmManager(private val context: Context) {
    
    private val alarmService = AlarmService(context)
    
    actual fun triggerAlarm(reminder: Reminder) {
        alarmService.triggerAlarm(reminder)
    }
    
    actual fun stopAlarm() {
        alarmService.stopAlarmSound()
    }
    
    actual fun dismissAlarm() {
        alarmService.dismissAlarm()
    }
    
    actual fun cleanup() {
        alarmService.cleanup()
    }
}
