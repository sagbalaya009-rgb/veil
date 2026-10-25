package com.veil.app

import com.veil.app.services.MonitoringRuntime
import com.veil.app.services.VeilAccessibilityService
import org.junit.Assert.assertEquals
import org.junit.Test

class AccessibilityInterruptTest {
    @Test fun windowTransitionsAreNotCoalescedAndContentAccessRemainsDisabled() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(android.view.accessibility.AccessibilityManager::class.java)
        val service = manager.installedAccessibilityServiceList.single {
            it.resolveInfo.serviceInfo.packageName == context.packageName
        }
        assertEquals("Keep every package transition rather than merging by event type", 0L, service.notificationTimeout)
        assertEquals(0, service.capabilities and android.accessibilityservice.AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT)
        assertEquals(android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, service.eventTypes)
    }

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
