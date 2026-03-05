package com.rp_elderycareapp

import java.util.prefs.Preferences

actual class PreferencesManager {
    private val prefs: Preferences = Preferences.userRoot().node("com.rp_elderycareapp")

    actual fun saveBaseUrl(url: String) {
        prefs.put(KEY_BASE_URL, url)
        prefs.flush()
    }

    actual fun getBaseUrl(): String? {
        return prefs.get(KEY_BASE_URL, null)
    }

    actual fun clearBaseUrl() {
        prefs.remove(KEY_BASE_URL)
        prefs.flush()
    }

    actual fun saveAccessToken(token: String) {
        prefs.put(KEY_ACCESS_TOKEN, token)
        prefs.flush()
    }

    actual fun getAccessToken(): String? {
        return prefs.get(KEY_ACCESS_TOKEN, null)
    }

    actual fun saveRefreshToken(token: String) {
        prefs.put(KEY_REFRESH_TOKEN, token)
        prefs.flush()
    }

    actual fun getRefreshToken(): String? {
        return prefs.get(KEY_REFRESH_TOKEN, null)
    }

    actual fun saveUserProfile(profileJson: String) {
        prefs.put(KEY_USER_PROFILE, profileJson)
        prefs.flush()
    }

    actual fun getUserProfile(): String? {
        return prefs.get(KEY_USER_PROFILE, null)
    }

    actual fun clearAuthData() {
        prefs.remove(KEY_ACCESS_TOKEN)
        prefs.remove(KEY_REFRESH_TOKEN)
        prefs.remove(KEY_USER_PROFILE)
        prefs.flush()
    }

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_PROFILE = "user_profile"
    }
}
