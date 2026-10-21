package com.veil.app.core

/** Monotonic deadlines survive process recreation, expire safely across reboot. */
data class SessionDeadline(val endElapsed: Long, val bootCount: Int) {
    fun remaining(nowElapsed: Long, currentBootCount: Int): Long =
        if (currentBootCount != bootCount) 0 else (endElapsed - nowElapsed).coerceAtLeast(0)
}
