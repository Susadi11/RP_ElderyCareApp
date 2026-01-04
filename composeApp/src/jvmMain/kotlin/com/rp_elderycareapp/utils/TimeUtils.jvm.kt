package com.rp_elderycareapp.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

actual object TimeUtils {
    actual fun getCurrentTimeFormatted(): String {
        val now = LocalDateTime.now()
        return now.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    actual fun getCurrentDateFormatted(): String {
        val now = LocalDateTime.now()
        return now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH))
    }

    actual fun getGreetingMessage(): String {
        val hour = LocalDateTime.now().hour
        return when {
            hour < 12 -> "Good Morning"
            hour < 18 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    actual fun getShortDateFormatted(): String {
        val now = LocalDateTime.now()
        return now.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
    }
}
