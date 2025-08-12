package com.rp_elderycareapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform