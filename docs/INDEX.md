# Current documentation index

The master directive supplied on 2026-10-21 overrides historical specifications where they conflict. Current implementation and verification are described here; previous product notes remain historical context, not evidence of completed features.

- [Repository audit](audits/REPOSITORY_AUDIT.md)
- [Architecture overview](architecture/OVERVIEW.md)
- [Android platform architecture](architecture/android-architecture.md)
- [State machines](architecture/STATE_MACHINE.md)
- [Model selection](ai/MODEL_SELECTION.md), [evaluation](ai/MODEL_EVALUATION.md)
- [Privacy](privacy/PRIVACY_ARCHITECTURE.md), [threat model](security/THREAT_MODEL.md)
- [Permissions](compliance/ANDROID_PERMISSIONS.md), [Play readiness](compliance/PLAY_STORE_READINESS.md)
- [Test plan](testing/TEST_PLAN.md), [device matrix](testing/DEVICE_TEST_MATRIX.md)
- [Executed verification record](testing/VERIFICATION_LOG.md)
- [Acceptance evidence matrix](testing/ACCEPTANCE_MATRIX.md), [synthetic capture journey](testing/CAPTURE_JOURNEY.md)
- [TECNO physical test instructions](testing/DEVICE_TESTING.md)
- [Build/release process](releases/BUILD_AND_RELEASE.md)
- [Engineering delivery report](releases/ENGINEERING_DELIVERY_REPORT.md)
- [Consented capture ADR](decisions/0001-consented-platform-monitoring.md)
- [Persistent state ADR](decisions/0002-persistent-voluntary-commitments.md)

Historical product, AI-system, app-specific protection, model-training and operations documents preserve earlier requirements and observations. In particular old fixed latency/battery targets are goals, old no-live-capture descriptions identify past builds, and Galaxy M20 references do not establish TECNO SPARK 30C qualification. The ML workshop remains available with its original dataset safety gates; its binary exports are not interchangeable with the bundled five-output model.
