package com.rp_elderycareapp.services

import com.rp_elderycareapp.data.reminder.Reminder
import kotlinx.browser.window

/**
 * Web/WasmJS implementation of PlatformAlarmManager
 */
actual class PlatformAlarmManager {
    
    actual fun triggerAlarm(reminder: Reminder) {
        println("=== 🔔 ALARM: ${reminder.title} ===")
        
        // Show browser notification if supported
        try {
            if (js("'Notification' in window") as Boolean) {
                if (js("Notification.permission === 'granted'") as Boolean) {
                    val options = js("""({
                        body: '${reminder.description ?: "Reminder alert!"}',
                        icon: '/icon.png',
                        badge: '/badge.png',
                        vibrate: [200, 100, 200],
                        requireInteraction: true
                    })""")
                    
                    js("new Notification('⏰ ${reminder.title}', options)")
                } else if (js("Notification.permission !== 'denied'") as Boolean) {
                    js("Notification.requestPermission()")
                }
            }
        } catch (e: Exception) {
            println("=== Error showing web notification: ${e.message} ===")
        }
        
        // Play a beep sound using Web Audio API
        try {
            window.alert("⏰ Reminder: ${reminder.title}\n\n${reminder.description ?: ""}")
        } catch (e: Exception) {
            println("=== Error playing web audio: ${e.message} ===")
        }
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
