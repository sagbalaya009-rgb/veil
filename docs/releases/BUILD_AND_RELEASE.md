# Build and release

The checked-in wrapper uses Gradle 8.10.2 with Android Gradle Plugin 8.7.3,
Kotlin 2.0.21, Java 17, compile/target API 35 and minimum API 31.
This intentionally retains the existing compatible stack. Android's
[AGP 8.7 compatibility table](https://developer.android.com/build/releases/agp-8-7-0-release-notes)
requires Gradle 8.9 or newer and JDK 17, and supports API 35.

Install JDK 17 and Android SDK command-line tools, then install
`platforms;android-35` and `build-tools;34.0.0` with `sdkmanager`.
Point `ANDROID_HOME` at the SDK, or use an untracked `local.properties`.

On Windows run `powershell -File tools/verify.ps1`. On Linux/macOS run:

```sh
bash gradlew --no-daemon --stacktrace testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease
```

Unit test results are in `app/build/test-results/testDebugUnitTest/`, lint reports in
`app/build/reports/`, and APKs in `app/build/outputs/apk/`.
The debug APK is automatically debug-signed and installable for testing. The release APK is
unsigned and is not an owner-signed production release. Keep release signing keys and passwords
outside Git. There is deliberately no automatic Google Play publication or signing secret.

The GitHub Android verification workflow runs the same tasks and retains reports and APKs.
A Python check verifies privacy properties of the merged debug manifest, including the absence
of network and broad device-data permissions, disabled backups and cleartext traffic, and
permission protection for exported services. A second job boots an API 35 emulator and runs
instrumentation tests. Both jobs use only SDK licenses already present on the hosted runner.
A workflow definition does not itself prove a successful remote run. Device tests are separate;
use [the device matrix](../testing/DEVICE_TEST_MATRIX.md) and record results honestly.
