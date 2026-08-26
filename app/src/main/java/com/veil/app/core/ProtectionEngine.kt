package com.veil.app.core

import java.time.Clock
import java.time.Duration
import java.time.Instant

/** The user's selected intervention strictness. */
enum class ProtectionLevel {
    BALANCED,
    STRICT,
    NUCLEAR,
}

/** Cooldown choices defined by the MVP specification. */
enum class CooldownDuration(val minutes: Long) {
    FIVE(5),
    TEN(10),
    FIFTEEN(15),
    TWENTY(20),
}

/** Recovery tasks defined by the MVP specification. */
enum class RecoveryTask {
    PUSH_UPS,
    BREATHING,
    WALK,
    JOURNAL,
}

/** Lock periods defined by the MVP specification. */
enum class LockPeriod(val duration: Duration) {
    TWO_HOURS(Duration.ofHours(2)),
    EIGHT_HOURS(Duration.ofHours(8)),
    TWENTY_FOUR_HOURS(Duration.ofHours(24)),
    THREE_DAYS(Duration.ofDays(3)),
    SEVEN_DAYS(Duration.ofDays(7)),
}

sealed interface ProtectionState {
    data object Inactive : ProtectionState
    data class Active(val lockEndsAt: Instant) : ProtectionState
    data class Cooldown(
        val cooldownEndsAt: Instant,
        val recoveryTask: RecoveryTask,
        val lockEndsAt: Instant,
    ) : ProtectionState
    data class Degraded(val reason: String) : ProtectionState
    data object PermissionRequired : ProtectionState
}

data class ProtectionSettings(
    val level: ProtectionLevel = ProtectionLevel.BALANCED,
    val cooldown: CooldownDuration = CooldownDuration.TEN,
    val recoveryTask: RecoveryTask = RecoveryTask.BREATHING,
    val lockPeriod: LockPeriod = LockPeriod.TWO_HOURS,
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

/**
 * Pure domain logic for the first Veil MVP slice.
 *
 * Android services and TensorFlow Lite will call this engine through an adapter.
 * The engine deliberately knows nothing about screen frames, permissions, or UI.
 */
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

    fun registerDetection(confidence: Float): ProtectionState {
        val now = clock.instant()
        val active = state as? ProtectionState.Active ?: return state
        if (now >= active.lockEndsAt || confidence < ProtectionSettings.INITIAL_THRESHOLD) {
            return state
        }

        val cooldownEndsAt = now.plus(Duration.ofMinutes(settings.cooldown.minutes))
        state = ProtectionState.Cooldown(
            cooldownEndsAt = cooldownEndsAt,
            recoveryTask = settings.recoveryTask,
            lockEndsAt = active.lockEndsAt,
        )
        _events += ProtectionEvent(
            type = ProtectionEventType.INTERVENTION_TRIGGERED,
            at = now,
            confidence = confidence,
        )
        return state
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
            current is ProtectionState.Cooldown && now >= current.cooldownEndsAt -> {
                ProtectionState.Active(current.lockEndsAt)
            }
            current is ProtectionState.Cooldown && now >= current.lockEndsAt -> ProtectionState.Inactive
            else -> current
        }
        return state
    }
}
