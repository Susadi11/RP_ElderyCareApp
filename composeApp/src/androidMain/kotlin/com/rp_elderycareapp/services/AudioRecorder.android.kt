package com.rp_elderycareapp.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Android implementation of AudioRecorder using MediaRecorder API
 */
class AndroidAudioRecorder(private val context: Context) : AudioRecorder {
    
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _audioFilePath = MutableStateFlow<String?>(null)
    override val audioFilePath: StateFlow<String?> = _audioFilePath.asStateFlow()
    
    private val _recordingDuration = MutableStateFlow(0L)
    override val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()
    
    private var startTime: Long = 0
    
    override suspend fun startRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check permission
            if (!hasPermission()) {
                return@withContext Result.failure(Exception("Audio recording permission not granted"))
            }
            
            // Clean up previous recording
            deleteRecording()
            
            // Create output file
            val audioFile = createAudioFile()
            _audioFilePath.value = audioFile.absolutePath
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile.absolutePath)
                
                try {
                    prepare()
                    start()
                    _isRecording.value = true
                    startTime = System.currentTimeMillis()
                    
                    // Duration will be updated externally if needed
                } catch (e: IOException) {
                    throw Exception("Failed to start recording: ${e.message}")
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            _isRecording.value = false
            Result.failure(e)
        }
    }
    
    override suspend fun stopRecording(): Result<String> = withContext(Dispatchers.IO) {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            
            val filePath = _audioFilePath.value
            if (filePath != null && File(filePath).exists()) {
                Result.success(filePath)
            } else {
                Result.failure(Exception("Recording file not found"))
            }
        } catch (e: Exception) {
            _isRecording.value = false
            Result.failure(Exception("Failed to stop recording: ${e.message}"))
        }
    }
    
    override suspend fun playRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val filePath = _audioFilePath.value
            if (filePath == null || !File(filePath).exists()) {
                return@withContext Result.failure(Exception("No recording to play"))
            }
            
            // Stop any existing playback
            stopPlayback()
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to play recording: ${e.message}"))
        }
    }
    
    override suspend fun stopPlayback() {
        withContext(Dispatchers.IO) {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        }
    }
    
    override fun deleteRecording() {
        _audioFilePath.value?.let { path ->
            File(path).delete()
        }
        _audioFilePath.value = null
        _recordingDuration.value = 0
    }
    
    override suspend fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override suspend fun requestPermission(): Boolean {
        // Permission request must be handled by Activity
        // This will be called from Compose with accompanist permissions
        return hasPermission()
    }
    
    override fun cleanup() {
        mediaRecorder?.release()
        mediaPlayer?.release()
        deleteRecording()
    }
    
    private fun createAudioFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "reminder_audio_$timestamp.3gp"
        val storageDir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(storageDir, fileName)
    }
}

/**
 * Actual implementation for Android
 */
actual fun createAudioRecorder(): AudioRecorder {
    throw IllegalStateException("AudioRecorder must be created with Android context. Use createAndroidAudioRecorder(context)")
}

/**
 * Android-specific factory function that requires context
 */
fun createAndroidAudioRecorder(context: Context): AudioRecorder {
    return AndroidAudioRecorder(context)
}
