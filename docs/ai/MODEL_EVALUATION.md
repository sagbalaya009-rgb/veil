# Model evaluation status â€” 2026-10-21

**Experimental integration. Semantic content accuracy and Android performance are not qualified.**

## Evidence classes

1. Byte integrity: the bundled 17,355,548-byte TFLite SHA-256 is `380f98f7685f9d8a386f8cc595b6dfcb972989aae3d1b8b270d3a4a5b96fab40`; the loader checks the manifest and the code pin. The exact upstream distributor commit is recorded in MODEL_SELECTION.md.
2. Desktop execution: **passed** using `ai-edge-litert==2.1.2`, Python 3.12.14 on Windows 11, two CPU threads. The generated `desktop-smoke-report.json` is the authoritative executed result: six generated solid colors plus 20 additional gray inputs, after two excluded warmups; finite normalized five-class outputs. Observed median invoke time **125.93 ms**, p95 **152.66 ms** (26 measured invokes). These fixtures test loading, shape, numerics, and actual inference only. They contain no meaningful labeled NSFW examples.
3. Android execution: `BundledModelInferenceTest` initializes the shipped asset and repeatedly processes generated solid colors. Its presence is not a claim that it ran. See the build/device report for executed instrumentation status.
4. Semantic accuracy: **not measured**. No labeled content dataset was acquired. No precision, recall, false-positive rate, F1, skin-tone robustness, screenshot-domain accuracy, or validated threshold is claimed.
5. Performance: only values actually present in the desktop smoke report may be described as measured. Desktop invocation timing excludes capture, decoding, resizing, Android/JNI, overlays, and battery costs. It does not establish latency, thermal, or battery behavior on the TECNO SPARK 30C.

## Reproduce real offline inference

Install Python 3.12 and the optional runtime packages in a local virtual environment, then run from the repository root:

```text
python -m pip install -r ml/inference-requirements.txt
python ml/evaluate_bundled_model.py --output work/model-smoke-report.json
```

After dependency installation, the script performs no networking. It verifies the asset hash, dtype, shapes, labels, finite softmax probabilities, runs six generated color inputs plus 20 additional inferences, and writes aggregate desktop invocation timing and synthetic scores. Missing/invalid assets or inference errors fail the command. No raw pixels or personal paths enter reports.

The training workshop remains independent:

```text
cd ml
python -m unittest discover -s tests -v
```

Eleven ML tests passed on 2026-10-21: the five existing data/metric/export safety tests and six evaluation-manifest tests covering local valid files, absolute paths, parent traversal, duplicate content, strict reviewer assertions, and boolean labels. An initial invocation from the root failed module import; using the documented ml working directory resolved the import, without changing tests.

## Independent semantic evaluation

`evaluate_bundled_model.py --evaluation-manifest /controlled/path/manifest.json --output /controlled/path/report.json` accepts a separately reviewed local image set. The manifest must declare `approvedForEvaluation`, `adultOnlyAssurance`, `rightsVerified`, and `independentOfTraining` as true; record `sourceUrl`, `license`, and an `images` array of `{ "path": "relative/local/path", "explicit": true/false }`. Both classes are required. Image paths must be relative and remain within the manifest directory after symlink resolution. Duplicate paths or duplicate file bytes are refused to protect sample counts. The source must be an HTTPS URL and the license must be named. These are reviewer assertions, not automated verification of image rights, consent, age, or split independence. A qualified reviewer remains responsible for the evidence. Do not use Veil users' captures as a dataset.

The script reports a confusion matrix, precision, recall, and false-positive rate at the provisional 0.90 single-frame threshold. It never declares automatic release qualification. Separate phone testing must evaluate temporal confirmation, false-positive recovery, supported capture behavior, inference throughput, peak memory, battery drain over a fixed duration, screen rotation, permission revocation, thermal slowdown, and unsupported/protected surfaces. Keep safety-critical medical information and emergency use accessible.

Do not treat upstream promotional accuracy as Veil accuracy, and do not tune thresholds on the held-out final test set. Before a broad public release, define acceptance limits, evaluate independent lawful adult-only data with representative benign medical, educational, artistic, sports, family-safe illustrations, varied lighting/skin tones, and supported app screenshot layouts, and investigate false positives and false negatives.

## Known limitations

- Whole-screen resizing can erase small images or distort a portrait feed; multi-image layouts and text are outside the model's established image domain.
- Softmax scores are uncalibrated. Temporal confirmation reduces isolated triggers but cannot establish correctness or eliminate systematic errors.
- Medical images, artwork, swimwear, cartoons, unusual lighting, compression, and partial body views may be misclassified. Demographic performance is unknown.
- Not every explicit scene will be found. The model cannot infer consent, age, identity, context, or legality.
- No bounding boxes or precise redaction regions are produced by this classifier.
- MIT license evidence exists; a complete training-data rights/consent audit and a conversion parity comparison against the original training artifact do not.
- The original repository's future Galaxy M20 release gate was tied to an absent custom binary model. The current master directive authorizes a real experimental upstream integration and names TECNO SPARK 30C as the owner test device. The new manifest records engineering integration review, not owner sign-off, model accuracy qualification, or completed physical-device testing. Existing training-data/export safety gates were retained.
