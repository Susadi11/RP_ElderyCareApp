package com.rp_elderycareapp.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS stub implementation of AudioRecorder
 * TODO: Implement using AVAudioRecorder when iOS support is needed
 */
class IosAudioRecorder : AudioRecorder {
    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _audioFilePath = MutableStateFlow<String?>(null)
    override val audioFilePath: StateFlow<String?> = _audioFilePath.asStateFlow()
    
    private val _recordingDuration = MutableStateFlow(0L)
    override val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()
    
    override suspend fun startRecording(): Result<Unit> {
        return Result.failure(Exception("Audio recording not supported on iOS yet"))
    }
    
    override suspend fun stopRecording(): Result<String> {
        return Result.failure(Exception("Audio recording not supported on iOS yet"))
    }
    
    override suspend fun playRecording(): Result<Unit> {
        return Result.failure(Exception("Audio playback not supported on iOS yet"))
    }
    
    override suspend fun stopPlayback() {
        // No-op
    }
    
    override fun deleteRecording() {
        // No-op
    }
    
    override suspend fun hasPermission(): Boolean {
        return false
    }
    
    override suspend fun requestPermission(): Boolean {
        return false
    }
    
    override fun cleanup() {
        // No-op
    }
}

actual fun createAudioRecorder(): AudioRecorder {
    return IosAudioRecorder()
}
