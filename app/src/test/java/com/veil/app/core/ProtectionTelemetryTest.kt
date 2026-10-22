package com.veil.app.core

import org.junit.Assert.*
import org.junit.Test

class ProtectionTelemetryTest {
    @Test fun countersAreAggregateAndSnapshotIsDetached() {
        val telemetry = ProtectionTelemetry()
        telemetry.event(ProtectionTelemetry.Event.TASK_REMOVED)
        telemetry.inference(2_000_000)
        telemetry.inference(4_000_000)
        val snapshot = telemetry.snapshot()
        assertEquals(2L, snapshot.inferences)
        assertEquals(3.0, snapshot.meanInferenceMillis, 0.001)
        assertEquals(4_000_000L, snapshot.maxInferenceNanos)
        telemetry.event(ProtectionTelemetry.Event.TASK_REMOVED)
        assertEquals(1L, snapshot.events[ProtectionTelemetry.Event.TASK_REMOVED])
        assertEquals(2L, telemetry.snapshot().events[ProtectionTelemetry.Event.TASK_REMOVED])
    }
    @Test fun clearErasesAllTechnicalCounters() {
        val telemetry = ProtectionTelemetry()
        telemetry.event(ProtectionTelemetry.Event.START_REQUESTED)
        telemetry.inference(100)
        telemetry.policyTrigger()
        telemetry.clear()
        val cleared = telemetry.snapshot()
        assertTrue(cleared.events.isEmpty())
        assertEquals(0L, cleared.inferences)
        assertEquals(0L, cleared.policyTriggers)
        assertEquals(0.0, cleared.meanInferenceMillis, 0.0)
    }
    @Test(expected = IllegalArgumentException::class) fun negativeTimingIsRejected() {
        ProtectionTelemetry().inference(-1)
    }
}
