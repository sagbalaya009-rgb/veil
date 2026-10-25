# Controlled supervised training workshop

V2 repairs and extends `ml/veil_ml.py`; it does not claim a new trained model. No approved semantic dataset, licensed base checkpoint or completed training run was available. The shipped app uses its existing five-class pretrained model. The workshop creates a **different binary candidate**, requiring an Android adapter migration and separate release review.

## Inputs and contract

Use a fresh Python 3.12 environment with `ml/requirements.txt`. Supply a completed dataset manifest, licensed local MobileNetV3Small checkpoint with verified hash, an immutable run configuration, and reviewed evaluation policy. No runtime command downloads media, checkpoints or cloud resources. Keep all images/checkpoints and output run folders outside version control.

`validate-data` checks explicit/non_explicit class folder names, source/evaluation-source separation assertions, positive reviewed minimum counts, actual decodable local files, content/decoded duplicates and cross-split near-duplicate candidates. Legal/age/consent and label review are human evidence, not proven by a boolean or URL. Do not fabricate those assertions to run the command. Near-duplicate screening cannot replace collection-level/sequence-level separation.

Candidate input is exactly FLOAT32 RGB `[0,1]` at 224x224 using nearest resize, with scalar explicit score output. Directory loading performs `/255` once; the model applies `2*x-1`, and MobileNetV3Small is constructed with `include_preprocessing=False`. This matches the [Keras documented `[-1,1]` backbone contract](https://keras.io/api/applications/mobilenet/mobilenet_models/). V1 workshop had combined `/255` with MobileNetV3's default built-in rescaler, and its external tensor declaration disagreed with training input. This defect affected the unused custom-training workshop, not the shipped MobileNetV2 adapter. Do not reuse an old workshop checkpoint without checking its normalization.

## Run sequence

```text
python ml/veil_ml.py validate-data --manifest /controlled/dataset-manifest.json
python ml/veil_ml.py train --manifest /controlled/dataset-manifest.json --config /controlled/run-config.json
python ml/veil_ml.py evaluate --manifest /controlled/dataset-manifest.json --policy /controlled/policy.json --model /controlled/run/candidate.keras --threshold 0.90
python ml/veil_ml.py export-tflite --evaluation-report /controlled/run/evaluation-report.json --policy /controlled/policy.json --model /controlled/run/candidate.keras --output /controlled/run/candidate-float.tflite
```

The example threshold is a command example, not a validated operating point. Select a threshold using a distinct validation set and reviewed cost limits before the frozen final test. The evaluation command uses the `evaluation` split; do not repeatedly retune against it.

Training seeds Python/Keras, requests deterministic TensorFlow operators, records dataset/config/base/model hashes, TensorFlow/Python versions, seed and device types. Determinism is requested, not a guarantee across hardware/library versions. It first trains a frozen-backbone head with early stopping and best checkpoints. `fineTuneEpochs` and `fineTuneLayers` optionally unfreeze a bounded tail at one-tenth learning rate, while BatchNormalization statistics remain frozen. Existing nonempty output folders are refused. The final model and separate training histories are saved. No claim of training success is valid until this path is actually executed with approved inputs.

Export accepts `--quantization none`, `dynamic` or `float16`. It checks the exact evaluated policy hash and metrics, source-model hash, shapes/dtypes, then compares eight deterministic synthetic black/gray/white/noise tensors between Keras and TFLite. Maximum absolute error must meet the declared tolerance (default .001, at most .01). Failed conversion/parity removes the newly created model. Existing artifact/parity-report paths are never overwritten. A sidecar binds the TFLite hash to source hash, conversion mode and parity result.

Synthetic parity verifies conversion numerics, not semantic accuracy after quantization. Re-evaluate the converted artifact on approved held-out data using `ml/evaluation/benchmark.py` with the binary contract, measure phone latency/energy/memory, and document regressions before considering replacement. [LiteRT conversion guidance](https://developers.google.com/edge/litert/conversion/tensorflow/quantization/post_training_quantization) recommends a float baseline for diagnosing quantization changes. Integer-only I/O/representative-dataset quantization is intentionally not implemented until its calibration data and Android adapter are approved.

`create-review-manifest` binds the source evaluation through the conversion sidecar, and emits a hash-derived candidate ID, rollback reference and `releaseQualified:false`. It cannot automatically authorize deployment. Keep the current known artifact available; deliberately update the Android contract, hash pin, labels, preprocessing and tests together. Do not silently load arbitrary runtime-downloaded models.
