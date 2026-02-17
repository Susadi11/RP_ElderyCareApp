package com.rp_elderycareapp.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePickerLauncher(
    onImageSelected: (String?) -> Unit
): ImagePickerLauncher {
    return remember {
        object : ImagePickerLauncher {
            override fun launch() {
                // iOS implementation omitted for stability
                println("Image Picker not implemented for iOS yet")
                onImageSelected(null)
            }
        }
    }
}
