package com.rp_elderycareapp.api

import java.io.File

actual suspend fun readAudioFile(filePath: String): ByteArray {
    return File(filePath).readBytes()
}
