package com.rp_elderycareapp.services

import com.rp_elderycareapp.data.reminder.Reminder

/**
 * Platform-independent alarm manager interface
 * Implementations should handle playing alarm sounds, vibration, and notifications
 */
expect class PlatformAlarmManager {
    
    /**
     * Trigger alarm with music, vibration, and notification for the given reminder
     */
    fun triggerAlarm(reminder: Reminder)
    
    /**
     * Stop the alarm sound
     */
    fun stopAlarm()
    
    /**
     * Dismiss the alarm completely (sound, vibration, notification)
     */
    fun dismissAlarm()
    
    /**
     * Clean up resources
     */
    fun cleanup()
}
