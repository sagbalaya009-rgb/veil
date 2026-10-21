> Historical context: this document describes an earlier Veil scope or build. For the current implementation and executed evidence, use docs/INDEX.md and docs/releases/ENGINEERING_DELIVERY_REPORT.md. Earlier device gates, scalar-model contracts, feature restrictions and benchmark targets are not claims about the current experimental build.

# Native TensorFlow Lite initialization milestone

The browser MVP now demonstrates the intended control flow: a simulated detection event starts a cooldown, presents the selected recovery activity and saved note, and clearly states that a browser cannot inspect other applications or blur the operating system.

The full device-protection path belongs to the Android app. The Android project already includes the TensorFlow Lite runtime, an in-memory `ScreenClassifier`, temporal evaluation, and a new `TensorFlowLiteInitializer`. The initializer expects a reviewed model at:

```text
app/src/main/assets/models/veil_explicit_v1.tflite
```

It loads that model locally, creates the classifier, and returns an explicit `Ready`, `MissingModel`, or `Failed` state. It does not enable screen capture, cloud inference, model downloading, or persistent storage of frames.

## Required model contract before bundling

| Item | Required value or action |
| --- | --- |
| Format | TensorFlow Lite `.tflite` model |
| Input | 224 × 224 RGB image tensor, normalized to 0–1 floats |
| Output | One explicit-content confidence score in the 0–1 range |
| Model version | `veil-explicit-v1` or a documented successor |
| Initial threshold | 0.90, evaluated together with temporal confirmation |
| Privacy | Inference is local; frames are processed in memory and discarded |
| Validation | Reproducible test set, false-positive/false-negative review, latency, memory, CPU, and battery measurements |
| Governance | Documented dataset change, privacy review, versioning, and rollback plan |

## Next implementation sequence

The team must first select or train a legally sourced, evaluated model that meets this contract. After that, add the model asset to the Android project, wire `TensorFlowLiteInitializer` into the foreground monitoring service, pass confidence scores through the temporal evaluator, and activate the documented protection engine only after real-device testing. The protection system must continue to avoid screenshot storage, cloud processing, OCR, browsing-history collection, and remote monitoring.
