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



def read_evaluation_manifest(path: Path):
    """Validate reviewer assertions and constrain local files to the manifest directory.

    Assertions cannot prove age, consent, licensing, or split independence. A qualified
    reviewer remains responsible for those claims; the harness never downloads images.
    """
    manifest = json.loads(path.read_text(encoding="utf-8"))
    required = ["approvedForEvaluation", "adultOnlyAssurance", "rightsVerified", "independentOfTraining"]
    if not isinstance(manifest, dict) or any(manifest.get(field) is not True for field in required):
        raise ValueError("Evaluation requires reviewed independent, licensed, approved adult-only data.")
    source, license_name = manifest.get("sourceUrl"), manifest.get("license")
    if not isinstance(source, str) or not source.startswith("https://") or not isinstance(license_name, str) or not license_name.strip():
        raise ValueError("Evaluation requires an HTTPS source URL and a named license.")
    records = manifest.get("images")
    if not isinstance(records, list) or not records:
        raise ValueError("Evaluation requires a nonempty image list.")
    base = path.resolve().parent
    seen_paths, seen_bytes, samples = set(), set(), []
    for record in records:
        if not isinstance(record, dict) or type(record.get("explicit")) is not bool:
            raise ValueError("Every evaluation record needs a boolean explicit label.")
        relative = record.get("path")
        if not isinstance(relative, str) or not relative.strip() or Path(relative).is_absolute():
            raise ValueError("Evaluation image paths must be relative to the manifest directory.")
        candidate = (base / relative).resolve()
        if not candidate.is_relative_to(base):
            raise ValueError("Evaluation image path escapes the approved manifest directory.")
        if not candidate.is_file() or candidate in seen_paths:
            raise ValueError("Evaluation image is absent or listed more than once.")
        digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
        if digest in seen_bytes:
            raise ValueError("Duplicate image bytes would invalidate independent sample counts.")
        seen_paths.add(candidate)
        seen_bytes.add(digest)
        samples.append((candidate, record["explicit"]))
    if {label for _, label in samples} != {True, False}:
        raise ValueError("Evaluation must contain both positive and negative cases.")
    return manifest, samples


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
        manifest, samples_to_evaluate = read_evaluation_manifest(args.evaluation_manifest)
        matrix = {"tp": 0, "fp": 0, "tn": 0, "fn": 0}
        for path, label in samples_to_evaluate:
            with Image.open(path) as image:
                scores, _ = infer(image.convert("RGB").resize((224, 224), Image.Resampling.NEAREST))
            predicted = float(scores[1] + scores[3]) >= .90
            matrix[("t" if predicted == label else "f") + ("p" if predicted else "n")] += 1
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
