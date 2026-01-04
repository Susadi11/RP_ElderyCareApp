package com.rp_elderycareapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.rp_elderycareapp.MainActivity
import com.rp_elderycareapp.data.reminder.Reminder

/**
 * Android-specific alarm service that plays music, vibrates, and shows notifications
 * when a reminder is triggered.
 */
class AlarmService(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val CHANNEL_ID = "reminder_alarms"
        private const val CHANNEL_NAME = "Reminder Alarms"
        private const val NOTIFICATION_ID = 1001
    }
    
    init {
        createNotificationChannel()
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    /**
     * Trigger alarm with music, vibration, and notification
     */
    fun triggerAlarm(reminder: Reminder) {
        try {
            println("=== 🔔 Triggering alarm for: ${reminder.title} ===")
            
            // 1. Play alarm music
            playAlarmSound()
            
            // 2. Vibrate device
            vibrateDevice()
            
            // 3. Show notification with the reminder text
            showNotification(reminder)
            
        } catch (e: Exception) {
            println("=== Error triggering alarm: ${e.message} ===")
            e.printStackTrace()
        }
    }
    
    /**
     * Play alarm sound using system default alarm or notification sound
     */
    private fun playAlarmSound() {
        try {
            // Stop any currently playing alarm
            stopAlarmSound()
            
            // Get default alarm sound URI
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            // Create and configure MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = true // Keep playing until stopped
                prepare()
                start()
            }
            
            println("=== 🎵 Alarm music started ===")
        } catch (e: Exception) {
            println("=== Error playing alarm sound: ${e.message} ===")
            e.printStackTrace()
        }
    }
    
    /**
     * Stop alarm sound
     */
    fun stopAlarmSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            println("=== 🎵 Alarm music stopped ===")
        } catch (e: Exception) {
            println("=== Error stopping alarm sound: ${e.message} ===")
            e.printStackTrace()
        }
    }
    
    /**
     * Vibrate device with pattern
     */
    private fun vibrateDevice() {
        try {
            vibrator?.let {
                // Vibration pattern: [delay, vibrate, sleep, vibrate, ...]
                val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // For Android O and above
                    val vibrationEffect = VibrationEffect.createWaveform(pattern, 0) // 0 means repeat
                    it.vibrate(vibrationEffect)
                } else {
                    // For older versions
                    @Suppress("DEPRECATION")
                    it.vibrate(pattern, 0)
                }
            }
            println("=== 📳 Vibration started ===")
        } catch (e: Exception) {
            println("=== Error vibrating device: ${e.message} ===")
            e.printStackTrace()
        }
    }
    
    /**
     * Stop vibration
     */
    fun stopVibration() {
        try {
            vibrator?.cancel()
            println("=== 📳 Vibration stopped ===")
        } catch (e: Exception) {
            println("=== Error stopping vibration: ${e.message} ===")
            e.printStackTrace()
        }
    }
    
    /**
     * Show high-priority notification with reminder details
     */
    private fun showNotification(reminder: Reminder) {
        try {
            // Intent to open the app when notification is tapped
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("reminder_id", reminder.id)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Build the notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // You can replace with your app icon
                .setContentTitle("⏰ ${reminder.title}")
                .setContentText(reminder.description ?: "Reminder alert!")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(buildNotificationText(reminder))
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false) // Don't dismiss when tapped
                .setOngoing(true) // Keep notification visible
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true) // Show full screen on lock screen
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID, notification)
            println("=== 📱 Notification shown ===")
        } catch (e: Exception) {
            println("=== Error showing notification: ${e.message} ===")
            e.printStackTrace()
        }
    }
    
    /**
     * Build detailed notification text
     */
    private fun buildNotificationText(reminder: Reminder): String {
        val builder = StringBuilder()
        builder.append("📋 ${reminder.title}\n\n")
        
        if (!reminder.description.isNullOrBlank()) {
            builder.append("${reminder.description}\n\n")
        }
        
        builder.append("⏰ Time: ${reminder.scheduledTime}\n")
        builder.append("📂 Category: ${reminder.category.uppercase()}\n")
        builder.append("⚡ Priority: ${reminder.priority.uppercase()}\n")
        
        if (reminder.repeat_pattern != null) {
            builder.append("🔁 Repeat: ${reminder.repeat_pattern}\n")
        }
        
        return builder.toString()
    }
    
    /**
     * Dismiss alarm and notification
     */
    fun dismissAlarm() {
        stopAlarmSound()
        stopVibration()
        notificationManager.cancel(NOTIFICATION_ID)
        println("=== ✅ Alarm dismissed ===")
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for reminder alarms"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                enableLights(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        dismissAlarm()
    }
}
