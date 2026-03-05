package com.rp_elderycareapp.screens

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import rp_elderycareapp.composeapp.generated.resources.Res
import rp_elderycareapp.composeapp.generated.resources.Hale_logo

@Composable
actual fun AppLogo(modifier: Modifier) {
    Image(
        painter = painterResource(Res.drawable.Hale_logo),
        contentDescription = "Hale App Logo",
        modifier = modifier
    )
}
