package com.rp_elderycareapp.platform

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Composable
actual fun rememberAudioRecorder(): AudioRecorder {
    val context = LocalContext.current
    return remember { WavAudioRecorder(context) }
}

class WavAudioRecorder(private val context: Context) : AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _outputFilePath = MutableStateFlow<String?>(null)
    override val outputFilePath: StateFlow<String?> = _outputFilePath.asStateFlow()

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    override fun startRecording(fileName: String) {
        if (_isRecording.value) return

        val file = File(context.cacheDir, fileName)
        _outputFilePath.value = file.absolutePath

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()
        _isRecording.value = true

        recordingJob = scope.launch {
            writeAudioDataToFile(file)
        }
    }

    override fun stopRecording() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
    }

    private suspend fun writeAudioDataToFile(file: File) = withContext(Dispatchers.IO) {
        val data = ByteArray(bufferSize)
        try {
            FileOutputStream(file).use { fos ->
                // Write placeholder for WAV header
                fos.write(ByteArray(44))

                while (isActive && _isRecording.value) {
                    val read = audioRecord?.read(data, 0, bufferSize) ?: 0
                    if (read > 0) {
                        fos.write(data, 0, read)
                    }
                }
            }
            updateWavHeader(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateWavHeader(file: File) {
        val fileSize = file.length()
        val dataSize = fileSize - 44
        val header = createWavHeader(dataSize)
        
        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.write(header)
        }
    }

    private fun createWavHeader(dataSize: Long): ByteArray {
        val totalSize = dataSize + 36
        val byteRate = (sampleRate * 1 * 16 / 8).toLong()
        val header = ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            // RIFF chunk
            put("RIFF".toByteArray())
            putInt(totalSize.toInt())
            put("WAVE".toByteArray())
            // fmt chunk
            put("fmt ".toByteArray())
            putInt(16) // Subchunk1Size (16 for PCM)
            putShort(1.toShort()) // AudioFormat (1 for PCM)
            putShort(1.toShort()) // NumChannels (1 for Mono)
            putInt(sampleRate)
            putInt(byteRate.toInt())
            putShort(2.toShort()) // BlockAlign
            putShort(16.toShort()) // BitsPerSample
            // data chunk
            put("data".toByteArray())
            putInt(dataSize.toInt())
        }
        return header.array()
    }
}
