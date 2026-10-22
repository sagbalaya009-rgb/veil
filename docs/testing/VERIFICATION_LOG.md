# Verification record

Each run is evaluated on its exact source commit. A failed setup or optional preview is not a passing workflow, and a synthetic model fixture is not a semantic content evaluation.

| Source/run | Executed result | Resolution |
| --- | --- | --- |
| Initial local environment | Gradle/SDK download timeouts before Android compilation; cached host core fallback20 tests passed | Cloud builds used; host result kept separate |
| Initial cloud lint | Notification call failed MissingPermission lint | Runtime SecurityException handling added without lint suppression |
| 4060d1d | Android build/22 JVM tests succeeded; emulator failed Unknown AVD before tests | Shared explicit AVD home/path |
| [daa0c5f](https://github.com/sagbalaya009-rgb/veil/actions/runs/39682956175) | 22 JVM tests/build passed; six Android tests executed, five passed; accessibility provisioning timed out | Removed literal shell quote characters from direct-argv settings value |
| [a67fff3](https://github.com/sagbalaya009-rgb/veil/actions/runs/39713113680) | ML tests/real smoke and normal upload succeeded; private fallback refused public repository; later job cancelled by newer push | Fallback runs only after normal upload failure and still refuses public repositories |
| [c2844cf](https://github.com/sagbalaya009-rgb/veil/actions/runs/39713569524) | Build/model passed; five of six Android tests passed; disclosure control missed after async launch/scroll | Synchronized actual activity launch and stable clickable-parent selection |
| [d3a8395](https://github.com/sagbalaya009-rgb/veil/actions/runs/39714899829) | Real accessibility binding, Android consent, projection and local analysis ran; numeric trigger timed out with Android immersive tutorial obscuring the input | Clicked actual visible Got it control; no app-ops bypass or threshold change |
| [4df6487](https://github.com/sagbalaya009-rgb/veil/actions/runs/39716028322) | First emulator attempt failed corrupt SDK ZIP; retry reached actual capture/model/cooldown/overlay, then failed safe Home because stale overlay returned over launcher | Retried environment download; fixed production attribution before Home/recovery navigation, strengthened actual-launcher assertion |
| [d3c49fb](https://github.com/sagbalaya009-rgb/veil/actions/runs/39717336714) | All six Android tests passed; actual one-minute focus elapsed60,731ms; final optional preview failed because UTP uninstalled target | Reinstall tested app only for preview after successful tests |
| [b96b853](https://github.com/sagbalaya009-rgb/veil/actions/runs/39718305409) | Build, model and all six Android tests plus preview succeeded | Complete passing baseline; later review found disk-retention cleanup gap |
| [ba69cc7](https://github.com/sagbalaya009-rgb/veil/actions/runs/39719655453) | Actual private-file/backup retention test passed; six of seven tests passed, capture interrupted by tutorial/overlay tap race | Reopening now rewrites expired history; UI diagnosis continued |
| [7d5f9fd](https://github.com/sagbalaya009-rgb/veil/actions/runs/39721842929) | Build/model and six of seven Android tests passed; tutorial node coordinates overlapped a newly added real emergency-exit button, correctly stopping capture | Moved fixture education before any capture/rules and forbid tutorial taps through interventions |
| [bcb44a9](https://github.com/sagbalaya009-rgb/veil/actions/runs/39723402183) | **All three jobs succeeded:22 JVM,11 ML,7 Android tests; lint0 errors/20 warnings/1 informational; native guide60.227s and focus61.364s** | Final verified APK/source; documentation-only updates follow |

The final seven-test suite covers fresh-install onboarding/disclosure cancellation, Room reopen persistence, three commitment-policy/persistence/actual-disk-retention boundaries,15 actual native model invocations, and the real synthetic capture/intervention/Home/direct-Reset/breathing/false-positive/focus-expiry/emergency journey. Focus rule creation in that integration test uses the store API; complete UI selection/delay configuration remains a physical checklist item.
