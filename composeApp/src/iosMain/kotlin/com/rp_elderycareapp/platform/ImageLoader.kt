package com.rp_elderycareapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import rp_elderycareapp.composeapp.generated.resources.Res
import rp_elderycareapp.composeapp.generated.resources.ic_launcher_foreground
import rp_elderycareapp.composeapp.generated.resources.ic_pencil
import rp_elderycareapp.composeapp.generated.resources.ic_wristwatch

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun loadImageResource(imageType: String): Painter {
    val drawableRes = when (imageType) {
        "watch" -> Res.drawable.ic_wristwatch
        "pencil" -> Res.drawable.ic_pencil
        else -> Res.drawable.ic_launcher_foreground
    }
    return painterResource(drawableRes)
}
