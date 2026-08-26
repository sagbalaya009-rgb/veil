# Desktop PC protection architecture — initial scope

## Scope

The first desktop target is **Windows 11**. macOS and Linux are deferred until the Windows implementation is evaluated. The desktop product must use the same product promise as Android: it protects the triggering application during a cooldown while allowing unrelated applications to remain usable.

## Intended desktop behavior

When local detection is confirmed in a supported foreground desktop application, Veil records the target application identity, starts the selected cooldown, and displays an application-specific topmost protective surface whenever that application is active. The surface shows the remaining cooldown time, recovery activity, and the user’s saved note. It does not claim to control the whole computer.

## Required desktop components

| Component | Role |
| --- | --- |
| Desktop shell | Local settings, recovery UI, and transparent permission explanation |
| Foreground-app adapter | Reports an application process or window identifier, without reading content |
| Consent-based capture adapter | Provides local frames only where platform consent allows it |
| Local inference engine | Runs the reviewed model locally and discards frames after inference |
| Cooldown coordinator | Targets only the triggering application identifier |
| Topmost protective overlay | Covers the target app while active and captures interaction within the overlay |

## Constraints

Windows application control and capture APIs vary by application, elevation level, display configuration, and security software. Veil must clearly display a degraded state when it cannot protect a target app. It must not use keylogging, hidden persistence, covert capture, remote administration, or unannounced monitoring. Development starts with an architecture and permission review before desktop code is packaged.
