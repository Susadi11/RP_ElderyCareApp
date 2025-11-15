package com.rp_elderycareapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rp_elderycareapp.R

@Composable
actual fun ProfileUserIcon() {
    Image(
        painter = painterResource(R.drawable.outline_account_circle_24),
        contentDescription = "Profile User",
        colorFilter = ColorFilter.tint(Color.White),
        modifier = Modifier.size(60.dp)
    )
}

@Composable
actual fun ProfileEditIcon() {
    Image(
        painter = painterResource(R.drawable.outline_edit_24),
        contentDescription = "Edit",
        modifier = Modifier.size(18.dp)
    )
}

@Composable
actual fun ProfileLockIcon() {
    Image(
        painter = painterResource(R.drawable.outline_lock_24),
        contentDescription = "Lock",
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun ProfileDeleteIcon() {
    Image(
        painter = painterResource(R.drawable.outline_delete_24),
        contentDescription = "Delete",
        colorFilter = ColorFilter.tint(Color(0xFFEF4444)),
        modifier = Modifier.size(22.dp)
    )
}

@Composable
actual fun ProfileLogoutIcon() {
    Image(
        painter = painterResource(R.drawable.outline_logout_24),
        contentDescription = "Logout",
        colorFilter = ColorFilter.tint(Color(0xFFB91C1C)),
        modifier = Modifier.size(20.dp)
    )
}

