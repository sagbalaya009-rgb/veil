> Historical context: this document describes an earlier Veil scope or build. For the current implementation and executed evidence, use docs/INDEX.md and docs/releases/ENGINEERING_DELIVERY_REPORT.md. Earlier device gates, scalar-model contracts, feature restrictions and benchmark targets are not claims about the current experimental build.

# TensorFlow Lite Model Review Gate

## Decision

Veil does **not** bundle an explicit-content TensorFlow Lite model yet. The two public candidates reviewed for this milestone do not meet the present acceptance bar as a drop-in Android asset: GantMan’s repository states that its training data is not easily distributable and documents a 299 × 299 model family, which conflicts with Veil’s 224 × 224 contract.[1] Falconsai’s model page identifies an Apache-2.0 model but exposes a Transformers/PyTorch/Safetensors implementation rather than the reviewed `.tflite` artifact and evaluation evidence Veil requires.[2]

> A permissive model license alone is not sufficient. Veil needs documented model provenance, dataset provenance, a reproducible evaluation record, an evaluated mobile artifact, and a rollback reference before a model can enter the app.

## Local asset gate

When a candidate is ready, the Android app will contain two release-reviewed files:

```text
app/src/main/assets/models/veil_explicit_v1.tflite
app/src/main/assets/models/veil_explicit_v1.review.json
```

The initializer refuses to create a classifier unless the manifest declares the model ID, SHA-256, HTTPS source and dataset-provenance URLs, license, reproducible evaluation-report URL, rollback model ID, named approval/date, `224 × 224 × 3` RGB input, one output score, and `rgb_float_0_to_1` preprocessing. It computes the model checksum locally and opens the model only long enough to verify the input/output tensor shapes before creating the local classifier.

## Required evaluation evidence

| Gate | Requirement |
|---|---|
| Legal source | Preserve the artifact source, model license, dataset provenance, and all distribution obligations. |
| Offline reproducibility | Provide a versioned, non-user evaluation set and a local command/report that can be repeated. |
| Safety | Report false positives and false negatives, particularly for medical images, anatomy, art, swimwear, fitness, family photos, educational material, animation, low-light frames, crops, thumbnails, split screens, and rotation. |
| Device performance | Measure Android latency, CPU, memory, and battery against Veil’s documented targets: under 800 ms end-to-end, average CPU under 10%, and memory under 200 MB.[3] |
| Human review | Keep thresholds and temporal confirmation, obtain privacy review, identify a rollback model, and test on the Galaxy M20 before enabling an intervention path. |

## Boundaries retained in this milestone

This preparation update does not add a model, model download, model training data, screen capture, MediaProjection session, accessibility logging, OCR, cloud inference, or live app blocking. The existing reset button remains a visual local preview and does not route through the TensorFlow Lite initializer.

## References

[1]: https://github.com/GantMan/nsfw_model "GantMan/nsfw_model repository"
[2]: https://huggingface.co/Falconsai/nsfw_image_detection "Falconsai NSFW image-detection model card"
[3]: https://developers.google.com/edge/litert/libraries/modify/image_classification "Google AI Edge — Image classification with TensorFlow Lite Model Maker"
