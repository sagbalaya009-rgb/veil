#!/usr/bin/env python3
"""Offline integrity, real inference, and optional independent evaluation for the bundled model.

Synthetic smoke tests establish executable tensor compatibility, never semantic accuracy.
No screenshots, test images, or image paths are written into reports or uploaded.
"""
from __future__ import annotations
import argparse
import hashlib
import json
import platform
import statistics
import time
from pathlib import Path

import numpy as np
from PIL import Image

LABELS = ["drawings", "hentai", "neutral", "porn", "sexy"]


def interpreter_class():
    try:
        from ai_edge_litert.interpreter import Interpreter
        return Interpreter, "ai-edge-litert"
    except ImportError:
        from tensorflow.lite.python.interpreter import Interpreter
        return Interpreter, "tensorflow"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--model", type=Path, default=Path("app/src/main/assets/models/veil_explicit_v1.tflite"))
    parser.add_argument("--review", type=Path, default=Path("app/src/main/assets/models/veil_explicit_v1.review.json"))
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--evaluation-manifest", type=Path)
    args = parser.parse_args()
    review = json.loads(args.review.read_text(encoding="utf-8-sig"))
    model_bytes = args.model.read_bytes()
    digest = hashlib.sha256(model_bytes).hexdigest()
    if digest != review["sha256"] or review["labels"] != LABELS:
        raise ValueError("Model hash or label contract mismatch.")
    cls, runtime = interpreter_class()
    runner = cls(model_path=str(args.model), num_threads=2)
    runner.allocate_tensors()
    inp, out = runner.get_input_details(), runner.get_output_details()
    assert len(inp) == len(out) == 1
    assert inp[0]["shape"].tolist() == [1, 224, 224, 3] and inp[0]["dtype"] == np.float32
    assert out[0]["shape"].tolist() == [1, 5] and out[0]["dtype"] == np.float32

    def infer(rgb):
        tensor = np.asarray(rgb, dtype=np.float32).reshape(1, 224, 224, 3) / 255.0
        runner.set_tensor(inp[0]["index"], tensor)
        start = time.perf_counter()
        runner.invoke()
        elapsed = (time.perf_counter() - start) * 1000
        scores = runner.get_tensor(out[0]["index"])[0]
        if not np.isfinite(scores).all() or (scores < 0).any() or (scores > 1).any() or abs(float(scores.sum()) - 1) >= .02:
            raise ValueError("Invalid softmax scores.")
        return scores, elapsed

    synthetic = {name: np.full((224, 224, 3), color, dtype=np.uint8) for name, color in {
        "black": [0, 0, 0], "white": [255, 255, 255], "gray": [127, 127, 127],
        "red": [255, 0, 0], "green": [0, 255, 0], "blue": [0, 0, 255],
    }.items()}
    infer(synthetic["gray"])
    infer(synthetic["gray"])
    samples = []
    latencies = []
    for name, rgb in synthetic.items():
        scores, elapsed = infer(rgb)
        latencies.append(elapsed)
        samples.append({"fixture": name, "scores": dict(zip(LABELS, map(float, scores))),
                        "explicitScore": float(scores[1] + scores[3]), "invokeMs": elapsed})
    for _ in range(20):
        _, elapsed = infer(synthetic["gray"])
        latencies.append(elapsed)
    report = {
        "status": "real-inference-smoke-passed", "semanticValidation": "not-performed",
        "modelId": review["modelId"], "sha256": digest, "bytes": len(model_bytes),
        "runtime": runtime, "host": platform.platform(), "python": platform.python_version(),
        "threads": 2, "input": [1, 224, 224, 3], "output": [1, 5], "dtype": "float32",
        "normalization": "RGB / 255; nearest resize", "labels": LABELS,
        "explicitPolicy": "hentai + porn; sexy is separate; no calibration claim",
        "syntheticFixtures": samples, "timing": {
            "kind": "desktop CPU invoke only, two warmups excluded; not Android/battery/end-to-end",
            "count": len(latencies), "medianMs": statistics.median(latencies),
            "p95Ms": float(np.percentile(latencies, 95)),
        },
    }
    if args.evaluation_manifest:
        manifest = json.loads(args.evaluation_manifest.read_text(encoding="utf-8"))
        required = ["approvedForEvaluation", "adultOnlyAssurance", "rightsVerified", "independentOfTraining"]
        if any(manifest.get(field) is not True for field in required) or not manifest.get("sourceUrl") or not manifest.get("license"):
            raise ValueError("Evaluation requires documented independent, licensed, approved adult-only data.")
        matrix = {"tp": 0, "fp": 0, "tn": 0, "fn": 0}
        counts = {True: 0, False: 0}
        for record in manifest["images"]:
            label = record["explicit"]
            if type(label) is not bool:
                raise ValueError("explicit labels must be booleans")
            path = args.evaluation_manifest.parent / record["path"]
            with Image.open(path) as image:
                scores, _ = infer(image.convert("RGB").resize((224, 224), Image.Resampling.NEAREST))
            predicted = float(scores[1] + scores[3]) >= .90
            matrix[("t" if predicted == label else "f") + ("p" if predicted else "n")] += 1
            counts[label] += 1
        if not all(counts.values()):
            raise ValueError("Evaluation must contain both positive and negative cases.")
        tp, fp, tn, fn = (matrix[k] for k in ["tp", "fp", "tn", "fn"])
        report["semanticValidation"] = "evaluated-on-owner-supplied-manifest; not automatic release qualification"
        report["evaluation"] = {"confusionMatrix": matrix, "threshold": .90,
            "precision": tp / (tp + fp) if tp + fp else None, "recall": tp / (tp + fn),
            "falsePositiveRate": fp / (fp + tn), "sourceUrl": manifest["sourceUrl"], "license": manifest["license"]}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"status": report["status"], "semanticValidation": report["semanticValidation"], "sha256": digest, "timing": report["timing"]}))


if __name__ == "__main__":
    main()
