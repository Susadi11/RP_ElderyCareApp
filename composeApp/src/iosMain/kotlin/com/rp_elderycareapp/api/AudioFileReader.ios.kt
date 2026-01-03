package com.rp_elderycareapp.api

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile

actual suspend fun readAudioFile(filePath: String): ByteArray {
    val data = NSData.dataWithContentsOfFile(filePath)
        ?: throw IllegalArgumentException("Could not read file: $filePath")

    val byteArray = ByteArray(data.length.toInt())
    byteArray.usePinned { pinned ->
        data.getBytes(pinned.addressOf(0))
    }
    return byteArray
}
