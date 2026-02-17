package com.rp_elderycareapp.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(
    onImageSelected: (String?) -> Unit
): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}
