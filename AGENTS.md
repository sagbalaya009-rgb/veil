# Working on Veil

Veil is an Android voluntary wellbeing app. Never imply guaranteed blocking, perfect classification, or production readiness without evidence. The current delivery report records verified scope; historical specifications describe earlier iterations.

## Layout and ownership

`app/core`: pure Kotlin policy and monotonic deadlines. `app/services`: capture, local model, package-only accessibility overlays and `CommitmentStore`. `app/data`: existing Room settings/notes. `app/ui/VeilDashboard.kt`: current entry flow. `MainActivity.kt` retains the existing localized recovery-plan UI. `ml`: local-only evaluation/training workshop. The web project is independent; do not deploy it as part of Android work.

## Commands

JDK17, Android API35, pinned Gradle wrapper. Run `./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease`; Windows use `gradlew.bat` or `tools/verify.ps1`. Install the test-only fixture and use `connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.veil.app.VeilVerificationSuite` on an authorized emulator; see docs/testing/CAPTURE_JOURNEY.md. Python workshop tests: from `ml`, `python -m unittest discover -s tests -v`. Run model harness per `ml/README.md` and `docs/ai/MODEL_EVALUATION.md`. Inspect exact CI commit and test reports before claiming success.

## Architecture and privacy rules

No INTERNET permission, frame/URL/text logging, screenshot storage, remote analytics, covert capture or security bypasses. Accessibility reads package identifiers only; no root/window nodes, text, gestures or automatic device actions. Essential apps, Settings, system UI, launcher and calls are excluded; all restrictions have an immediate safe exit. Validate new package exclusions on actual OEM devices. Process/activity state must never masquerade as a running service. Screen sharing uses fresh Android consent, a visible mediaProjection FGS, START_NOT_STICKY, bounded latest frames and complete cleanup. Reboot ends commitments and never restarts capture. Timers use elapsedRealtime plus boot count, not wall-clock time.

## Models

Keep code-pinned SHA256, source, full upstream license, tensor shape/dtype/labels/normalization checks. Never replace real inference with hardcoded scores. Contract smoke tests and semantic evaluation are different. Never mark pending dataset/device review approved. The provisional threshold and class grouping require held-out mobile-screen evaluation before broad release. Do not collect private screenshots or run dataset scrapers. Preserve existing `ml` approval gates.

## Done means evidence

Build, applicable unit/instrumentation tests, lint and privacy checks must pass on the exact delivered commit. Record host, emulator and physical results separately. Keep docs/current status truthful, retain user work and migration paths, test denial/revocation/start-stop/failure/reboot handling. No store publication, purchased services or signing under the owner without authorization. Commit source and deliver a real APK plus checksum; do not invent download links or benchmarks.
