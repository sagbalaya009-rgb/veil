# Synthetic capture fixture (test only)

This separate APK displays a generated geometric image without people or explicit media. It contains no classifier, scores, permissions, networking, or intervention logic. It is never bundled into the Veil app APK.

The image was deliberately optimized to produce a high numeric response from the real bundled classifier. This is an adversarial false positive used only to test actual MediaProjection → actual model → unchanged temporal policy → persisted cooldown → actual accessibility overlay. It proves integration, not semantic accuracy, pornography detection quality, or absence of false positives. Screen resampling can change the model response, so the complete emulator journey must pass before recording success.

Display: portrait, immersive full screen, nearest-neighbor stretch of the PNG to the content surface. Android MediaProjection performs its own display downsampling. The app has no input or fake-model injection path.

Build/install this APK only on an isolated test emulator before running `com.veil.app.CaptureJourneyTest` in the Veil instrumentation APK. That test drives Android's real sharing-consent UI. Accessibility enabling is isolated emulator setup, saved/restored by the test; capture consent is never granted by shell/app-ops or token fabrication.
