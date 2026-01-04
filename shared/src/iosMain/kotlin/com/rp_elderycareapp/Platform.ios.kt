package com.rp_elderycareapp

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

// Global preferences manager instance for iOS
private val preferencesManagerInstance = PreferencesManager()

actual fun getApiBaseUrl(): String {
    val customUrl = preferencesManagerInstance.getBaseUrl()
    return customUrl ?: ApiConfig.BASE_URL_LOCALHOST
}