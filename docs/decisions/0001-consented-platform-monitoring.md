# ADR: User-consented local capture with revocable intervention

Date: 2026-10-21. Status: implemented in source; device behavior requires qualification.

## Context

The original monitoring service only displayed “Protection active” and returned START_STICKY. It did not capture or classify content. Android requires explicit session consent and a foreground MediaProjection service, while a consumer self-control app cannot promise unremovable or universal protection.

## Decision

Use one Android consent result for one MediaProjection session. Initialize the bundled classifier through TensorFlowLiteInitializer and fail closed with a visible unavailable status if initialization fails. Use one handler thread for acquisition, inference and resource disposal, a two-image latest-frame reader, long-edge capture cap720 and a 500ms sampling interval. Keep pixels in memory only; reuse the row-copy buffer and zero it on release. Discard results taking longer than two seconds and reset confirmation after a two-second frame gap. Use the existing temporal evaluator to confirm multiple frames, resetting it across app transitions or pauses. Bind confirmed intervention to the known foreground package and recheck identity and its transition revision after inference, including A-to-B-to-A changes. The persisted CommitmentStore is responsible for cooldown state; an explicitly enabled package-only AccessibilityService displays dismissible interventions.

The service ends on system projection revocation, stop, configuration/aspect changes or inference failure. It never silently restarts. Privacy and permission revocation override commitment strength. A source-level bound is not a performance measurement and a confirmation window is not evidence of model accuracy.

## Alternatives

UsageStats needs special usage access and cannot enforce app restrictions itself; a current window-package accessibility adapter was selected for the focus intervention feature. A regular application overlay would add another special permission. Device-owner controls, root, stealth capture and uninstall prevention are outside this voluntary consumer MVP.

## Consequences

Android stop controls always work. Full-display capture is explicitly disclosed, and app attribution is best effort under overlays/multi-window/OEM screen-sharing choices. Multi-window qualification is outstanding. Captured frames can include sensitive material; absence of disk/network paths reduces exposure but does not make in-process memory immune to a compromised device. Secure/DRM surfaces remain unavailable; blank frames are not proof of safety. Rotation currently needs another consent grant. Required tests and sources are in [Android permissions](../compliance/ANDROID_PERMISSIONS.md).

