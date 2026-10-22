# State machines

## Visual monitoring

Off → disclosure → Android consent pending → cancelled/off OR foreground service starting → model validation → capture waiting → analysis active / analysis paused → stopped.

Missing/invalid model, lost accessibility connection, projection revocation, setup/inference error, display reconfiguration or explicit Stop leads to stopped with a visible reason and full resource cleanup. A new session needs new consent. Process death clears ephemeral running state; no START_STICKY restart exists. Capturing but waiting/paused is distinguished from inference activity. Capture states are not persisted as proof of running services.

## Detection and intervention

Eligible foreground frame → validated model scores → provisional explicit-score threshold and temporal confirmation → recheck foreground revision/device state → persistent cooldown for that package → dismissible accessibility overlay when that package is foreground. Other apps remain available. During cooldown analysis is paused. Expiry returns to ordinary observation; false-positive dismissal stops sharing before ending the cooldown, and emergency exit stops sharing and ends current restrictions. Both record only local event kinds/times. New sharing consent is required to resume AI.

## Focus

Idle → user selects eligible launchable apps and confirms duration/delay → scheduled OR active → completed. Early end is permitted and recorded. Scheduled rules become effective by monotonic time while accessibility service is connected; no exact alarm is claimed. Loss of accessibility means unavailable enforcement, not a completed commitment. Same-boot process recreation restores deadlines. Reboot terminates commitments with a local recovery event on evaluation. Wall-clock changes cannot extend countdowns; wall time is used only for history labels/retention.

## Recovery

Optional breathing guide → 60-second guide complete → self-reported recovery event. Optional reflection or activity confirmation records no written content in new history. The user can stop any guide. Legacy local notes and recovery plans remain separate. Completing an activity does not pretend to verify exercise, and does not silently end a focus commitment.
