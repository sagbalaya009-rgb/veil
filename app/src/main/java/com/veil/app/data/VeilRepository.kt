package com.veil.app.data

import com.veil.app.core.CooldownDuration
import com.veil.app.core.LockPeriod
import com.veil.app.core.ProtectionEvent
import com.veil.app.core.ProtectionEventType
import com.veil.app.core.ProtectionLevel
import com.veil.app.core.ProtectionSettings
import com.veil.app.core.RecoveryTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VeilRepository(
    private val settingsDao: VeilSettingsDao,
    private val eventDao: ProtectionEventDao,
) {
    suspend fun loadSettings(): VeilSettingsEntity = settingsDao.get() ?: VeilSettingsEntity()

    suspend fun saveSettings(settings: ProtectionSettings, onboardingComplete: Boolean) {
        settingsDao.save(
            VeilSettingsEntity(
                onboardingComplete = onboardingComplete,
                protectionLevel = settings.level.name,
                cooldownMinutes = settings.cooldown.minutes,
                recoveryTask = settings.recoveryTask.name,
                lockPeriod = settings.lockPeriod.name,
                recoveryNote = settings.recoveryNote,
            ),
        )
    }

    suspend fun recordEvent(event: ProtectionEvent) {
        eventDao.insert(
            ProtectionEventEntity(
                type = event.type.name,
                atEpochMillis = event.at.toEpochMilli(),
                confidence = event.confidence,
            ),
        )
    }

    fun observeEventCount(): Flow<Int> = eventDao.observeAll().map { it.size }

    suspend fun observeEventCountSnapshot(): Int = eventDao.count()

    fun settingsFrom(entity: VeilSettingsEntity): ProtectionSettings = ProtectionSettings(
        level = runCatching { ProtectionLevel.valueOf(entity.protectionLevel) }
            .getOrDefault(ProtectionLevel.BALANCED),
        cooldown = CooldownDuration.entries.firstOrNull { it.minutes == entity.cooldownMinutes }
            ?: CooldownDuration.TEN,
        recoveryTask = runCatching { RecoveryTask.valueOf(entity.recoveryTask) }
            .getOrDefault(RecoveryTask.BREATHING),
        lockPeriod = runCatching { LockPeriod.valueOf(entity.lockPeriod) }
            .getOrDefault(LockPeriod.TWO_HOURS),
        recoveryNote = entity.recoveryNote,
    )
}
