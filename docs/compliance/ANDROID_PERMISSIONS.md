# Android permissions and platform constraints

Verified against authoritative Android and Google Play documentation on 2026-10-21. These are engineering constraints, not a claim of store approval.

## Screen analysis

Veil uses explicit MediaProjection consent per session. The dashboard requests capture only following the user's disclosed action. The service enters a visible `mediaProjection` foreground service before acquiring the projection. It does not save or replay consent intents, restart itself, or start capture after boot. Stop is always available through Android's capture controls and Veil's notification. Projection revocation releases the reader, virtual display and interpreter; another session needs fresh consent.

The manifest declares `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PROJECTION`. Android 14 requires the corresponding service type and a new consent grant per session. Android 15 prohibits starting this foreground service type from BOOT_COMPLETED. Veil uses START_NOT_STICKY and no reboot capture recovery.

Frames remain in process memory: a reader queue of two frames, acquired using acquireLatestImage; capture is scaled to at most 720 pixels on its long edge and inference is sampled at most twice per second. Images close in finally; temporary bitmaps are recycled. The row-copy buffer is reused and zeroed immediately after copying into the caller-owned bitmap, and again on release. Confirmation resets across frame gaps longer than two seconds, and inference results older than that are discarded. No screenshot persistence, upload, debug image export or content logging is implemented. This bounds application processing, but is not a measured total-memory or battery guarantee. Protected/DRM surfaces, FLAG_SECURE, lockscreen and unsupported device surfaces cannot be promised analysis. Never attempt a bypass. A configuration/aspect change ends the session and requires a new consent grant.

Full default-display consent is requested on Android 14+ because package-specific intervention otherwise cannot safely correspond to single-app shared content. Android/OEM compatibility controls can still alter the chooser. Multi-window and attribution remain best effort: foreground package transitions discard a frame and reset confirmation; the package is checked again after inference. This does not prove causal app attribution in every windowing mode. Limit release qualification to ordinary single-foreground-app use until device tests establish broader support.

A missing or invalid classifier stops capture with an unavailable status; an active notification never means a safe classifier result. Black/protected/unavailable content must not be represented as proven safe.

## App intervention and Accessibility

An explicitly enabled AccessibilityService observes package identifiers from window-state changes, not text, view trees, browser URLs or keystrokes. The adapter supplies foreground app identity and manages a dismissible TYPE_ACCESSIBILITY_OVERLAY. Veil is a voluntary self-control product, not a disability accessibility tool; do not set isAccessibilityTool=true. Revoking access, stopping the service and uninstalling must stay available. System settings, permission prompts, Home and calls must remain usable.

Accessibility is sensitive access. Before enabling it, disclose exactly what is observed and why, obtain affirmative consent, and explain how to revoke it. Google Play requires the declaration/review flow for this use; approval is not established by this implementation. Do not use accessibility to change settings, deny uninstall, click controls automatically or defeat Android privacy controls.

No broad UsageStats access is required by the current implementation. UsageStatsManager is an alternative for package-usage signals, requires PACKAGE_USAGE_STATS for most queries, has timing/availability limits, and does not itself block apps. Do not add usage access or QUERY_ALL_PACKAGES merely as a convenience. Launchable-app discovery should use a narrow launcher intent query.

The current design uses an accessibility overlay, so SYSTEM_ALERT_WINDOW is unnecessary. Ordinary application overlays require separate special access and are subject to obscured-touch and background-start restrictions; adding that permission would require a new justified design decision.

## Notifications

POST_NOTIFICATIONS is requested voluntarily. Denial does not grant covert operation: Android still requires the foreground-service notification, though notification visibility varies by Android's user controls. Optional cooldown notifications must respect permission denial. No exact alarms or battery-optimization exemption are necessary for current foreground tick and persisted-deadline behavior.

## Sources

- [MediaProjection lifecycle, service type, consent and secure content](https://developer.android.com/media/grow/media-projection)
- [MediaProjectionConfig](https://developer.android.com/reference/android/media/projection/MediaProjectionConfig)
- [Foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager)
- [WindowManager overlay types](https://developer.android.com/reference/android/view/WindowManager.LayoutParams)
- [AccessibilityService Play policy](https://support.google.com/googleplay/android-developer/answer/10964491)
- [Sensitive permissions and API policy](https://support.google.com/googleplay/android-developer/answer/16558241)
- [Foreground-service Play declarations](https://support.google.com/googleplay/android-developer/answer/13392821)
