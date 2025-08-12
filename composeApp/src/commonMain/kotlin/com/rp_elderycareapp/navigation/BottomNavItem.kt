package com.rp_elderycareapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = NavRoutes.CHAT.route,
        icon = Icons.Default.AccountCircle,
        label = "Chat"
    ),
    BottomNavItem(
        route = NavRoutes.GAME.route,
        icon = Icons.Default.PlayArrow,
        label = "Game"
    ),
    BottomNavItem(
        route = NavRoutes.HOME.route,
        icon = Icons.Default.Home,
        label = "Home"
    ),
    BottomNavItem(
        route = NavRoutes.MMSE_TEST.route,
        icon = Icons.Default.Star,
        label = "MMSE Test"
    ),
    BottomNavItem(
        route = NavRoutes.REMINDER.route,
        icon = Icons.Default.Notifications,
        label = "Reminder"
    )
)
