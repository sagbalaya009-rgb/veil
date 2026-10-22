# Veil V2 engineering report

Status: verified experimental preview, installed on the owner phone. Engineering gates below passed; physical visual-detection and broad release qualification remain open. Do not interpret this document as production approval.

## Product and preservation

V2 adds independent correction feedback, encrypted optional Recovery goals/plans/check-ins, optional user-written faith encouragement, practical coping activities, app search, a redesigned five-tab interface, explicit monitoring health and aggregate-only diagnostics export. Legacy Room records and V1 source remain. The debug preview uses com.veil.app.v2 and the label Veil V2 Preview so it can coexist with V1 without deleting its data. Release packaging keeps com.veil.app but remains unsigned and is not an owner-approved production release.

Preview updates use a persistent private development signing key held outside source and supplied through a repository secret on trusted push builds. The development certificate SHA256 is 5d8aa05246b4687991807f18626ae3a6ba96bff6b9e65d32b1abfb451b6a4e88. This is an engineering certificate, not owner production signing. Neither its private key nor owner screenshots belong in deliverables.

## Background behavior

The service and its initialized interpreter are independent of the UI task, stop policy immediately during terminal cleanup, prevent duplicate starts and expose interrupted/paused states. Screen sharing always requires Android consent and never silently resumes after process death or reboot. A confirmed AccessibilityService.onInterrupt contract misuse was corrected: feedback interruption no longer clears connection and foreground state. Unbind/destroy still stop protection. No ordinary app can guarantee survival after force-stop or OEM process termination.

Earlier true Recents removal preserved the service but exposed lost foreground observation. Some later green capture runs skipped their unreliable launcher gesture. A deterministic test now requires the real Android task-removal callback, explicitly recording an AppTask fallback separately from a successful launcher swipe. Final results must be read from its JSON rather than inferred from a green workflow.

## AI and data

The shipped model remains the experimental upstream GantMan MobileNetV2 1.40/224 TFLite, 17,355,548 bytes, SHA256 380f98f7685f9d8a386f8cc595b6dfcb972989aae3d1b8b270d3a4a5b96fab40. Input is float RGB/255; output labels are drawings, hentai, neutral, porn and sexy. The unchanged intervention score is hentai+porn, threshold .90 with two-of-three temporal confirmation. Upstream license and pinned source remain documented. Upstream image provenance and consent are unresolved; no new dataset was acquired, scraped or used for training in this project.

New offline tools compare approved candidates, detect exact/decoded/near-duplicate leakage, calculate held-out error/calibration/category metrics and verify export parity. The future binary MobileNetV3 training workshop fixes normalization and records deterministic/checkpoint/export contracts. Its binary output is not a drop-in replacement for the five-label application adapter. No training was performed; no accuracy improvement, calibrated Balanced/Strict setting, demographic fairness, region/video accuracy or clinical benefit is claimed.

Actual TensorFlow 2.17.1 conversion engineering run 39758874836 tested eight cases per float/dynamic/float16 export with maximum absolute error 2.9802322387695312e-08. This verifies mathematics and conversion, not content detection. A desktop native-model smoke run executed 26 inferences; its latency is not phone performance.

## Privacy and security

No INTERNET permission or server was added. Frames remain bounded in memory and are discarded. Accessibility receives package metadata only; it cannot retrieve window content or perform gestures. Corrections retain bounded metadata without app names, pixels or embeddings and never modify model policy. Optional Recovery records use AndroidKeyStore AES-GCM and AtomicFile in noBackupFilesDir with corruption refusal and scoped deletion. Legacy Room notes have their existing storage behavior; do not imply those older records were migrated into the new encrypted store.

The completed managed static diff review covers bf1b86a..a7b7565 and reported zero reportable findings in that scope. Its generated report is the authoritative security artifact. Later preview/signing/lifecycle/test changes were inspected separately; they are outside that immutable scan. This is not a dependency advisory audit, native-parser assessment, dynamic exploit test or universal security guarantee.

## Executed evidence and limits

Initial V2 run 39757165988 passed 35 JVM, 21 Python and 12 Android tests, plus lint and APK packaging. Run 39827374429 passed build/model/emulator jobs with the added Android feedback-interruption regression (13 tests), but explicitly skipped Recents removal. The capture journey uses genuine consent, genuine captured geometric frames, real inference, unchanged policy, actual overlays and elapsed-time breathing/focus exercises. A synthetic false positive does not validate semantic accuracy.

The TECNO KL5/API34 accepted separate V2 installation, loaded the model, connected accessibility and started a mediaProjection foreground service after owner consent. Its Shield app-search form created a one-minute Compass rule and the focus overlay appeared. A read-only count of event types later confirmed FOCUS_CREATED=1 and FOCUS_COMPLETED=1. The geometric fixture opened during sharing but did not produce an observed AI intervention. Physical recovery-button routing, semantic detection, Recents survival, emergency exits and long-duration power behavior remain unqualified. The full resetting instrumentation suite was not run on the owner's phone.

See ../testing/V2_TEST_PLAN.md for the qualification matrix. Remaining release gates include approved semantic data/provenance, calibrated policy, actual phone capture diagnosis, target-OEM background/power tests, TalkBack/large-font/landscape qualification, complete dashboard localization, owner production signing and store review. Reboot-spanning focus restoration is not implemented; reboot ends commitments safely.

## Source and delivery

Work is on codex/veil-v2 in draft PR https://github.com/sagbalaya009-rgb/veil/pull/2, based on preserved V1. No main merge, store publication or website deployment was performed.

### Final application artifact and executed checks

- Application/build commit: **1cb08d8cc9badf9f20cdecf006310cae3b97aff6**. Subsequent documentation-only commit records this result without changing the delivered application.
- Android verification run **39830051602**: model, verify and emulator jobs succeeded. Reports contain **35 JVM tests and 13 Android tests, zero failures/errors/skips**. The Python tooling suite passed **21 tests**. Lint: **zero errors, 20 warnings, one informational item**, not warning-free.
- Final TensorFlow conversion run **39830055306** also passed.
- Capture JSON: actual Android task-removal callback **true** through **AppTask fallback**; launcher gesture/card removal **not qualified**. Capture and real inference subsequently produced an actual synthetic-fixture intervention. Safe Home exit, Recovery routing, false-positive stop, focus emergency exit and actual breathing/focus durations **60,220 ms / 60,700 ms** passed. This qualifies emulator task lifecycle, not the physical TECNO Recents gesture.
- Delivery file: **Veil-V2-Preview-0.3.0.apk**, **58,357,881 bytes**. SHA256 **39cf815cc786d985d7b49f82ed9508248365ef2125da1b14580bf120a8d25599**. APK signature verified against the development certificate above.
- Artifact IDs: debug **12448616985**; verification reports **12447844367**; emulator reports **12450040435**. ZIP digests respectively **da5db26c96cb079d7931bbeddd500acc2fbb7ad359fd416e7a319189a8a6a8f3**, **46e32b3f859af2c154d58475ee17e55c67bdb526e6273693503244fb57c71e87**, **b019ab21113d3326f926123b33f126e88b0f049fd62ac0165be6c67c8e4ef6ba**; each checked before extraction.
- Final update on TECNO: **adb install -r succeeded**, V2 focus event counts remained intact, V1 remained installed, accessibility connected, model loaded. Screen sharing is **off after update**; starting another session requires fresh owner-operated Android consent. This final build has not completed the physical AI capture journey.

The evidence package contains selected synthetic emulator reports/screenshots, host test/lint reports, conversion engineering proof and aggregate phone observations. It excludes owner screenshots, private signing material, journals, credentials and datasets.
