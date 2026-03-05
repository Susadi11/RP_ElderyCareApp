package com.rp_elderycareapp.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberImagePickerLauncher(
    onImageSelected: (String?) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() }
                
                if (bytes != null) {
                    // Resize logic could go here, but for now strict base64
                    // Re-compress to JPEG to ensure format and size control
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val outputStream = ByteArrayOutputStream()
                    // Compress to JPEG with 70% quality to save space
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val compressedBytes = outputStream.toByteArray()
                    
                    val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                    onImageSelected(base64)
                    
                    // Cleanup
                    bitmap.recycle()
                    outputStream.close()
                } else {
                    onImageSelected(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onImageSelected(null)
            }
        }
    }

    return remember(launcher) {
        object : ImagePickerLauncher {
            override fun launch() {
                launcher.launch("image/*")
            }
        }
    }
}
