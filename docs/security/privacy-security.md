# Privacy and Security Architecture

## Core guarantee

Veil analyzes screen content locally and does not store or transmit screenshots, screen recordings, browsing history, message contents, or classifier inputs. Local data is limited to settings, intervention timestamps, recovery completion, streaks, and lock periods.

## Permission discipline

Accessibility events are limited to what is required for foreground detection, intervention timing, lock enforcement, and permission recovery. Veil must not perform OCR, read message contents unnecessarily, log raw accessibility events, or hide its active state. MediaProjection consent remains visible to the user, and the foreground notification clearly says when protection is active.

## Security controls

The application should use least privilege, encrypted local storage where appropriate, dependency scanning, code review, model-file integrity checks, and a responsible disclosure process. Debug builds must not silently weaken privacy guarantees in ways that could leak into release builds.

## Privacy review gates

Any change involving permissions, network access, data storage, analytics, model distribution, or third-party SDKs requires a privacy review. The review must answer what data is processed, where it exists, how long it exists, who can access it, and how the user can revoke the behavior.

## Ethical red lines

Veil must never support covert surveillance, remote supervision, hidden capture, third-party dashboards of user activity, manipulation through shame, or product incentives that depend on collecting sensitive screen data. If a proposed feature conflicts with user autonomy or local-only processing, it is out of scope unless the project's ethical constitution is explicitly revised through a documented decision.
