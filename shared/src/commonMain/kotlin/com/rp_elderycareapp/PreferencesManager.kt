package com.rp_elderycareapp

expect class PreferencesManager {
    fun saveBaseUrl(url: String)
    fun getBaseUrl(): String?
    fun clearBaseUrl()
    
    // Authentication token storage
    fun saveAccessToken(token: String)
    fun getAccessToken(): String?
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    
    // User profile storage
    fun saveUserProfile(profileJson: String)
    fun getUserProfile(): String?
    
    // Clear all auth data
    fun clearAuthData()
}
