package com.example.mydemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform