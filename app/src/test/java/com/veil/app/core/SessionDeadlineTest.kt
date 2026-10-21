package com.veil.app.core

import org.junit.Assert.*
import org.junit.Test

class SessionDeadlineTest {
    @Test fun processRecreationRetainsMonotonicDeadline() {
        val restored = SessionDeadline(10_000, 7)
        assertEquals(6_000L, restored.remaining(4_000, 7))
    }
    @Test fun rebootNeverExtendsCommitment() { assertEquals(0L, SessionDeadline(10_000, 7).remaining(100, 8)) }
    @Test fun expiredDeadlineNeverNegative() { assertEquals(0L, SessionDeadline(10_000, 7).remaining(20_000, 7)) }
    @Test fun invalidScoresBreakTemporalChain() {
        val detector = TemporalDetectionEvaluator()
        assertFalse(detector.observe(.99f))
        assertFalse(detector.observe(Float.NaN))
        assertFalse(detector.observe(.99f))
        assertTrue(detector.observe(.99f))
    }
    @Test fun malformedThresholdRejected() {
        assertThrows(IllegalArgumentException::class.java) { TemporalDetectionEvaluator(Float.NaN) }
    }
}
