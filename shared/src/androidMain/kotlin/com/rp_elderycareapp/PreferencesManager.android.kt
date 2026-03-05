package com.rp_elderycareapp

import android.content.Context
import android.content.SharedPreferences

actual class PreferencesManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )

    actual fun saveBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url).apply()
    }

    actual fun getBaseUrl(): String? {
        return prefs.getString(KEY_BASE_URL, null)
    }

    actual fun clearBaseUrl() {
        prefs.edit().remove(KEY_BASE_URL).apply()
    }

    actual fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    actual fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    actual fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    actual fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    actual fun saveUserProfile(profileJson: String) {
        prefs.edit().putString(KEY_USER_PROFILE, profileJson).apply()
    }

    actual fun getUserProfile(): String? {
        return prefs.getString(KEY_USER_PROFILE, null)
    }

    actual fun clearAuthData() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_PROFILE)
            .apply()
    }

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_PROFILE = "user_profile"
    }
}
