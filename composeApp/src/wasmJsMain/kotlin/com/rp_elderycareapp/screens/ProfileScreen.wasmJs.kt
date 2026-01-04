package com.rp_elderycareapp.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.rp_elderycareapp.ui.theme.AppColors

@Composable
actual fun ProfileUserIcon() {
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = "User Profile",
        tint = AppColors.Primary
    )
}

@Composable
actual fun ProfileEditIcon() {
    Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Edit Profile",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun ProfileLockIcon() {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Change Password",
        tint = AppColors.PrimaryText
    )
}

@Composable
actual fun ProfileDeleteIcon() {
    Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "Delete Account",
        tint = Color.Red
    )
}

@Composable
actual fun ProfileLogoutIcon() {
    Icon(
        imageVector = Icons.Default.ExitToApp,
        contentDescription = "Logout",
        tint = Color.Red
    )
}