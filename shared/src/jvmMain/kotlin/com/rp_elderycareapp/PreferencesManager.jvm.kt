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

    companion object {
        private const val KEY_BASE_URL = "base_url"
    }
}
