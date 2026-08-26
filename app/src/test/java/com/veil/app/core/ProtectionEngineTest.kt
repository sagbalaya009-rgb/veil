package com.veil.app.core

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectionEngineTest {
    private val clock = MutableTestClock(Instant.parse("2026-01-01T00:00:00Z"))
    private val settings = ProtectionSettings(
        cooldown = CooldownDuration.FIVE,
        recoveryTask = RecoveryTask.BREATHING,
        lockPeriod = LockPeriod.TWO_HOURS,
    )

    @Test
    fun startsProtectionWithConfiguredLockPeriod() {
        val engine = ProtectionEngine(clock, settings)

        val state = engine.startProtection()

        assertEquals(
            ProtectionState.Active(Instant.parse("2026-01-01T02:00:00Z")),
            state,
        )
        assertEquals(ProtectionEventType.PROTECTION_STARTED, engine.events.single().type)
    }

    @Test
    fun ignoresConfidenceBelowDocumentedThreshold() {
        val engine = ProtectionEngine(clock, settings)
        engine.startProtection()

        val state = engine.registerDetection(0.899f)

        assertTrue(state is ProtectionState.Active)
        assertEquals(1, engine.events.size)
    }

    @Test
    fun highConfidenceDetectionStartsCooldownWithRecoveryTask() {
        val engine = ProtectionEngine(clock, settings)
        engine.startProtection()

        val state = engine.registerDetection(0.95f)

        assertEquals(
            ProtectionState.Cooldown(
                cooldownEndsAt = Instant.parse("2026-01-01T00:05:00Z"),
                recoveryTask = RecoveryTask.BREATHING,
                lockEndsAt = Instant.parse("2026-01-01T02:00:00Z"),
            ),
            state,
        )
        assertEquals(ProtectionEventType.INTERVENTION_TRIGGERED, engine.events.last().type)
    }

    @Test
    fun recoveryCannotCompleteBeforeCooldownEnds() {
        val engine = ProtectionEngine(clock, settings)
        engine.startProtection()
        engine.registerDetection(0.95f)

        assertTrue(engine.completeRecovery() is ProtectionState.Cooldown)

        clock.advance(Duration.ofMinutes(5))
        assertTrue(engine.completeRecovery() is ProtectionState.Active)
    }

    @Test
    fun emergencyOverrideCanBeUsedOnlyOnce() {
        val engine = ProtectionEngine(clock, settings)
        engine.startProtection()
        engine.registerDetection(0.95f)

        assertTrue(engine.useEmergencyOverride())
        assertFalse(engine.useEmergencyOverride())
        assertEquals(ProtectionEventType.EMERGENCY_OVERRIDE, engine.events.last().type)
    }

    private class MutableTestClock(initial: Instant) : Clock() {
        private var current = initial

        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId) = this
        override fun instant() = current

        fun advance(duration: Duration) {
            current = current.plus(duration)
        }
    }
}
