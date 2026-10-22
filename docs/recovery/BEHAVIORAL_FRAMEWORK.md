# Veil Recovery V2: rationale and boundaries

Veil offers user-directed wellbeing exercises. It does not diagnose addiction, provide psychotherapy, or claim to treat a medical condition. The app has not been clinically evaluated. Choosing abstinence, reducing unwanted use, and improving focus are separate voluntary goals; none is imposed by the detector.

## Research used

Reviewed 2026-10-22:

- [WHO, Doing What Matters in Times of Stress](https://www.who.int/europe/publications/i/item/9789240003910): grounding, making room for feelings, acting on values, and kindness. This general stress-management guide supports the rationale for offering practical skills. It does not establish Veil's effectiveness or prove an effect on pornography use.
- [WHO's overview of the five techniques](https://www.who.int/thailand/activities/doing-what-matters-in-times-of-stress): used to distinguish noticing and making room from trying to suppress every feeling.
- [NIH NCCIH, Relaxation Techniques](https://www.nccih.nih.gov/health/relaxation-techniques-what-you-need-to-know): relaxation/breathing research and safety limits. Some people find these practices uncomfortable; users can stop, skip, or choose a different activity. Comfortable breathing is offered without breath holding or a demand to breathe deeply.

Veil's short instructions are original paraphrases, not copied exercise manuals. “Urge surfing” here means briefly noticing an urge without an immediate response. It is a self-management prompt, not a standalone clinical protocol. The coping plan's “If [situation], I will [small action]” format is a practical preparation tool; its effectiveness in Veil has not been measured.

## Implemented domain

`core/recovery/RecoveryModels.kt` defines three selectable goals and four encouragement choices: practical/secular, personal values, a user-written faith reminder, or none. Goals can be empty. Practical mode is the default. Faith and religious identity are never inferred from screen content or imported from legacy preferences.

Activities include comfortable breathing, sensory grounding (with nonvisual options), noticing an urge, a meaningful task, optional gentle movement, and the user's coping plan. Movement is never compulsory. Reflection, trigger selection, and an intensity rating from 0 to 10 are independent optional inputs. Skipping every check-in field creates no record.

Exercise completion and helpfulness are self-reported. A setback creates its own record and does not remove earlier activity records, plans, preferences, or goals. Intervention counts cannot diagnose progress; counts depend on capture availability, exposure, and model errors. No streak or clinical score is derived.

If distress or impairment is substantial or persistent, the user may choose support from a qualified professional. Veil should not promise that an exercise will resolve distress or replace appropriate care.

## Responsible outcome evaluation

The record model can summarize activities a user marked complete/helpful and voluntarily reported intensity. A before/after change is self-report, not proof of causality. Do not compare intervention counts as if monitoring exposure were controlled. A research claim would require a predeclared protocol, meaningful outcomes, consent, ethical review where applicable, and appropriate controls. V2 has no such study.
