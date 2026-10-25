package com.veil.app.core.recovery

import org.junit.Assert.*
import org.junit.Test

class RecoveryRulesTest {
    private val now = 40L * 24 * 60 * 60 * 1000

    @Test fun setbackDoesNotEraseGoalsOrCompletedActivities() {
        val baseline = RecoveryState(goals = setOf(RecoveryGoal.IMPROVE_FOCUS), planAction = "Read one page")
        val completed = RecoveryRules.add(baseline, RecoveryRecord("activity", now - 5, RecoveryRecordKind.ACTIVITY,
            activity = RecoveryActivity.GROUNDING, helpful = true), now)
        val next = RecoveryRules.add(completed, RecoveryRecord("setback", now, RecoveryRecordKind.SETBACK), now)
        assertEquals(1, next.completedActivities)
        assertEquals(1, next.helpfulActivities)
        assertEquals(baseline.goals, next.goals)
        assertEquals("Read one page", next.planAction)
    }

    @Test fun retentionDropsExpiredFutureDuplicateAndOldestRows() {
        val rows = (0..109).map { RecoveryRecord("$it", now - it, RecoveryRecordKind.SETBACK) } + listOf(
            RecoveryRecord("expired", now - RecoveryRules.RETENTION_MILLIS - 1, RecoveryRecordKind.SETBACK),
            RecoveryRecord("future", now + 1, RecoveryRecordKind.SETBACK),
            RecoveryRecord("0", now, RecoveryRecordKind.SETBACK),
        )
        val retained = RecoveryRules.retain(rows, now)
        assertEquals(100, retained.size)
        assertEquals("0", retained.first().id)
        assertEquals("99", retained.last().id)
        assertEquals(100, retained.map { it.id }.distinct().size)
    }

    @Test fun skippedEncouragementAndFaithHaveNoInferredReligiousText() {
        assertNull(RecoveryState(encouragement = EncouragementMode.NONE).encouragementText())
        assertNull(RecoveryState(encouragement = EncouragementMode.FAITH).encouragementText())
        assertEquals("My own reminder", RecoveryState(encouragement = EncouragementMode.FAITH,
            customReminder = "My own reminder").encouragementText())
        assertTrue(RecoveryState().goals.isEmpty())
    }

    @Test fun checkInAllowsSkippedFieldsAndKeepsSelfReportSeparate() {
        val state = RecoveryRules.add(RecoveryState(), RecoveryRecord("check", now, RecoveryRecordKind.CHECK_IN,
            intensity = 0), now)
        assertEquals(0, state.checkIns.single().intensity)
        assertNull(state.checkIns.single().trigger)
        assertEquals(0, state.completedActivities)
    }

    @Test fun rejectsInvalidIntensityAndActivityShape() {
        assertThrows(IllegalArgumentException::class.java) {
            RecoveryRules.add(RecoveryState(), RecoveryRecord("bad", now, RecoveryRecordKind.CHECK_IN, intensity = 11), now)
        }
        assertThrows(IllegalArgumentException::class.java) {
            RecoveryRules.add(RecoveryState(), RecoveryRecord("bad", now, RecoveryRecordKind.SETBACK, helpful = true), now)
        }
        assertThrows(IllegalArgumentException::class.java) {
            RecoveryRules.add(RecoveryState(), RecoveryRecord("bad", now, RecoveryRecordKind.ACTIVITY), now)
        }
    }
}
