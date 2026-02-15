package com.rp_elderycareapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.rp_elderycareapp.PreferencesManager
import com.rp_elderycareapp.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class AuthViewModel(private val preferencesManager: PreferencesManager) {
    private val userApi = UserApi()
    private val json = Json { ignoreUnknownKeys = true }
    
    // UI State
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val successMessage = mutableStateOf<String?>(null)
    val isAuthenticated = mutableStateOf(false)
    val currentUser = mutableStateOf<UserProfile?>(null)

    init {
        // Check if user is already logged in
        loadUserFromStorage()
    }

    /**
     * Load user profile from local storage
     */
    private fun loadUserFromStorage() {
        try {
            val profileJson = preferencesManager.getUserProfile()
            val token = preferencesManager.getAccessToken()
            
            if (profileJson != null && token != null) {
                currentUser.value = json.decodeFromString<UserProfile>(profileJson)
                isAuthenticated.value = true
                println("AuthViewModel: User loaded from storage: ${currentUser.value?.email}")
            }
        } catch (e: Exception) {
            println("AuthViewModel: Failed to load user from storage: ${e.message}")
        }
    }

    /**
     * Register new user
     */
    suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String?,
        age: Int?,
        password: String,
        confirmPassword: String
    ) {
        withContext(Dispatchers.Main) {
            isLoading.value = true
            errorMessage.value = null
            successMessage.value = null
        }

        try {
            val request = RegisterRequest(
                full_name = fullName,
                email = email,
                phone_number = phoneNumber,
                age = age,
                password = password,
                confirm_password = confirmPassword
            )

            val result = withContext(Dispatchers.Default) {
                userApi.register(request)
            }

            withContext(Dispatchers.Main) {
                result.onSuccess { response ->
                    currentUser.value = response.user
                    successMessage.value = "Registration successful! Please login."
                    println("AuthViewModel: Registration successful for ${response.user.email}")
                }.onFailure { error ->
                    errorMessage.value = parseErrorMessage(error)
                    println("AuthViewModel: Registration failed: ${error.message}")
                }
                isLoading.value = false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage.value = "Registration failed: ${e.message}"
                isLoading.value = false
            }
        }
    }

    /**
     * Login user
     */
    suspend fun login(email: String, password: String) {
        withContext(Dispatchers.Main) {
            isLoading.value = true
            errorMessage.value = null
            successMessage.value = null
        }

        try {
            val request = LoginRequest(email = email, password = password)

            val result = withContext(Dispatchers.Default) {
                userApi.login(request)
            }

            withContext(Dispatchers.Main) {
                result.onSuccess { response ->
                    // Save tokens
                    preferencesManager.saveAccessToken(response.access_token)
                    preferencesManager.saveRefreshToken(response.refresh_token)
                    
                    // Save user profile
                    val profileJson = json.encodeToString(response.user)
                    preferencesManager.saveUserProfile(profileJson)
                    
                    // Update state
                    currentUser.value = response.user
                    isAuthenticated.value = true
                    successMessage.value = "Login successful!"
                    
                    println("AuthViewModel: Login successful for ${response.user.email}")
                }.onFailure { error ->
                    errorMessage.value = parseErrorMessage(error)
                    println("AuthViewModel: Login failed: ${error.message}")
                }
                isLoading.value = false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage.value = "Login failed: ${e.message}"
                isLoading.value = false
            }
        }
    }

    /**
     * Login or register via Google Sign-In
     */
    suspend fun googleSignIn(idToken: String) {
        withContext(Dispatchers.Main) {
            isLoading.value = true
            errorMessage.value = null
            successMessage.value = null
        }

        try {
            val result = withContext(Dispatchers.Default) {
                userApi.googleLogin(idToken)
            }

            withContext(Dispatchers.Main) {
                result.onSuccess { response ->
                    preferencesManager.saveAccessToken(response.access_token)
                    preferencesManager.saveRefreshToken(response.refresh_token)

                    val profileJson = json.encodeToString(response.user)
                    preferencesManager.saveUserProfile(profileJson)

                    currentUser.value = response.user
                    isAuthenticated.value = true
                    successMessage.value = "Login successful!"

                    println("AuthViewModel: Google sign-in successful for ${response.user.email}")
                }.onFailure { error ->
                    errorMessage.value = parseErrorMessage(error)
                    println("AuthViewModel: Google sign-in failed: ${error.message}")
                }
                isLoading.value = false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage.value = "Google sign-in failed: ${e.message}"
                isLoading.value = false
            }
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        preferencesManager.clearAuthData()
        currentUser.value = null
        isAuthenticated.value = false
        successMessage.value = "Logged out successfully"
        println("AuthViewModel: User logged out")
    }

    /**
     * Load user profile from backend
     */
    suspend fun loadUserProfile() {
        val token = preferencesManager.getAccessToken() ?: return

        withContext(Dispatchers.Main) {
            isLoading.value = true
            errorMessage.value = null
        }

        try {
            val result = withContext(Dispatchers.Default) {
                userApi.getProfile(token)
            }

            withContext(Dispatchers.Main) {
                result.onSuccess { response ->
                    currentUser.value = response.user
                    
                    // Update stored profile
                    val profileJson = json.encodeToString(response.user)
                    preferencesManager.saveUserProfile(profileJson)
                    
                    println("AuthViewModel: Profile loaded for ${response.user.email}")
                }.onFailure { error ->
                    errorMessage.value = "Failed to load profile: ${parseErrorMessage(error)}"
                    println("AuthViewModel: Load profile failed: ${error.message}")
                }
                isLoading.value = false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage.value = "Failed to load profile: ${e.message}"
                isLoading.value = false
            }
        }
    }

    /**
     * Request password reset code
     */
    suspend fun forgotPassword(email: String) {
        withContext(Dispatchers.Main) {
            isLoading.value = true
            errorMessage.value = null
            successMessage.value = null
        }

        try {
            val result = withContext(Dispatchers.Default) {
                userApi.forgotPassword(email)
            }

            withContext(Dispatchers.Main) {
                result.onSuccess { response ->
                    successMessage.value = "Reset code sent to your email. Code: ${response.reset_code ?: "Check your email"}"
                    println("AuthViewModel: Reset code generated for $email")
                }.onFailure { error ->
                    errorMessage.value = parseErrorMessage(error)
                    println("AuthViewModel: Forgot password failed: ${error.message}")
                }
                isLoading.value = false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage.value = "Failed to request reset code: ${e.message}"
                isLoading.value = false
            }
        }
    }

    /**
     * Reset password with code
     */
    suspend fun resetPassword(
        email: String,
        resetCode: String,
        newPassword: String,
        confirmPassword: String
    ) {
        withContext(Dispatchers.Main) {
            isLoading.value = true
            errorMessage.value = null
            successMessage.value = null
        }

        try {
            val request = ResetPasswordRequest(
                email = email,
                reset_code = resetCode,
                new_password = newPassword,
                confirm_password = confirmPassword
            )

            val result = withContext(Dispatchers.Default) {
                userApi.resetPassword(request)
            }

            withContext(Dispatchers.Main) {
                result.onSuccess { response ->
                    successMessage.value = "Password reset successful! Please login."
                    println("AuthViewModel: Password reset successful for $email")
                }.onFailure { error ->
                    errorMessage.value = parseErrorMessage(error)
                    println("AuthViewModel: Reset password failed: ${error.message}")
                }
                isLoading.value = false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage.value = "Failed to reset password: ${e.message}"
                isLoading.value = false
            }
        }
    }

    /**
     * Parse error message from exception
     */
    private fun parseErrorMessage(error: Throwable): String {
        val message = error.message ?: "Unknown error occurred"
        
        // Try to extract meaningful error from API response
        return when {
            message.contains("404") -> "User not found"
            message.contains("401") -> "Invalid credentials"
            message.contains("400") -> message.substringAfter("detail=").substringBefore(",").trim('"')
            else -> message
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        successMessage.value = null
    }
}
