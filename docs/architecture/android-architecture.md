# Android Architecture

## Architectural pattern

Veil follows **MVVM with Clean Architecture**. The domain layer owns protection rules and must remain testable without Android framework dependencies. Platform adapters handle permissions, screen capture, accessibility events, overlays, notifications, and service lifecycle. UI observes state rather than directly controlling long-running work.

## Runtime flow

```text
User consent
   |
Permission coordinator
   |
Foreground monitoring service
   |\
   | +--> Accessibility adapter: foreground and lifecycle events
   | +--> MediaProjection adapter: consented screen frames
   |
Frame preprocessor
   |
On-device TensorFlow Lite classifier
   |
Confidence and temporal evaluator
   |
Protection engine
   |\
   | +--> Intervention overlay
   | +--> Cooldown and recovery task
   | +--> Local event repository
   |
Status UI and notification
```

## Modules

| Module | Responsibility |
|---|---|
| `core` | Protection state machine, thresholds, cooldowns, lock periods, recovery tasks, and domain events. |
| `services` | Foreground service, MediaProjection capture, Accessibility Service integration, overlay lifecycle, boot recovery, and notifications. |
| `data` | Local settings, event history, streaks, and lock-state persistence. |
| `ui` | Onboarding, permission education, home/status, intervention, settings, and recovery-task screens. |
| `app` | Dependency injection, navigation, application startup, and Android composition root. |
| `tests` | Unit, integration, device, battery, OEM compatibility, and model evaluation suites. |

## Capture and inference policy

The MVP samples at 0.5 FPS when idle, 1 FPS during ordinary use, and 2 FPS during active scrolling. Sampling stops when the screen is off or during cooldown. Frames are processed in memory and discarded after inference. The pipeline must use bounded buffers, avoid unnecessary bitmap copies, and enforce a latency budget below 800 ms.

## Failure handling

If MediaProjection is revoked, Veil stops capture, marks protection degraded, and requests reauthorization. If Accessibility is revoked, Veil disables only the capabilities that depend on it while preserving any safe partial protection. If overlay permission is revoked, Veil warns that interventions cannot be displayed. If the model is unavailable, the product must fail safely and report that protection is temporarily unavailable.

## OEM and reboot resilience

The service uses a foreground notification, persists state, verifies permissions, and attempts recovery after `BOOT_COMPLETED`. Onboarding includes vendor-specific guidance for battery optimization and auto-start behavior, with priority testing for Samsung, Pixel, OnePlus, Xiaomi, Oppo, Vivo, Tecno, and Infinix devices.

## Security boundary

The MVP requires no network permission. No remote control, stealth mode, screenshot archive, or third-party monitoring pathway may be introduced through platform adapters or debug tooling.
