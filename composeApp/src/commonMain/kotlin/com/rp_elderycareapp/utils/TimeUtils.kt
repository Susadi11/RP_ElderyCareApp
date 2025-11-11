package com.rp_elderycareapp.utils

/**
 * Common interface for time utilities
 * Platform-specific implementations are in androidMain/iosMain
 */

expect object TimeUtils {
    /**
     * Get current time as formatted string (HH:MM)
     * Example: "09:30"
     */
    fun getCurrentTimeFormatted(): String

    /**
     * Get current date as formatted string
     * Example: "Monday, November 11, 2025"
     */
    fun getCurrentDateFormatted(): String

    /**
     * Get greeting message based on current hour
     * Returns: "Good Morning", "Good Afternoon", or "Good Evening"
     */
    fun getGreetingMessage(): String

    /**
     * Get short date format (e.g., "Nov 11, 2025")
     */
    fun getShortDateFormatted(): String
}
