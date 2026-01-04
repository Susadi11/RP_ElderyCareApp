package com.rp_elderycareapp.services

import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Platform-agnostic interface for audio recording functionality.
 * Implementations are provided for each platform (Android, iOS, etc.)
 */
interface AudioRecorder {
    /**
     * Current recording state
     */
    val isRecording: StateFlow<Boolean>
    
    /**
     * Current audio file path (null if not recorded yet)
     */
    val audioFilePath: StateFlow<String?>
    
    /**
     * Recording duration in seconds
     */
    val recordingDuration: StateFlow<Long>
    
    /**
     * Start recording audio
     * @return Success or error message
     */
    suspend fun startRecording(): Result<Unit>
    
    /**
     * Stop recording audio
     * @return Path to recorded audio file or error
     */
    suspend fun stopRecording(): Result<String>
    
    /**
     * Play recorded audio
     */
    suspend fun playRecording(): Result<Unit>
    
    /**
     * Stop playback
     */
    suspend fun stopPlayback()
    
    /**
     * Delete recorded audio file
     */
    fun deleteRecording()
    
    /**
     * Check if audio permission is granted
     */
    suspend fun hasPermission(): Boolean
    
    /**
     * Request audio recording permission
     */
    suspend fun requestPermission(): Boolean
    
    /**
     * Clean up resources
     */
    fun cleanup()
}

/**
 * Factory function to create platform-specific AudioRecorder
 */
expect fun createAudioRecorder(): AudioRecorder
