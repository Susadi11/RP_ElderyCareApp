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
    @SerialName("timeout_seconds")
    val timeoutSeconds: Int? = null,
    @SerialName("requires_acknowledgment")
    val requiresAcknowledgment: Boolean = true,
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
    val escalationThresholdMinutes: Int = 10,
    @SerialName("escalation_enabled")
    val escalationEnabled: Boolean = true,
    @SerialName("adaptive_scheduling_enabled")
    val adaptiveSchedulingEnabled: Boolean = true
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
    val reminder: T? = null,
    val reminders: T? = null,  // Backend uses "reminders" instead of "data" for lists!
    val error: String? = null
)

// WebSocket message types
@Serializable
data class WebSocketMessage(
    val type: String,  // "reminder", "reminder_repeat", "alarm_acknowledged", "alarm_missed", "alert", "update"
    val data: String? = null,              // JSON string of reminder or alert (used by "reminder" type)
    @SerialName("user_id") val userId: String? = null,
    @SerialName("reminder_id") val reminderId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val priority: String? = null,
    val category: String? = null,
    @SerialName("scheduled_time") val scheduledTime: String? = null,
    val message: String? = null,
    @SerialName("repeat_count") val repeatCount: Int? = null,
    @SerialName("total_attempts") val totalAttempts: Int? = null,
    @SerialName("timeout_seconds") val timeoutSeconds: Int? = null,
    @SerialName("requires_acknowledgment") val requiresAcknowledgment: Boolean? = null,
    @SerialName("escalation_enabled") val escalationEnabled: Boolean? = null,
    @SerialName("caregiver_notified") val caregiverNotified: Boolean? = null,
    val urgency: String? = null,
    val timestamp: String? = null
)

// Alarm acknowledge response from backend
@Serializable
data class AcknowledgeAlarmResponse(
    val status: String,
    val message: String,
    @SerialName("reminder_id") val reminderId: String? = null
)

// Represents an alarm-related event broadcast from the WebSocket layer
data class AlarmEvent(
    val type: String,          // "reminder_repeat", "alarm_acknowledged", "alarm_missed"
    val reminderId: String,
    val repeatCount: Int = 0,
    val totalAttempts: Int? = null,
    val timeoutSeconds: Int? = null,
    val escalationEnabled: Boolean? = null,
    val caregiverNotified: Boolean? = null,
    val message: String? = null
)

// Due now response for polling
@Serializable
data class DueNowResponse(
    val status: String,
    @SerialName("should_trigger_alarm")
    val shouldTriggerAlarm: Boolean,
    @SerialName("urgent_count")
    val urgentCount: Int,
    @SerialName("urgent_reminders")
    val urgentReminders: List<Reminder> = emptyList()
)

// Audio reminder response from voice recording
@Serializable
data class AudioReminderResponse(
    val status: String,
    val message: String,
    val reminder: Reminder,
    val transcription: String,
    @SerialName("audio_file")
    val audioFile: String
)

// Cognitive analysis features for dementia detection
@Serializable
data class CognitiveAnalysisFeatures(
    @SerialName("filler_words")
    val fillerWords: Double = 0.0,
    @SerialName("semantic_incoherence")
    val semanticIncoherence: Double = 0.0,
    @SerialName("memory_markers")
    val memoryMarkers: Int = 0,
    @SerialName("confusion_markers")
    val confusionMarkers: Int = 0,
    @SerialName("repetitions")
    val repetitions: Double = 0.0,
    @SerialName("lexical_diversity")
    val lexicalDiversity: Double = 0.0
)

// Cognitive analysis result from stop alarm
@Serializable
data class CognitiveAnalysis(
    @SerialName("risk_score")
    val riskScore: Double,
    @SerialName("interaction_type")
    val interactionType: String,
    val features: CognitiveAnalysisFeatures,
    @SerialName("caregiver_notified")
    val caregiverNotified: Boolean,
    @SerialName("confusion_detected")
    val confusionDetected: Boolean,
    @SerialName("memory_issue_detected")
    val memoryIssueDetected: Boolean
)

// Stop alarm with response tracking
@Serializable
data class StopAlarmResponse(
    val status: String,
    val message: String,
    @SerialName("reminder_id")
    val reminderId: String,
    @SerialName("completed_at")
    val completedAt: String,
    @SerialName("cognitive_analysis")
    val cognitiveAnalysis: CognitiveAnalysis,
    @SerialName("has_next_occurrence")
    val hasNextOccurrence: Boolean = false,
    @SerialName("next_reminder_id")
    val nextReminderId: String? = null,
    @SerialName("next_scheduled_time")
    val nextScheduledTime: String? = null
)

// Snooze with behavior tracking
@Serializable
data class SnoozeTrackedResponse(
    val status: String,
    val message: String,
    @SerialName("reminder_id")
    val reminderId: String,
    @SerialName("new_scheduled_time")
    val newScheduledTime: String,
    @SerialName("snooze_count_week")
    val snoozeCountWeek: Int,
    @SerialName("snooze_rate")
    val snoozeRate: Double,
    @SerialName("caregiver_alert")
    val caregiverAlert: Boolean,
    val recommendation: String
)

// Help request response
@Serializable
data class HelpRequestResponse(
    val status: String,
    val message: String,
    @SerialName("reminder_id")
    val reminderId: String,
    @SerialName("cognitive_risk_score")
    val cognitiveRiskScore: Double,
    @SerialName("caregiver_notified")
    val caregiverNotified: Boolean,
    @SerialName("caregivers_alerted")
    val caregiversAlerted: List<String> = emptyList(),
    val recommendation: String,
    @SerialName("help_reason")
    val helpReason: String
)

// Weekly cognitive data point
@Serializable
data class WeeklyCognitiveData(
    val week: Int,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("avg_risk_score")
    val avgRiskScore: Double,
    @SerialName("confused_count")
    val confusedCount: Int,
    @SerialName("confirmed_count")
    val confirmedCount: Int,
    @SerialName("ignored_count")
    val ignoredCount: Int
)

// Weekly dementia risk report
@Serializable
data class WeeklyDementiaRiskResponse(
    val status: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("weeks_analyzed")
    val weeksAnalyzed: Int,
    @SerialName("overall_trend")
    val overallTrend: String,
    @SerialName("weekly_data")
    val weeklyData: List<WeeklyCognitiveData>,
    @SerialName("average_risk_score")
    val averageRiskScore: Double,
    @SerialName("total_confused_interactions")
    val totalConfusedInteractions: Int,
    val recommendation: String,
    @SerialName("requires_medical_attention")
    val requiresMedicalAttention: Boolean
)
