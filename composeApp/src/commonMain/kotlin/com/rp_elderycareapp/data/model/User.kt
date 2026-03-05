package com.rp_elderycareapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
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
    val role: String = "user",
    val account_status: String = "active",
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class UserRegisterRequest(
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
    val medical_conditions: List<String>? = emptyList(),
    val caregiver_id: String? = null
)

@Serializable
data class UserLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserLoginResponse(
    val success: Boolean,
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val user: User
)

@Serializable
data class UserRegisterResponse(
    val success: Boolean,
    val message: String,
    val user: User
)

@Serializable
data class UserProfileResponse(
    val success: Boolean,
    val user: User
)

@Serializable
data class UserUpdateRequest(
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

@Serializable
data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String,
    val confirm_new_password: String
)

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String? = null
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
