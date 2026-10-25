# Unified Veil 0.3.1 engineering record

## Scope

The owner requested removal of both phone installations and one combined Veil with inconsistencies corrected. The application now uses com.veil.app for debug and release with the visible name Veil. Version code 4; version name 0.3.1-unified-experimental. The trusted development signing certificate remains unchanged from V2. Because old V1 has a different certificate, it cannot receive this APK as a preserving update; the explicitly requested clean uninstall is required. Uninstall erases local records and AndroidKeyStore keys. Existing V1/V2 APKs remain available for application rollback, not as a promise of recovering erased personal records.

## Corrections

- Replace the nested original dashboard behind the notes link with one saved-note screen using the current appearance. This removes a second navigation system, independent theme, conflicting cooldown controls and simulated preview interventions from the active app path. Preserve the actual original Room note store; describe its storage separately from encrypted check-ins.
- Report note-save success only after persistence succeeds; disable editing during loading/saving and retain the draft on write failure.
- Remove the duplicated temporary reflection/activity-completion panel; keep the encrypted check-in and coping-activity flow. Correction reports remain in Settings and appear contextually during an active AI cooldown.
- Remove V2 branding from Recovery deletion controls and clarify the separate protection-history scope.
- Show scheduled focus start delay and actual duration separately instead of presenting delay plus duration as active focus time.
- Disable Stop screen sharing when there is no sharing session. Explain that analysis pauses while Veil or excluded system surfaces are open instead of implying that foreground access necessarily failed.
- Stop coalescing window-state events across apps; reject stale window observations and pane disappearance/title changes as foreground transitions. This changes no accessibility capability or content access.
- Keep the real hash-pinned model, consent boundaries, safe exits, privacy controls and focus lifecycle. No dataset acquisition or model retraining occurred.

## Verification

Application source: **d1f4cf649d752727bb0123982f5b59ee6fa3e04e**. Android workflow run **40221856239** passed model, verify and emulator jobs. Executed **37 JVM, 21 Python and 16 Android tests**, with zero failures/errors/skips. The Python count is confirmed in the model job log. Conversion engineering run **40221856193** also passed. Lint has **zero errors, 20 warnings and one informational item**; it is not warning-free. Added regressions exercise scheduled duration presentation, actual saved-note save/reopen behavior, window-event ordering and the installed accessibility configuration without content retrieval. Previous V2 test results do not qualify this changed build.

Final capture JSON confirms **actual launcher task-card removal=true and onTaskRemoved callback=true**, followed by an actual captured-frame/model intervention while the service remained alive. This run did not need the AppTask fallback. Real breathing/focus durations were **60,135 ms / 60,563 ms**; Home exit, Recovery routing, false-positive stop, focus expiry and emergency exit passed. This is an API35 emulator qualification of the previously failing sequence, not a guarantee for all launchers or the physical TECNO, and not semantic accuracy evidence.

Earlier run 40185728532 passed build/unit/model checks and the real capture journey, but failed the new saved-note navigation test because the link was beneath a long lazy-list form and not composed. The link was moved above the form and the test now scrolls the lazy container to materialize its target. That failed run is not reported as a passing application suite.

Run 40219939756 passed note and scheduling tests but reproduced the background defect after a genuine launcher swipe: task removal and surviving capture were confirmed, while foreground tracking later held an excluded essential app although the fixture was visible. This is retained failure evidence. The final run above qualifies the corrected sequence on this emulator; it does not establish universal Recents reliability.

[Android's accessibility event contract](https://developer.android.com/reference/android/view/accessibility/AccessibilityEvent) distinguishes pane disappearance/title changes from window transitions. [Android's delivery implementation](https://android.googlesource.com/platform/frameworks/base/+/a0f9d3664705/services/accessibility/java/com/android/server/accessibility/AbstractAccessibilityServiceConnection.java) maintains one pending event per type when notificationTimeout is positive; the source does not key that queue by package. Setting the timeout to zero avoids that loss of intervening package transitions. These source properties justify the corrections; the final executed test above verifies the previously failing sequence on the emulator.

## Delivery artifact

**Veil-Unified-0.3.1.apk**, **58,374,253 bytes**, package **com.veil.app**, label **Veil**. SHA256: **12bdad1b70e1f4691e0413af80f633c031ff653aa366b3382b9e0026c6f34177**. Signature verified with development certificate SHA256 **5d8aa05246b4687991807f18626ae3a6ba96bff6b9e65d32b1abfb451b6a4e88**. Manifest permissions remain notifications and mediaProjection foreground service plus the generated private signature receiver permission; no INTERNET permission. Installed-service instrumentation verifies content-retrieval capability remains disabled and only window-state events are subscribed.

Verified GitHub artifact ZIPs: debug **12610076228** / SHA256 **910f2ff22b1f23574ad5f3503a50fc39778c98022586a99a719f87b361bd664b**; host reports **12610096171** / **6355e64523cdaeb75c5c02e4e86ead67b713cf83c080c1d69649ed0926b5d9fc**; emulator reports **12610087275** / **abb4d2910e312b7ffe862ee3f2ff36ccb4e1e6e62321d1fe9d01b4489f69a62d**. Each digest was checked before extraction. The evidence bundle contains synthetic emulator evidence and technical reports, not owner screenshots or private signing material.

The later documentation-only commit records these results without changing the application artifact. V1 source remains on **codex/veil-mvp** at bf1b86a; the old V2 preview is preserved on **archive/veil-v2-0.3.0** at f5ffba2. Unified application changes are pushed on **codex/veil-v2**. This engineering installer has not been published to a store or merged to main.

## Phone installation

Pending: Android debugging currently sees no phone. Windows reports the TECNO through Bluetooth, but no Android USB debug device. No uninstall, data removal or unified installation has been performed during this run. The owner has been asked to reconnect/unlock the phone and approve debugging. After connection, target only the authorized TECNO, remove com.veil.app, com.veil.app.v2 and the test-only com.veil.fixture if present, install the verified unified APK and confirm that only one Veil package remains. Obtain new owner-operated accessibility and Android screen-sharing consent; neither may be bypassed.

## Remaining qualification

Semantic accuracy and upstream training-image provenance remain unresolved. Physical-phone AI intervention, actual TECNO Recents survival, long-duration battery behavior, TalkBack/large-font/landscape checks, full dashboard localization, production signing and store review remain open. Reboot ends commitments safely and never restarts screen capture. Ordinary applications cannot prevent permission revocation, force-stop or uninstall.
