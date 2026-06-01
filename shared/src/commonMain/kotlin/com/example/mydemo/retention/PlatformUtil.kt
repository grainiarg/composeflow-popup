package com.example.mydemo.retention

expect object PlatformUtil {
    fun track(eventId: String, extra: String)
    fun navigate(route: String)
}
