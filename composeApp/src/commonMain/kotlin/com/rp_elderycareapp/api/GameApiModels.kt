package com.rp_elderycareapp.api

import kotlinx.serialization.Serializable

// Calibration Models
@Serializable
data class CalibrationRequest(
    val userId: String,
    val device: DeviceInfo,
    val tapReactionTimesMs: List<Int>
)

@Serializable
data class DeviceInfo(
    val type: String = "android",
    val screenHz: Int = 60
)

@Serializable
data class CalibrationResponse(
    val userId: String,
    val motorBaselineMs: Double,
    val calibratedAt: String
)

// Session Models
@Serializable
data class GameSessionRequest(
    val userId: String,
    val gameType: String = "grid_tap_3x3",
    val startedAt: String,
    val durationMs: Long,
    val trials: List<TrialData>
)

@Serializable
data class TrialData(
    val trial: Int,
    val targetIndex: Int,
    val shownAtMs: Long,
    val tapIndex: Int?,
    val tapAtMs: Long?,
    val rtRawMs: Int?,
    val result: String // "hit", "wrong", "miss"
)

@Serializable
data class GameSessionResponse(
    val sessionId: String,
    val accuracy: Double,
    val avgRtMs: Double,
    val sac: Double,
    val ies: Double,
    val message: String
)

// Stats Models
@Serializable
data class UserStatsResponse(
    val userId: String,
    val totalSessions: Int,
    val avgAccuracy: Double,
    val avgRtMs: Double,
    val avgSac: Double,
    val lastPlayed: String?,
    val trend: String? // "improving", "stable", "declining"
)

// History Models
@Serializable
data class SessionHistoryResponse(
    val sessions: List<SessionSummary>
)

@Serializable
data class SessionSummary(
    val sessionId: String,
    val playedAt: String,
    val accuracy: Double,
    val avgRtMs: Double,
    val sac: Double,
    val totalTrials: Int
)

// Motor Baseline Response
@Serializable
data class MotorBaselineResponse(
    val userId: String,
    val motorBaselineMs: Double,
    val calibratedAt: String?
)