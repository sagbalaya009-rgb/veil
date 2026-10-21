> Historical context: this document describes an earlier Veil scope or build. For the current implementation and executed evidence, use docs/INDEX.md and docs/releases/ENGINEERING_DELIVERY_REPORT.md. Earlier device gates, scalar-model contracts, feature restrictions and benchmark targets are not claims about the current experimental build.

# Pre-test security audit

Date: 2026-08-26

## Result

The first debug APK was **not the right build to install for testing** because it declared unused sensitive permissions and inactive service components. The foundation build has now been hardened and rebuilt.

The hardened APK contains only the launcher activity from Veil's own code. Its packaged manifest requests no Android dangerous or special permissions. The only remaining generated permission is the non-exported AndroidX dynamic-receiver permission created by a library. It has no `INTERNET`, overlay, Accessibility, MediaProjection, notification, foreground-service, boot, contacts, SMS, camera, microphone, account, or external-storage permission.

The hardened manifest also sets `android:allowBackup="false"`, `android:fullBackupContent="false"`, and `android:usesCleartextTraffic="false"`.

## Static code findings

The source tree contains no HTTP client, WebView, Firebase/analytics dependency, socket, upload path, file-output call, OCR, accessibility text read, raw event logging, or screen-capture implementation. The current TensorFlow Lite class is only an in-memory classifier boundary; there is no bundled trained model or live MediaProjection pipeline in this slice. Room stores only the documented settings and local protection-event metadata.

The foreground service, Accessibility Service, boot receiver, and sensitive permission declarations remain in the source tree for the documented future integration, but they are not packaged in the hardened foundation APK until their flows are implemented and reviewed.

## Testing recommendation

Only test the hardened APK. Before installation, verify its SHA-256 hash and install it on a secondary/test device if available. During this phase, do not grant any special permission because the hardened APK does not request them. The first test should cover only onboarding, local settings, home screen, and protection-state UI.

This is a static review, not a formal security audit or proof of absence of all vulnerabilities. Do not use real sensitive accounts or personal data for early testing. Do not enable Accessibility, overlay, screen capture, or notification permissions until a later build explicitly implements and documents those flows.
