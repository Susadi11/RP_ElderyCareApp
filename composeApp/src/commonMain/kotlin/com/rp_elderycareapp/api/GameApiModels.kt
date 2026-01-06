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
    val trialNumber: Int? = null,      // Optional - backend doesn't require
    val targetPosition: Int? = null,   // Optional - backend doesn't require
    val rt_raw: Double,                // Backend expects rt_raw (reaction time in seconds)
    val correct: Boolean,              // Required by backend (0 or 1, but Boolean works)
    val timestamp: Long? = null,       // Optional - backend doesn't require
    val hint_used: Int? = null         // Optional - backend field for hints
) {
    // Backward compatibility constructor
    constructor(
        trialNumber: Int,
        targetPosition: Int,
        reactionTime: Double,
        correct: Boolean,
        timestamp: Long
    ) : this(
        trialNumber = trialNumber,
        targetPosition = targetPosition,
        rt_raw = reactionTime,  // Map reactionTime to rt_raw
        correct = correct,
        timestamp = timestamp,
        hint_used = 0
    )
}

@Serializable
data class SessionSummary(
    val totalAttempts: Int,      // Backend expects totalAttempts
    val correct: Int,            // Backend expects correct (not correctTrials)
    val meanRtRaw: Double,       // Backend expects meanRtRaw (mean reaction time in seconds)
    val errors: Int? = null,     // Optional - backend computes this
    val hintsUsed: Int? = null,  // Optional - backend field
    val medianRtRaw: Double? = null  // Optional - backend uses this
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
    val rtAdjMedian: Double,  // Backend uses rtAdjMedian (reaction time adjusted for motor baseline)
    val variability: Double   // Backend uses variability
)

@Serializable
data class RiskPrediction(
    val riskLevel: String,  // "LOW", "MEDIUM", "HIGH"
    val riskScore0_100: Double,
    val riskProbability: Map<String, Double>,  // Backend uses riskProbability (not probabilities)
    val lstmDeclineScore: Double  // Backend includes LSTM decline score
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