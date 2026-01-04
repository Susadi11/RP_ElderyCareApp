package com.rp_elderycareapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.rp_elderycareapp.R

@Composable
actual fun loadImageResource(imageType: String): Painter {
    val drawableRes = when (imageType) {
        "watch" -> R.drawable.ic_wristwatch
        "pencil" -> R.drawable.ic_pencil
        else -> R.drawable.ic_launcher_foreground // Fallback
    }
    return painterResource(id = drawableRes)
}
