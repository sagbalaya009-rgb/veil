package com.veil.app.core.recovery

/** These are user choices, never inferred from screen content or intervention counts. */
enum class RecoveryGoal(val label: String) {
    REDUCE_UNWANTED_USE("Reduce unwanted use"),
    ABSTAIN_SELECTED_CONTENT("Abstain from selected content"),
    IMPROVE_FOCUS("Improve digital focus"),
}

enum class EncouragementMode(val label: String) {
    PRACTICAL("Practical and secular"),
    PERSONAL_VALUES("Personal values and reflection"),
    FAITH("My own faith reminder"),
    NONE("No encouragement messages"),
}

enum class Trigger(val label: String) {
    BOREDOM("Boredom"), STRESS("Stress"), LONELINESS("Loneliness"), TIREDNESS("Tiredness"),
    ANXIETY("Anxiety"), PROCRASTINATION("Procrastination"), LATE_NIGHT("Late-night scrolling"),
    HABIT("Habitual browsing"), OTHER("Something else"),
}

enum class RecoveryActivity(val label: String, val instructions: List<String>) {
    BREATHING("Comfortable breathing", listOf(
        "Breathe at a comfortable pace. There is no need to hold your breath or breathe deeply.",
        "If focusing on breathing feels uncomfortable, stop and choose grounding instead.",
    )),
    GROUNDING("Notice your surroundings", listOf(
        "Look around and name three things you can see. You may use sound or touch instead.",
        "Notice one point of contact with your chair, floor, or another comfortable surface.",
        "Choose one small thing to do next. Movement is optional.",
    )),
    URGE_SURFING("Make room for the urge", listOf(
        "Notice the urge without needing to act on it or make it disappear.",
        "If it helps, silently name it: I am noticing an urge. Let it change at its own pace.",
        "Return attention to your surroundings, then choose your next action.",
    )),
    MEANINGFUL_TASK("Return to something meaningful", listOf(
        "Choose a small task that matters to you: read a page, prepare a drink, or send a kind message.",
        "Make the first step small enough to start now. You choose what fits.",
    )),
    WALK("Optional gentle movement", listOf(
        "If safe and comfortable for you, put the phone down and move or walk for a few minutes.",
        "A seated stretch, noticing sounds, or returning to a task are equally valid alternatives.",
    )),
    PERSONAL_PLAN("Use my coping plan", listOf(
        "Read the alternative you chose in advance. Adjust it if it no longer fits.",
        "A plan is an option, not an obligation. You can choose another activity.",
    )),
}

enum class RecoveryRecordKind { CHECK_IN, ACTIVITY, SETBACK }

data class RecoveryRecord(
    val id: String,
    val atEpochMillis: Long,
    val kind: RecoveryRecordKind,
    val intensity: Int? = null,
    val trigger: Trigger? = null,
    val note: String = "",
    val activity: RecoveryActivity? = null,
    val helpful: Boolean? = null,
)

data class RecoveryState(
    val goals: Set<RecoveryGoal> = emptySet(),
    val encouragement: EncouragementMode = EncouragementMode.PRACTICAL,
    val customReminder: String = "",
    val planTrigger: String = "",
    val planAction: String = "",
    val records: List<RecoveryRecord> = emptyList(),
    val storageAvailable: Boolean = true,
    val storageMessage: String? = null,
) {
    val completedActivities: Int get() = records.count { it.kind == RecoveryRecordKind.ACTIVITY }
    val helpfulActivities: Int get() = records.count { it.kind == RecoveryRecordKind.ACTIVITY && it.helpful == true }
    val checkIns: List<RecoveryRecord> get() = records.filter { it.kind == RecoveryRecordKind.CHECK_IN }

    fun encouragementText(): String? = when (encouragement) {
        EncouragementMode.PRACTICAL -> "An urge does not need an immediate response. Choose one small next step."
        EncouragementMode.PERSONAL_VALUES -> "What matters to you in this moment? Let that guide your next small action."
        EncouragementMode.FAITH -> customReminder.takeIf { it.isNotBlank() }
        EncouragementMode.NONE -> null
    }
}

/** Shared retention for disk reads and writes. The counts describe only this retained window. */
object RecoveryRules {
    const val MAX_RECORDS = 100
    const val RETENTION_MILLIS = 30L * 24 * 60 * 60 * 1000
    const val MAX_NOTE_LENGTH = 1000
    const val MAX_PLAN_LENGTH = 160
    const val MAX_REMINDER_LENGTH = 400

    fun retain(records: List<RecoveryRecord>, now: Long): List<RecoveryRecord> = records
        .filter { it.atEpochMillis in (now - RETENTION_MILLIS)..now }
        .distinctBy { it.id }
        .sortedByDescending { it.atEpochMillis }
        .take(MAX_RECORDS)

    fun add(state: RecoveryState, record: RecoveryRecord, now: Long): RecoveryState {
        require(record.id.isNotBlank())
        require(record.intensity == null || record.intensity in 0..10)
        require(record.note.length <= MAX_NOTE_LENGTH)
        require(record.kind == RecoveryRecordKind.CHECK_IN || (record.intensity == null && record.trigger == null && record.note.isEmpty()))
        require((record.kind == RecoveryRecordKind.ACTIVITY) == (record.activity != null))
        require(record.kind == RecoveryRecordKind.ACTIVITY || record.helpful == null)
        return state.copy(records = retain(state.records + record, now))
    }
}
