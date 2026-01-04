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
import com.rp_elderycareapp.screens.*
import com.rp_elderycareapp.ui.theme.ElderyCareTheme

@Composable
@Preview
fun App() {
    ElderyCareTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        // Hide bottom bar on auth screens, chat, settings, profile, and reminder dashboard screens
        val showBottomBar = currentRoute != NavRoutes.LOGIN.route &&
                           currentRoute != NavRoutes.SIGNUP.route &&
                           currentRoute != NavRoutes.CHAT.route &&
                           currentRoute != NavRoutes.SETTINGS.route &&
                           currentRoute != NavRoutes.PROFILE.route &&
                           currentRoute != NavRoutes.PATIENT_DASHBOARD.route &&
                           currentRoute != NavRoutes.CAREGIVER_ALERTS.route
        
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
                startDestination = NavRoutes.LOGIN.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // Authentication Screens
                composable(NavRoutes.LOGIN.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(NavRoutes.HOME.route) {
                                popUpTo(NavRoutes.LOGIN.route) { inclusive = true }
                            }
                        },
                        onNavigateToSignup = {
                            navController.navigate(NavRoutes.SIGNUP.route)
                        },
                        onForgotPassword = {
                            // TODO: Implement forgot password flow
                        }
                    )
                }
                composable(NavRoutes.SIGNUP.route) {
                    SignupScreen(
                        onSignupSuccess = {
                            navController.navigate(NavRoutes.HOME.route) {
                                popUpTo(NavRoutes.SIGNUP.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // Main App Screens
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
                            onTakeMmseTest = { navController.navigate(NavRoutes.MMSE_TEST.route) },
                            onNavigateToProfile = { navController.navigate(NavRoutes.PROFILE.route) },
                            onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS.route) }
                        )
                    }
                }
                composable(NavRoutes.MMSE_TEST.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MmseTestScreen(
                            onStartAssessmentClick = { navController.navigate(NavRoutes.MMSE_START_TEST.route) }
                        )
                    }
                }
                composable(NavRoutes.MMSE_START_TEST.route) {
                    MmseStartTestScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onStartTest = {
                            // ✅ Navigate to questions screen
                            navController.navigate(NavRoutes.MMSE_QUESTIONS.route)
                        },
                        onTalkWithUs = {
                            navController.navigate(NavRoutes.CHAT.route)
                        }
                    )
                }
                composable(NavRoutes.MMSE_QUESTIONS.route) {
                    MmseQuestionsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onTalkWithUs = {
                            navController.navigate(NavRoutes.CHAT.route)
                        },
                        onComplete = {
                            // TODO: Navigate to results/score screen
                            // For now, go back to MMSE test screen
                            navController.navigate(NavRoutes.MMSE_QUESTIONS.route) {
                                popUpTo(NavRoutes.MMSE_TEST.route)
                            }
                        }
                    )
                }

                composable(NavRoutes.REMINDER.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ReminderScreen()
                    }
                }
                composable(NavRoutes.PATIENT_DASHBOARD.route) {
                    // Dashboard screen takes full height
                    PatientDashboardScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.CAREGIVER_ALERTS.route) {
                    // Caregiver alerts screen takes full height
                    CaregiverAlertScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.SETTINGS.route) {
                    // Settings screen takes full height (no bottom padding)
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.PROFILE.route) {
                    // Profile screen takes full height (no bottom padding)
                    ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = {
                            // Navigate back to login and clear backstack
                            navController.navigate(NavRoutes.LOGIN.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}