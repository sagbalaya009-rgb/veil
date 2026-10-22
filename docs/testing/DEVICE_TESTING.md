# TECNO SPARK 30C physical qualification

No physical TECNO test has been executed by this engineering session. Use only the final APK and checksum recorded in ENGINEERING_DELIVERY_REPORT.md. The debug APK is a test build; do not assume it provides reliable blocking yet.

## Phone-only installation

1. Use the delivered `Veil-0.2.0-experimental.apk`, checking its SHA256 against the delivery report. Alternatively, open the exact successful Actions run linked in the report, download `veil-debug-apk`, extract it in Files and open `app-debug.apk`. Earlier builds used private draft downloads while Actions storage was exhausted; use the final report to identify the verified build. The unsigned release APK and capture fixture are not the installable Veil test build.
2. Android may ask to permit installation from that browser/file manager. Enable that source only if you intend to install this verified test file. No root, device administrator, bootloader change or computer is required.
3. Open Veil, complete onboarding and review Privacy. Do not grant optional access until you understand the separate disclosures. For sideloaded-app restricted settings, stop and use Android's documented owner-controlled installation/settings flow; do not bypass security prompts.

## Record the test context

Record exact TECNO model, Android/HiOS build from About phone, APK hash/version and date. HiOS labels and background-management controls vary: inspect the actual device rather than relying on a guessed menu. Capture behavior, accessibility service survival and power management must be measured on this device. Do not disable platform security or claim a successful fix merely from changing a battery option.

## Required journey

- Fresh install → onboarding → Home; deny notifications and cancel capture consent. UI must stay usable and never claim active analysis.
- Review foreground-awareness disclosure; independently enable the Veil accessibility service in Android Settings. Confirm Home shows connected. Disable it again and confirm disconnected; no overlay remains.
- Choose a harmless distracting app, one-minute focus, start immediately. The selected app should show Veil's dismissible pause, another app should stay usable, and expiry should allow the selected app. Repeat with a one-minute scheduled delay.
- From an intervention, test Leave app, Open recovery, Incorrect detection where available, and Emergency exit. Test Home, notification shade, Android Settings and making an ordinary test call through the dialer. Never call emergency services for testing. Emergency calling/system access must stay available.
- Configure guardian cooldown5/10/15 minutes. Review capture disclosure and Android sharing dialog. Capture must have visible Android/Veil indicators and actual status. Use safe ordinary screens for false-positive checks; generated color fixtures establish inference, not semantic content recognition.
- Stop sharing from Android, from Veil and from its notification. Revoke accessibility during sharing. Rotate, lock/unlock, background/reopen, rapidly start/stop, and force-stop. Resources and status must recover without silent recapture. Reboot must end commitments, and capture must require fresh consent.
- Confirm a deliberate incorrect-intervention report stores only an event type/time, then delete history. Local notes can be erased through Android App info → Storage → Clear data. Do not send private screenshots as reports.
- At largest system font and with TalkBack, verify all exits/consent controls remain usable. Test supported portrait/landscape layouts and record multi-window limitations.

## Performance procedure

Compare a fixed30-minute baseline with a same-brightness30-minute screen-sharing run on the same safe test sequence. Record starting/ending battery, temperature, frame/inference behavior, memory and any HiOS service termination. Repeat after cooling. Android Studio/ADB profiling may provide CPU/memory/thermal detail if separately available. Do not turn a single battery-percent comparison into a general daily-consumption claim. Desktop timing in the model report is not phone latency.

Semantic detection/precision/recall evaluation requires a separately approved lawful labeled set, representative benign medical/art/fitness cases and predeclared acceptance criteria. Do not collect private device frames centrally or use inappropriate/unlawful material. This remains a release gate, not an already completed phone test.
