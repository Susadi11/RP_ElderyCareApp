package com.rp_elderycareapp.utils

import java.util.Calendar
import java.util.Locale

/**
 * Utility functions for time and date formatting
 * Designed for elderly users with clear, readable formats
 * Android implementation using java.util.Calendar
 */

actual object TimeUtils {
    /**
     * Get current time as formatted string (HH:MM)
     * Example: "09:30"
     */
    actual fun getCurrentTimeFormatted(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    /**
     * Get current date as formatted string
     * Example: "Monday, November 11, 2025"
     */
    actual fun getCurrentDateFormatted(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = getDayOfWeekName(calendar.get(Calendar.DAY_OF_WEEK))
        val monthName = getMonthName(calendar.get(Calendar.MONTH) + 1)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        return "$dayOfWeek, $monthName $dayOfMonth, $year"
    }

    /**
     * Get greeting message based on current hour
     * Returns: "Good Morning", "Good Afternoon", or "Good Evening"
     */
    actual fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    /**
     * Get day of week name
     * Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, ..., 7=Saturday
     */
    private fun getDayOfWeekName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Sunday"
            2 -> "Monday"
            3 -> "Tuesday"
            4 -> "Wednesday"
            5 -> "Thursday"
            6 -> "Friday"
            7 -> "Saturday"
            else -> "Unknown"
        }
    }

    /**
     * Get month name
     */
    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }
    }

    /**
     * Get short date format (e.g., "Nov 11, 2025")
     */
    actual fun getShortDateFormatted(): String {
        val calendar = Calendar.getInstance()
        val monthName = getMonthName(calendar.get(Calendar.MONTH) + 1).take(3)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        return "$monthName $dayOfMonth, $year"
    }
}
