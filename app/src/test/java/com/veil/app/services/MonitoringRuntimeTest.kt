package com.veil.app.services

import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class MonitoringRuntimeTest {
    @After fun reset() { MonitoringRuntime.updateRunning(false); MonitoringRuntime.modelReady(false) }
    @Test fun terminationClosesGateAndInvalidatesSessionReadinessImmediately() {
        MonitoringRuntime.updateRunning(true)
        MonitoringRuntime.modelReady(true)
        MonitoringRuntime.captureState(MonitoringRuntime.CaptureState.ANALYZING)
        assertTrue(MonitoringRuntime.isRunning.value)
        assertTrue(MonitoringRuntime.health.value.inferenceReady)
        assertFalse(MonitoringRuntime.health.value.resumeRequired)
        MonitoringRuntime.updateRunning(false)
        assertFalse(MonitoringRuntime.isRunning.value)
        assertFalse(MonitoringRuntime.health.value.inferenceReady)
        assertTrue(MonitoringRuntime.health.value.resumeRequired)
    }
    @Test fun startingIsNotAnAuthorizedActiveCaptureState() {
        MonitoringRuntime.updateRunning(false)
        MonitoringRuntime.updateRunning(true)
        assertEquals(MonitoringRuntime.CaptureState.STARTING, MonitoringRuntime.health.value.captureState)
        assertTrue(MonitoringRuntime.health.value.resumeRequired)
        assertFalse(MonitoringRuntime.health.value.inferenceReady)
    }
    @Test fun returnToSameAppStillInvalidatesPendingObservation() {
        MonitoringRuntime.updateForegroundPackage("synthetic.a")
        val before = MonitoringRuntime.foregroundObservation()
        MonitoringRuntime.updateForegroundPackage("synthetic.b")
        MonitoringRuntime.updateForegroundPackage("synthetic.a")
        val after = MonitoringRuntime.foregroundObservation()
        assertEquals(before.packageName, after.packageName)
        assertNotEquals(before.revision, after.revision)
        MonitoringRuntime.updateForegroundPackage(null)
    }
}
