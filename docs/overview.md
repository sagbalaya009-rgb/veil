> Historical context: this document describes an earlier Veil scope or build. For the current implementation and executed evidence, use docs/INDEX.md and docs/releases/ENGINEERING_DELIVERY_REPORT.md. Earlier device gates, scalar-model contracts, feature restrictions and benchmark targets are not claims about the current experimental build.

# Project Veil Overview

## Executive summary

Project Veil is a privacy-first Android digital-wellbeing product for people who want help honoring personal commitments around explicit content and compulsive distraction. The product does not attempt to supervise another person. It gives the device owner a visible, user-initiated intervention layer that makes an impulsive return to triggering content harder.

Veil combines local screen analysis with behavioral friction. Detection is only one part of the product: the more important experience is the intervention, which pauses the moment, starts a cooldown, and asks the user to complete a short recovery task such as breathing, walking, push-ups, or journaling.

## The problem

People often understand what they want to avoid but struggle in a high-impulse moment. Conventional blockers can be too easy to disable, too broad in what they block, or too judgmental in how they communicate. Veil is intended to close that gap with a narrow, transparent, and autonomy-preserving intervention.

## The proposed solution

A user chooses a protection level, cooldown, recovery task, and lock duration during onboarding. With protection active, Veil samples the visible screen, runs an on-device classifier, and invokes a full-screen intervention only when confidence exceeds a conservative threshold. The user then completes the recovery task before the cooldown can finish.

## Primary users

The initial audience is adults who are actively trying to reduce or interrupt their own exposure to explicit content. The broader product direction extends the same behavioral-protection engine to focus, sleep, doomscrolling, and other self-defined commitments, while retaining the same privacy and consent boundaries.

## MVP definition of done

The MVP is complete when a user can install Veil on Android 12+, understand and grant the required permissions, activate protection, encounter explicit content, receive an intervention within the target latency, complete a recovery task, and see the resulting local history without any screenshot or screen-content upload.

## Success measures

| Area | Initial target |
|---|---|
| Detection latency | Less than 800 ms in the target device test set |
| Average CPU usage | Less than 10% during active monitoring |
| Memory usage | Less than 200 MB |
| Crash-free sessions | Greater than 99% |
| Additional battery use | No more than approximately 5% per day |
| Privacy | No stored screenshots, recordings, browsing history, or cloud image processing |
| User experience | Intervention is calm, clear, non-judgmental, and difficult to dismiss casually |

## Strategic risks

The five risks requiring founder-level attention are Google Play rejection, privacy distrust, a trust-damaging incident, future Android restrictions, and mission drift toward surveillance. Veil's strongest defense is not a marketing claim; it is an architecture that makes the safer behavior the easiest behavior.
