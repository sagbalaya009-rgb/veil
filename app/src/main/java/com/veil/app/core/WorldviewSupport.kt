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
    val arabicText: String? = null,
    val transliteration: String? = null,
    val translationAttribution: String? = null,
)

/**
 * Calm, selected messages only. Veil does not infer religion, use a message to shame a user,
 * or show a religious text unless the user has selected that preference on this device.
 */
fun supportiveMessageFor(preference: WorldviewPreference?): SupportiveMessage? = when (preference) {
    WorldviewPreference.ISLAM -> SupportiveMessage(
        label = "A reminder from the Qur’an",
        body = "O My servants who have exceeded the limits against their souls! Do not lose hope in Allah’s mercy, for Allah certainly forgives all sins. He is indeed the All-Forgiving, Most Merciful.",
        reference = "Surah Az-Zumar · 39:53",
        sourceUrl = "https://quran.com/az-zumar/53",
        arabicText = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا ۚ إِنَّهُ هُوَ الْغَفُورُ الرَّحِيمُ",
        transliteration = "Qul yaa’ibaadiyal lazeena asrafoo ‘alaaa anfusihim laa taqnatoo mirrahmatil laah; innal laaha yaghfiruz zunooba jamee’aa; innahoo Huwal Ghafoorur Raheem.",
        translationAttribution = "English translation: The Clear Quran · Dr. Mustafa Khattab",
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
