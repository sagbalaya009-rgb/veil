# Feedback and future learning boundaries

V2 feedback is local product feedback, not visual training data. A correction does not show what an image looked like. Counts or confidence buckets alone cannot train an image classifier, and one tap must never change global weights, thresholds, permanent allowlists or application exemptions. Keep explicit stop controls distinct from correction labels, make accidental feedback reversible, and offer deletion. Do not include feedback, journals, faith settings or app/viewing histories in technical diagnostics.

No screenshot collection, local screenshot archive, upload service, federated client or federated server is added by the ML tooling. Curated supervised training remains the current development path. An independent reviewer/provider must establish rights, adult assurance, labeling and held-out evaluation without requiring the owner to inspect explicit galleries. Numerical synthetic fixtures establish only engineering contracts.

## Future opt-in local supervised update (design only)

1. Separate informed consent must explain a bounded purpose, temporary local image use, retention/deletion, update risks and withdrawal. Default is off; the current capture permission is not training consent.
2. If approved, keep a narrowly bounded frame only in volatile memory, protect against background/disk/cache persistence, accept correction or uncertain label, and discard ambiguous/contradictory examples. Never silently retain frames to enable future learning.
3. Train only a deliberately constrained local component on idle/charging devices within explicit CPU/time/thermal budgets. Separate personalization from the global model. Preserve a reviewed baseline and local undo/reset.
4. Validate on a distinct safe challenge set, reject regressions and poison-like/outlier updates, version candidates and permit rollback. A local loss reduction does not imply screening quality.
5. Delete the temporary example at the agreed boundary, including failure/interruption paths. No hidden backups, embeddings archive or raw gradient logs.

This is an extension design, not implemented or approved data handling.

## Federated threat model (future research)

Federated learning alone is not a privacy guarantee. [Deep Leakage from Gradients](https://arxiv.org/abs/1906.08935) demonstrates reconstruction risks from shared updates. [Secure aggregation](https://research.google/pubs/practical-secure-aggregation-for-privacy-preserving-machine-learning/) can hide individual updates within a cohort, but it does not itself prevent information leakage from aggregate/global models or malicious updates. Client-level [differential privacy in federated learning](https://www.tensorflow.org/federated/tutorials/federated_learning_with_differential_privacy) additionally requires clipping, calibrated noise and privacy accounting.

A credible proposal must specify adversaries (curious/malicious server, colluding clients, poisoned corrections, device compromise), minimum cohort and dropout rules, authenticated client selection without viewing-history profiles, contribution limits, secure aggregation and anti-replay, clipping bounds, a fixed accountant/epsilon-delta budget with composition across rounds, and an explicit stop once the budget is spent. Do not invent a privacy budget without dataset/client assumptions and a privacy review.

Aggregation must tolerate dropped clients while failing closed if the anonymity cohort is too small. Poisoning defenses and robust aggregation can conflict with hiding individual updates; evaluate that tension rather than promising both without a protocol. Test replay, Sybil contribution, malicious labels, model backdoors, gradient inversion and membership inference. Global rollout needs independent held-out evaluation, canary gates, signed version identity and rollback. Updates/embeddings/gradients must be treated as sensitive, not ordinary analytics.

No server, purchase or federated deployment is warranted for V2 without an independently reviewed secure use case and explicit authorization. The privacy-first result today is to retain no visual examples and improve reviewed model releases through separately sourced lawful data.
