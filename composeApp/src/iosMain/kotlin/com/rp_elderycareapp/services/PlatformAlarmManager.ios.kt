package com.rp_elderycareapp.services

import com.rp_elderycareapp.data.reminder.Reminder
import platform.UserNotifications.*
import platform.Foundation.*

/**
 * iOS implementation of PlatformAlarmManager
 */
actual class PlatformAlarmManager {
    
    actual fun triggerAlarm(reminder: Reminder) {
        // Request notification permission
        val center = UNUserNotificationCenter.currentNotificationCenter()
        
        // Create notification content
        val content = UNMutableNotificationContent().apply {
            setTitle("⏰ ${reminder.title}")
            setBody(reminder.description ?: "Reminder alert!")
            setSound(UNNotificationSound.defaultSound())
            setCategoryIdentifier("REMINDER_ALARM")
        }
        
        // Trigger immediately
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = 1.0,
            repeats = false
        )
        
        // Create and add the request
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = reminder.id,
            content = content,
            trigger = trigger
        )
        
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("=== Error showing iOS notification: ${error.localizedDescription} ===")
            } else {
                println("=== 📱 iOS notification scheduled ===")
            }
        }
    }
    
    actual fun stopAlarm() {
        // Stop any playing sounds (iOS handles this automatically)
        println("=== Alarm stopped ===")
    }
    
    actual fun dismissAlarm() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removeAllDeliveredNotifications()
        println("=== ✅ iOS alarm dismissed ===")
    }
    
    actual fun cleanup() {
        dismissAlarm()
    }
}
