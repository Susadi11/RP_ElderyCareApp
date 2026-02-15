package com.rp_elderycareapp.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
actual fun ResetPasswordVisibilityIcon() {
    var visible by remember { mutableStateOf(false) }
    Icon(
        imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
        contentDescription = "Toggle password visibility"
    )
}
