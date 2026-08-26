package com.veil.app.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Receives only the window lifecycle events needed by the future protection adapter.
 * Raw events and text are intentionally ignored and never logged.
 */
class VeilAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Foreground detection will be added behind a tested adapter. Do not read or log content.
    }

    override fun onInterrupt() = Unit
}

/**
 * Reboot entry point. The future implementation will verify persisted settings and permissions
 * before restarting protection; it must never silently enable monitoring.
 */
class VeilBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        // Intentionally no automatic start until persisted settings and user-granted permissions
        // are verified by the recovery coordinator.
    }
}
