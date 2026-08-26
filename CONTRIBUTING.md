# Contributing to Veil

Veil welcomes contributions that improve user autonomy, privacy, reliability, accessibility, performance, and test coverage. Before proposing implementation work, read the product overview, MVP specification, Android architecture, and privacy/security architecture.

Every change should have a clear scope, tests appropriate to its risk, and documentation when it changes behavior or a platform assumption. Changes involving permissions, screen capture, accessibility events, overlays, persistence, analytics, or network access require an explicit privacy and security review.

Pull requests should explain the user problem, the design decision, test evidence, privacy impact, and any known limitations. Contributors must not add telemetry, screenshots, hidden behavior, or remote-monitoring pathways without a documented, approved architecture decision.

## Development sequence

Start with the domain protection engine and deterministic tests. Add platform adapters behind interfaces. Integrate live capture and model inference only after permission education, failure states, and observability are in place. Validate on representative Android versions and OEM configurations before treating a feature as complete.
