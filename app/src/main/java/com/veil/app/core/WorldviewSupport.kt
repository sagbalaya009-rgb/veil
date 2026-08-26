package com.veil.app.core

/** A voluntary local preference that controls only the reminder shown during a cooldown. */
enum class WorldviewPreference {
    ISLAM,
    CHRISTIANITY,
    ATHEIST,
}

data class SupportiveMessage(
    val label: String,
    val body: String,
    val reference: String,
    val sourceUrl: String? = null,
)

/**
 * Calm, selected messages only. Veil does not infer religion, use a message to shame a user,
 * or show a religious text unless the user has selected that preference on this device.
 */
fun supportiveMessageFor(preference: WorldviewPreference?): SupportiveMessage? = when (preference) {
    WorldviewPreference.ISLAM -> SupportiveMessage(
        label = "A reminder from the Qur’an",
        body = "Do not lose hope in Allah’s mercy.",
        reference = "Qur’an 39:53 · The Clear Quran",
        sourceUrl = "https://quran.com/az-zumar/53",
    )
    WorldviewPreference.CHRISTIANITY -> SupportiveMessage(
        label = "A reminder from the Bible",
        body = "If we confess our sins, he is faithful and just to forgive us our sins.",
        reference = "1 John 1:9 · King James Version",
        sourceUrl = "https://www.kingjamesbibleonline.org/1-John-1-9/",
    )
    WorldviewPreference.ATHEIST -> SupportiveMessage(
        label = "A reminder for you",
        body = "Pause. Your next deliberate step is still yours.",
        reference = "A secular recovery reminder",
    )
    null -> null
}
