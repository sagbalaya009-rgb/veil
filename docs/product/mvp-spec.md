# Veil MVP Specification

## Objective

Prove that a user-controlled, local-only intervention can interrupt exposure to explicit content quickly enough to support a self-control commitment without creating an invasive monitoring experience.

## Functional requirements

| ID | Requirement | Acceptance condition |
|---|---|---|
| FR-1 | Protection levels | The user can select Balanced, Strict, or Nuclear, with documented thresholds and behavior. |
| FR-2 | Screen analysis | Veil captures frames only after explicit MediaProjection consent and analyzes them on-device. |
| FR-3 | Explicit-content detection | A classifier produces a confidence score; the initial intervention threshold is 0.90. |
| FR-4 | App-specific cooldown | After confirmed detection, Veil protects only the triggering foreground application with an input-capturing overlay. Other apps remain usable. The user selects 5, 10, 15, 20, 30, 45, or 60 minutes; the timer cannot be casually skipped. |
| FR-5 | Recovery task | The user completes one configured task: push-ups, breathing, walking, or journaling. |
| FR-6 | Lock periods | The user can choose 2 hours, 8 hours, 24 hours, 3 days, or 7 days. |
| FR-7 | Emergency override | One override is available with a 60-second countdown and a recorded local history event. |
| FR-8 | Status | The home screen shows protection state, lock end time, today's interventions, streak, and settings. |
| FR-9 | Failure states | Revoked permissions or unavailable inference produce a clear warning and never claim protection is active. |
| FR-10 | Persistence | Settings, recovery note, intervention timestamps, recovery completion, streaks, protected-app cooldown state, and lock periods survive process restarts and reboot recovery. |

## Android-first delivery boundary

Android is the only active native implementation target for this MVP. Desktop PC architecture remains documented but its application code, packaging, and platform permissions are deferred. On Android, Veil must make the following boundary explicit: it can protect the app where a confirmed detection occurred, not the entire phone. If the user switches to another app, Veil does not block that unrelated app; when the protected app returns to the foreground before cooldown expiry, Veil restores its protective overlay.

The app must show a degraded state if Accessibility, overlay, MediaProjection, foreground-service, or notification capabilities required for the selected protection level are unavailable. It must never claim that an app is protected when Android has revoked one of these capabilities.

## Required permissions

Veil uses Accessibility Service for foreground-app and intervention-related events, MediaProjection for consented screen capture, overlay permission for the intervention surface, notification permission for status and reminders, and a foreground service for persistent monitoring. Every permission request must explain purpose, timing, revocation, and reduced-functionality behavior.

## Non-goals for V1

The MVP will not store screenshots, record video, upload frames, use cloud image inference, read message contents, perform OCR, provide remote monitoring, or attempt full uninstall prevention. It will not use root access or hidden behavior. It will not claim that it can make a consumer Android application impossible to bypass.

## Edge cases

The implementation must define behavior for screen-off state, rotation, video playback, split screen, low battery, phone calls, emergency services, process termination, reboot, and OEM battery restrictions. Emergency calling must remain available. During calls, overlays should pause when required for safe operation.

## MVP validation checklist

The release candidate must demonstrate explicit-image detection, explicit-video sampling, ignoring representative medical/fitness/beach content, intervention rendering, cooldown timing, recovery completion, permission persistence after reboot, acceptable battery use, and confirmation that no screenshot data is written or transmitted.
