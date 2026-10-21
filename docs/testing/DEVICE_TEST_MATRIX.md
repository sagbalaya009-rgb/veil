# Device verification matrix

Host unit tests and Android lint cannot certify foreground service behavior, capture consent,
OEM process management, accessibility events, or visual classifier accuracy. Record device model,
Android build, APK SHA-256, steps and observed results for every executed row.

| Scenario | Expected evidence | Status |
| --- | --- | --- |
| Fresh install on API 31 and API 35 | Onboarding, readable content, no crash | Not run |
| Large text and TalkBack | Controls named, focus order usable, no clipped critical actions | Not run |
| Permission declined/revoked | Protection status truthful; setup remains usable | Not run |
| Screen capture consent denied/stopped | No retained token or capture; service stops cleanly | Not run |
| Voluntary focus session | Selected app triggers intervention; unselected app remains available | Not run |
| Session expiry and explicit emergency exit | Focus enforcement ends predictably | Not run |
| Process death and device restart | No silent screen capture restart; persisted commitment accurately restored | Not run |
| Delete local data | Local settings/history cleared; protection stopped | Not run |
| Accessibility disabled / usage access revoked | No enforcement claim when unavailable | Not run |
| Guardian without a validated bundled model | Honest unavailable status; no fabricated detection | Not run |
| Approved labeled classifier evaluation | Measured sensitivity, false positives, device latency | Not run |
| Sustained device operation | Measure battery, thermal state, peak memory and service stability | Not run |

Never use private screen media as test artifacts. Use benign synthetic fixtures and consenting
testers. A model accuracy claim requires an approved, labeled evaluation set and a recorded protocol.
