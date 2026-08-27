# Veil Local Model Workshop

## Purpose

This folder is Project Veil’s **Option-C training workshop**. It prepares a repeatable local workflow for a future explicit-content classifier while intentionally containing **no training images, evaluation images, model checkpoint, trained model, or TensorFlow Lite artifact**.

The workflow cannot train, evaluate, export, or generate a release review manifest until a dataset manifest passes the required evidence checks. Raw data and model artifacts must remain on a controlled local drive outside version control; the repository’s `.gitignore` prevents accidental commits of common data and model formats.

## Future approved-data layout

When an approved source exists, keep its files outside the Git repository or inside the ignored `ml/data/` path. The validator expects an image folder layout such as:

```text
ml/data/<dataset-id>/
├── train/
│   ├── explicit/
│   └── non_explicit/
├── validation/
│   ├── explicit/
│   └── non_explicit/
└── evaluation/
    ├── explicit/
    └── non_explicit/
```

The `evaluation` split must come from a separately documented source or collection process. It may not be silently reused as training material.

The workshop explicitly maps `non_explicit` to model label `0` and `explicit` to model label `1`. This means the final sigmoid output is always the **explicit-content confidence** expected by Veil’s Android classifier contract.

The validator also requires `evaluationSourceUrl` to differ from the training-data source and requires every split/class to have a reviewed minimum image count. It refuses class folders that are not a matched `explicit/` and `non_explicit/` pair under the same split parent.

## Required local inputs

| Input | What it proves |
|---|---|
| `dataset-manifest.json` | Dataset license, provenance, adult-only assurance, commercial training/distribution rights, label definitions, approval record, reviewed minimum class counts, and a separate evaluation-source URL. |
| `training-config.json` | Immutable run configuration: seed, 224×224 contract, local base-model checkpoint identity, and artifact directory. |
| `evaluation-policy.json` | Human-approved metric gates for precision, recall, false-positive rate, latency, and device acceptance. The example deliberately leaves the gates unset. |
| Approved base-model checkpoint | A locally supplied MobileNetV3Small checkpoint whose source URL, license, and SHA-256 are recorded in the training configuration. |

## Commands

All commands are executed locally and never upload data. The first safe command to run with a future manifest is:

```bash
python3 ml/veil_ml.py validate-data --manifest /controlled/path/dataset-manifest.json
```

The other commands remain blocked until validation passes:

```bash
python3 ml/veil_ml.py train --manifest /controlled/path/dataset-manifest.json --config /controlled/path/training-config.json
python3 ml/veil_ml.py evaluate --manifest /controlled/path/dataset-manifest.json --policy /controlled/path/evaluation-policy.json --model /controlled/path/candidate.keras
python3 ml/veil_ml.py export-tflite --evaluation-report /controlled/path/evaluation-report.json --policy /controlled/path/evaluation-policy.json --model /controlled/path/candidate.keras --output /controlled/path/veil_explicit_v1.tflite
python3 ml/veil_ml.py create-review-manifest --model /controlled/path/veil_explicit_v1.tflite --dataset-manifest /controlled/path/dataset-manifest.json --evaluation-report /controlled/path/evaluation-report.json --output /controlled/path/veil_explicit_v1.review.json
```

## Privacy and safety boundaries

> Never use screenshots, photos, messages, browser history, accessibility events, or any other information from a Veil user as training or evaluation data.

This workshop does not download data, call a cloud model, generate images, upload files, enable MediaProjection, enable accessibility capture, or activate a monitoring/overlay service. A resulting model is not placed into the Android app until its local review manifest passes the existing `VeilModelReview` gate and Galaxy M20 measurement has been recorded.

For source approval rules and candidate-research history, see [`docs/architecture/tflite-training-data-plan.md`](../docs/architecture/tflite-training-data-plan.md). For Android asset acceptance, see [`docs/architecture/tflite-model-review-gate.md`](../docs/architecture/tflite-model-review-gate.md).
