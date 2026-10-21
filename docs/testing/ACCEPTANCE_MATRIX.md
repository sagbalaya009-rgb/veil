# Acceptance evidence matrix

Implemented source and executed validation are separate. See ENGINEERING_DELIVERY_REPORT.md for the final commit/run/artifact. A source filename alone is not a passing acceptance test.

| Acceptance area | Current implementation | Required evidence / remaining qualification |
| --- | --- | --- |
| App builds and installs | Gradle native project/CI configured | Actual Gradle APK packaging and emulator install/launch results |
| First launch/navigation | Dashboard onboarding, tabs and retained recovery plan | VeilJourneyTest on actual emulator; physical small-display/font-scale/TalkBack checks |
| Persistence | Room notes and private commitment snapshot | Android CommitmentStoreTest plus process recreation/reboot device scenario |
| Voluntary consent | Separate accessibility disclosure and fresh Android projection dialog | Cancellation UI test; physical permission grant/revoke/projection tests |
| Monitoring lifecycle | Non-sticky FGS, cleanup, health/paused status, safe stop | Android capture/revocation/rotation/stale-frame behavior on supported device |
| Genuine model | Bundled pinned MobileNetV2, five-score adapter | Desktop real inference already executed; Android BundledModelInferenceTest needs executed result |
| Correct contract | Hash/dtype/dimensions/label/normalization checks | Native tensor smoke and repeated Bitmap inference; conversion parity still unverified |
| Detection policy | Experimental threshold and temporal confirmation | Core unit tests; lawful representative semantic held-out set still needed |
| Intervention/recovery | Package-bound cooldown/overlay, breathing/reflection/self-reported activity | Android store exit tests; actual detection→overlay and accessibility/essential-access device qualification |
| Focus | Launchable-app selection, duration, delayed start, early exit | Android rule persistence tests and live selected/unselected-app scenario |
| Privacy | No source INTERNET, no frame writes/logs, private minimal history, backup off | Actual merged-manifest gate, synthetic-session file/network/log review on device |
| Quality | Host/core tests, ML safety tests, instrumentation, lint and CI defined | Exact final CI results, independent semantic evaluation, physical qualification |
| Delivery | Draft PR and branch preserve source/history | Actual debug APK plus checksum and recorded CI/build result |

Source-level resolution/buffer/sampling bounds and desktop timings are not TECNO battery/thermal/latency evidence. Permission revocation, force-stop/uninstall and protected surfaces are intentional Android limits rather than bugs to bypass. Semantic dataset rights/provenance and physical device access are genuine external qualification dependencies.
