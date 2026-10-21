# Veil repository audit

Baseline: `71bd46a457cfe04ad45b7ddfc315c913f8d9222e` on `main`, cloned 2026-10-21. Clean checkout; original history retained. Reconstruction branch: `codex/veil-mvp`. This records the initial inspection, before the integrated implementation described in the delivery report.

## Inventory and reusable work

The repository is real application code, not documentation alone. History includes the native foundation, Hilt launch fix, least-privilege manifest, website-matched Compose recovery flow, offline localization, Room migrations 1–5, and a local ML workshop with dataset approval gates. The web/Vercel project is separate and retained. Reuse the native Kotlin/Compose/Hilt/Room stack, recovery screens, language/worldview settings, temporal evaluator and ML safety tooling.

## Initial failures and gaps

The manifest declared only the activity and notification permission: no functioning capture service, projection flow, foreground-awareness integration or app restrictions. `VeilMonitoringService` was a sticky notification scaffold claiming protection; it had no capture pipeline. The classifier assumed a scalar model output, with no bundled model. Focus selection, scheduled sessions, authoritative monitoring status, persistent app cooldowns, CI workflows and instrumentation tests were absent. Existing protection-domain code accepted NaN scores. The original README incorrectly called implemented Android code scaffolding; its repository map linked missing documents. Old build claims in operations notes are historical evidence, not current checks.

## Requirement conflicts and decisions

The 2026-10-21 master directive supersedes historical scope limited to a manual cooldown preview. Legacy rules prescribe minimum ten-minute cooldown, an immutable one-use emergency delay, 0.90 as a fixed confidence and Galaxy M20 as qualification target. Current work uses the directive's voluntary exits, 5/10/15-minute AI cooldowns and TECNO SPARK 30C test target. The historical recovery plan remains accessible. A provisional 0.90 threshold is explicitly experimental, not calibrated. No three-class semantic accuracy is invented from a five-class model.

Android capture needs renewed consent; consumer apps cannot guarantee enforcement. Existing no-network, no-frame-storage and autonomy restrictions remain binding. Existing training-data gates are retained. Model artifact contract/smoke validation does not constitute adult-only training provenance verification or phone-feed qualification.

## Reconstruction scope

Retain healthy code. Add a dashboard, consent flows, package-only accessibility adapter, reversible accessibility overlay, monotonic persisted focus/cooldown state, bounded local history, real model adapter and authorized capture lifecycle. Add CI/build artifacts, tests and accurate documentation. No cloud service, root, device administration or uninstall prevention.

## Baseline verification and constraints

JDK17 and Android SDK are present. Initially only API37 is cached, while this repository needs API35 and Gradle8.10.2. No connected ADB device, cached emulator or AVD was found. Dependencies require downloads; network throughput is limiting. Existing tests will be executed and recorded separately from Android runtime/device checks. Baseline has no new claim of an APK or on-device success.

## Security risks

Screen frames are sensitive even in memory; native inference and image conversion are attack surfaces. Foreground attribution can race or become ambiguous in multi-window mode. Accessibility overlays must not hide emergency/system controls. Local notes and selected app rules are sensitive app-private data. Debug APKs are debuggable. Model provenance and CI dependency supply chains need explicit review. See the threat model and privacy architecture for implemented controls and remaining qualification work.
