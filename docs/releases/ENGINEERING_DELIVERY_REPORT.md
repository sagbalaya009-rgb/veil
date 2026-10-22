## VEIL — ENGINEERING DELIVERY REPORT

Date: 2026-10-22. Experimental engineering delivery; physical and semantic qualification remain open.

### 1. Executive summary

Delivered an executable Android implementation of voluntary focus commitments and user-consented local visual screening, with genuine bundled TFLite inference, reversible app-specific interventions, recovery, minimal local history and privacy controls. Source is on `codex/veil-mvp`; [draft PR #1](https://github.com/sagbalaya009-rgb/veil/pull/1) preserves review before merge. This is not a production-ready, store-approved or accuracy-qualified filter.

Final build commit: `bcb44a9e6940ae62ae411dc9f7e17e15eef0ffd9`. Final CI: [successful Android verification run](https://github.com/sagbalaya009-rgb/veil/actions/runs/39723402183). The installable APK and checksum are recorded in section 9. Documentation-only changes after that build are separate; no app/model changes are included after the recorded APK commit.

### 2. Repository reconstruction

Inspected the existing tree and Git history from baseline `71bd46a457cfe04ad45b7ddfc315c913f8d9222e`. Reused Kotlin/Compose/Material3, Hilt, Room migrations and notes, offline language/worldview settings, the recovery-plan UI, temporal policy foundations and the existing ML safety workshop. Retained the separate website without deployment changes.

Replaced an inactive scalar-classifier/capture scaffold with an actual five-score model adapter, fresh MediaProjection consent and bounded frame processing. Added authoritative running/health state, foreground package attribution, persistent rules and monotonic deadlines, focus UI, explicit disclosures, accessibility overlays, tests and CI. Repaired NaN policy acceptance, permission-denial notification handling, bitmap ownership/row padding, resource cleanup, attribution/stop races, stale-overlay reattachment after Home/recovery navigation, cold-start on-disk retention, direct recovery routing and emulator provisioning. Historical specifications are labelled; current ADRs and the audit explain scope conflicts. Main and original history were retained; no merge, store publication or owner signing was performed.

### 3. Implemented MVP features

- Understandable onboarding, Home/Focus/Reset/History/Privacy navigation and visible model/service availability.
- Separate voluntary accessibility and screen-sharing disclosures; real Android sharing consent and visible foreground service; clear stop and unavailable states.
- Launchable-app focus selection, durations from one minute through seven days, delayed starts up to one day, persistent same-boot rules, early exit and actual expiry policy.
- App-specific AI cooldowns of 5/10/15 minutes, two-of-three provisional score confirmation, Home/recovery exits, immediate emergency exit, and incorrect-detection reporting that stops capture before clearing the restriction.
- Guided one-minute breathing, reflection, self-reported recovery activity and retained local recovery notes/plan.
- Capped local event history, scoped deletion, theme preference and protection that can always be stopped. No accounts, analytics, remote moderation or payment service.

Implemented behavior and executed acceptance evidence are distinguished in [the acceptance matrix](../testing/ACCEPTANCE_MATRIX.md). UI focus setup beyond disclosure, delayed activation, reflection/legacy-plan interactions and OEM lifecycle scenarios still require the physical checklist. The direct recovery route and breathing guide ran on the emulator.

### 4. Android architecture

Kept the existing compatible native stack: Java 17, Gradle 8.10.2, AGP 8.7.3, Kotlin 2.0.21, Compose/Material3, Hilt 2.52 and Room 2.6.1; minSDK 31, compile/target 35. A compatible build was preferred over an unrelated major-toolchain migration; versions, alternatives and limitations are recorded in ADRs.

`CommitmentStore` owns an atomic private preference snapshot and StateFlow, with elapsedRealtime deadlines plus boot identity. Existing Room notes remain a separate persistence resource. `MonitoringRuntime` represents actual ephemeral service health and foreground revisions. The non-sticky mediaProjection service owns capture and inference on one worker; the package-metadata-only accessibility service owns dismissible overlays. Reboot ends commitments on evaluation and never resumes capture. Configuration changes terminate sharing and require fresh consent.

### 5. AI model

Integrated the real GantMan MobileNetV2 1.40/224 five-category model from a pinned distributor commit, with full MIT notice and source links. The bundled asset is 17,355,548 bytes; SHA256 `380f98f7685f9d8a386f8cc595b6dfcb972989aae3d1b8b270d3a4a5b96fab40`. Loader validates code/metadata/bytes hash, FLOAT32 tensors `[1,224,224,3]` and `[1,5]`, labels and output validity. RGB nearest resizing and division by 255 follow the documented upstream contract. Android runtime is TensorFlow Lite 2.16.1, two CPU threads.

Explicit score groups `hentai + porn`; suggestive score remains separate. The 0.90 threshold and rolling confirmation are provisional, uncalibrated policy. Real inference and synthetic numerical parity checks establish execution, not explicit-content accuracy. No labeled content set was acquired, no precision/recall/F1 or demographic robustness is claimed, and no cross-model accuracy benchmark was run. MIT redistribution evidence is documented; complete individual training-image rights/age/consent provenance and original conversion parity remain unresolved. See [selection](../ai/MODEL_SELECTION.md) and [evaluation](../ai/MODEL_EVALUATION.md).

### 6. Testing

All three CI jobs passed on the recorded build commit.

| Check | Executed result |
| --- | --- |
| Gradle JVM tests | **22 passed**, 0 failed/errors across six test classes |
| Python ML safety/evaluation tooling | **11 passed**; actual bundled desktop model smoke also passed |
| API 35 x86_64 Android instrumentation | **7 passed**, 0 failures/errors/skips; total159.325 seconds |
| Real capture integration | Actual Android consent → MediaProjection → bundled native model → unchanged numeric threshold/temporal policy → real overlay; actual launcher exit and direct Reset navigation passed |
| Real recovery/focus | Breathing guide completed after **60.227s**; focus with AI off expired after **61.364s**; false-positive stop/no immediate re-block and emergency exit passed |
| Persistence/privacy | Room settings/notes survived close/reopen; rule/history boundaries passed; expired history was removed from the actual private XML and backup on cold construction |
| Android lint | **0 errors,20 warnings,1 informational**; no baseline/suppression added |
| Packaging/manifest | Debug, Android-test, fixture and unsigned-release packaging; wrapper/source-whitespace and merged-manifest privacy gates passed |
| Downloaded binary | GitHub ZIP digest, APK v2 signature, model hash/license, permissions, backup rules and absence of fixture resources verified |

The accessibility binding is genuinely created by Android using isolated emulator secure-setting provisioning, with prior values restored. Projection consent is clicked in the real system UI. Focus creation in the capture test uses the store API; complete focus-selection/delay UI qualification remains on the phone checklist. The synthetic geometric trigger is an intentionally optimized false positive and proves numeric integration only. Test timings are real elapsed journey times, not detector latency or proof that a person followed breathing/exercise instructions. Verify and emulator jobs compile the same commit separately; their debug signing keys may differ.

Failures found and fixed include notification-permission handling, stop/attribution races and actual stale-overlay reattachment after Home navigation. The capture harness was corrected for real accessibility binding, activity synchronization and Android's visible immersive tutorial. Runner SDK downloads/AVD setup and post-test UTP cleanup failures were separated from application failures. All prior failures remain in Actions; [the verification log](../testing/VERIFICATION_LOG.md) records their outcomes. No test was disabled, threshold lowered or classifier score fabricated.

Earlier Actions storage quota failures used an unpublished private draft fallback. Normal uploads later resumed; when GitHub reported the repository public, the helper correctly refused private-only delivery. CI uses that fallback only after normal upload failure. Repository visibility was not changed by this implementation.

Missing evidence: physical TECNO lifecycle/consent/accessibility behavior; API 31 coverage; TalkBack/large-font usability; battery, thermal and memory profiling; protected/multi-window behavior; reboot/process-kill qualification; runtime file/log/network inspection; representative lawful semantic evaluation. A source-level threat model and code review are not a complete security scan or penetration test.

### 7. Privacy and security

Merged-manifest checks require no INTERNET or broad data/device permissions, backups and cleartext disabled, protected exported services, and explicit cloud/device-transfer exclusions. Capture is non-exported, freshly consented, non-sticky and stoppable; accessibility disables window-content retrieval and gestures and uses package/class routing metadata only. Essential system, launcher, settings and call packages are excluded with an immediate safe exit. OEM exclusions still need real-device qualification.

Production capture never writes or uploads frames, URLs or content logs. Latest bounded frames, reusable cleared pixel/tensor buffers, image/bitmap ownership and serial cleanup limit retained memory; cryptographic native-memory erasure is not guaranteed. Current event metadata contains kind/time only, capped at 100 and filtered after 30 days when read/evaluated; force-stop cannot run scheduled purges. Notes remain private Room data without added application-layer encryption. Scoped in-app deletion is labelled; Android Clear data/uninstall removes all private app data. Only emulator tests export benign synthetic screenshots and diagnostics.

A threat model was independently reviewed with a fresh context and updated to final architecture. It records hypotheses and unresolved risks, not a clean security certification. The retained website has a distinct logging/proxy boundary and does not inherit the APK offline promise.

### 8. Performance

Measured desktop inference using Python 3.12/LiteRT 2.1.2 on Windows, two CPU threads: median 125.93 ms and p95 152.66 ms across 26 measured invokes after two warmups, on generated color inputs. The final Linux x86_64 GitHub runner, Python 3.12.15/LiteRT 2.1.2, two threads, measured median **7.72 ms**, p95 **9.11 ms** over 26 invocations after two warmups. These are different desktop/VM environments. These timings exclude Android capture/preprocessing/JNI/overlay costs and are not phone throughput. Android inference tests check genuine native execution and numerical references; emulator journey timings are not model latency benchmarks.

Source bounds: at most 720 pixels on the capture long edge, ImageReader capacity 2, acquireLatestImage, at most 2 inference attempts/second, stale-result rejection after 2 seconds and no accumulated frame queue. No TECNO battery, thermal, memory or sustained latency result is available. The physical checklist defines a controlled 30-minute comparison.

### 9. APK

Deliverable: `Veil-0.2.0-experimental.apk`, **58,128,457 bytes** (58.1 MB decimal).

SHA256:

```text
fa5d97675ad0d3d8f8500fab4520286a3898807f76a56be3d7f2da121ac8da19
```

[Download its GitHub Actions APK artifact](https://github.com/sagbalaya009-rgb/veil/actions/runs/39723402183/artifacts/12402246881) and extract `app-debug.apk`, or use the supplied renamed APK. Both contain the same bytes. `SHA256SUMS.txt`, the engineering report, phone checklist and `Veil-verification-evidence.zip` accompany the local deliverable. The evidence ZIP contains actual Gradle/lint/manifest reports, native test XML and benign synthetic screenshots, model smoke, binary checks and normalized CI results. No private user screens or credentials are included.

Version `0.2.0-experimental` (code 2), package `com.veil.app`, Android 12/API 31 minimum. The debug APK is installable and debuggable, suitable for deliberate engineering testing. Unsigned release packaging is also built; it is not an installable owner-signed release. Hosted debug signatures may change, so an update can require uninstalling an earlier test build; preserve wanted notes first. No signing keys or credentials are committed. Test-only capture fixture APK is excluded from the consumer APK.

### 10. Physical-device validation

No TECNO SPARK 30C was connected; no physical pass is claimed. [Phone installation and exact checklist](../testing/DEVICE_TESTING.md) cover Android/HiOS version recording, voluntary grant/deny/revoke, real sharing and all stop controls, selected/unselected apps and delayed focus, expiry, recovery and safe emergency/Home/Settings/dialer access, lock/rotation/process termination/reboot, privacy deletion, largest font/TalkBack, and battery/thermal/service-survival measurements. Do not place emergency calls for testing, bypass restricted settings or disable platform protections.

### 11. Known limitations

Semantic accuracy and threshold calibration are unmeasured; small feed images, full-screen distortion, art/medical/fitness imagery, demographic variation and adversarial content may produce errors. Static surfaces may not supply enough new frames for temporal confirmation. Secure/DRM/FLAG_SECURE and unsupported surfaces cannot be promised capture. Consumer Android permissions, force-stop and uninstall remain under user control. Attribution in multi-window/OEM transitions is best effort. TargetSDK35 is retained for this engineering build; current Play eligibility and native-library/16KB-page compatibility are not qualified. All Play declarations require review before publication. Debug signing, model provenance, physical qualification and remaining lint warnings prevent production qualification.

### 12. Next actions

1. Install the verified debug APK on the owner's TECNO SPARK 30C and record the physical checklist, including reliable safe exit and every permission-revocation path.
2. Define semantic acceptance criteria and evaluate an independently reviewed lawful labeled set with representative benign cases; calibrate on a separate set and resolve training/conversion provenance before broad release.
3. Measure sustained phone battery/thermal/memory/latency and qualify supported layouts, Android versions and HiOS service behavior.
4. Resolve release-relevant lint/native compatibility findings, establish owner-controlled signing and complete Play/privacy declarations only if publication is separately authorized.
