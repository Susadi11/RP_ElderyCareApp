package com.rp_elderycareapp.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rp_elderycareapp.PreferencesManager

@Composable
actual fun ProfileUserIcon() {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = "Profile User",
        tint = Color.White,
        modifier = Modifier.size(60.dp)
    )
}

@Composable
actual fun ProfileEditIcon() {
    Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Edit",
        modifier = Modifier.size(18.dp)
    )
}

@Composable
actual fun ProfileLockIcon() {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Lock",
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun ProfileDeleteIcon() {
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "Delete",
        tint = Color(0xFFEF4444),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun ProfileLogoutIcon() {
    Icon(
        imageVector = Icons.Default.ExitToApp,
        contentDescription = "Logout",
        tint = Color(0xFFB91C1C),
        modifier = Modifier.size(20.dp)
    )
}
@Composable
actual fun ProfileSettingsIcon() {
    Icon(
        imageVector = Icons.Default.Settings,
        contentDescription = "Settings",
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun getPreferencesManager(): PreferencesManager {
    return PreferencesManager()
}
