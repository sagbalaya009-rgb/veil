#!/usr/bin/env python3
"""Compare reviewed local TFLite candidates on the same approved image manifest.

Reports contain aggregates, never image paths, images, or per-image scores. This
measures host CPU invocation and sampled sequence replay, not Android end-to-end.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
import platform
import re
import statistics
import sys
import time
from collections import defaultdict
from pathlib import Path

import numpy as np
from PIL import Image, ImageOps

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from evaluation.leakage import reject_split_leakage
from evaluation.metrics import summarize, temporal_first_trigger
from evaluate_bundled_model import interpreter_class, read_evaluation_manifest

STRATA = {"ordinary-screen", "feed", "thumbnail", "small-region", "video", "dark-mode", "light-mode",
          "text-overlay", "medical", "fitness", "art", "beachwear", "family-safe", "other"}


def load_samples(path):
    manifest, samples = read_evaluation_manifest(path)
    if manifest.get("split") not in ("validation", "held-out-test"):
        raise ValueError("Declare validation or held-out-test; do not tune on held-out-test.")
    if not isinstance(manifest.get("datasetId"), str) or not re.fullmatch(r"[A-Za-z0-9_.-]{1,80}", manifest["datasetId"]):
        raise ValueError("Dataset ID must be a short opaque identifier, never a personal path.")
    records = manifest["images"]
    for record in records:
        if not isinstance(record.get("strata"), list) or not record["strata"] or set(record["strata"]) - STRATA:
            raise ValueError("Each image needs predeclared non-sensitive benchmark strata.")
        if record.get("category") not in ("safe", "suggestive", "explicit"):
            raise ValueError("Each image needs an independently reviewed category.")
        if record["explicit"] != (record["category"] == "explicit"):
            raise ValueError("Binary and category labels disagree.")
        if "sequence" in record:
            if not re.fullmatch(r"[A-Za-z0-9_.-]{1,80}", str(record["sequence"])):
                raise ValueError("Sequence IDs must be opaque identifiers.")
            for key in ("timestampMs", "explicitOnsetMs"):
                if type(record.get(key)) not in (int, float) or not math.isfinite(record[key]) or record[key] < 0:
                    raise ValueError("Sequence records need nonnegative timestamps and annotated onset.")
    reject_split_leakage([(p, manifest["split"], y) for p, y in samples])
    return manifest, samples


def validate_candidate(candidate, base):
    if not isinstance(candidate, dict) or not re.fullmatch(r"[A-Za-z0-9_.-]{1,80}", str(candidate.get("modelId", ""))):
        raise ValueError("Candidate needs an opaque modelId.")
    model = (base / candidate["path"]).resolve()
    digest = hashlib.sha256(model.read_bytes()).hexdigest()
    if digest != candidate.get("sha256"):
        raise ValueError("Candidate hash differs from approved configuration.")
    if candidate.get("contract") not in ("gantman-five-rgb01", "binary-explicit-rgb01"):
        raise ValueError("Unimplemented model contract; add and test an explicit adapter first.")
    thresholds = candidate.get("thresholds")
    if not isinstance(thresholds, list) or not thresholds or any(type(t) not in (int, float) or not math.isfinite(t) or not 0 < t < 1 for t in thresholds):
        raise ValueError("Predeclare valid operating thresholds before evaluation.")
    return model, digest


def benchmark(candidate, base, manifest, samples, view="full"):
    model, digest = validate_candidate(candidate, base)
    cls, runtime = interpreter_class()
    runner = cls(model_path=str(model), num_threads=2)
    runner.allocate_tensors()
    inputs, outputs = runner.get_input_details(), runner.get_output_details()
    scores_count = 5 if candidate["contract"] == "gantman-five-rgb01" else 1
    if len(inputs) != 1 or len(outputs) != 1 or inputs[0]["shape"].tolist() != [1, 224, 224, 3] or outputs[0]["shape"].tolist() != [1, scores_count]:
        raise ValueError("Candidate tensor dimensions differ from declared contract.")
    if inputs[0]["dtype"] != np.float32 or outputs[0]["dtype"] != np.float32:
        raise ValueError("Only verified float32 I/O supported; quantized I/O requires an explicit adapter.")
    def infer(tensor):
        runner.set_tensor(inputs[0]["index"], tensor)
        start = time.perf_counter()
        runner.invoke()
        elapsed = (time.perf_counter() - start) * 1000
        raw = runner.get_tensor(outputs[0]["index"])[0]
        if not np.isfinite(raw).all() or (raw < 0).any() or (raw > 1).any() or (scores_count == 5 and abs(float(raw.sum()) - 1) >= .02):
            raise ValueError("Candidate produced invalid output.")
        return float(raw[1] + raw[3]) if scores_count == 5 else float(raw[0]), elapsed
    for _ in range(2):
        infer(np.zeros((1, 224, 224, 3), dtype=np.float32))
    scores, latencies = [], []
    for path, _ in samples:
        with Image.open(path) as original:
            image = ImageOps.exif_transpose(original).convert("RGB")
            views = [image]
            if view == "full-plus-center":
                width, height = image.size
                side = min(width, height)
                left, top = (width-side)//2, (height-side)//2
                views.append(image.crop((left, top, left+side, top+side)))
            results = [infer(np.asarray(v.resize((224, 224), Image.Resampling.NEAREST), dtype=np.float32)[None] / 255.0) for v in views]
            scores.append(max(score for score, _ in results))
            latencies.append(sum(elapsed for _, elapsed in results))
    labels = [int(label) for _, label in samples]
    reports = []
    for threshold in candidate["thresholds"]:
        result = summarize(labels, scores, threshold)
        result["strata"] = {}
        for group in sorted(STRATA):
            indexes = [i for i, record in enumerate(manifest["images"]) if group in record["strata"]]
            if indexes:
                result["strata"][group] = summarize([labels[i] for i in indexes], [scores[i] for i in indexes], threshold)
        result["annotatedCategories"] = {}
        for category in ("safe", "suggestive", "explicit"):
            indexes = [i for i, record in enumerate(manifest["images"]) if record["category"] == category]
            result["annotatedCategories"][category] = {
                "count": len(indexes), "interventionRate": sum(scores[i] >= threshold for i in indexes) / len(indexes) if indexes else None}
        sequences = defaultdict(list)
        for record, score in zip(manifest["images"], scores, strict=True):
            if "sequence" in record:
                sequences[record["sequence"]].append((record["timestampMs"], record["explicitOnsetMs"], score))
        delays, missed, pre_onset = [], 0, 0
        for points in sequences.values():
            points.sort()
            if len({onset for _, onset, _ in points}) != 1:
                raise ValueError("Sequence onset must remain consistent.")
            trigger = temporal_first_trigger([t for t, _, _ in points], [s for _, _, s in points], threshold)
            if trigger is None:
                missed += 1
            elif trigger < points[0][1]:
                pre_onset += 1
            else:
                delays.append(trigger - points[0][1])
        result["sampledTemporalReplay"] = {"sequences": len(sequences), "missed": missed, "preOnsetTriggers": pre_onset,
                                          "medianSampleDelayMs": statistics.median(delays) if delays else None,
                                          "policy": "2 of last 3; reset gap >2000ms; not device end-to-end latency"}
        reports.append(result)
    return {"modelId": candidate["modelId"], "sha256": digest, "bytes": model.stat().st_size, "runtime": runtime,
            "view": view, "threads": 2, "results": reports,
            "hostInvokeTiming": {"count": len(latencies), "medianMs": statistics.median(latencies), "p95Ms": float(np.percentile(latencies, 95)),
                                 "excludes": "image load/resize/capture/policy/overlay; two warmups excluded"}}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--candidates", type=Path, required=True)
    parser.add_argument("--view", choices=("full", "full-plus-center"), default="full")
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    manifest, samples = load_samples(args.manifest)
    config = json.loads(args.candidates.read_text(encoding="utf-8"))
    if not isinstance(config.get("models"), list) or not config["models"]:
        raise ValueError("Predeclare at least one candidate.")
    report = {"schemaVersion": 2, "datasetId": manifest["datasetId"], "split": manifest["split"],
              "manifestSha256": hashlib.sha256(args.manifest.read_bytes()).hexdigest(),
              "candidateConfigSha256": hashlib.sha256(args.candidates.read_bytes()).hexdigest(),
              "host": platform.platform(), "python": platform.python_version(),
              "models": [benchmark(c, args.candidates.resolve().parent, manifest, samples, args.view) for c in config["models"]],
              "releaseQualified": False, "notMeasured": ["phone energy", "thermal", "CPU/GPU utilization", "Android end-to-end", "clinical outcomes"],
              "limitations": ["Reviewer assertions do not prove licensing, adult assurance, or label quality.",
                              "Binary explicit scores do not establish three-class calibrated probabilities.",
                              "No automatic best-model selection or threshold deployment.",
                              "Center-crop maximum is an evaluation experiment; it can increase false positives."]}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2, allow_nan=False) + "\n", encoding="utf-8")
    print(json.dumps({"models": len(report["models"]), "images": len(samples), "releaseQualified": False}))


if __name__ == "__main__":
    main()
