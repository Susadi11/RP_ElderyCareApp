package com.rp_elderycareapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val icon: Any, // Can be ImageVector or Painter
    val label: String
)

@Composable
expect fun getBottomNavItems(): List<BottomNavItem>
