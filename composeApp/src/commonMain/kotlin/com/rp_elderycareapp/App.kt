package com.rp_elderycareapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.HOME.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(NavRoutes.CHAT.route) {
                    ChatScreen()
                }
                composable(NavRoutes.GAME.route) {
                    GameScreen()
                }
                composable(NavRoutes.HOME.route) {
                    HomeScreen(
                        onStartChat = { navController.navigate(NavRoutes.CHAT.route) },
                        onPlayGames = { navController.navigate(NavRoutes.GAME.route) },
                        onTakeMmseTest = { navController.navigate(NavRoutes.MMSE_TEST.route) }
                    )
                }
                composable(NavRoutes.MMSE_TEST.route) {
                    MmseTestScreen()
                }
                composable(NavRoutes.REMINDER.route) {
                    ReminderScreen()
                }
            }
        }
    }
}