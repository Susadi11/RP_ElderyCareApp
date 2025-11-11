package com.rp_elderycareapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.rp_elderycareapp.components.BottomNavigationBar
import com.rp_elderycareapp.navigation.NavRoutes
import com.rp_elderycareapp.screens.ChatScreen
import com.rp_elderycareapp.screens.GameScreen
import com.rp_elderycareapp.screens.HomeScreen
import com.rp_elderycareapp.screens.MmseTestScreen
import com.rp_elderycareapp.screens.ReminderScreen
import com.rp_elderycareapp.ui.theme.ElderyCareTheme

@Composable
@Preview
fun App() {
    ElderyCareTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        // Hide bottom bar on chat screen
        val showBottomBar = currentRoute != NavRoutes.CHAT.route
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.HOME.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavRoutes.CHAT.route) {
                    // Chat screen takes full height (no bottom padding)
                    ChatScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.GAME.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        GameScreen()
                    }
                }
                composable(NavRoutes.HOME.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        HomeScreen(
                            onStartChat = { navController.navigate(NavRoutes.CHAT.route) },
                            onPlayGames = { navController.navigate(NavRoutes.GAME.route) },
                            onTakeMmseTest = { navController.navigate(NavRoutes.MMSE_TEST.route) }
                        )
                    }
                }
                composable(NavRoutes.MMSE_TEST.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MmseTestScreen()
                    }
                }
                composable(NavRoutes.REMINDER.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ReminderScreen()
                    }
                }
            }
        }
    }
}