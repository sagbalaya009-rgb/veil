> Historical context: this document describes an earlier Veil scope or build. For the current implementation and executed evidence, use docs/INDEX.md and docs/releases/ENGINEERING_DELIVERY_REPORT.md. Earlier device gates, scalar-model contracts, feature restrictions and benchmark targets are not claims about the current experimental build.

# Project Veil UI/UX Contract

## Design philosophy

Veil follows **calm, practical recovery**. It should feel like a private place to make a better next choice, not an alarm, a surveillance tool, or a punishment. The Android MVP deliberately mirrors the existing browser companion: a quiet paper-like surface, a blue accent rule, familiar words, and one decision at a time.

| Principle | Current requirement |
|---|---|
| Website fidelity | Android uses the same **Home**, **Reset**, and **Note** flow, wording, duration choices, recovery tasks, and quiet completion language as the browser companion. |
| Minimalism | The app avoids a stacked settings list. Duration uses compact chips; recovery uses two-column selection tiles; a note is a single local text field. |
| Local control | Notes and chosen settings remain on the device. The current build does not capture a screen, inspect other apps, transmit data, or request sensitive runtime permissions. |
| No shame | Headlines are supportive and direct: “What would help right now?”, “Good call. You chose a pause.”, and “You followed through.” |
| App-specific future | A future confirmed detection may protect only its triggering app. The current interface must not claim live detection or app blocking. |

## Navigation

The application header is the website-matched navigation model. It contains the VEIL mark, **Home**, **Reset**, **Note**, and a compact light/dark appearance control. It replaces prior onboarding, status-dashboard, bottom-navigation, streak, and history concepts for this MVP build.

| Destination | Purpose |
|---|---|
| Home | Provides two actions only: **Set your cooldown** and **Write a note**. |
| Reset | Sets the duration and one recovery activity, then starts a clearly labelled local preview cooldown. |
| Note | Stores one local reminder for the future cooldown screen. |

## Reset flow

The Reset screen follows the browser companion’s order and language. The minimum cooldown is **10 minutes**. Available choices are **10 minutes, 15 minutes, 30 minutes, 45 minutes, and 1 hour**. The user chooses one recovery activity: push-ups, breathing, walk, journal, water, stretch, or an offline pause.

> **Current capability boundary:** “Test cooldown” is a deliberate local preview. It does not mean Veil has detected content, captured a screen, or protected another application.

The preview screen presents the same core elements as the website: a calm confirmation, a live countdown, the selected recovery activity, the saved note when available, and “I’m done” / “Change plan” controls. An emergency override remains visible as a future friction mechanism but is not described as an undefeatable control.

## Visual language

The surface defaults to warm off-white with charcoal text, thin neutral rules, white panels, and **Veil blue (`#385C98`)** as the signature color. Dark mode swaps the paper surface for charcoal while retaining the same hierarchy. The Android implementation uses platform fonts where necessary; its headings use a serif face to echo the browser companion’s display typography.

Cards and controls use restrained 5–6 dp corners instead of highly rounded capsules. The layout uses generous empty space, a thin blue vertical accent at the page edge where appropriate, compact chips for duration, and simple bordered two-column activity cards. Icons are secondary to plain-language labels.

## Future sensitive permission flow

No Accessibility, MediaProjection, overlay, foreground-service, notification, or boot permission is requested by the current website-matched build. If and only if live app-specific protection is later introduced, consent must appear as a separate, plain-language capability screen before Android’s system prompt. It must state the purpose, what is not collected, revocation consequences, and degraded-state behavior.
