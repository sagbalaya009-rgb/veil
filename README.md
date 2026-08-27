# Project Veil

**Project Veil** is an Android-first digital-wellbeing application that helps people enforce self-control commitments around explicit content and other distracting behaviors. Veil is designed as a **user-controlled protective layer**, not a surveillance product: processing is local, operation is visible, and the user chooses when protection is active.

## What Veil does

Veil’s target Android protection path will analyze bounded screen frames locally only after the user has completed a separate, explicit Android consent flow and a reviewed classifier model has passed real-device validation. A confirmed detection will present a calm intervention for the triggering application only, with a configurable cooldown and recovery task. It must not shame the user, block unrelated apps, or become a surveillance tool.

The first release is intentionally narrow. The MVP proves one core hypothesis: a user can enable protection, encounter explicit content, be interrupted within approximately one second, complete a recovery action, and avoid immediately returning to the triggering content.

## Product principles

| Principle | Meaning |
|---|---|
| User autonomy | Protection is explicitly initiated and controlled by the person using the device. |
| Privacy by architecture | Screen frames, recordings, browsing history, and classifier inputs are never stored or sent to a cloud service. |
| Calm authority | Intervention is direct, non-judgmental, and focused on the next healthy action. |
| Trust over growth | Veil will choose stronger transparency and privacy over faster adoption. |
| Graceful degradation | If Android revokes a permission or a device limits background execution, Veil reports the reduced protection state rather than pretending to be active. |

## MVP scope

The current Android MVP targets Android 12+ and includes a minimal Home / Reset / Note flow, cooldown choices from 10 minutes through one hour, recovery tasks, local notes, a voluntary worldview preference, local cooldown preview, optional quiet completion notification, and a local language preference. It does not currently enable Android Accessibility Service, MediaProjection, screen capture, a foreground monitoring service, an overlay, live TensorFlow Lite inference, or app-specific enforcement. Those capabilities require clear consent, visible status, model approval, and real-device validation before they can be enabled.

The initial model target is an on-device TensorFlow Lite classifier. The first threshold is **0.90 confidence**, intentionally favoring fewer false positives during early testing. The battery target is no more than approximately **5% additional daily consumption**, with adaptive sampling: idle at 0.5 FPS, normal use at 1 FPS, and active scrolling at 2 FPS.

## Repository map

```text
veil/
├── README.md
├── FOUNDER_MEMO.md
├── ROADMAP.md
├── CHANGELOG.md
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
├── SECURITY.md
├── LICENSE
├── docs/
│   ├── overview.md
│   ├── product/mvp-spec.md
│   ├── product/problem.md
│   ├── product/vision.md
│   ├── architecture/android-architecture.md
│   ├── architecture/ai-system.md
│   ├── security/privacy-security.md
│   ├── security/ethical-boundaries.md
│   ├── security/risk-register.md
│   ├── operations/build-plan.md
│   ├── operations/testing.md
│   ├── decisions/0001-on-device-ai.md
│   └── source/original-notes.txt
├── app/        # Android application (UI, domain, persistence, services, and tests)
├── ml/         # Local-only training, evaluation, conversion, and review workshop; no raw data
└── .github/    # Issue forms, pull request template, and CI configuration
```

The Android implementation directories are scaffolds until the initial application code is added. The documentation is the current source of truth for product boundaries, architecture, privacy, and release gates.

## Status

**Phase:** Working Android UI and safe local-model preparation

**Next milestone:** Secure approved adult-only training/evaluation data, run the documented local model workshop, and benchmark any passing TensorFlow Lite candidate on the Galaxy M20 before introducing sensitive capture or enforcement capabilities.

## Important boundary

Veil must never become a remote monitoring tool. It must not hide its operation, transmit screen content, read message contents unnecessarily, log accessibility events, or provide third-party supervision. Any future feature that weakens these boundaries requires an explicit architecture decision and privacy review.

## License

This repository is currently released under the Apache License 2.0. See [LICENSE](LICENSE).

## Source notes

The initial product brief supplied for this repository is preserved at [`docs/source/original-notes.txt`](docs/source/original-notes.txt). The structured documents in `docs/` normalize those notes under the Veil name and organize them for implementation.
