# Recovery data and integration contract

## Baseline audit

The V1 dashboard offered a one-minute breathing guide, an ephemeral reflection input, and a legacy recovery-plan preview. Room schema v5 separately held an optional `recoveryNote`, worldview preference, and older settings. There was no structured current goal selection, reusable coping-plan store, optional urge check-in history, or strategy-helpfulness record. The existing Room migrations and data remain intact.

V2 adds an isolated `RecoveryStore`; it does not reinterpret or delete legacy notes. A user who wishes to remove legacy Room data must use the separately identified legacy deletion path or Android's app-data controls. “Delete Recovery data” refers specifically to V2 records and preferences, not the legacy Room database, focus rules, protection history, or feedback.

## Store API for Compose

`RecoveryStore.get(context).state` exposes a `StateFlow<RecoveryState>`.

- `configure(goals, encouragement, customReminder)` saves voluntary preferences.
- `savePlan(trigger, action)` saves an editable “If …, I will …” plan.
- `logCheckIn(intensity?, trigger?, note)` returns a new ID after a successful save; an entirely skipped check-in returns null without a write.
- `completeActivity(activity, helpful?)` records self-reported completion and returns its ID; a UI can later call `setHelpful(id, helpful?)` or undo using `deleteRecord(id)`.
- `reportSetback()` adds a record without resetting prior progress.
- `deleteRecords()` removes check-ins, notes, completions, and setback records while preserving preferences/plan.
- `deletePreferences()` resets goals, encouragement, reminder, and plan while preserving records.
- `deleteAll()` removes V2 Recovery storage, including when decryption is unavailable.

Boolean mutation results and nullable returned IDs must be checked before displaying a save confirmation. `storageAvailable=false` and `storageMessage` expose failure without logging sensitive content. Store failure never changes model policy or focus/cooldown rules.

## Privacy and retention

The complete Recovery payload is AES-GCM encrypted with an installation-local Android Keystore AES key. Each write uses a new randomized IV and an authenticated schema identifier. An `AtomicFile` under `noBackupFilesDir` protects against partial writes. There is no plaintext fallback, cloud synchronization, network API, package identifier, screenshot, embedding, or diagnostic-export hook. Android backups and device transfers are already disabled by the application manifest and extraction rules.

This is local at-rest protection, not a claim of safety on a compromised/rooted phone or while app memory is accessible. The Keystore key is not gated by per-read biometric authentication. Users can read and edit their own data inside the app; text exists in memory during display/editing. Ordinary deletion removes the app file and UI state; no promise of forensic flash erasure is made.

Records are capped at 100 within a trailing 30-day window, filtered on cold read and every mutation. Cold open rewrites the pruned encrypted payload. Text bounds: note 1,000 characters; plan situation and action 160 each; custom reminder 400. Goals/plan persist until explicitly changed or deleted. Progress labels must disclose the retained window. No sensitive data appears in technical diagnostics.

Corrupted ciphertext, unknown schemas, unavailable keys, or write failures surface storage unavailability. Existing bytes are preserved rather than overwritten with empty defaults. Explicit `deleteAll()` restores an empty usable state. Restoring encrypted files to another installation/device is unsupported because the key is installation local and export is intentionally absent.

## Verification scope

`RecoveryRulesTest` covers setback preservation, retention/capping, no inferred faith text, optional self-report fields, and invalid record shapes. A standalone run on Windows/JDK17 using cached Kotlin 2.2.10 and JUnit 4.13.2 passed all five tests on 2026-10-22. This is domain evidence, not the pinned Gradle/Android build. The initial offline Gradle attempt timed out waiting for a wrapper ZIP lock; the local Android API35 platform JAR was also absent during a separate compile attempt.

`RecoveryStorePersistenceTest` uses its own unique test directory to cover encryption/plaintext absence, actual cold reopening, scoped deletion, and authenticated-corruption refusal. These two Android tests require execution on the supported test environment; use the V2 release/test report for their actual final status. Neither test touches the owner's Recovery payload. UI tests and accessibility/device checks remain separate evidence classes.

`RecoveryJourneyTest` adds two Compose journeys against isolated encrypted storage: user-chosen goal and coping plan, optional faith reminder, skipped check-in, activity helpfulness, setback preservation, and complete deletion; and corrupted storage refusing saves without a false success message. Save controls are disabled when storage is unavailable, while explicit deletion remains accessible. The UI labels goal checkboxes for assistive technology. These tests are included in `VeilVerificationSuite`; final execution evidence belongs in the V2 test/release report.
