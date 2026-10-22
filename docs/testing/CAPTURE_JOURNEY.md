# Real consent capture integration test

`CaptureJourneyTest` is an emulator-only test. It refuses physical hardware by checking the emulator's hardware/build identifiers. No physical user settings should be changed for this test.

## Scope and limitations

The synthetic fixture is an intentionally optimized geometric false positive. The full journey tests Android consent UI, actual MediaProjection frames, the real bundled model, the unmodified production threshold and temporal policy, cooldown persistence, app-specific accessibility overlay, Home exit, direct Reset routing, a real one-minute breathing guide, and false-positive recovery. With screen sharing then stopped and AccessibilityService still bound, the test creates an actual one-minute focus rule, waits for real elapsed time and service-driven expiry, verifies FOCUS_COMPLETED and overlay removal, and tests another focus rule through the real emergency-exit overlay control. It does not modify clocks or deadlines. Focus rule setup uses the store boundary; UI rule setup is covered separately. It is a numeric integration test. It does not validate explicit-content accuracy, pornography detection, real feed quality, physical-device behavior or battery life. A failing synthetic score after Android screen resampling is a real integration-test failure and must not be hidden by lowering the threshold, replacing classifier outputs or granting projection through shell commands.

The instrumentation test provisions only AccessibilityService through secure settings in its isolated emulator, saving/restoring both prior setting values in finally. Android actually binds that production service. Sharing requires clicking the real system consent UI. The test does not obtain projection tokens directly and does not use app-ops to bypass consent. UIAutomator is configured with FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES so it does not suppress the very service under test; its previous flags are restored.

## Integration

Add `include(":capture-fixture")` to settings.gradle.kts and `androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")` to the app module. The fixture module reuses the repository Android/Kotlin plugins. Build the fixture APK and install it before invoking connectedDebugAndroidTest:

```sh
bash gradlew :capture-fixture:assembleDebug :app:assembleDebug :app:assembleDebugAndroidTest
adb install -r capture-fixture/build/outputs/apk/debug/capture-fixture-debug.apk
bash gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.veil.app.CaptureJourneyTest
adb pull /sdcard/Download/veil-capture-evidence capture-evidence
```

Use an API35 default x86_64 Pixel 2 emulator in English, portrait 1080x1920, matching the screen-resampling qualification. Other dimensions need their own fixture qualification. Use the ordered `VeilVerificationSuite` for the full seven-test run; its onboarding test runs before deliberate local-state mutations. The test first opens the fixture and confirms Android's visible immersive education using its ordinary Got it button while capture is off and no rules can create an overlay. It returns Home, then performs the actual disclosure/consent/capture journey. This prevents a stale tutorial node behind Veil from redirecting a coordinate click onto the real emergency exit. Tutorial clicks are forbidden during capture. Activity launch and clickable control selection are synchronized. A tiny 4 × 4 pixel visual pulse in the fixture ensures new display buffers for real temporal confirmation. The fixture APK must never be merged into or distributed as the Veil consumer app.

Evidence contains synthetic-only screenshots, consent/overlay view hierarchies, and `capture-journey.json` with actual timing and assertion outcome. The timings are test journey timings, not model latency or battery benchmarks. Both the breathing and focus assertions require at least 60,000 milliseconds each of real monotonic elapsed time; each timeout allows service tick and emulator scheduling overhead. Only this emulator test copies its synthetic evidence to the emulator Download directory before UTP uninstalls the target; production code never writes captures. Artifact collection runs after test failure too. Report a pass only from the executed instrumentation result and generated JSON; source existence is not proof of behavior.

## Sources

- [UiAutomation flags and accessibility suppression](https://developer.android.com/reference/android/app/UiAutomation)
- [UIAutomator configuration](https://developer.android.com/reference/androidx/test/uiautomator/Configurator)
- [MediaProjection consent and lifecycle](https://developer.android.com/media/grow/media-projection)
