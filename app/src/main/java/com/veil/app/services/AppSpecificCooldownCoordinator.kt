/**
 * App-specific protection boundary.
 *
 * Accessibility adapters pass only the foreground package identifier to this coordinator. It
 * returns whether an overlay must capture input for that app; it does not read, log, or store
 * accessibility text, window titles, screenshots, or browsing history.
 */
package com.veil.app.services

import com.veil.app.core.ProtectionEngine

sealed interface ForegroundAppDecision {
    data class Protect(val packageName: String) : ForegroundAppDecision
    data object Allow : ForegroundAppDecision
}

class AppSpecificCooldownCoordinator(
    private val protectionEngine: ProtectionEngine,
) {
    fun onForegroundApplicationChanged(packageName: String): ForegroundAppDecision =
        if (protectionEngine.shouldBlockForegroundApp(packageName)) {
            ForegroundAppDecision.Protect(packageName)
        } else {
            ForegroundAppDecision.Allow
        }
}
