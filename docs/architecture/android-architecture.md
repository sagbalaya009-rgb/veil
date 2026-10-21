# Android architecture

The current architecture is described in [OVERVIEW](OVERVIEW.md), with legal transitions in [STATE_MACHINE](STATE_MACHINE.md) and exact permission behavior in [ANDROID_PERMISSIONS](../compliance/ANDROID_PERMISSIONS.md).

Capture requires an activity disclosure, fresh Android MediaProjection consent, and connected disclosed accessibility access for app-specific interventions. The service is non-exported, foreground mediaProjection typed and non-sticky. It uses ImageReader capacity two and acquireLatestImage, samples at most twice per second, caps capture's long dimension at 720 pixels, performs native inference on a worker and releases images/bitmaps/buffers. These are source limits, not measured device performance. It drops stale inference and resets confirmation on foreground changes. No screenshot storage or network code exists in this Android pipeline.

Accessibility uses only window-state event metadata, cannot retrieve window content, and is explicitly not designated an accessibility tool. The overlay is dismissible, scrollable and limited to selected/triggering packages; Settings, launcher, call and essential system packages are excluded. OEM and split-screen attribution remain best effort and require physical qualification. No UsageStats polling, QUERY_ALL_PACKAGES, SYSTEM_ALERT_WINDOW, device administrator or root is required.

The dashboard and accessibility service expire persisted monotonic deadlines. Process recreation on the same boot retains them. Reboot ends them safely on next evaluation. The app has no boot receiver or background capture restart. Do not replace this with silent recovery or reuse consent tokens.

Target/minimum SDK, dependency versions, Java and wrapper are pinned in source. Compatibility was checked against the official AGP8.7 documentation; modernization is optional and must be verified as a coordinated change. Current API35 target is an engineering baseline, not a claim of meeting all future Play submissions.
