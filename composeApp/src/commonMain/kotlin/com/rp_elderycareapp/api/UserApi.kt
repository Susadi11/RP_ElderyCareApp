package com.rp_elderycareapp.api

import com.rp_elderycareapp.getApiBaseUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

    private suspend inline fun <reified T> handleResponse(response: HttpResponse): T {
        if (!response.status.isSuccess()) {
            val errorBody = try { response.bodyAsText() } catch (_: Exception) { "" }
            throw Exception("Server error ${response.status.value}: $errorBody")
        }
        return response.body()
    }

    /**
     * Register a new user account
     */
    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val url = "$baseUrl/api/user/register"
            println("UserApi: Registering user at: $url")

            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val response: RegisterResponse = handleResponse(httpResponse)

            println("UserApi: Registration successful for ${response.user.email}")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Registration failed: ${e::class.simpleName} - ${e.message}")
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

            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val response: LoginResponse = handleResponse(httpResponse)

            println("UserApi: Login successful for ${response.user.email}")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Login failed: ${e::class.simpleName} - ${e.message}")
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

            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GoogleLoginRequest(id_token = idToken))
            }
            val response: LoginResponse = handleResponse(httpResponse)

            println("UserApi: Google login successful for ${response.user.email}")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Google login failed: ${e::class.simpleName} - ${e.message}")
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

            val httpResponse = httpClient.get(url) {
                header("Authorization", "Bearer $token")
            }
            val response: ProfileResponse = handleResponse(httpResponse)

            println("UserApi: Profile retrieved for ${response.user.email}")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Get profile failed: ${e::class.simpleName} - ${e.message}")
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

            val httpResponse = httpClient.put(url) {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(updates)
            }
            val response: ProfileResponse = handleResponse(httpResponse)

            println("UserApi: Profile updated for ${response.user.email}")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Update profile failed: ${e::class.simpleName} - ${e.message}")
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

            val request = ForgotPasswordRequest(email = email)

            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val response: GenericResponse = handleResponse(httpResponse)

            println("UserApi: Password reset code sent for $email")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Forgot password failed: ${e::class.simpleName} - ${e.message}")
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

            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val response: GenericResponse = handleResponse(httpResponse)

            println("UserApi: Password reset successful")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Reset password failed: ${e::class.simpleName} - ${e.message}")
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

            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val response: RefreshTokenResponse = handleResponse(httpResponse)

            println("UserApi: Token refreshed successfully")
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Token refresh failed: ${e::class.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }

    // ===== Profile Photo =====

    suspend fun uploadProfilePhoto(token: String, photoBase64: String, contentType: String = "image/jpeg"): Result<GenericResponse> {
        return try {
            val httpResponse = httpClient.put("${baseUrl}/api/user/profile-photo") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(ProfilePhotoUploadRequest(photoBase64, contentType))
            }
            val response: GenericResponse = handleResponse(httpResponse)
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Upload photo failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun getProfilePhotoUrl(userId: String): String {
        return "${baseUrl}/api/user/profile-photo/$userId"
    }

    // ===== Medical Records =====

    suspend fun updateMedicalRecords(token: String, records: MedicalRecordsUpdateRequest): Result<GenericResponse> {
        return try {
            val httpResponse = httpClient.put("${baseUrl}/api/user/medical-records") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(records)
            }
            val response: GenericResponse = handleResponse(httpResponse)
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Update medical records failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getMedicalRecords(token: String): Result<MedicalRecordsResponse> {
        return try {
            val httpResponse = httpClient.get("${baseUrl}/api/user/medical-records") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
            }
            val response: MedicalRecordsResponse = handleResponse(httpResponse)
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Get medical records failed: ${e.message}")
            Result.failure(e)
        }
    }

    // ===== Profile Completion =====

    suspend fun getProfileCompletion(token: String): Result<ProfileCompletionResponse> {
        return try {
            val httpResponse = httpClient.get("${baseUrl}/api/user/profile-completion") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
            }
            val response: ProfileCompletionResponse = handleResponse(httpResponse)
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Get profile completion failed: ${e.message}")
            Result.failure(e)
        }
    }

    // ===== Caregiver Lookup & Linking =====

    suspend fun lookupCaregiver(caregiverId: String): Result<CaregiverLookupResponse> {
        return try {
            val httpResponse = httpClient.get("${baseUrl}/api/caregiver/lookup/$caregiverId") {
                contentType(ContentType.Application.Json)
            }
            val response: CaregiverLookupResponse = handleResponse(httpResponse)
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Lookup caregiver failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun linkToCaregiver(token: String, caregiverId: String): Result<LinkCaregiverResponse> {
        return try {
            val httpResponse = httpClient.post("${baseUrl}/api/user/link-caregiver") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(LinkCaregiverRequest(caregiverId))
            }
            val response: LinkCaregiverResponse = handleResponse(httpResponse)
            Result.success(response)
        } catch (e: Exception) {
            println("UserApi: Link caregiver failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}
