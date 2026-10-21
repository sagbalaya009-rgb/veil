> Historical context: this document describes an earlier Veil scope or build. For the current implementation and executed evidence, use docs/INDEX.md and docs/releases/ENGINEERING_DELIVERY_REPORT.md. Earlier device gates, scalar-model contracts, feature restrictions and benchmark targets are not claims about the current experimental build.

# App-specific protection boundaries

## Intended behavior

When the local classifier and temporal evaluator confirm explicit content in a foreground application, Veil starts the configured cooldown for **that triggering application only**. For example, a confirmed detection in X (`com.twitter.android`) starts a cooldown that protects X; Chrome and other applications remain available. If the user returns to X before the cooldown expires, Veil presents an input-capturing protective overlay over X with the timer, selected recovery activity, and the user’s saved note.

> Veil must not claim to blur or lock the entire device. The protected application is the only target; the user may move to other applications during the cooldown.

## Android enforcement model

| Layer | Responsibility | Privacy boundary |
| --- | --- | --- |
| Accessibility Service | Observe foreground window package changes only | Ignore event text, window titles, and node content |
| MediaProjection | Supply consented in-memory frames to local inference | Never save or upload frames |
| TensorFlow Lite + temporal evaluator | Produce a confirmed explicit-content signal | No cloud inference |
| Protection engine | Bind cooldown to a `ProtectedApplication` package | Stores no screen content |
| Overlay adapter | Capture interactions while the protected package is foregrounded | Does not block other packages |
| Foreground coordinator | Hide or show the overlay as the user changes apps | Receives package identifier only |

Android cannot make an ordinary consumer application impossible to bypass. The user can disable Accessibility or overlay permissions, force-stop Veil, or use Android’s settings. Veil must show **Degraded** status when required permissions or services are unavailable. It should never pretend that app-specific protection remains active when Android has revoked the needed capability.

## Cooldown lifecycle

```text
Confirmed local detection in X
  → bind cooldown to X package
  → user sees protective overlay while X is foregrounded
  → user may use Chrome or another app normally
  → returning to X restores the overlay until cooldown ends
  → recovery completion returns Veil to active protection state
```

The initial target remains an 800 ms end-to-end detection/intervention budget, a 0.90 confidence threshold, and temporal confirmation to reduce false positives. The documented recovery tasks are push-ups, breathing, walking, and journaling; extended cooldown choices up to one hour are supported by the domain model.

## Browser versus native app

The deployed website only demonstrates this sequence. It cannot detect explicit content in other tabs, inspect browser or operating-system activity, or block X, Chrome, or any desktop application. Those actions require the respective native Android or desktop app permissions and explicit user consent.
