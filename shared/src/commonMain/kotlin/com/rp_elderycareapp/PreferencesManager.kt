package com.rp_elderycareapp

expect class PreferencesManager {
    fun saveBaseUrl(url: String)
    fun getBaseUrl(): String?
    fun clearBaseUrl()
}
