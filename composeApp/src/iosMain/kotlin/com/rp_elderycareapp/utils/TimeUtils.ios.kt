package com.rp_elderycareapp.utils

import kotlinx.datetime.*

/**
 * Utility functions for time and date formatting
 * Designed for elderly users with clear, readable formats
 * iOS implementation using kotlinx.datetime
 */

actual object TimeUtils {
    /**
     * Get current time as formatted string (HH:MM)
     * Example: "09:30"
     */
    actual fun getCurrentTimeFormatted(): String {
        val now = Clock.System.now()
        val localTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localTime.hour.toString().padStart(2, '0')
        val minute = localTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    /**
     * Get current date as formatted string
     * Example: "Monday, November 11, 2025"
     */
    actual fun getCurrentDateFormatted(): String {
        val now = Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dayOfWeek = getDayOfWeekName(localDate.dayOfWeek)
        val monthName = getMonthName(localDate.monthNumber)
        val dayOfMonth = localDate.dayOfMonth
        val year = localDate.year
        return "$dayOfWeek, $monthName $dayOfMonth, $year"
    }

    /**
     * Get greeting message based on current hour
     * Returns: "Good Morning", "Good Afternoon", or "Good Evening"
     */
    actual fun getGreetingMessage(): String {
        val now = Clock.System.now()
        val localTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localTime.hour
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    /**
     * Get day of week name
     */
    private fun getDayOfWeekName(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
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
        val now = Clock.System.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val monthName = getMonthName(localDate.monthNumber).take(3)
        val dayOfMonth = localDate.dayOfMonth
        val year = localDate.year
        return "$monthName $dayOfMonth, $year"
    }
}
