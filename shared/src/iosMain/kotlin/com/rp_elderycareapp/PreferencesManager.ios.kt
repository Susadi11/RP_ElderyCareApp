package com.rp_elderycareapp

import platform.Foundation.NSUserDefaults

actual class PreferencesManager {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun saveBaseUrl(url: String) {
        userDefaults.setObject(url, KEY_BASE_URL)
        userDefaults.synchronize()
    }

    actual fun getBaseUrl(): String? {
        return userDefaults.stringForKey(KEY_BASE_URL)
    }

    actual fun clearBaseUrl() {
        userDefaults.removeObjectForKey(KEY_BASE_URL)
        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_BASE_URL = "base_url"
    }
}
