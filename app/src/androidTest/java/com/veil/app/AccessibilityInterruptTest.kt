package com.veil.app

import com.veil.app.services.MonitoringRuntime
import com.veil.app.services.VeilAccessibilityService
import org.junit.Assert.assertEquals
import org.junit.Test

class AccessibilityInterruptTest {
    @Test fun feedbackInterruptDoesNotEraseForegroundObservation() {
        val previous = MonitoringRuntime.foregroundPackage.value
        val reason = MonitoringRuntime.foregroundReason
        try {
            MonitoringRuntime.updateForegroundPackage("com.veil.fixture")
            val observation = MonitoringRuntime.foregroundObservation()
            VeilAccessibilityService().onInterrupt()
            assertEquals(observation, MonitoringRuntime.foregroundObservation())
        } finally { MonitoringRuntime.updateForegroundPackage(previous, reason) }
    }
}
