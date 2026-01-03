package com.rp_elderycareapp.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(): String {
        // Create output file
        outputFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.wav")

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile?.absolutePath)

            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("Failed to start recording: ${e.message}")
            }
        }

        return outputFile?.absolutePath ?: ""
    }

    fun stopRecording(): String? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            return outputFile?.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            outputFile?.delete()
            outputFile = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
