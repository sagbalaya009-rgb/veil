# Veil V2 — implementation overview

V2 evolves the existing Android app on codex/veil-v2, preserving the real bundled model, MediaProjection consent, package-only accessibility, focus deadlines, Room notes and safe exits. It does not claim improved semantic accuracy without data.

## Shield
Capture remains independent of the UI task. Explicit stopWithTask=false and onTaskRemoved keep the currently authorized foreground session alive where Android permits, without restart/token reuse. Structured health and phase notifications distinguish off/startup/waiting/analyzing/paused/interrupted. Terminal cleanup closes the policy gate synchronously. User-initiated diagnostics export fixed technical aggregate fields only.

## Intelligence
New versioned local feedback stores report type, model version, random ID and time only. Ten-second undo/reset; no app names, screenshots, embeddings, allowlists, threshold changes or model updates. Reporting a correction differs from the separately labelled stop-guardian exit. The existing explicit stop exit remains for safe false-positive recovery. Feedback metadata is not visual training data.

The same hash-pinned experimental GantMan MobileNetV2 remains installed. Expanded model comparison/calibration/leakage/fine-tuning tools are offline and gated by approved independent datasets. No new semantic dataset was obtained or training performance invented. Balanced/Strict modes and crop pipelines are evaluation candidates, not falsely calibrated production settings.

## Recovery
Optional goals, encouragement, reusable coping plan, grounding/urge observation/meaningful activity, skip-capable intensity/trigger/reflection check-ins and helpfulness. Setbacks preserve all other retained progress. User-written faith reminder is optional; secular/default and no-message options are equally functional. No religion is inferred or copied from legacy settings. Recovery records encrypted with AES-GCM AndroidKeyStore in noBackupFilesDir, separate from legacy Room and diagnostics, capped100/30days with scoped deletion and corruption refusal.

## Product
Home, Shield, Recovery, Progress and Settings navigation; ink/ivory/violet/mint palette, static independent health rings, app search, type-led hierarchy, Material touch controls, no continuous animation or fabricated recovery score. Existing localized manual recovery notes remain accessible. V2 dashboard copy remains English; complete dashboard localization remains open.

## Verification
See [V2_RELEASE_REPORT.md](releases/V2_RELEASE_REPORT.md) for executed evidence and limits. Earlier V1 phone tests do not qualify V2. No ordinary application can guarantee execution after force-stop, OEM kill, projection revocation or reboot. Capture always requires renewed consent. No server, remote inference, media collection, user-screen dataset harvesting or federated infrastructure is added.
