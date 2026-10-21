> Historical context: this document describes an earlier Veil scope or build. For the current implementation and executed evidence, use docs/INDEX.md and docs/releases/ENGINEERING_DELIVERY_REPORT.md. Earlier device gates, scalar-model contracts, feature restrictions and benchmark targets are not claims about the current experimental build.

# Veil Android Language Support

## Purpose

Veil’s Android MVP keeps the user’s preferred interface language on the device. No interface text, chosen language, recovery note, worldview preference, or cooldown plan is sent to a translation service. The language setting applies immediately to Veil’s Home, Reset, Note, local cooldown preview, navigation, notification choice, and surrounding labels.

## Initial offline languages

| Language | Native selector label | Coverage |
|---|---:|---|
| English | English | Complete offline interface copy. |
| Arabic | العربية | Complete offline interface copy; Arabic remains the original Qur’an text. |
| Spanish | Español | Complete offline interface copy. |
| French | Français | Complete offline interface copy. |
| Portuguese | Português | Complete offline interface copy. |
| Hindi | हिन्दी | Complete offline interface copy. |
| Urdu | اردو | Complete offline interface copy. |
| Swahili | Kiswahili | Complete offline interface copy. |
| Yoruba | Yorùbá | Complete offline interface copy. |
| Hausa | Hausa | Complete offline interface copy. |
| Igbo | Igbo | Complete offline interface copy. |

## Qur’an reminder display

The Islam reminder always keeps the **Arabic source text**, its **transliteration**, the **Surah Az-Zumar 39:53** reference, and the currently verified English rendering from *The Clear Quran*, Dr. Mustafa Khattab, together. [1] [2] The translation block is placed inside a scrollable cooldown panel so it remains reachable on a phone screen rather than being cut off below the fold.

Veil does not yet claim localized Qur’an translations for every interface language. Adding a displayed religious translation requires a separately reviewed, attributed source for that language; the app must not machine-translate a sacred-text quotation.

## Product boundaries

This language selector is a local UI preference, not detection, surveillance, cloud translation, or content analysis. The current build continues to use a local visual cooldown preview only. It does not inspect other applications, capture the screen, detect explicit content, block an app, or use TensorFlow Lite for live decisions.

## References

[1]: https://quran.com/az-zumar/53 "Qur’an 39:53 — Quran.com, The Clear Quran"
[2]: https://myislam.org/surah-zumar/ayat-53/ "Surah Az-Zumar Ayat 53 — Arabic text and transliteration"
