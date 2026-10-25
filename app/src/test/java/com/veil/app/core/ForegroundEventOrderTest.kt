package com.veil.app.core

import org.junit.Assert.*
import org.junit.Test

class ForegroundEventOrderTest {
    @Test fun disappearingRecentsPaneCannotReplaceTheNewApp() {
        val order = ForegroundEventOrder()
        assertTrue(order.accept(100, false, false)) // foreground fixture/activity
        assertFalse(order.accept(110, true, false)) // old launcher pane removed later
        assertFalse(order.accept(120, false, true)) // title change is not a new app
        assertTrue(order.accept(115, false, false)) // actual Settings transition still accepted
    }
    @Test fun delayedOlderWindowCannotOverwriteNewerObservation() {
        val order = ForegroundEventOrder()
        assertTrue(order.accept(100, false, false))
        assertFalse(order.accept(99, false, false))
        assertTrue(order.accept(101, false, false))
        assertTrue(order.accept(101, false, false)) // timestamps may have millisecond ties
    }
}
