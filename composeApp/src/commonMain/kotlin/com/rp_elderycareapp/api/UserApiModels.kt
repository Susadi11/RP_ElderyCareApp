package com.rp_elderycareapp.api

import kotlinx.serialization.Serializable

// ===== REQUEST MODELS =====

@Serializable
data class RegisterRequest(
    val full_name: String,
    val email: String,
    val phone_number: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val address: String? = null,
    val emergency_contact_name: String? = null,
    val emergency_contact_number: String? = null,
    val password: String,
    val confirm_password: String,
    val profile_photo: String? = null,
    val medical_conditions: List<String>? = null,
    val caregiver_id: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class GoogleLoginRequest(
    val id_token: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val reset_code: String,
    val new_password: String,
    val confirm_password: String
)

@Serializable
data class ProfileUpdateRequest(
    val full_name: String? = null,
    val phone_number: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val address: String? = null,
    val profile_photo: String? = null,
    val emergency_contact_name: String? = null,
    val emergency_contact_number: String? = null,
    val medical_conditions: List<String>? = null,
    val caregiver_id: String? = null
)

// ===== RESPONSE MODELS =====

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: UserProfile
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val user: UserProfile
)

@Serializable
data class UserProfile(
    val user_id: String,
    val full_name: String,
    val email: String,
    val phone_number: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val address: String? = null,
    val emergency_contact_name: String? = null,
    val emergency_contact_number: String? = null,
    val profile_photo: String? = null,
    val medical_conditions: List<String>? = null,
    val caregiver_id: String? = null,
    val role: String? = null,
    val account_status: String? = null
)

@Serializable
data class ProfileResponse(
    val success: Boolean,
    val user: UserProfile
)

@Serializable
data class GenericResponse(
    val success: Boolean,
    val message: String,
    val email: String? = null,
    val reset_code: String? = null  // For testing only, will be removed in production
)

@Serializable
data class RefreshTokenRequest(
    val refresh_token: String
)

@Serializable
data class RefreshTokenResponse(
    val success: Boolean,
    val access_token: String,
    val token_type: String
)
