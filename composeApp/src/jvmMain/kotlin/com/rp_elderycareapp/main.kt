package com.rp_elderycareapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RP_ElderyCareApp",
    ) {
        App()
    }
}