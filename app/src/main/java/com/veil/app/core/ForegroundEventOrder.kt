package com.veil.app.core

/** Window-pane disappearance/title changes do not mean that their app became foreground. */
class ForegroundEventOrder {
    private var newestUptime = Long.MIN_VALUE
    fun accept(uptime: Long, paneDisappeared: Boolean, titleOnly: Boolean): Boolean {
        if (paneDisappeared || titleOnly || uptime < newestUptime) return false
        newestUptime = uptime
        return true
    }
}
