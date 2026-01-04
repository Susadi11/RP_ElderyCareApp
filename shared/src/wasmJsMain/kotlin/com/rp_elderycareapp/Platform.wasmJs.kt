package com.rp_elderycareapp

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

// Global preferences manager instance for Wasm
private val preferencesManagerInstance = PreferencesManager()

actual fun getApiBaseUrl(): String {
    val customUrl = preferencesManagerInstance.getBaseUrl()
    return customUrl ?: ApiConfig.BASE_URL_LOCALHOST
}