package com.rp_elderycareapp

import kotlinx.browser.localStorage

actual class PreferencesManager {
    actual fun saveBaseUrl(url: String) {
        localStorage.setItem(KEY_BASE_URL, url)
    }

    actual fun getBaseUrl(): String? {
        return localStorage.getItem(KEY_BASE_URL)
    }

    actual fun clearBaseUrl() {
        localStorage.removeItem(KEY_BASE_URL)
    }

    companion object {
        private const val KEY_BASE_URL = "base_url"
    }
}
