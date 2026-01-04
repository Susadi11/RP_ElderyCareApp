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

    companion object {
        private const val KEY_BASE_URL = "base_url"
    }
}
