# Veil V2 baseline audit — 2026-10-22

Baseline bf1b86a24dbd935ddedb865efb1cba0d4d874038; clean checkout checkpoint preserved on codex/veil-mvp; V2 work on codex/veil-v2.

## Existing implementation and evidence
Native Kotlin/Compose single Android app plus isolated capture fixture. Started mediaProjection FGS, private bounded commitments, package-only accessibility overlay, real five-output MobileNetV2 TFLite asset, Room v5 notes/preferences, offline Python workshop, JVM/native CI. No internet permission or backend. Previous source's seven Android tests passed on API35 emulator; earlier physical installation record independently verifies TECNO Android14 installation, focus overlay/expiry/recovery/exit, user-approved capture start/stop. It does not verify semantic accuracy or Recents survival.

## Root causes and reproduction status
- Feedback: exact source trace finds FALSE_POSITIVE only as an event and cooldown dismissal. The visible Incorrect detection — stop guardian control explicitly stops MediaProjection. It does not train, modify thresholds, or create allowlists. Thus accidental use can stop the current session, but cannot alter model weights/global sensitivity. Unit/regression checks will protect independent V2 feedback; reported historical device use cannot be reconstructed from absent telemetry.
- Recents: Activity has no onStop/onDestroy shutdown hook. Capture is already a started FGS and manifest's omitted stopWithTask defaults false. No reproduced source proof that swiping the task stops capture. OEM kill, accessibility unbind and projection revocation remain hypotheses; require real authorized lifecycle trials. Do not invent a root cause. Immediate isRunning gate closure is missing in finishSession until onDestroy and warrants correction.
- Capture: acquireLatestImage, two bounded buffers, 500ms cadence, transition-frame discard, foreground revision recheck, two-of-three >=.90, static-buffer starvation and full-frame224 resizing explain plausible missed scenes; not semantic measurements. Sensitive and protected surfaces unavailable. No data justifies lowering thresholds or claiming a better model.
- Model contract: FLOAT32 RGB/255 nearest224, five original labels, explicit=hentai+porn. Hash/shape/finite validation retained. No independent semantic set, calibration, classwise/bias/video metrics. Unresolved upstream training-data provenance.
- Workshop: separate binary MobileNetV3 training scaffold has preprocessing/export consistency defects, lacks near-duplicate split checks and calibration/comparison reports. Repair tooling without pretending to fine-tune on absent approved data.
- Recovery/UI: breathing works; transient reflection, manual preview and existing Room notes remain. Missing goals, editable coping plans, optional trigger/urge records/outcomes, setback support, neutral-by-default V2 personalization and cohesive status hierarchy. Preserve healthy foundations and migrate no religious preference implicitly.

## Security/privacy and architecture
No root/admin/covert recapture, no screenshot/URL/message histories, no server. Preserve system/emergency exits and consent each capture session. New feedback must be bounded local metadata only, independent of policy/training; new recovery records separately deletable and excluded from technical diagnostics. App-private storage/backup exclusion are not claims of end-to-end encryption. Debug package is not a production-signed/store-approved release.

## Implementation decisions
Retain model until independent comparisons justify change; implement truthful structured health and non-sensitive aggregate diagnostics, independent feedback+undo/reset, real local recovery workflow and minimal redesigned Compose navigation. Explicit stopWithTask=false documents desired FGS ownership; no sticky token restart. Preserve legacy notes and all authorized user data. Tests must distinguish pure-policy, native/emulator, actual device, semantic and performance evidence.

## Required verification
Run expanded Python/JVM tests, lint, APK and fixture builds, merged-manifest privacy checks, and emulator journeys on exact V2 commit. Existing phone requires compatible signing for data-preserving update and fresh owner consent for capture. Keep any physical, semantic, font/TalkBack, battery/thermal and OEM gaps open until executed. Baseline findings above are source-backed except explicitly marked prior physical/CI evidence; no new physical defect reproduction claimed.
