# Worldview-Aware Cooldown Message Policy

## Purpose

Veil may show one **supportive, user-selected message** during a future app-specific cooldown. This message is not a diagnosis, judgement, punishment, or attempt to infer a user’s beliefs. It is a local preference, stored only with the user’s local Veil settings, and is never sent to a server.

## Current message catalog

| Selected preference | Local cooldown message | Source |
|---|---|---|
| Islam | “Do not lose hope in Allah’s mercy.” | Qur’an 39:53, *The Clear Quran*.[1] |
| Christianity | “If we confess our sins, he is faithful and just to forgive us our sins.” | 1 John 1:9, King James Version.[2] |
| Atheist | “Pause. Your next deliberate step is still yours.” | Original secular recovery copy; not a quotation. |

The catalog deliberately uses a **mercy-and-return** tone. Veil does not default to punishment language, threats, or fear-based messages. A user who has not made a selection sees no worldview message.

## Product boundaries

The preference affects only the optional copy shown in a local cooldown preview today and a future app-specific intervention later. It does not change the detection threshold, retention rules, app-specific protection model, emergency override, or access to unrelated apps. Veil must never infer a worldview from usage or content.

The present Android build still has **no live screen detection, Accessibility monitoring, MediaProjection capture, overlay enforcement, or app blocking**. The user can only test the visual cooldown locally. Any live implementation must undergo a separate consent, privacy, and reliability review.

## Source handling

Verse wording and reference must be preserved with attribution. Before a public release, the team must re-check the chosen translation’s display and licensing terms and provide a source link or in-app source note. The KJV wording used here is a public-domain translation in the United States; territorial rights and distribution requirements must be reviewed for the release market.

## References

[1]: https://quran.com/az-zumar/53 "Qur’an 39:53 — Quran.com, The Clear Quran"
[2]: https://www.kingjamesbibleonline.org/1-John-1-9/ "1 John 1:9 — King James Version"
