package com.rp_elderycareapp.data

import com.rp_elderycareapp.api.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class GameRepository(private val gameApi: GameApi) {

    suspend fun submitCalibration(
        userId: String,
        tapTimes: List<Double>  // Changed to Double to match backend
    ): Result<CalibrationResponse> = withContext(Dispatchers.Default) {
        try {
            val request = CalibrationRequest(
                userId = userId,
                tapTimes = tapTimes  // Removed device info, backend doesn't need it
            )
            gameApi.submitCalibration(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMotorBaseline(userId: String): Result<MotorBaselineResponse> =
        withContext(Dispatchers.Default) {
            try {
                gameApi.getMotorBaseline(userId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun submitGameSession(
        userId: String,
        sessionId: String,
        gameType: String,
        level: Int,
        trials: List<TrialData>
    ): Result<GameSessionResponse> = withContext(Dispatchers.Default) {
        try {
            val request = GameSessionRequest(
                userId = userId,
                sessionId = sessionId,
                gameType = gameType,
                level = level,
                trials = trials
            )
            gameApi.submitSession(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserStats(userId: String): Result<UserStatsResponse> =
        withContext(Dispatchers.Default) {
            try {
                gameApi.getUserStats(userId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getSessionHistory(userId: String): Result<SessionHistoryResponse> =
        withContext(Dispatchers.Default) {
            try {
                gameApi.getSessionHistory(userId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}