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
    val isLoading: Boolean = false
)

class GameViewModel(
    private val repository: GameRepository,
    private val userId: String
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
                    _uiState.value = _uiState.value.copy(
                        motorBaseline = response.motor_baseline,
                        gameState = GameState.Ready,
                        isLoading = false
                    )
                    loadUserStats()
                },
                onFailure = {
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
                kotlinx.coroutines.delay(3000) // 3 second timeout
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
    }

    fun startGame() {
        _uiState.value = _uiState.value.copy(
            gameState = GameState.Playing,
            currentTrial = 0,
            score = 0,
            streak = 0,
            trials = emptyList()
        )
    }

    fun recordTrial(trial: TrialData) {
        val currentTrials = _uiState.value.trials + trial
        val newScore = if (trial.correct) _uiState.value.score + 1 else _uiState.value.score
        val newStreak = if (trial.correct) _uiState.value.streak + 1 else 0

        _uiState.value = _uiState.value.copy(
            currentTrial = trial.trialNumber,
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
                level = 1,
                trials = _uiState.value.trials
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
                    _uiState.value = _uiState.value.copy(stats = stats)
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
}