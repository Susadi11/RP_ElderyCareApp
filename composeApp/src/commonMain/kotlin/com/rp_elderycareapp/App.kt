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
import com.rp_elderycareapp.viewmodel.AuthViewModel
import com.rp_elderycareapp.viewmodel.ChatViewModel
import androidx.compose.runtime.remember
import com.rp_elderycareapp.screens.getPreferencesManager
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    ElderyCareTheme {
        val preferencesManager = getPreferencesManager()
        val authViewModel = remember { AuthViewModel(preferencesManager) }
        val chatViewModel = remember { ChatViewModel() }
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val mmseApi = remember { com.rp_elderycareapp.api.MmseApi() }
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        
        // Hide bottom bar on auth screens, chat, settings, profile, and reminder dashboard screens
        val showBottomBar = currentRoute != NavRoutes.LOGIN.route &&
                           currentRoute != NavRoutes.SIGNUP.route &&
                           currentRoute != NavRoutes.CHAT.route &&
                           currentRoute != NavRoutes.SETTINGS.route &&
                           currentRoute != NavRoutes.PROFILE.route &&
                           currentRoute != NavRoutes.PATIENT_DASHBOARD.route &&
                           currentRoute != NavRoutes.CAREGIVER_ALERTS.route
        
        val isAuthenticated by authViewModel.isAuthenticated
        androidx.compose.runtime.LaunchedEffect(isAuthenticated) {
            if (!isAuthenticated &&
                currentRoute != NavRoutes.LOGIN.route &&
                currentRoute != NavRoutes.SIGNUP.route) {
                navController.navigate(NavRoutes.LOGIN.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            // Dynamic start destination based on authentication state
            val startDestination = if (authViewModel.isAuthenticated.value) {
                NavRoutes.HOME.route
            } else {
                NavRoutes.LOGIN.route
            }
            
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                // Authentication Screens
                composable(NavRoutes.LOGIN.route) {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(NavRoutes.HOME.route) {
                                popUpTo(NavRoutes.LOGIN.route) { inclusive = true }
                            }
                        },
                        onNavigateToSignup = {
                            navController.navigate(NavRoutes.SIGNUP.route)
                        },
                        onForgotPassword = {
                            navController.navigate(NavRoutes.FORGOT_PASSWORD.route)
                        }
                    )
                }
                composable(NavRoutes.SIGNUP.route) {
                    SignupScreen(
                        authViewModel = authViewModel,
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
                composable(NavRoutes.FORGOT_PASSWORD.route) {
                    ForgotPasswordScreen(
                        authViewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToReset = { email ->
                            navController.navigate("${NavRoutes.RESET_PASSWORD.route}/$email")
                        }
                    )
                }
                composable("${NavRoutes.RESET_PASSWORD.route}/{email}") { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    ResetPasswordScreen(
                        authViewModel = authViewModel,
                        initialEmail = email,
                        onResetSuccess = {
                            navController.navigate(NavRoutes.LOGIN.route) {
                                popUpTo(NavRoutes.FORGOT_PASSWORD.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(NavRoutes.LOGIN.route) {
                                popUpTo(NavRoutes.LOGIN.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Main App Screens
                composable(NavRoutes.CHAT.route) {
                    // Chat screen takes full height (no bottom padding)
                    ChatScreen(
                        authViewModel = authViewModel,
                        chatViewModel = chatViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.GAME.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        GameScreen(
                            authViewModel = authViewModel,
                            onNavigateToTrends = {
                                val userId = authViewModel.currentUser.value?.user_id ?: ""
                                navController.navigate("game_trends/$userId")
                            }
                        )
                    }
                }
                composable("game_trends/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    Box(modifier = Modifier.padding(innerPadding)) {
                        GameTrendScreen(
                            userId = userId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(NavRoutes.HOME.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val currentUser by authViewModel.currentUser
                        HomeScreen(
                            userName = currentUser?.full_name ?: "User",
                            authViewModel = authViewModel,
                            onStartChat = { navController.navigate(NavRoutes.CHAT.route) },
                            onPlayGames = { navController.navigate(NavRoutes.GAME.route) },
                            onTakeMmseTest = { navController.navigate(NavRoutes.MMSE_TEST.route) },
                            onNavigateToProfile = { navController.navigate(NavRoutes.PROFILE.route) },
                            onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS.route) },
                            onNavigateToPatientDetails = { navController.navigate(NavRoutes.PATIENT_DETAILS.route) }
                        )
                    }
                }
                composable(NavRoutes.MMSE_TEST.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val userId = authViewModel.currentUser.value?.user_id ?: ""
                        MmseTestScreen(
                            userId = userId,
                            onStartAssessmentClick = { navController.navigate(NavRoutes.MMSE_START_TEST.route) }
                        )
                    }
                }
                composable(NavRoutes.MMSE_START_TEST.route) {
                    MmseStartTestScreen(
                        authViewModel = authViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onStartTest = { assessmentId ->
                            // ✅ Navigate to questions screen with assessmentId and userId
                            val userId = authViewModel.currentUser.value?.user_id ?: "USER-SUSADI-24-F71B"
                            navController.navigate("mmse_questions/$userId/$assessmentId")
                        }
                    )
                }

                composable(NavRoutes.MMSE_RESULTS.route) { backStackEntry ->
                    val score = backStackEntry.arguments?.getString("score")?.toFloatOrNull() ?: 0f
                    val riskLabel = backStackEntry.arguments?.getString("riskLabel")
                    val probability = backStackEntry.arguments?.getString("probability")?.toFloatOrNull()
                    
                    MmseResultScreen(
                        score = score,
                        rawMaxScore = 20,
                        maxScore = 30,
                        mlRiskLabel = riskLabel,
                        mlProbability = probability,
                        onNavigateToHome = {
                            navController.navigate(NavRoutes.HOME.route) {
                                popUpTo(NavRoutes.HOME.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable("mmse_questions/{userId}/{assessmentId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: "unknown_user"
                    val assessmentId = backStackEntry.arguments?.getString("assessmentId") ?: ""
                    MmseQuestionsScreen(
                        userId = userId,
                        assessmentId = assessmentId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onComplete = { score ->
                            // ✅ Hit finalize endpoint when assessment completes
                            scope.launch {
                                val result = mmseApi.finalizeMmse(assessmentId, userId)
                                if (result.isSuccess) {
                                    val finalizeData = result.getOrNull()
                                    // Use server's total score if available, fallback to local score
                                    val finalScore = finalizeData?.total_score ?: score
                                    val riskLabel = finalizeData?.ml_risk_label ?: ""
                                    val probability = finalizeData?.avg_ml_probability ?: 0f
                                    
                                    navController.navigate("mmse_results/$finalScore?riskLabel=$riskLabel&probability=$probability")
                                } else {
                                    println("Finalize MMSE error: ${result.exceptionOrNull()?.message}")
                                    // Navigate anyway so the user sees results
                                    navController.navigate("mmse_results/$score")
                                }
                            }
                        }
                    )
                }

                composable(NavRoutes.REMINDER.route) {
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val userId = authViewModel.currentUser.value?.user_id ?: "unknown_user"
                        ReminderScreen(userId = userId)
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
                composable(NavRoutes.PATIENT_DETAILS.route) {
                    PatientDetailsScreen(
                        viewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.PROFILE.route) {
                    // Profile screen takes full height (no bottom padding)
                    ProfileScreen(
                        authViewModel = authViewModel,
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