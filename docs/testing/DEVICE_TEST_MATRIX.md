# Device verification matrix

Host unit tests and Android lint cannot certify foreground service behavior, capture consent,
OEM process management, accessibility events, or visual classifier accuracy. Record device model,
Android build, APK SHA-256, steps and observed results for every executed row.

| Scenario | Expected evidence | Status |
| --- | --- | --- |
| Fresh install on API 31 and API 35 | Onboarding, readable content, no crash | API35 emulator passed; API31 and physical not run |
| Large text and TalkBack | Controls named, focus order usable, no clipped critical actions | Not run |
| Permission declined/revoked | Protection status truthful; setup remains usable | Not run |
| Screen capture consent denied/stopped | No retained token or capture; service stops cleanly | Not run |
| Voluntary focus session | Selected app triggers intervention; unselected app remains available | Actual selected synthetic fixture restricted with AI off; full UI/unselected/physical checks pending |
| Session expiry and explicit emergency exit | Focus enforcement ends predictably | Actual one-minute focus and emergency overlay exit passed on API35; physical pending |
| Same-boot process recreation | Persisted monotonic commitment restored; capture needs new consent | Not run |
| Device reboot | Commitments end safely; no silent capture restart | Not run |
| In-app rule/history deletion | Named current rules/history removed; protection stopped | Not run |
| Android Clear data | Both current state and legacy Room notes/preferences erased | Not run |
| Accessibility disabled | No enforcement claim when unavailable | Not run |
| Guardian without a validated bundled model | Honest unavailable status; no fabricated detection | Not run |
| Static versus changing screens | Fresh-frame availability and temporal confirmation, with truthful paused/no-frame status | Not physically qualified; integration fixture is animated |
| Approved labeled classifier evaluation | Measured sensitivity, false positives, device latency | Not run |
| Sustained device operation | Measure battery, thermal state, peak memory and service stability | Not run |

Never use private screen media as test artifacts. Use benign synthetic fixtures and consenting
testers. A model accuracy claim requires an approved, labeled evaluation set and a recorded protocol.
