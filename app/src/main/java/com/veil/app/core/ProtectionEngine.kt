/**
 * Veil protection domain model.
 * Cooldowns are bound to a triggering app package, so platform adapters protect only that app
 * while allowing unrelated applications to remain usable.
 */
package com.veil.app.core

import java.time.Clock
import java.time.Duration
import java.time.Instant

enum class ProtectionLevel { BALANCED, STRICT, NUCLEAR }

enum class CooldownDuration(val minutes: Long) {
    TEN(10),
    FIFTEEN(15),
    TWENTY(20),
    THIRTY(30),
    FORTY_FIVE(45),
    SIXTY(60),
}

enum class RecoveryTask {
    PUSH_UPS,
    BREATHING,
    WALK,
    JOURNAL,
    WATER,
    STRETCH,
    OFFLINE_PAUSE,
}

enum class LockPeriod(val duration: Duration) {
    TWO_HOURS(Duration.ofHours(2)),
    EIGHT_HOURS(Duration.ofHours(8)),
    TWENTY_FOUR_HOURS(Duration.ofHours(24)),
    THREE_DAYS(Duration.ofDays(3)),
    SEVEN_DAYS(Duration.ofDays(7)),
}

/** Package identifier only. Veil does not retain app content, window text, or screenshots. */
data class ProtectedApplication(val packageName: String) {
    init {
        require(packageName.isNotBlank()) { "A triggering app package is required." }
    }
}

sealed interface ProtectionState {
    data object Inactive : ProtectionState
    data class Active(val lockEndsAt: Instant) : ProtectionState
    data class Cooldown(
        val cooldownEndsAt: Instant,
        val recoveryTask: RecoveryTask,
        val lockEndsAt: Instant,
        val protectedApplication: ProtectedApplication,
    ) : ProtectionState
    data class Degraded(val reason: String) : ProtectionState
    data object PermissionRequired : ProtectionState
}

data class ProtectionSettings(
    val level: ProtectionLevel = ProtectionLevel.BALANCED,
    val cooldown: CooldownDuration = CooldownDuration.TEN,
    val recoveryTask: RecoveryTask = RecoveryTask.BREATHING,
    val lockPeriod: LockPeriod = LockPeriod.TWO_HOURS,
    val recoveryNote: String = "",
    val worldview: WorldviewPreference? = null,
    val cooldownCompletionNotificationsEnabled: Boolean = false,
) {
    companion object {
        const val INITIAL_THRESHOLD = 0.90f
    }
}

enum class ProtectionEventType {
    PROTECTION_STARTED,
    INTERVENTION_TRIGGERED,
    RECOVERY_COMPLETED,
    EMERGENCY_OVERRIDE,
}

data class ProtectionEvent(
    val type: ProtectionEventType,
    val at: Instant,
    val confidence: Float? = null,
)

class ProtectionEngine(
    private val clock: Clock = Clock.systemUTC(),
    private val settings: ProtectionSettings = ProtectionSettings(),
) {
    var state: ProtectionState = ProtectionState.Inactive
        private set

    private val _events = mutableListOf<ProtectionEvent>()
    val events: List<ProtectionEvent> get() = _events.toList()

    private var emergencyOverrideUsed = false

    fun startProtection(): ProtectionState {
        val now = clock.instant()
        val lockEndsAt = now.plus(settings.lockPeriod.duration)
        state = ProtectionState.Active(lockEndsAt)
        _events += ProtectionEvent(ProtectionEventType.PROTECTION_STARTED, now)
        return state
    }

    fun registerDetection(
        confidence: Float,
        triggeringApplication: ProtectedApplication,
    ): ProtectionState {
        val now = clock.instant()
        val active = state as? ProtectionState.Active ?: return state
        if (now >= active.lockEndsAt || confidence < ProtectionSettings.INITIAL_THRESHOLD) return state

        val cooldownEndsAt = now.plus(Duration.ofMinutes(settings.cooldown.minutes))
        state = ProtectionState.Cooldown(
            cooldownEndsAt = cooldownEndsAt,
            recoveryTask = settings.recoveryTask,
            lockEndsAt = active.lockEndsAt,
            protectedApplication = triggeringApplication,
        )
        _events += ProtectionEvent(
            type = ProtectionEventType.INTERVENTION_TRIGGERED,
            at = now,
            confidence = confidence,
        )
        return state
    }

    /** True only for the triggering app while its cooldown and lock period remain active. */
    fun shouldBlockForegroundApp(foregroundPackageName: String): Boolean {
        val cooldown = state as? ProtectionState.Cooldown ?: return false
        val now = clock.instant()
        return now < cooldown.cooldownEndsAt &&
            now < cooldown.lockEndsAt &&
            cooldown.protectedApplication.packageName == foregroundPackageName
    }

    fun completeRecovery(): ProtectionState {
        val cooldown = state as? ProtectionState.Cooldown ?: return state
        val now = clock.instant()
        if (now < cooldown.cooldownEndsAt) return state

        state = if (now < cooldown.lockEndsAt) {
            ProtectionState.Active(cooldown.lockEndsAt)
        } else {
            ProtectionState.Inactive
        }
        _events += ProtectionEvent(ProtectionEventType.RECOVERY_COMPLETED, now)
        return state
    }

    fun useEmergencyOverride(): Boolean {
        val now = clock.instant()
        val cooldown = state as? ProtectionState.Cooldown ?: return false
        if (emergencyOverrideUsed || now >= cooldown.lockEndsAt) return false

        emergencyOverrideUsed = true
        state = ProtectionState.Degraded("Emergency override used")
        _events += ProtectionEvent(ProtectionEventType.EMERGENCY_OVERRIDE, now)
        return true
    }

    fun markPermissionRequired(): ProtectionState {
        state = ProtectionState.PermissionRequired
        return state
    }

    fun markDegraded(reason: String): ProtectionState {
        state = ProtectionState.Degraded(reason)
        return state
    }

    fun refresh(): ProtectionState {
        val now = clock.instant()
        val current = state
        state = when {
            current is ProtectionState.Active && now >= current.lockEndsAt -> ProtectionState.Inactive
            current is ProtectionState.Cooldown && now >= current.lockEndsAt -> ProtectionState.Inactive
            current is ProtectionState.Cooldown && now >= current.cooldownEndsAt -> ProtectionState.Active(current.lockEndsAt)
            else -> current
        }
        return state
    }
}
