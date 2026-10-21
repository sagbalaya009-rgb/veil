# ADR 0002: Persist voluntary commitments with monotonic deadlines

Status: accepted for experimental MVP, 2026-10-21.

Retain existing Room migrations and local recovery settings. New live focus/cooldown rules use a single private SharedPreferences JSON snapshot, published through StateFlow. It stores selected package rules, elapsedRealtime deadlines, boot count, preferences, and up to100 event kinds/timestamps for30 days; it does not store screens, foreground history or confidence. This small local state does not warrant an added DataStore dependency and migration during platform reconstruction.

Same-boot deadlines survive process recreation and do not move with wall-clock edits. Boot identity mismatch ends commitments safely; no capture consent or active monitoring state is persisted. Accessibility and UI ticks evaluate expiry; no background exact alarm or automatic capture recovery is implied. Voluntary early end, incorrect-detection dismissal and emergency exit are always permitted. Android Clear data removes both current rules and existing Room notes; in-app deletion labels the resource it clears.

Tradeoff: two persistence boundaries are explicit. Consolidation and richer relational history can follow separately. Deadlines are not tamper-proof, Android settings/uninstall remain available, and retention purge runs when the app/service evaluates state rather than while force-stopped.
