package com.rp_elderycareapp.services

import com.rp_elderycareapp.data.reminder.Reminder

/**
 * JVM/Desktop implementation of PlatformAlarmManager
 */
actual class PlatformAlarmManager {
    
    actual fun triggerAlarm(reminder: Reminder) {
        println("=== 🔔 ALARM: ${reminder.title} ===")
        println("Description: ${reminder.description}")
        println("Time: ${reminder.scheduledTime}")
        println("Priority: ${reminder.priority}")
        // Desktop could use javax.sound or other audio libraries
    }
    
    actual fun stopAlarm() {
        println("=== Alarm stopped ===")
    }
    
    actual fun dismissAlarm() {
        println("=== ✅ Alarm dismissed ===")
    }
    
    actual fun cleanup() {
        dismissAlarm()
    }
}
