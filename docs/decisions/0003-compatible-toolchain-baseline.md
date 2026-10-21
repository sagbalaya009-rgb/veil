# ADR 0003: Preserve a compatible build baseline while qualifying the platform integration

Status: accepted for the experimental branch,2026-10-21.

The repository had Kotlin2.0.21, AGP8.7.3, Gradle8.10.2, Compose BOM2024.12.01/Material3, Hilt2.52, Room2.6.1 and Lifecycle2.8.7. Retain them while reconstructing the missing runtime, with lifecycle-runtime-compose matching2.8.7. The [official AGP8.7 compatibility table](https://developer.android.com/build/releases/agp-8-7-0-release-notes) specifies JDK17, Gradle8.9 minimum, API35 maximum and Build Tools34.0.0. This baseline intentionally fits that combination. It does not claim to be the latest stable stack.

A current official documentation review found newer alternatives:

| Area | Current documentation reviewed | Decision |
| --- | --- | --- |
| Android toolchain | [AGP9.3](https://developer.android.com/build/releases/agp-9-3-0-release-notes), [Kotlin compatibility](https://kotlinlang.org/docs/gradle-configure-project.html) | A coordinated major-toolchain migration adds risk before runtime qualification; retain compatible baseline |
| Compose/Material3 | [BOM guide](https://developer.android.com/develop/ui/compose/bom) shows2026.09.00 | Retain existing BOM; avoid changing the design/runtime/compiler combination while introducing capture |
| Hilt | [Official setup](https://dagger.dev/hilt/gradle-setup) shows2.60.1 | Retain2.52 with existing KSP/Kotlin combination pending dedicated upgrade verification |
| Room | [Release page](https://developer.android.com/jetpack/androidx/releases/room) lists2.8.5 | Preserve working2.6.1 migrations and existing user notes |
| Lifecycle | [Release page](https://developer.android.com/jetpack/androidx/releases/lifecycle) lists2.11.0 | Use2.8.7 consistently for lifecycle-aware Compose collection |
| DataStore | [Release page](https://developer.android.com/jetpack/androidx/releases/datastore) lists1.3.0 | Not added; persistence choice is documented in ADR0002 |
| LiteRT | [Android runtime guide](https://developers.google.com/edge/litert/android) lists2.3.0 CompiledModel and1.4.2 Interpreter line | Existing TensorFlow Lite2.16.1 Interpreter is the initial Android baseline; qualify real model execution first, then migrate runtime with repeatable numerical/device tests |

No ONNX runtime is needed for an already available verified TFLite artifact. CompiledModel/NPU/GPU and quantization are meaningful future experiments, not presumed performance wins. CPU execution is simpler to reproduce across the test emulator and budget target phone. On-device performance and Android16KB native-library compatibility must be qualified before broad release; no Play approval or universal current-device compatibility is claimed.

The source pins direct versions and wrapper checksum; actual resolution/build results, rather than this compatibility reasoning, decide whether this branch is buildable. A future upgrade should separately verify Kotlin/KSP/Hilt, Compose compiler/BOM, SDK/AGP/Gradle, native runtime alignment and model numerical behavior. Do not update versions just to make a table look current, and do not represent old versions as latest.
