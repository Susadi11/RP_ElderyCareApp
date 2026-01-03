package com.rp_elderycareapp.api

actual suspend fun readAudioFile(filePath: String): ByteArray {
    // Web implementation would use File API or Blob
    throw UnsupportedOperationException("Audio file reading not yet implemented for Web")
}
