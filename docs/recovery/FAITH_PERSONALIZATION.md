# Optional encouragement and faith personalization

V2 starts with practical/secular encouragement. The user can select personal-values prompts, their own faith-based reminder, or no encouragement. No choice requires naming a religion. All Recovery exercises are available in every mode and the user may leave goals blank.

The V2 faith mode displays only the reminder the user chose to write. It does not generate scripture, religious rulings, or spiritual interpretations, and does not claim a user-written reminder is a verified quotation. A blank reminder displays no faith message. Changing mode to None suppresses encouragement; `deletePreferences()` removes the stored reminder and returns preferences to defaults. Scoped deletion and at-rest encryption are described in [RECOVERY_EXPERIENCE.md](RECOVERY_EXPERIENCE.md).

Legacy V1 Room settings included Islam/Christianity/Atheist choices and sourced text in `WorldviewSupport.kt`. They remain preserved as legacy data; the new store does not infer consent to V2 faith mode from them. Do not silently show the legacy religious content in the new experience. Any future curated faith library requires reference verification, clear quote/paraphrase attribution, licensing/translation checks, and a separate review of nonjudgmental tone before shipping.

No religious preference or reminder enters screen analysis, intervention sensitivity, feedback, training, analytics, or diagnostic exports. Veil never frames a setback as spiritual failure or treats faith as a condition of access to support.
