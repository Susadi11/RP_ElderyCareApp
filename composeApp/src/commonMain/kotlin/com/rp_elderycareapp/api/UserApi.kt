package com.rp_elderycareapp.api

import com.rp_elderycareapp.getApiBaseUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class UserApi {
    private val baseUrl = getApiBaseUrl()

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    /**
     * Register a new user account
     */
    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val url = "$baseUrl/api/user/register"
            println("UserApi: Registering user at: $url")
            println("UserApi: Email: ${request.email}, Name: ${request.full_name}")

            val response: RegisterResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            println("UserApi: Registration successful for ${response.user.email}")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Registration failed: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Login user with email and password
     */
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val url = "$baseUrl/api/user/login"
            println("UserApi: Logging in at: $url")
            println("UserApi: Email: ${request.email}")

            val response: LoginResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            println("UserApi: Login successful for ${response.user.email}")
            println("UserApi: Access token received: ${response.access_token.take(20)}...")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Login failed: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Login or register via Google Sign-In
     */
    suspend fun googleLogin(idToken: String): Result<LoginResponse> {
        return try {
            val url = "$baseUrl/api/user/google-login"
            println("UserApi: Google login at: $url")

            val response: LoginResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GoogleLoginRequest(id_token = idToken))
            }.body()

            println("UserApi: Google login successful for ${response.user.email}")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Google login failed: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get current user profile
     */
    suspend fun getProfile(token: String): Result<ProfileResponse> {
        return try {
            val url = "$baseUrl/api/user/profile"
            println("UserApi: Getting profile at: $url")

            val response: ProfileResponse = httpClient.get(url) {
                header("Authorization", "Bearer $token")
            }.body()

            println("UserApi: Profile retrieved for ${response.user.email}")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Get profile failed: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateProfile(
        token: String,
        updates: ProfileUpdateRequest
    ): Result<ProfileResponse> {
        return try {
            val url = "$baseUrl/api/user/profile"
            println("UserApi: Updating profile at: $url")

            val response: ProfileResponse = httpClient.put(url) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(updates)
            }.body()

            println("UserApi: Profile updated for ${response.user.email}")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Update profile failed: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Request password reset code
     */
    suspend fun forgotPassword(email: String): Result<GenericResponse> {
        return try {
            val url = "$baseUrl/api/user/forgot-password"
            println("UserApi: Requesting password reset at: $url")
            println("UserApi: Email: $email")

            val request = ForgotPasswordRequest(email = email)

            val response: GenericResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            println("UserApi: Password reset code sent for $email")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Forgot password failed: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Reset password using reset code
     */
    suspend fun resetPassword(request: ResetPasswordRequest): Result<GenericResponse> {
        return try {
            val url = "$baseUrl/api/user/reset-password"
            println("UserApi: Resetting password at: $url")
            println("UserApi: Email: ${request.email}")

            val response: GenericResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            println("UserApi: Password reset successful")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Reset password failed: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Refresh access token using refresh token
     */
    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponse> {
        return try {
            val url = "$baseUrl/api/user/refresh-token"
            println("UserApi: Refreshing token at: $url")

            val request = RefreshTokenRequest(refresh_token = refreshToken)

            val response: RefreshTokenResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            println("UserApi: Token refreshed successfully")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Token refresh failed: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}
