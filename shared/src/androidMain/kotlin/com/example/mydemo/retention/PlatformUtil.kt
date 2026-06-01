package com.example.mydemo.retention

actual object PlatformUtil {
    actual fun track(eventId: String, extra: String) {
        println("[Track] $eventId | $extra")
    }

    actual fun navigate(route: String) {
        println("[Navigate] $route")
    }
}
