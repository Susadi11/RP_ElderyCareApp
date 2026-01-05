package com.rp_elderycareapp.api

import kotlinx.serialization.Serializable

// ============================================================================
// Calibration Models (matches backend CalibrationRequest/Response)
// ============================================================================
@Serializable
data class CalibrationRequest(
    val userId: String,
    val tapTimes: List<Double>  // Changed from tapReactionTimesMs to match backend
)

@Serializable
data class CalibrationResponse(
    val userId: String,
    val motorBaseline: Double,  // Changed from motorBaselineMs to match backend
    val calibrationDate: String  // Changed from calibratedAt to match backend
)

// ============================================================================
// Motor Baseline Response (matches backend GET /game/motor-baseline)
// ============================================================================
@Serializable
data class MotorBaselineResponse(
    val userId: String,
    val motor_baseline: Double?,  // Backend uses snake_case
    val n_taps: Int? = null,
    val created_at: String? = null,
    val message: String? = null
)

// ============================================================================
// Session Models (matches backend GameSessionRequest/Response)
// ============================================================================
@Serializable
data class GameSessionRequest(
    val userId: String,
    val sessionId: String,
    val gameType: String,
    val level: Int,
    val trials: List<TrialData>? = null,
    val summary: SessionSummary? = null
)

@Serializable
data class TrialData(
    val trialNumber: Int,
    val targetPosition: Int,
    val reactionTime: Double,
    val correct: Boolean,
    val timestamp: Long
)

@Serializable
data class SessionSummary(
    val totalTrials: Int,
    val correctTrials: Int,
    val avgReactionTime: Double,
    val duration: Long
)

@Serializable
data class GameSessionResponse(
    val sessionId: String,
    val userId: String,
    val features: SessionFeatures,
    val prediction: RiskPrediction,
    val timestamp: String
)

@Serializable
data class SessionFeatures(
    val sac: Double,
    val ies: Double,
    val accuracy: Double,
    val avgReactionTime: Double,
    val rtVariability: Double
)

@Serializable
data class RiskPrediction(
    val riskLevel: String,  // "LOW", "MEDIUM", "HIGH"
    val riskScore0_100: Double,
    val probabilities: Map<String, Double>? = null
)

// ============================================================================
// Stats Models (matches backend UserStatsResponse)
// ============================================================================
@Serializable
data class UserStatsResponse(
    val userId: String,
    val totalSessions: Int,
    val avgSAC: Double,
    val avgIES: Double,
    val currentRiskLevel: String,
    val recentRiskScore: Double,
    val lastSessionDate: String
)

// ============================================================================
// History Models (matches backend SessionHistoryResponse)
// ============================================================================
@Serializable
data class SessionHistoryResponse(
    val userId: String,
    val totalSessions: Int,
    val sessions: List<SessionHistoryItem>
)

@Serializable
data class SessionHistoryItem(
    val sessionId: String,
    val timestamp: String,
    val gameType: String,
    val level: Int,
    val sac: Double,
    val ies: Double,
    val riskLevel: String,
    val riskScore: Double
)