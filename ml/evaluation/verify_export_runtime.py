#!/usr/bin/env python3
"""Engineering-only TensorFlow/export smoke on generated tensors; no model training.

Creates a deterministic tiny mathematical graph, never an NSFW candidate. Its
temporary passing report is a conversion gate fixture, not a semantic evaluation.
All temporary model bytes are removed when the process exits normally.
"""
from __future__ import annotations

import json
import sys
import tempfile
from pathlib import Path

import numpy as np
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from veil_ml import _dataset, export_tflite, sha256


def main():
    import tensorflow as tf
    tf.keras.utils.set_random_seed(20261022)
    results = []
    with tempfile.TemporaryDirectory(prefix="veil-conversion-math-") as workspace:
        root = Path(workspace)
        for name, color in (("explicit", "white"), ("non_explicit", "black")):
            directory = root / "train" / name
            directory.mkdir(parents=True)
            Image.new("RGB", (16, 16), color).save(directory / "math.png")
        manifest = {"splits": {"train": {label: str(root / "train" / label) for label in ("explicit", "non_explicit")}}}
        images, labels = next(iter(_dataset(tf, manifest, "train", 2, False, 20261022)))
        assert set(np.unique(images.numpy()).tolist()) == {0.0, 1.0}
        # Synthetic class directory names exercise ordering only, never NSFW labels.
        assert labels.numpy().reshape(-1).tolist() == [0.0, 1.0]
        inputs = tf.keras.Input(shape=(224, 224, 3))
        transformed = tf.keras.layers.Rescaling(2.0, offset=-1.0)(inputs)
        values = tf.keras.layers.GlobalAveragePooling2D()(transformed)
        output = tf.keras.layers.Dense(1, activation="sigmoid", kernel_initializer="ones", bias_initializer="zeros")(values)
        model = tf.keras.Model(inputs, output)
        # This verifies the exported RGB01 math, not a trained backbone's quality.
        normalizer = tf.keras.Model(inputs, transformed)
        assert set(np.unique(normalizer(images).numpy()).tolist()) == {-1.0, 1.0}
        source = root / "math-only.keras"
        model.save(source)
        policy = {"policyId": "math-conversion-fixture", "approvedBy": "automated-engineering-test", "approvedAt": "2026-10-22",
                  "minPrecision": .9, "minRecall": .9, "maxFalsePositiveRate": .05, "maxMedianInferenceMs": 1000}
        policy_path = root / "policy.json"
        policy_path.write_text(json.dumps(policy), encoding="utf-8")
        report = {"policyId": policy["policyId"], "approvedBy": policy["approvedBy"], "approvedAt": policy["approvedAt"],
                  "datasetId": "mathematical-gate-fixture-not-semantic", "modelPath": str(source), "modelSha256": sha256(source),
                  "evaluationPolicySha256": sha256(policy_path), "metrics": {"precision": 1., "recall": 1., "falsePositiveRate": 0.},
                  "medianInferenceMs": 1., "passed": True}
        report_path = root / "math-gate-fixture.json"
        report_path.write_text(json.dumps(report), encoding="utf-8")
        for mode in ("none", "dynamic", "float16"):
            target = root / f"math-only-{mode}.tflite"
            export_tflite(report_path, policy_path, source, target, mode)
            sidecar = json.loads(target.with_suffix(".export.json").read_text(encoding="utf-8"))
            assert sidecar["releaseQualified"] is False
            results.append({"mode": mode, "parityCases": sidecar["syntheticParityCases"], "maxAbsoluteError": sidecar["maxAbsoluteError"]})
    print(json.dumps({"status": "engineering-runtime-smoke-passed", "tensorflow": tf.__version__,
                      "trainingPerformed": False, "semanticAccuracyMeasured": False, "exports": results}))


if __name__ == "__main__":
    main()
