# Privacy architecture

Scope: native Android APK. The retained website/dev server is a separate product surface and is not covered by the APK's offline guarantee.

The Android manifest requests no INTERNET permission. No account, analytics SDK, cloud moderation or frame upload is integrated. The mediaProjection service is non-exported and accepts one legitimate consent result. Screen sharing is visible and reversible from Android or Veil. Accessibility requires its own prominent disclosure and Android settings opt-in. It receives window package/class metadata for attribution/routing; window-content retrieval, text reading and gestures are disabled.

Screen frames exist transiently in ImageReader/bitmap/native input buffers. Latest-frame acquisition, bounded resolution and sampling avoid unbounded queues. Images are closed, bitmaps recycled, reusable working tensors/pixel buffers cleared, and native capture/classifier resources released. Secure surfaces remain uncapturable. No screenshot files, raw-event logs or classifier frame reports are written. Native/platform memory is not guaranteed cryptographically erased; debugging/rooted/compromised systems are outside the ordinary sandbox guarantee.

Current selected app rules, monotonic deadlines, boot identity, appearance, cooldown settings and minimal event kinds/times are stored in app-private veil_commitments preferences. Events cap at100 and filter after30 days on read/change/tick. Purging does not run while force-stopped. Legacy notes/worldview/language/recovery preferences remain in veil.db; no application-layer encryption is added. Android app sandbox and device encryption apply; backup is disabled. A debug build is debuggable and suitable only for deliberate testing.

History deletion clears current event metadata. Delete focus rules/history clears the named resources and stops capture. Existing Room notes and all application data can be erased via Android Settings → Apps → Veil → Storage → Clear data, or uninstall. Revoking accessibility stops analysis and removes overlays; it never blocks revocation or uninstall. Focus can be ended early; incorrect AI interventions can be dismissed without sending content.

Verification must inspect the final merged APK manifest, local files during a synthetic session, native dependency behavior and device network/capture lifecycle. Source controls alone do not prove runtime qualification. See the threat model, build report and device matrix for residual risks and evidence.
