# Veil

Veil is an experimental, offline Android wellbeing app: voluntary focus commitments, user-consented visual screening and calm recovery activities.

The current unified build combines focus protection and saved notes with encrypted optional Recovery records, coping plans, independent correction feedback, aggregate-only diagnostics and the redesigned interface. It installs as **Veil** (`com.veil.app`), version **0.3.1-unified-experimental**. At the owner's request it replaces the separate V1/V2 phone installations through a clean install; removal erases their app-local data. See the [unified release record](docs/releases/UNIFIED_RELEASE_REPORT.md) for verification and installation status. The [V2 report](docs/releases/V2_RELEASE_REPORT.md) is historical evidence for the separate preview. No new semantic dataset or model training was performed.

The native app includes onboarding, a live-status dashboard, focus app selection and scheduled starts, package-bound dismissible overlays, monotonic timers, local history and privacy controls. Original saved notes are available within Recovery using the current interface. The old simulated-preview dashboard is no longer an active destination; its separate settings are not presented as live protection settings. The separate website is retained without deployment changes.

Visual screening uses a real bundled, checksum-pinned MobileNetV2 TensorFlow Lite model. Android screen sharing always needs explicit consent; capture never restarts silently. The app has no INTERNET permission and does not store screen frames. Accessibility access is optional to the app overall, but required for focus restrictions and visual interventions; it receives app-window metadata without retrieving text or window content. All restrictions can be ended immediately; system settings, launchers and calls are excluded.

**This is an experimental engineering build.** Semantic accuracy on mobile feeds, target-device battery/performance and complete physical lifecycle qualification are pending. A provisional score threshold is not calibrated accuracy. Protected surfaces cannot be analyzed; ordinary Android apps cannot guarantee blocking. See the delivery report for exact build, test and artifact evidence.

## Build

Use JDK 17, Android SDK 35 and the pinned Gradle wrapper:

```text
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease
```

Windows: `gradlew.bat` or `tools/verify.ps1`. CI builds debug APKs and unsigned release APKs, with a separate API35 emulator instrumentation job. A workflow file alone is not evidence of a passing run. Debug APK path: `app/build/outputs/apk/debug/app-debug.apk` after a successful build. [Build and release instructions](docs/releases/BUILD_AND_RELEASE.md).

## Read next

- [Documentation index](docs/INDEX.md)
- [Engineering delivery report](docs/releases/ENGINEERING_DELIVERY_REPORT.md)
- [Repository audit](docs/audits/REPOSITORY_AUDIT.md)
- [Architecture](docs/architecture/OVERVIEW.md)
- [Model source, license and limits](docs/ai/MODEL_SELECTION.md)
- [Model evaluation evidence](docs/ai/MODEL_EVALUATION.md)
- [Privacy architecture](docs/privacy/PRIVACY_ARCHITECTURE.md)
- [Android permissions](docs/compliance/ANDROID_PERMISSIONS.md)
- [Device test matrix](docs/testing/DEVICE_TEST_MATRIX.md)
- [Contributor instructions](AGENTS.md)

Minimum Android version: 12 (API31). No accounts, paid backend, remote moderation or analytics. Project source is Apache-2.0; the bundled model carries its separate upstream MIT notice. Model training-data provenance remains incomplete; no screenshot-domain performance or independently audited training rights are claimed.
