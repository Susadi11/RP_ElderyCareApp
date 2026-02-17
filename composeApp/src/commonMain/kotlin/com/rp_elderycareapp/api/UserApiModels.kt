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
    val user_id: String = "",
    val full_name: String = "",
    val email: String = "",
    val phone_number: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val address: String? = null,
    val emergency_contact_name: String? = null,
    val emergency_contact_number: String? = null,
    val profile_photo: String? = null,
    val has_profile_photo: Boolean = false,
    val medical_conditions: List<String>? = null,
    val allergies: List<String>? = null,
    val special_treatments: List<String>? = null,
    val medicines: List<String>? = null,
    val medical_history: String? = null,
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

// ===== Medical Records =====

@Serializable
data class MedicalRecordsUpdateRequest(
    val allergies: List<String>? = null,
    val special_treatments: List<String>? = null,
    val medicines: List<String>? = null,
    val medical_history: String? = null,
    val medical_conditions: List<String>? = null
)

@Serializable
data class MedicalRecords(
    val allergies: List<String> = emptyList(),
    val special_treatments: List<String> = emptyList(),
    val medicines: List<String> = emptyList(),
    val medical_history: String = "",
    val medical_conditions: List<String> = emptyList()
)

@Serializable
data class MedicalRecordsResponse(
    val success: Boolean,
    val records: MedicalRecords? = null
)

// ===== Profile Photo =====

@Serializable
data class ProfilePhotoUploadRequest(
    val photo_base64: String,
    val content_type: String = "image/jpeg"
)

// ===== Caregiver Lookup =====

@Serializable
data class CaregiverLookupInfo(
    val caregiver_id: String = "",
    val full_name: String = "",
    val mobile_number: String = "",
    val has_profile_photo: Boolean = false
)

@Serializable
data class CaregiverLookupResponse(
    val success: Boolean,
    val caregiver: CaregiverLookupInfo? = null
)

// ===== Profile Completion =====

@Serializable
data class ProfileCompletionData(
    val percentage: Int = 0,
    val completed_fields: Int = 0,
    val total_fields: Int = 0,
    val missing_fields: List<String> = emptyList()
)

@Serializable
data class ProfileCompletionResponse(
    val success: Boolean,
    val completion: ProfileCompletionData? = null
)

// ===== Link Caregiver =====

@Serializable
data class LinkCaregiverRequest(
    val caregiver_id: String
)

@Serializable
data class LinkCaregiverResponse(
    val success: Boolean,
    val message: String = "",
    val user: UserProfile? = null
)
