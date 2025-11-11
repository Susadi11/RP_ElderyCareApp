package com.rp_elderycareapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.rp_elderycareapp.R

@Composable
actual fun getBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem(
            route = NavRoutes.HOME.route,
            icon = painterResource(R.drawable.outline_home_24),
            label = "Home"
        ),
        BottomNavItem(
            route = NavRoutes.CHAT.route,
            icon = painterResource(R.drawable.outline_chat_24),
            label = "Chat"
        ),
        BottomNavItem(
            route = NavRoutes.GAME.route,
            icon = painterResource(R.drawable.outline_gamepad_circle_down_24),
            label = "Game"
        ),
        BottomNavItem(
            route = NavRoutes.MMSE_TEST.route,
            icon = painterResource(R.drawable.outline_health_metrics_24),
            label = "MMSE"
        ),
        BottomNavItem(
            route = NavRoutes.REMINDER.route,
            icon = painterResource(R.drawable.outline_notifications_active_24),
            label = "Reminder"
        )
    )
}
