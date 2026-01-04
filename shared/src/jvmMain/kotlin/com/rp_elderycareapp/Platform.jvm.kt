package com.rp_elderycareapp

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

// Global preferences manager instance for JVM
private val preferencesManagerInstance = PreferencesManager()

actual fun getApiBaseUrl(): String {
    val customUrl = preferencesManagerInstance.getBaseUrl()
    return customUrl ?: ApiConfig.BASE_URL_LOCALHOST
}