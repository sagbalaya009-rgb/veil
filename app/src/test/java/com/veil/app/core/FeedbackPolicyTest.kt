package com.veil.app.core

import org.junit.Assert.*
import org.junit.Test

class FeedbackPolicyTest {
    @Test fun retentionRejectsExpiredFutureDuplicateAndExcessRecords() {
        val now = FeedbackPolicy.retentionMs + 1_000
        val valid = FeedbackEntry("one", now, Correction.NOT_EXPLICIT, "model")
        val records = listOf(valid.copy(id = "old", at = 0), valid, valid, valid.copy(id = "future", at = now + 1))
        assertEquals(listOf(valid), FeedbackPolicy.retained(records, now))
        assertEquals(100, FeedbackPolicy.retained((0..200).map { valid.copy(id = "$it") }, now).size)
    }
    @Test fun correctionNeverChangesDetectorPolicy() {
        val detector = TemporalDetectionEvaluator()
        assertFalse(detector.observe(.95f))
        FeedbackPolicy.retained(listOf(FeedbackEntry("one", 1, Correction.NOT_EXPLICIT, "model")), 1)
        assertTrue(detector.observe(.95f))
        assertEquals(.90f, ProtectionSettings.INITIAL_THRESHOLD, 0f)
    }
}
