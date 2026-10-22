# Veil threat model

## Overview

Scope: native Android runtime, its model/build supply chain, and the distinct existing web boundary. Source-based model reviewed independently by a fresh-context specialist on 2026-10-21. This document is architecture/threat analysis, not a completed vulnerability scan or proof of device safety.

Veil is voluntarily controlled by its device user. Fresh Android consent starts a non-exported mediaProjection foreground service. Latest eligible frames pass to native on-device inference; confirmed provisional scores can request a package-bound cooldown. A package-metadata-only accessibility adapter presents reversible focus/cooldown overlays. Existing recovery notes are a separate Room data resource.

| Component | Code evidence |
| --- | --- |
| Consent-scoped capture service | app/src/main/AndroidManifest.xml:24; app/src/main/java/com/veil/app/services/VeilMonitoringService.kt:86 |
| Latest-frame input and cleanup | app/src/main/java/com/veil/app/services/VeilMonitoringService.kt:158,259 |
| Model integrity/native parsing | app/src/main/java/com/veil/app/services/TensorFlowLiteInitializer.kt:39,57,94 |
| Reversible accessibility overlay | app/src/main/java/com/veil/app/services/VeilPlatformServices.kt:41,53,79 |
| Current local commitments/history | app/src/main/java/com/veil/app/services/CommitmentStore.kt:33,40,47,60 |
| Existing Room notes/settings | app/src/main/java/com/veil/app/di/AppModule.kt:22 |

| Deployment or workflow | Resource/capability | Configuration and effective value | Readers/writers | Enforcing control / unknown |
| --- | --- | --- | --- | --- |
| Android capture | Permitted display pixels | Explicit consent intent → one MediaProjection; latest reader, bounded resolution | Veil worker and native classifier | Non-exported FGS, fresh consent, non-sticky restart, cleanup; OEM behavior unverified |
| Android inference | APK asset/native model parser | Fixed models/veil_explicit_v1.tflite and review JSON; FLOAT32 RGB224 → five scores | In-process Interpreter | Code+manifest+bytes hash match; dtype/shape/label checks; no remote import |
| Current rules/history | shared_prefs/veil_commitments.xml, key state | MODE_PRIVATE, selected package rules/deadlines and up to100 event kinds/times | App UID | Android sandbox, backup off and explicit cloud/device-transfer exclusions,30-day filtering and cold-start rewrite; expiry updates while app/service runs |
| Legacy notes/settings | databases/veil.db | Hilt Room builder, existing migrations | App UID / recovery repository | Android sandbox; no app-layer encryption; separate deletion scope |
| Build/release | Source/dependencies/APKs and private draft assets | Google/Maven/plugin repositories, pinned versions and wrapper digest | Build runner / repository collaborators | PR verification uses read-only token. Only codex/veil-mvp push jobs receive contents:write for an unpublished private draft fallback if normal artifact upload fails; helper verifies private repository, event/branch, draft state and exact commit target. No publication or owner signing. See tools/preserve_ci_delivery.py and .github/workflows/android.yml |
| Separate Vite development server | .manus-logs/browserConsole.log, networkRequests.log, sessionReplay.log | Local debug ingestion configured in vite.config.ts:100 | Reachable dev clients → developer filesystem | Not part of Android no-network promise; reachability/operator setup prerequisite |
| Separate optional Vite storage proxy | Operator endpoint from BUILT_IN_FORGE_API_URL | Proxy at vite.config.ts:153 uses operator key reference for presign requests | Configured service and browser | Requires active configured dev server; backend scope unknown; no secret values inspected or reproduced |

## Threat model, trust boundaries, and assumptions

Assets are transient screen pixels, capture authority, foreground observations, local notes/rules, accurate protection status, safe emergency access, model integrity and APK integrity. An ordinary other app may display adversarial images or cause normal foreground transitions; it is not assumed able to read Veil private files, replace APK assets or bind a signature-protected platform service. The device owner may revoke permission, force-stop, clear data or uninstall; these intended controls are not vulnerabilities.

Android provides MediaProjection consent/indicators, BIND_ACCESSIBILITY_SERVICE binding (manifest:25), private app storage and protected surfaces. Accessibility XML disables content retrieval (app/src/main/res/xml/veil_accessibility_service.xml:7). The service reads only package/class routing metadata, not text/nodes. Screen capture remains sensitive even without network/storage. The no-INTERNET guarantee must be checked against the merged delivered APK, not source alone. Rooted/debuggable/compromised device access exceeds the ordinary-app threat boundary; a debug APK intentionally permits authorized debugging.

Model metadata references are not network destinations. Hash/tensor checks establish artifact integrity/contract, not classifier correctness, conversion parity or training-data rights. Native dependencies remain a supply-chain and memory-safety surface. Cold-start construction rewrites pruned metadata even without a user edit; a regression test checks the private XML and backup. Data retention runs only while state is read/evaluated; force-stop cannot provide a scheduled purge. Room notes and current history are distinct: current in-app deletion names its scope, while Android Clear data removes both. The website has different local-storage/debug/proxy behavior and must not inherit APK privacy claims.

## Attack surface, mitigations, and attacker stories

These are hypotheses and engineering risks, not confirmed vulnerabilities.

| Priority | Scenario / possible gain | Prerequisites and impact | Existing controls | Mitigation / evidence |
| --- | --- | --- | --- | --- |
| High | Malicious release dependency/model exposes sensitive native inputs | Compromised dependency/build path; potential frame or application compromise | Pinned versions, wrapper hash, code-pinned model hash, no model import/INTERNET | Review dependency/model changes and signed release chain; initializer:39,57,94 |
| High | Overlay obstructs essential device access | OEM packages or unexpected foreground events; interference with calls/settings | Essential package exclusion, locked/noninteractive suppression, first emergency button, scrollable overlay, no gestures | Physically verify TECNO call/settings/launcher/font-scale scenarios; CommitmentStore.kt:132; VeilPlatformServices.kt:45,54 |
| Medium | Foreground attribution race intervenes on wrong app | Rapid transitions, split screen, delayed native inference; incorrect friction | Foreground revision check, stale-frame drops, confirmation resets, safe dismissal | Qualify multi-window/OEM behavior; VeilMonitoringService.kt:158 and subsequent inference checks |
| Medium | Permission loss leaves false active status or capture resources | Revocation/process/lifecycle race | Real running/health StateFlow, callback/health checks, non-sticky service, worker cleanup | Execute rapid start/stop, rotation, lock/revocation tests; VeilMonitoringService.kt:259; VeilPlatformServices.kt:92 |
| Medium | Sensitive frame leaks to files/logs | Future regression or third-party native behavior | No frame file/log API in capture path, buffers cleared, no INTERNET, backup off | Inspect final APK permissions/files/network and logs with synthetic media; ScreenClassifier.kt:61; manifest:16 |
| Medium | Adversarial/ambiguous content causes false negatives or positives | Model domain shift, small feed thumbnails, medical/art/fitness imagery | Actual scores validated; temporal threshold and immediate incorrect-detection exit | Lawful independent semantic evaluation and calibration; no accuracy claim yet |
| Low | User changes clock/reboots to end commitment | Device-user action; self-only intended voluntary friction | elapsedRealtime deadline and boot identity; reboot ends rules | Document explicitly, do not add anti-uninstall/revocation behavior; CommitmentStore.kt:101 |
| Conditional | Separate dev logging/proxy exposed beyond intended host | Active reachable web dev server plus operator configuration | Distinct deployment and conditional endpoint | Separate web review required before deploying that tooling; vite.config.ts:100,153 |

## Severity calibration

Critical would require demonstrated broad device/account compromise or sensitive capture exfiltration crossing a real privilege boundary; none is established here. High includes reachable frame leakage or interference with emergency access under a plausible device configuration, with actual controls/prerequisites evaluated. Medium covers reachable lifecycle/attribution or data-handling failure causing meaningful unintended restriction or sensitive local exposure. Low includes bounded, recoverable self-only metadata or usability effects without new authority.

A model prediction error is not evidence of code execution; a hash check is not proof of semantic quality. An owner voluntarily ending protection is not an attack. A build contributor already controlling signed release code is different from an ordinary runtime app. Missing device tests lower confidence and leave risks unresolved; they do not establish exploitability or a clean bill of health.
