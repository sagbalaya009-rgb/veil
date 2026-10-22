# V2 privacy review

No INTERNET permission or backend. Inference stays local; screen frames remain bounded transient memory and are never serialized by the app. No accessibility text/nodes/gestures. Capture consent and system indicator required, user revocation respected, emergency exit maintained. No root/admin/uninstall interference or boot recapture.

Feedback schema1 stores random ID, time, correction enum and model integration version, capped100/30days. No app identifiers or pixels. It cannot train vision or alter protection. App-private preferences with backup exclusion; it is not represented as independently encrypted. Reset/undo affect only these reports.

V2 Recovery is separate AES-GCM ciphertext in noBackupFilesDir with AndroidKeyStore key. Optional goals, user reminders, coping plan and check-ins are never diagnostic fields. No legacy religion inference/migration. Storage failures are exposed and existing corrupt ciphertext is not overwritten. Explicit scoped deletion is available. Encryption does not protect an unlocked screen, a compromised device or data voluntarily shared by the owner. Legacy Room notes retain their original separate storage; V2 deletion explicitly does not erase them.

Technical diagnostic export uses Android document creation only after user action; fixed app/API/model version, current status and aggregate process counters/timing. No raw errors, viewing history, package names, confidence values, emotions, journal or faith fields. Data goes only to the owner-selected document location; export failure must be visible. Android app updates preserve existing data with compatible signing; never uninstall the owner app to resolve a signature mismatch without informed approval.

No automatic on-device learning or federated server. Future designs require separate consent, threat analysis, secure aggregation plus formal privacy accounting and poisoning defenses; federation alone offers no privacy guarantee. See ai/PRIVACY_PRESERVING_LEARNING.md. No security certification, complete source audit or medical efficacy claim is made.
