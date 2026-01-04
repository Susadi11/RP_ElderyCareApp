package com.rp_elderycareapp.data.reminder

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Priority levels for reminders
enum class ReminderPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

// Reminder categories
enum class ReminderCategory {
    MEDICATION, APPOINTMENT, MEAL, EXERCISE, SOCIAL, OTHER
}

// Reminder status
enum class ReminderStatus {
    ACTIVE, COMPLETED, SNOOZED, MISSED, CANCELLED
}

// Recurrence patterns
enum class RecurrencePattern {
    ONCE, DAILY, WEEKLY, MONTHLY
}

// Interaction types for responses
enum class InteractionType {
    CONFIRMED, CONFUSED, IGNORED, DELAYED, DENIED
}

// Main Reminder data model
@Serializable
data class Reminder(
    @SerialName("id")  // Backend returns "id", not "_id"
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    @SerialName("scheduled_time")
    val scheduledTime: String, // ISO 8601 format
    val priority: String,
    val category: String,
    val status: String = "active",
    val repeat_pattern: String? = null,
    @SerialName("repeat_interval_minutes")
    val repeatIntervalMinutes: Int? = null,
    @SerialName("caregiver_ids")
    val caregiverIds: List<String> = emptyList(),
    @SerialName("notify_caregiver_on_miss")
    val notifyCaregiverOnMiss: Boolean = true,
    @SerialName("escalation_threshold_minutes")
    val escalationThresholdMinutes: Int = 30,
    @SerialName("adaptive_scheduling_enabled")
    val adaptiveSchedulingEnabled: Boolean = true,
    @SerialName("escalation_enabled")
    val escalationEnabled: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null
)

// Create reminder request
@Serializable
data class CreateReminderRequest(
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    @SerialName("scheduled_time")
    val scheduledTime: String,
    val priority: String = "medium",
    val category: String,
    val repeat_pattern: String? = null,
    @SerialName("repeat_interval_minutes")
    val repeatIntervalMinutes: Int? = null,
    @SerialName("caregiver_ids")
    val caregiverIds: List<String> = emptyList(),
    @SerialName("notify_caregiver_on_miss")
    val notifyCaregiverOnMiss: Boolean = true,
    @SerialName("escalation_threshold_minutes")
    val escalationThresholdMinutes: Int = 30
)

// Natural language reminder creation
@Serializable
data class NaturalLanguageReminderRequest(
    val userId: String,
    val commandText: String,
    val audioPath: String? = null,
    val timestamp: String = Clock.System.now().toString()
)

// Response to reminder
@Serializable
data class ReminderResponseRequest(
    val reminderId: String,
    val userId: String,
    val responseText: String,
    val audioPath: String? = null,
    val responseTimestamp: String = Clock.System.now().toString()
)

// Analysis result from AI
@Serializable
data class ResponseAnalysis(
    val interactionType: String,
    val cognitiveRiskScore: Double,
    val recommendedAction: String,
    val confusionIndicators: List<String> = emptyList(),
    val sentiment: String? = null
)

// Complete response processing result
@Serializable
data class ReminderResponseResult(
    val status: String,
    val message: String,
    val analysis: ResponseAnalysis,
    val caregiverNotified: Boolean,
    val cognitiveRiskScore: Double,
    val recommendedAction: String
)

// Statistics for dashboard
@Serializable
data class ReminderStatistics(
    val totalReminders: Int,
    val confirmed: Int,
    val ignored: Int,
    val delayed: Int,
    val confused: Int,
    val confirmationRate: Double,
    val confusionRate: Double
)

// Cognitive health data
@Serializable
data class CognitiveHealth(
    val avgRiskScore: Double,
    val trend: String, // "improving", "stable", "declining"
    val escalationRecommended: Boolean
)

// Timing analytics
@Serializable
data class TimingAnalytics(
    val optimalHour: Int,
    val worstHours: List<Int>,
    val avgResponseTimeSeconds: Int
)

// Recommendations
@Serializable
data class DashboardRecommendations(
    val frequencyMultiplier: Double,
    val timeAdjustmentMinutes: Int
)

// Complete dashboard data
@Serializable
data class DashboardData(
    val userId: String,
    val periodDays: Int,
    val statistics: ReminderStatistics,
    val cognitiveHealth: CognitiveHealth,
    val timing: TimingAnalytics,
    val recommendations: DashboardRecommendations
)

// Caregiver alert severity
enum class AlertSeverity {
    INFO, WARNING, CRITICAL
}

// Caregiver alert
@Serializable
data class CaregiverAlert(
    val id: String,
    val caregiverId: String,
    val userId: String,
    val userName: String,
    val alertType: String,
    val severity: String,
    val message: String,
    val reminderId: String? = null,
    val reminderTitle: String? = null,
    val responseText: String? = null,
    val riskScore: Double? = null,
    val timestamp: String,
    val acknowledged: Boolean = false,
    val resolved: Boolean = false,
    val acknowledgedAt: String? = null,
    val resolvedAt: String? = null
)

// Behavior pattern analysis
@Serializable
data class BehaviorPattern(
    val userId: String,
    val days: Int,
    val optimalReminderHour: Int,
    val worstResponseHours: List<Int>,
    val confirmationRate: Double,
    val confusionFrequency: Double,
    val bestPerformanceCategories: List<String>,
    val worstPerformanceCategories: List<String>
)

// Weekly report summary
@Serializable
data class WeeklyReportSummary(
    val userId: String,
    val userName: String,
    val startDate: String,
    val endDate: String,
    val overallRiskLevel: String,
    val avgRiskScore: Double,
    val trend: String,
    val completionRate: Double,
    val totalAlerts: Int,
    val criticalAlerts: Int,
    val topIssues: List<String>,
    val recommendations: List<String>
)

// Snooze result
@Serializable
data class SnoozeResult(
    val status: String,
    val message: String,
    val newScheduledTime: String
)

// API response wrapper
@Serializable
data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val data: T? = null,
    val reminders: T? = null,  // Backend uses "reminders" instead of "data" for lists!
    val error: String? = null
)

// WebSocket message types
@Serializable
data class WebSocketMessage(
    val type: String,  // "reminder", "alert", "update"
    val data: String   // JSON string of reminder or alert
)
