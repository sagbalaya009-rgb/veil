# Veil Risk Register

| ID | Risk | Probability | Impact | Primary mitigation |
|---|---|---:|---:|---|
| R1 | Google Play rejects Accessibility, MediaProjection, overlay, or monitoring behavior. | High | Critical | Keep the product user-facing, transparent, minimally permissioned, and maintain direct-distribution fallback. |
| R2 | Android or OEM restrictions break capture, services, or overlays. | Medium/High | Critical | Modular adapters, beta monitoring, compatibility matrix, graceful degradation, and recovery flows. |
| R3 | Users distrust screen monitoring. | High | Critical | On-device inference, no screenshot storage, privacy dashboard, open documentation, and independent review. |
| R4 | False positives block legitimate medical, educational, fitness, art, or family content. | Medium | High | Conservative thresholds, temporal confirmation, representative evaluation data, and user reports. |
| R5 | False negatives or adversarial presentation bypass detection. | Medium | High | Diverse datasets, multi-scale and temporal evaluation, adversarial testing, and regular model reviews. |
| R6 | Battery drain or performance degradation harms daily use. | Medium | High | Adaptive sampling, bounded buffers, efficient TensorFlow Lite inference, and device profiling. |
| R7 | Users revoke protection or uninstall impulsively. | High | Medium | Lock periods, delayed disable, emergency override friction, and commitment-oriented UX. |
| R8 | Privacy or security incident causes irreversible trust damage. | Low | Existential | Minimal data, no cloud processing, audits, dependency scanning, and responsible disclosure. |
| R9 | Product is too narrow or market fit is weak. | Medium | High | Early interviews, beta testing, and expansion into adjacent self-defined behavioral commitments. |
| R10 | Mission drifts toward surveillance. | Low | Existential | Ethical red lines, architecture constraints, public commitments, and founder-level review. |

The risk register is reviewed monthly. The team records probability changes, new Android behavior, beta feedback, legal developments, competitive context, ownership, and mitigation progress. When faster growth conflicts with stronger trust, Veil chooses trust.
