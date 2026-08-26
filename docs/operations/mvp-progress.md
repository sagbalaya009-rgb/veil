# MVP Progress

## Completed slice

The first native Android Veil slice is implemented and pushed to GitHub. It contains the Kotlin/Compose Android foundation, MVVM-style ViewModel wiring, Hilt dependency injection, Room local persistence, documented onboarding screens, protection setup selections, the protection state machine, temporal confidence evaluation, the TensorFlow Lite classifier boundary, a foreground monitoring-service foundation, the minimal Accessibility Service declaration, reboot entry point, and the documented intervention screen.

## Verification

The following command passes locally:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`. The domain tests cover activation, the 0.90 confidence threshold, cooldown timing, recovery completion, emergency override behavior, temporal confirmation, and reset behavior.

## Scope boundary

The current classifier adapter is ready for a real TensorFlow Lite model but does not include a trained model asset. MediaProjection capture, permission-launch flows, live classifier invocation, and full persistence-driven service recovery remain the next integration steps. No screenshot storage, cloud processing, OCR, raw accessibility logging, or remote monitoring has been added.

## Device validation

ADB is installed and operational, but no USB Android device was visible during this build session. Once the device is connected and its RSA authorization prompt is approved, install the debug APK with:

```text
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Device validation must then cover onboarding, Android permission flows, service behavior, intervention rendering, reboot recovery, battery use, and OEM-specific behavior as defined in the MVP documentation.
