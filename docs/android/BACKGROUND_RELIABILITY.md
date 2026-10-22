# Protection lifecycle and background reliability — V2

## Baseline audit and root cause limits

V1 already used a started mediaProjection foreground service, a separate worker thread, and START_NOT_STICKY. The manifest omitted stopWithTask, whose Android default is false. MainActivity had no capture-destruction hook. Therefore source inspection does **not** prove that closing the UI directly stopped capture, and merely adding an explicit false flag cannot be described as an OEM persistence fix. The owner reports loss of protection after Recents removal; exact TECNO termination versus projection revocation remains a physical reproduction gate.

A confirmed code defect was that finishSession stopped native capture asynchronously while leaving the running policy flag true until onDestroy. V2 closes that gate immediately, avoiding interventions during terminal cleanup. Duplicate start deliveries cannot initialize a second capture/token while a session is running.

An additional confirmed lifecycle misuse cleared foreground/connection state in AccessibilityService.onInterrupt. Android defines this callback as interruption of accessibility feedback, not service disconnection. Veil emits no speech feedback, so this callback now leaves monitoring state intact; actual unbind/destroy still disconnect and stop capture. The regression test calls this feedback callback and checks that a foreground observation survives. This fixes the callback contract but does not, by itself, establish the cause of every Recents failure.

In emulator run 39809733085, the real task card disappeared and onTaskRemoved was received while the capture service remained alive. Later inference paused because its foreground observation became null. Run 39827374429 passed the integration journey after the callback change, but its launcher swipe did not remove the task. That run cannot establish a Recents fix. The subsequent test requires an actual Android task-removal callback, using an explicitly recorded AppTask fallback when the identified launcher gesture fails.

## Implemented behavior

- The capture service explicitly remains independent of the activity task. onTaskRemoved counts a technical event and leaves an existing valid session alone. It does not restart anything.
- A structured in-memory health flow separates service, capture phase, model/interpreter readiness and renewed-consent requirements. Brand-new processes begin off; old tokens are never persisted or reused.
- OnStop, failed initialization, lost accessibility and configuration changes stop the session, close policy immediately, then serially release reader/display/projection/classifier/buffers. Configuration changes deliberately require renewed consent rather than reusing a token.
- Notification text reflects startup, active analysis, paused analysis and unavailable fresh frames. Open Veil and Stop actions remain available. Terminal cleanup removes the active notification; when notification permission is available, interruption posts a separate non-ongoing notice that opens Veil. User Stop does not post a misleading interruption. The app status also says renewed consent is needed. No active notification is fabricated after process death.
- Technical diagnostics contain only fixed codes and aggregate inference counts/timing. No packages, image scores, screen text, images, URLs or activity timestamps are retained in this facility. Counters cover the current process and are not evidence of accuracy, battery consumption or ongoing capture after a process dies.

## Platform behavior and remaining tests

Home/background/Recents removal should retain a valid foreground capture where Android permits. Android task-manager Stop/force-stop, process death, projection revocation and reboot are terminal. A foreground service reduces kill risk; it is not a guarantee. START_STICKY would not restore MediaProjection consent and is intentionally not used. Android 14 requires per-session consent, mediaProjection FGS permission/type and one use per capture token. Android 15 QPR1+ provides a capture chip and stops projection on screen lock. No boot receiver, accessibility exploit, background launch bypass or uninstall interference is introduced.

The existing same-boot focus deadline behavior is retained. Device reboot clears elapsed-time commitments when evaluated; it restores preferences, not an old capture session. A true reboot-spanning commitment redesign requires a separate product decision and trustworthy clock semantics.

TECNO/HiOS settings vary by build. On the actual phone, inspect App info battery/background controls and notification permission; explain that restricting background work can interrupt protection. Do not claim guessed setting labels, disable platform security, grant unrelated permissions, or promise that an unrestricted battery option fixes OEM kills. Check the protection status after returning to Veil.

Physical procedure: start with real Android consent; use a safe animated fixture; record projection+FGS before/after Home, other app and Recents removal. Then separately test OS capture stop, notification Stop, app Stop, accessibility revocation, lock/unlock, rotation, force-stop/relaunch and reboot with fresh owner consent as needed. Check active state becomes off and recapture never occurs silently. Do not run state-clearing instrumentation on the owner's device. Low-memory/long-run battery/thermal and TECNO Recents survival remain unverified until actually executed.

## Authoritative sources checked 2026-10-22

- [Android Service lifecycle and onTaskRemoved](https://developer.android.com/reference/android/app/Service): task removal callback is distinct from service termination and stopWithTask behavior; processes may be terminated without onDestroy.
- [Android media projection](https://developer.android.com/media/grow/media-projection): foreground service types, consent per session, callback cleanup and lock behavior.
- [User stopping foreground-service apps](https://developer.android.com/develop/background-work/services/fgs/handle-user-stopping): Android13+ Task Manager Stop removes the entire process without callbacks. No reliable final cleanup notification can be promised for this case.

Automated evidence: ProtectionTelemetryTest covers aggregate timing arithmetic, detached snapshots, clear and invalid timing. MonitoringRuntimeTest covers immediate terminal gating, startup versus authorized capture, and A-to-B-to-A attribution invalidation. Source regressions and emulator journey evidence are recorded in the V2 release report after the integrated checks run. No physical V2 reliability pass is asserted here.


## HiOS research boundary

[Carlcare's official TECNO/Infinix/itel support guidance](https://www.carlcare.com/ke/tips-detail/fix-phone-battery-draining-fast-kenya-tecno-infinix-itel/) describes background-app management among battery controls; [Carlcare startup guidance](https://www.carlcare.in/in/tips-detail/smartphone-lagging-fix/) says controls may be under App Management or Battery Usage and may be absent. This supports checking the actual device settings, not asserting a model-specific menu or proven fix. The [official HiOS page](https://www.tecno-mobile.com/nga/hios/) describes current system optimizations, but does not establish the owner's installed version or a Spark30C-specific service-survival guarantee. No unverified third-party lock/whitelist recipe is automatically applied.
