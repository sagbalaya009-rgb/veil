# Veil UI/UX Specification

## Design philosophy

Veil follows **calm authority**. The user should feel protected, not punished. Every interaction should communicate clarity, safety, confidence, respect, and intentionality. The interface must not feel childish, aggressive, religious, guilt-inducing, manipulative, or noisy.

The product personality is calm under pressure, disciplined, discreet, competent, emotionally neutral, and firm during a lock period.

## Core principles

| Principle | Requirement |
|---|---|
| Minimalism | One primary action per screen and low cognitive load. |
| Breathing room | Generous spacing, large typography, and little visual clutter. |
| Dark first | Near-black and charcoal surfaces reduce nighttime stimulation and create intervention contrast. |
| Purposeful motion | Animation communicates state transitions; decorative motion is excluded. |
| Accessibility | Support large text, screen readers, high contrast, reduced motion, and color-blind users. |
| Anti-addictive UX | No infinite scrolling, social feeds, leaderboards, public streaks, achievements, or gamification. |

## Navigation

Use bottom navigation with no more than four tabs: Home, Focus, History, and Settings. The app should encourage fewer opens, not repeated checking.

## Onboarding flow

| Screen | Content |
|---|---|
| Welcome | “Protect the commitments you make to yourself.” Abstract shield illustration and Continue. |
| Privacy | Explain that the screen is analyzed locally, nothing is uploaded, and nothing is stored. |
| Permissions | Give each permission a one-sentence purpose and explain reduced functionality if revoked. |
| Protection setup | Select protection level, cooldown duration, recovery task, and lock period. |
| Activation | Confirm that protection is active and show the next state clearly. |

The onboarding should take less than two minutes.

## Core screens

### Home

The home screen is calm and uncluttered. It shows protection status, time remaining, lock end time, streak, interventions today, and protected hours. A large rounded protection card supports Active, Paused, Cooldown, and Permission Required states.

### Focus

Focus configures commitment periods: 2 hours, 8 hours, 24 hours, 3 days, 7 days, and documented future custom periods. A visual timeline communicates duration.

### History

History is a reflection screen, not a shame screen. It shows intervention count, cooldown completion, recovery tasks, and protection hours. It must not show explicit-content descriptions or triggering details.

### Settings

Settings are grouped into Protection, Privacy, Notifications, Recovery, and Advanced. The Privacy section should be unusually detailed.

### Intervention

This is the most important screen. On detection, transition to pure black with a decisive 150–250 ms fade. Show an outlined shield, “Take back control,” a clear explanation, a dominant countdown, and the selected recovery task. Do not use a progress bar or distracting effects, and do not provide a casual skip action.

The screen should communicate only three things: **you are safe, the urge will pass, and your future self asked for this protection.**

### Recovery tasks

Push-ups show a large count target and “I completed this.” Breathing uses an animated breathing circle, a two-minute timer, and optional haptic pacing. Walking may use a progress ring or step count. Journaling uses one prompt, a text area, and Done. The MVP should begin with simple, reliable flows rather than unnecessary device integrations.

### Completion

Show “Well done.” and “You protected a commitment you made to yourself.” Provide Return to focus. Never use “You resisted temptation” or “You failed.”

### Emergency override

Show a warning icon, “Pause protection?”, a clear explanation, a non-skippable 60-second countdown, a Wait action, and a Pause protection action. The design should create friction without humiliation.

## Visual language

Use near-black and dark-charcoal backgrounds, white primary text, soft-gray secondary text, deep-blue accents, muted green success, amber warnings, and black for critical intervention. Red is used extremely sparingly. Use a modern sans-serif font with the countdown timer as the dominant typographic element. Prefer outlined shield, lock, timer, moon, target, check, and activity icons.

Notifications are quiet and must never include explicit language. Haptics are reserved for state changes: one firm vibration for intervention, two soft vibrations for cooldown completion, and one soft vibration for activation.
