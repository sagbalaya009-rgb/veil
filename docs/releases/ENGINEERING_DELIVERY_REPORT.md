# VEIL — ENGINEERING DELIVERY REPORT

Status: integration and cloud verification in progress, 2026-10-21. This is not a completed MVP qualification report. The final revision will record exact artifact/build/runtime results.

## Delivered source

Existing Android UI, Hilt/Room persistence, recovery notes/localization and ML safety gates retained. Added live dashboard/onboarding, explicit capture/accessibility disclosures, focus app selection and scheduled starts, persistent monotonic deadlines, reversible package-specific overlays, cooldown/recovery/history/privacy controls, real checksum-pinned five-output TFLite inference, bounded authorized MediaProjection processing, CI and emulator test definitions. Original baseline71bd46a retained; branch codex/veil-mvp.

## Executed verification so far

- Host-only Kotlin core fallback:20 tests passed using cached Kotlin compiler/JUnit; this is not a Gradle/Android build.
- Existing ML safety workshop:5 tests passed.
- Actual bundled desktop LiteRT inference passed; median125.93ms, p95152.66ms across26 measured invocations after warmups. Desktop synthetic execution, not phone performance or classifier accuracy.
- Local Android build failed before compilation due Gradle/SDK network timeouts and absent required cached toolchain.
- Initial cloud setup failed before compilation because obsolete SDK tools package no longer exists; CI configuration repaired, rerun pending.
- No physical device or emulator result is claimed yet. No installed APK exists until a successful build is recorded.

## Qualification gaps

Phone-feed semantic accuracy/threshold calibration, physical permission/capture/focus/overlay/lifecycle behavior, TECNO SPARK30C battery/thermal performance and full model training provenance/conversion parity are not verified. No store publication or owner signing took place. Android secure surfaces, permission revocation and ordinary consumer bypass limitations remain.

See model, architecture, privacy, threat model, permissions and testing documents for exact controls and limits.
