> Historical context: this document describes an earlier Veil scope or build. For the current implementation and executed evidence, use docs/INDEX.md and docs/releases/ENGINEERING_DELIVERY_REPORT.md. Earlier device gates, scalar-model contracts, feature restrictions and benchmark targets are not claims about the current experimental build.

# Worldview-Aware Cooldown Message Policy

## Purpose

Veil may show one **supportive, user-selected message** during a future app-specific cooldown. This message is not a diagnosis, judgement, punishment, or attempt to infer a user’s beliefs. It is a local preference, stored only with the user’s local Veil settings, and is never sent to a server.

## Current message catalog

| Selected preference | Local cooldown message | Source |
|---|---|---|
| Islam | **Surah Az-Zumar, 39:53**: Arabic verse text, transliteration, and the full English translation are displayed together. | Arabic and translation verified against Quran.com; transliteration cross-checked against My Islam.[1] [2] |
| Christianity | “If we confess our sins, he is faithful and just to forgive us our sins.” | 1 John 1:9, King James Version.[3] |
| Atheist | “Pause. Your next deliberate step is still yours.” | Original secular recovery copy; not a quotation. |

The catalog deliberately uses a **mercy-and-return** tone. Veil does not default to punishment language, threats, or fear-based messages. A user who has not made a selection sees no worldview message.

## Qur’an presentation

When the user selects Islam, Veil shows this complete reference in the cooldown panel rather than a detached quotation:

> **Surah Az-Zumar · 39:53**
>
> **Arabic:** قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا ۚ إِنَّهُ هُوَ الْغَفُورُ الرَّحِيمُ
>
> **Transliteration:** Qul yaa’ibaadiyal lazeena asrafoo ‘alaaa anfusihim laa taqnatoo mirrahmatil laah; innal laaha yaghfiruz zunooba jamee’aa; innahoo Huwal Ghafoorur Raheem.
>
> **Translation:** “O My servants who have exceeded the limits against their souls! Do not lose hope in Allah’s mercy, for Allah certainly forgives all sins. He is indeed the All-Forgiving, Most Merciful.” — *The Clear Quran*, Dr. Mustafa Khattab.[1]

The app keeps the Arabic, transliteration, translation, and source together so the user can identify the verse and the translation rather than seeing an unattributed or shortened line. The entire message sits in a scrollable cooldown panel, so the translation remains visible on smaller Android screens instead of falling below an unscrollable panel.

## Optional quiet completion notification

The Reset screen offers an optional **Quiet completion notification** setting. It explains that the notification is sent only when the selected cooldown ends. Android permission is requested only after the user taps **Turn on**; Veil never prompts on application launch. This follows Android’s user-context guidance for the `POST_NOTIFICATIONS` permission.[4]

If consent is granted and Android notifications remain enabled, Veil posts one local notification with low importance, no sound, no vibration, and no badge: “Your cooldown is complete. You followed through.” Android requires notifications on modern versions to be assigned to a channel; users may alter or disable that channel in Android settings at any time.[5]

## Product boundaries

The preference affects only the optional copy shown in a local cooldown preview today and a future app-specific intervention later. It does not change the detection threshold, retention rules, app-specific protection model, emergency override, or access to unrelated apps. Veil must never infer a worldview from usage or content.

The present Android build still has **no live screen detection, Accessibility monitoring, MediaProjection capture, overlay enforcement, or app blocking**. The user can only test the visual cooldown locally. Any live implementation must undergo a separate consent, privacy, and reliability review.

## Source handling

Verse wording and reference must be preserved with attribution. Before a public release, the team must re-check the chosen translation’s display and licensing terms and provide a source link or in-app source note. The KJV wording used here is a public-domain translation in the United States; territorial rights and distribution requirements must be reviewed for the release market.

## References

[1]: https://quran.com/az-zumar/53 "Qur’an 39:53 — Quran.com, The Clear Quran"
[2]: https://myislam.org/surah-zumar/ayat-53/ "Surah Az-Zumar Ayat 53 — Arabic text and transliteration"
[3]: https://www.kingjamesbibleonline.org/1-John-1-9/ "1 John 1:9 — King James Version"
[4]: https://developer.android.com/develop/ui/compose/notifications/notification-permission "Android Developers — Notification runtime permission"
[5]: https://developer.android.com/develop/ui/compose/notifications/channels "Android Developers — Create and manage notification channels"
