package com.rp_elderycareapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rp_elderycareapp.api.*
import com.rp_elderycareapp.data.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ============================================================================
// Difficulty configuration — driven by user's current risk level
// ============================================================================
data class DifficultyConfig(
    val level: Int,               // 1=Easy, 2=Standard, 3=Challenge
    val label: String,
    val targetTimeoutMs: Long,    // How long the lit box stays before auto-miss
    val minIsiMs: Long,           // Min inter-stimulus interval (gap between trials)
    val maxIsiMs: Long,           // Max inter-stimulus interval
    val totalTrials: Int,
    val hintThreshold: Int        // Consecutive errors before hint appears
)

fun difficultyForRisk(riskLevel: String?): DifficultyConfig = when (riskLevel) {
    "LOW" -> DifficultyConfig(
        level = 3, label = "Challenge",
        targetTimeoutMs = 1800L, minIsiMs = 500L, maxIsiMs = 1000L,
        totalTrials = 50, hintThreshold = 4
    )
    "HIGH" -> DifficultyConfig(
        level = 1, label = "Easy",
        targetTimeoutMs = 3500L, minIsiMs = 1200L, maxIsiMs = 2000L,
        totalTrials = 40, hintThreshold = 2
    )
    else -> DifficultyConfig( // MEDIUM or no history
        level = 2, label = "Standard",
        targetTimeoutMs = 2500L, minIsiMs = 800L, maxIsiMs = 1500L,
        totalTrials = 50, hintThreshold = 3
    )
}

sealed class GameState {
    object Idle : GameState()
    object NeedCalibration : GameState()
    object Calibrating : GameState()
    object Ready : GameState()
    object Playing : GameState()
    data class Results(val response: GameSessionResponse) : GameState()
    data class Error(val message: String) : GameState()
}

data class GameUiState(
    val gameState: GameState = GameState.Idle,
    val motorBaseline: Double? = null,
    val currentTrial: Int = 0,
    val totalTrials: Int = 50,
    val score: Int = 0,
    val streak: Int = 0,
    val trials: List<TrialData> = emptyList(),
    val stats: UserStatsResponse? = null,
    val isLoading: Boolean = false,
    val difficulty: DifficultyConfig = difficultyForRisk(null)
)

class GameViewModel(
    private val repository: GameRepository,
    private val userId: String,
    private val token: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        checkMotorBaseline()
    }

    private fun checkMotorBaseline() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getMotorBaseline(userId).fold(
                onSuccess = { response ->
                    // Check if motor_baseline is null (new user, no calibration yet)
                    if (response.motor_baseline == null) {
                        // New user - need calibration
                        _uiState.value = _uiState.value.copy(
                            motorBaseline = null,
                            gameState = GameState.NeedCalibration,
                            isLoading = false
                        )
                    } else {
                        // Existing user with calibration - ready to play
                        _uiState.value = _uiState.value.copy(
                            motorBaseline = response.motor_baseline,
                            gameState = GameState.Ready,
                            isLoading = false
                        )
                        loadUserStats()
                    }
                },
                onFailure = {
                    // API error or no response - need calibration
                    _uiState.value = _uiState.value.copy(
                        gameState = GameState.NeedCalibration,
                        isLoading = false
                    )
                }
            )
        }
    }

    fun submitCalibration(tapTimes: List<Double>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                gameState = GameState.Calibrating,
                isLoading = true
            )

            // Set a timeout - if submission takes too long, proceed anyway
            val timeoutJob = launch {
                kotlinx.coroutines.delay(15000) // 15 second timeout (increased to allow for backend processing)
                if (_uiState.value.isLoading) {
                    // Timeout reached, proceed to game with default baseline
                    _uiState.value = _uiState.value.copy(
                        motorBaseline = 0.5, // Default baseline (500ms = 0.5s)
                        gameState = GameState.Ready,
                        isLoading = false
                    )
                }
            }

            repository.submitCalibration(userId, tapTimes).fold(
                onSuccess = { response ->
                    timeoutJob.cancel()
                    _uiState.value = _uiState.value.copy(
                        motorBaseline = response.motorBaseline,
                        gameState = GameState.Ready,
                        isLoading = false
                    )
                    loadUserStats()
                },
                onFailure = { error ->
                    timeoutJob.cancel()
                    // Don't show error, just proceed with default baseline
                    _uiState.value = _uiState.value.copy(
                        motorBaseline = 0.5, // Default baseline (500ms = 0.5s)
                        gameState = GameState.Ready,
                        isLoading = false
                    )
                }
            )
        }
    }

    fun skipCalibration() {
        _uiState.value = _uiState.value.copy(
            motorBaseline = 0.5, // Default baseline when skipped (500ms = 0.5s)
            gameState = GameState.Ready,
            isLoading = false
        )
        loadUserStats()
    }

    fun startGame() {
        val difficulty = difficultyForRisk(_uiState.value.stats?.currentRiskLevel)
        _uiState.value = _uiState.value.copy(
            gameState = GameState.Playing,
            currentTrial = 0,
            score = 0,
            streak = 0,
            trials = emptyList(),
            totalTrials = difficulty.totalTrials,
            difficulty = difficulty
        )
    }

    fun recordTrial(trial: TrialData) {
        val currentTrials = _uiState.value.trials + trial
        val newScore = if (trial.correct == 1) _uiState.value.score + 1 else _uiState.value.score
        val newStreak = if (trial.correct == 1) _uiState.value.streak + 1 else 0

        _uiState.value = _uiState.value.copy(
            currentTrial = trial.trialNumber ?: (_uiState.value.currentTrial + 1),
            trials = currentTrials,
            score = newScore,
            streak = newStreak
        )
    }

    fun submitGameSession(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Generate session ID
            val sessionId = "session_${userId}_${Clock.System.now().toEpochMilliseconds()}"

            repository.submitGameSession(
                userId = userId,
                sessionId = sessionId,
                gameType = "grid_tap_3x3",
                level = _uiState.value.difficulty.level,
                trials = _uiState.value.trials,
                token = token
            ).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        gameState = GameState.Results(response),
                        isLoading = false
                    )
                    loadUserStats()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        gameState = GameState.Error(error.message ?: "Failed to submit session"),
                        isLoading = false
                    )
                }
            )
        }
    }

    fun loadUserStats() {
        viewModelScope.launch {
            repository.getUserStats(userId).fold(
                onSuccess = { stats ->
                    _uiState.value = _uiState.value.copy(
                        stats = stats,
                        difficulty = difficultyForRisk(stats.currentRiskLevel)
                    )
                },
                onFailure = { /* Ignore stats loading errors */ }
            )
        }
    }

    fun resetToReady() {
        _uiState.value = _uiState.value.copy(
            gameState = GameState.Ready,
            currentTrial = 0,
            score = 0,
            streak = 0,
            trials = emptyList()
        )
    }

    fun forceRecalibration() {
        _uiState.value = _uiState.value.copy(
            gameState = GameState.NeedCalibration,
            motorBaseline = null
        )
    }
}