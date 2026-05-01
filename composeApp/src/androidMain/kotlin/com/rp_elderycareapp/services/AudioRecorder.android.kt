package com.rp_elderycareapp.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
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
    private var timerJob: Job? = null
    
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
                println("❌ Audio recording permission not granted")
                return@withContext Result.failure(Exception("Audio recording permission not granted"))
            }
            
            // Don't delete previous recording immediately
            // Keep it until new recording is successful
            
            // Create output file
            val audioFile = createAudioFile()
            val filePath = audioFile.absolutePath
            println("🎤 Creating audio file: $filePath")
            
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
                setOutputFile(filePath)
                
                try {
                    prepare()
                    start()
                    
                    // Only set the file path after successful start
                    _audioFilePath.value = filePath
                    _isRecording.value = true
                    startTime = System.currentTimeMillis()
                    
                    println("✅ Recording started successfully: $filePath")
                    
                    // Start timer to update duration
                    startDurationTimer()
                } catch (e: IOException) {
                    println("❌ Failed to start recording: ${e.message}")
                    e.printStackTrace()
                    throw Exception("Failed to start recording: ${e.message}")
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Exception in startRecording: ${e.message}")
            e.printStackTrace()
            _isRecording.value = false
            Result.failure(e)
        }
    }
    
    override suspend fun stopRecording(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Stop timer
            timerJob?.cancel()
            timerJob = null
            
            val filePath = _audioFilePath.value
            println("🎤 Stopping recording, file path: $filePath")
            
            mediaRecorder?.apply {
                try {
                    stop()
                    println("✅ MediaRecorder stopped successfully")
                } catch (e: Exception) {
                    println("⚠️ Error stopping MediaRecorder: ${e.message}")
                }
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            
            // Verify file exists and has content
            if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    val fileSize = file.length()
                    println("✅ Recording file exists: $filePath (${fileSize} bytes)")
                    if (fileSize > 0) {
                        return@withContext Result.success(filePath)
                    } else {
                        println("❌ Recording file is empty")
                        return@withContext Result.failure(Exception("Recording file is empty"))
                    }
                } else {
                    println("❌ Recording file does not exist: $filePath")
                    return@withContext Result.failure(Exception("Recording file not found at path: $filePath"))
                }
            } else {
                println("❌ Recording file path is null")
                return@withContext Result.failure(Exception("Recording file path is null"))
            }
        } catch (e: Exception) {
            println("❌ Exception in stopRecording: ${e.message}")
            e.printStackTrace()
            _isRecording.value = false
            Result.failure(Exception("Failed to stop recording: ${e.message}"))
        }
    }
    
    override suspend fun playRecording(): Result<Unit> {
        val filePath = _audioFilePath.value
        println("🔊 Attempting to play recording: $filePath")

        if (filePath == null) {
            return Result.failure(Exception("No recording found. Please record again."))
        }

        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) {
            return Result.failure(Exception("Recording file is missing or empty. Please record again."))
        }

        // Stop any existing playback safely
        try { stopPlayback() } catch (_: Exception) {}

        return suspendCancellableCoroutine { continuation ->
            val player = MediaPlayer()
            try {
                player.setDataSource(filePath)
                player.setOnPreparedListener { mp ->
                    mp.start()
                    mediaPlayer = mp
                    println("✅ Playback started successfully")
                    continuation.resume(Result.success(Unit))
                }
                player.setOnErrorListener { _, what, extra ->
                    player.release()
                    mediaPlayer = null
                    continuation.resume(Result.failure(Exception("Playback failed (error $what/$extra). Try recording again.")))
                    true
                }
                player.prepareAsync()
            } catch (e: Exception) {
                player.release()
                mediaPlayer = null
                continuation.resume(Result.failure(Exception("Failed to play recording: ${e.message ?: "unknown error"}")))
            }
        }
    }
    
    override suspend fun stopPlayback() {
        withContext(Dispatchers.IO) {
            try {
                mediaPlayer?.apply {
                    if (isPlaying) stop()
                    release()
                }
            } catch (_: Exception) {}
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
        timerJob?.cancel()
        timerJob = null
        mediaRecorder?.release()
        mediaPlayer?.release()
        deleteRecording()
    }
    
    private fun startDurationTimer() {
        timerJob?.cancel()
        timerJob = kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
            while (isActive && _isRecording.value) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _recordingDuration.value = elapsed
                delay(100) // Update every 100ms for smooth UI
            }
        }
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
