# On-Device AI System

## Purpose

The classifier detects explicit sexual content in screen frames quickly enough to support the intervention engine. It is a narrow component, not a general-purpose content surveillance system.

## Pipeline

```text
Screen frame -> resize/normalize -> TensorFlow Lite model -> confidence score -> temporal evaluator -> protection engine
```

The model runs locally, receives bounded in-memory inputs, and returns a score plus diagnostic metadata needed for testing. Raw frames are discarded after inference. The evaluator should require temporal confirmation where practical to reduce one-frame false positives.

## Initial operating targets

The first release uses a confidence threshold of 0.90 and adaptive sampling: 0.5 FPS while idle, 1 FPS during ordinary use, and 2 FPS during active scrolling. Inference must fit within the overall detection latency target of 800 ms while keeping average CPU below 10% and memory below 200 MB.

## Evaluation

The test set must include explicit images and video frames as well as medical imagery, anatomy diagrams, art, swimwear, fitness content, family photos, education content, animation, low brightness, crops, thumbnails, split-screen layouts, and rotation. Track precision, recall, false-positive rate, false-negative rate, latency, memory, and battery impact by device class.

## Model governance

Model updates require reproducible evaluation, a documented dataset change, privacy review, and rollback capability. No model or evaluation workflow may introduce cloud inference or retain user screen content.
