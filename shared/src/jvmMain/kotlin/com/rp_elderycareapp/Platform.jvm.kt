package com.rp_elderycareapp

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getApiBaseUrl(): String = ApiConfig.BASE_URL_LOCALHOST