# V2 verification and release gates

Run the documented JVM, lint, packaging, Python and isolated Android suite on the exact application commit used for delivery. Preserve original XML/JSON reports and artifact hashes. A green workflow is insufficient when a nested check explicitly says SKIPPED.

## Executed engineering checks

- Initial V2 a7b7565: 35 JVM tests, 21 Python tests and 12 Android tests passed. Capture used actual Android consent, captured geometric frames, real bundled inference, unchanged temporal policy and an actual overlay. Both breathing and focus lasted at least 60 seconds. Recents removal was skipped and is not qualified by this run.
- f59e064: build/model/emulator jobs passed in run 39827374429. The added interruption regression brings Android coverage to 13 tests. Its capture JSON again explicitly skipped the launcher removal; do not claim that path passed.
- 7c87364: real task removal and callback occurred, but subsequent inference paused after losing foreground observation. This is retained failure evidence, not a passing background test.
- Conversion workflow run 39758874836 exercised TensorFlow 2.17.1 float, dynamic and float16 exports with eight parity cases each. Maximum absolute error was 2.9802322387695312e-08. No training or semantic evaluation occurred.

## Required background regression

Final run 39830051602 at application commit 1cb08d8 passed 35 JVM, 21 Python and 13 Android tests. Capture JSON confirms the AppTask fallback delivered actual task removal and subsequent real model intervention. The launcher swipe remained unsuccessful; it is not qualified. Breathing/focus lasted 60,220/60,700 ms. No test failures/errors/skips; lint retains 20 warnings and one informational item with zero errors.

CaptureJourneyTest first attempts an identified Veil launcher-card swipe. If the gesture does not deliver removal, its emulator-only fallback calls Android AppTask.finishAndRemoveTask for the target application's own task, and requires the real service callback. It does not call onTaskRemoved directly. Record whether the launcher gesture or task API succeeded. Then require live capture, initialized inference, observed fixture package and an actual model-triggered intervention. A task API pass qualifies task lifecycle only; it does not qualify every launcher or OEM swipe behavior.

## Owner phone checks

Never run the state-resetting instrumentation suite on an owner's phone. Install the separate com.veil.app.v2 preview without deleting V1. Check the signing certificate before updates. Obtain owner-operated accessibility and separate Android screen-sharing approval. Use benign controlled surfaces; never inspect messages, accounts, feeds or personal images for test data. Keep any device status screenshots out of public evidence.

On 2026-10-22 the TECNO KL5/API34 accepted the preview installation; accessibility connected, the model loaded, Recovery opened, and the owner approved V2 screen sharing. Android reported an active mediaProjection foreground service. These observations alone do not qualify semantic accuracy, a complete detection journey, or background survival on this phone.

On installed commit 7c87364 a one-minute focus rule was created through the Shield interface, using app search to select Compass. Opening Compass displayed the focus intervention. Its Open recovery control was tapped, but the destination was not captured before the owner changed screens, so physical routing is not qualified. Later read-only inspection of event types confirmed one FOCUS_CREATED and one FOCUS_COMPLETED, with no AI_INTERVENTION. The geometric fixture APK installed and opened during the approved sharing session, but no AI overlay was observed. The fixture's emulator pass is not a physical detection pass; aspect ratio, protected surfaces and actual model/pipeline state require separate diagnosis.

## Open qualification matrix

| Area | Required evidence |
|---|---|
| Background | Actual task removal followed by fresh-frame inference; OEM kill, battery saver and long-duration observations separately |
| Consent | Denial, explicit stop, revocation and new-session consent; no token reuse or boot capture restart |
| Accessibility | TalkBack, largest font, landscape, essential-app exclusion and immediate emergency exits |
| Recovery | Persistence, encryption/corruption refusal, scoped deletion, optional secular/faith choices and skipped check-ins |
| Feedback | Independent reports, undo/retention, corruption refusal and unchanged thresholds |
| ML | Approved independent consent/provenance-reviewed mobile-screen dataset; leak checks; held-out category and calibration metrics |
| Distribution | Exact delivered APK hash/certificate, final commit and matching CI reports; owner production signing and store review remain separate |

Emulator persistence tests exercise Recovery and feedback boundaries. They do not establish clinical benefit, dataset consent, phone battery life or universal Android reliability.
