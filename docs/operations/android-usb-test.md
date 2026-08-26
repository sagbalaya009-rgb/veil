# Android-first USB device test procedure

## Current build status

The Android project compiles and its unit tests pass with:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The current environment does not detect an attached Android device through ADB, so the following real-device checks remain pending.

## Connect safely

1. On the Android phone, enable Developer options and USB debugging.
2. Connect the phone through a trusted USB cable.
3. Approve the Android RSA prompt only for the intended development computer.
4. Confirm the device appears as `device` with `adb devices -l`.
5. Install the debug build with `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

## Required Android-first checks

| Test | Expected result |
| --- | --- |
| Launch and onboarding | App launches, explains local-only processing, and stores the selected recovery note locally. |
| Permission transparency | No sensitive permission is requested until the corresponding feature is deliberately enabled. |
| App-targeted cooldown state | A simulated confirmed detection associated with X targets only the X package; Chrome remains allowed by the domain engine. |
| Cooldown UI | The intervention surface shows remaining time, target package, selected recovery task, and saved note. |
| Permission loss | If a future required service is unavailable, Veil shows a degraded state rather than claiming protection. |
| Privacy | No raw frames, screenshots, browser history, accessibility text, or network traffic are retained by the MVP. |

## Before live TensorFlow Lite detection

Do not enable live screen capture or app-specific overlays on a personal device until a reviewed `.tflite` model is bundled, temporal confirmation is connected, and the permission and failure-state flows have been exercised on a secondary/test device. The present initialization scaffold does not include a classifier model and cannot perform live detection.
