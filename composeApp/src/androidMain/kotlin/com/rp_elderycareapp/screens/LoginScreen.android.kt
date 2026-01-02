package com.rp_elderycareapp.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rp_elderycareapp.ui.theme.AppColors

@Composable
actual fun LoginVisibilityIcon(isVisible: Boolean) {
    Icon(
        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
        contentDescription = if (isVisible) "Hide password" else "Show password",
        tint = AppColors.SecondaryText,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun LoginEmailIcon() {
    Icon(
        imageVector = Icons.Default.Email,
        contentDescription = "Email",
        tint = AppColors.Primary,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun LoginLockIcon() {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Password",
        tint = AppColors.Primary,
        modifier = Modifier.size(22.dp)
    )
}
