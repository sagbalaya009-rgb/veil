# Test plan

Evidence is split into host Kotlin policy tests, Python ML safety tests, actual desktop model execution, Android compilation/lint, emulator instrumentation, and physical device qualification. Do not infer one class of evidence from another.

Run the full wrapper command from the release instructions. Instrumentation includes actual MainActivity/Hilt launch/onboarding/navigation/disclosure cancellation, persistent selected-rule behavior/essential-app exclusion, intervention exit, and real bundled model inference on generated benign bitmaps. Permission dialogs and detection-to-overlay behavior still require authorized interactive/device testing; synthetic model smoke is not a semantic accuracy test.

Host policy tests cover protection state transitions, temporal confirmation, invalid scores, cooldowns, emergency behavior, localization/worldview and monotonic deadline reboot handling. The local ML workshop preserves data-approval/metric/export tests. The bundled-model evaluation harness checks the genuine tensor contract and offers an independently reviewed local dataset interface for precision/recall/confusion-matrix evaluation; no labeled semantic results exist yet.

The CI verification job runs unit tests, Android lint, debug/instrumentation packaging and unsigned release packaging. The emulator job runs connectedDebugAndroidTest on API35. A missing emulator run is a gap, not a pass. Required physical scenarios are in DEVICE_TEST_MATRIX.md. Add deterministic regression tests for discovered failures without weakening assertions. Never store private screen media in reports.
