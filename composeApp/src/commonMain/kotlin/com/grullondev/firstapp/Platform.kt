package com.grullondev.firstapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform