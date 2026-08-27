#!/usr/bin/env python3
"""Project Veil's local-only explicit-content classifier workshop.

This command deliberately refuses to train, evaluate, export, or create a release manifest unless
the caller supplies approved local manifests and evidence. It never downloads data or models.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import random
import sys
import time
from collections.abc import Iterable
from dataclasses import dataclass
from pathlib import Path
from typing import Any

IMAGE_SUFFIXES = {".jpg", ".jpeg", ".png", ".webp"}
SPLITS = ("train", "validation", "evaluation")
CLASSES = ("explicit", "non_explicit")
MODEL_CLASS_ORDER = ("non_explicit", "explicit")
EXPECTED_SHAPE = (224, 224, 3)
EXPECTED_NORMALIZATION = "rgb_float_0_to_1"


class RefusalError(ValueError):
    """Raised when a request does not satisfy Veil's data or release safety gates."""


def read_json(path: Path) -> dict[str, Any]:
    try:
        raw = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as error:
        raise RefusalError(f"Required file does not exist: {path}") from error
    except json.JSONDecodeError as error:
        raise RefusalError(f"Invalid JSON in {path}: {error.msg}") from error
    if not isinstance(raw, dict):
        raise RefusalError(f"Expected an object at the top of {path}.")
    return raw


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def required_text(source: dict[str, Any], key: str, errors: list[str]) -> str:
    value = source.get(key)
    if not isinstance(value, str) or not value.strip() or value.startswith("replace-with"):
        errors.append(f"{key} must be a completed non-empty value.")
        return ""
    return value.strip()


def required_https(source: dict[str, Any], key: str, errors: list[str]) -> str:
    value = required_text(source, key, errors)
    if value and not value.startswith("https://"):
        errors.append(f"{key} must use an https:// URL.")
    return value


def image_files(path: Path) -> list[Path]:
    return sorted(item for item in path.rglob("*") if item.is_file() and item.suffix.lower() in IMAGE_SUFFIXES)


@dataclass(frozen=True)
class DatasetValidation:
    manifest: dict[str, Any]
    class_counts: dict[str, dict[str, int]]


def validate_dataset_manifest(path: Path, verify_files: bool = True) -> DatasetValidation:
    manifest = read_json(path)
    errors: list[str] = []
    for key in ("datasetId", "version", "license", "adultOnlyAssurance", "provenance", "approvedBy", "approvedAt"):
        required_text(manifest, key, errors)
    required_https(manifest, "sourceUrl", errors)
    evaluation_source_url = required_https(manifest, "evaluationSourceUrl", errors)
    if evaluation_source_url and evaluation_source_url == manifest.get("sourceUrl"):
        errors.append("evaluationSourceUrl must identify a source separate from the training-data source.")
    if manifest.get("commercialTrainingAllowed") is not True:
        errors.append("commercialTrainingAllowed must be true after legal review.")
    if manifest.get("modelDistributionAllowed") is not True:
        errors.append("modelDistributionAllowed must be true after legal review.")
    labels = manifest.get("labelDefinitions")
    if not isinstance(labels, dict):
        errors.append("labelDefinitions must define explicit and non_explicit.")
    else:
        for label in CLASSES:
            required_text(labels, label, errors)
    independence = required_text(manifest, "evaluationIndependence", errors)
    if independence and "same" in independence.lower() and "not" not in independence.lower():
        errors.append("evaluationIndependence cannot describe reuse of training data.")

    splits = manifest.get("splits")
    minimum_counts = manifest.get("minimumImageCounts")
    if not isinstance(minimum_counts, dict):
        errors.append("minimumImageCounts must declare reviewed minimum counts for every split and class.")
    else:
        for split in SPLITS:
            reviewed_counts = minimum_counts.get(split)
            if not isinstance(reviewed_counts, dict):
                errors.append(f"minimumImageCounts.{split} must define explicit and non_explicit counts.")
                continue
            for label in CLASSES:
                required_count = reviewed_counts.get(label)
                if not isinstance(required_count, int) or isinstance(required_count, bool) or required_count <= 0:
                    errors.append(f"minimumImageCounts.{split}.{label} must be a reviewed positive integer.")
    counts: dict[str, dict[str, int]] = {}
    if not isinstance(splits, dict):
        errors.append("splits must define train, validation, and evaluation directories.")
    else:
        for split in SPLITS:
            split_paths = splits.get(split)
            if not isinstance(split_paths, dict):
                errors.append(f"splits.{split} must define explicit and non_explicit directories.")
                continue
            counts[split] = {}
            parent_folders: set[Path] = set()
            for label in CLASSES:
                raw_path = split_paths.get(label)
                if not isinstance(raw_path, str) or not raw_path.strip():
                    errors.append(f"splits.{split}.{label} must be a local directory path.")
                    continue
                folder = Path(raw_path).expanduser()
                parent_folders.add(folder.parent.resolve())
                if verify_files:
                    if not folder.is_dir():
                        errors.append(f"Required data directory does not exist: {folder}")
                        continue
                    count = len(image_files(folder))
                    counts[split][label] = count
                    if count == 0:
                        errors.append(f"Required data directory has no supported images: {folder}")
                    required_count = minimum_counts[split][label] if isinstance(minimum_counts, dict) else None
                    if isinstance(required_count, int) and count < required_count:
                        errors.append(f"Required data directory {folder} has {count} images; the reviewed minimum is {required_count}.")
            if len(parent_folders) > 1:
                errors.append(f"splits.{split} class folders must share one parent directory.")
    if errors:
        raise RefusalError("Dataset validation refused:\n- " + "\n- ".join(errors))
    return DatasetValidation(manifest=manifest, class_counts=counts)


def validate_training_config(path: Path) -> dict[str, Any]:
    config = read_json(path)
    errors: list[str] = []
    required_text(config, "runId", errors)
    if tuple(config.get(key) for key in ("inputWidth", "inputHeight", "inputChannels")) != EXPECTED_SHAPE:
        errors.append("The training input must be exactly 224 × 224 × 3.")
    if config.get("normalization") != EXPECTED_NORMALIZATION:
        errors.append(f"normalization must be {EXPECTED_NORMALIZATION}.")
    if config.get("architecture") != "MobileNetV3Small":
        errors.append("architecture must be MobileNetV3Small for the first Veil candidate.")
    for key in ("randomSeed", "epochs", "batchSize", "learningRate"):
        if not isinstance(config.get(key), (int, float)) or isinstance(config.get(key), bool) or config[key] <= 0:
            errors.append(f"{key} must be a positive number.")
    output_directory = required_text(config, "outputDirectory", errors)
    if output_directory and not Path(output_directory).expanduser().is_absolute():
        errors.append("outputDirectory must be an absolute local path.")
    checkpoint = config.get("baseCheckpoint")
    if not isinstance(checkpoint, dict):
        errors.append("baseCheckpoint must record path, sourceUrl, license, and sha256.")
    else:
        checkpoint_path = required_text(checkpoint, "path", errors)
        required_https(checkpoint, "sourceUrl", errors)
        required_text(checkpoint, "license", errors)
        expected_sha = required_text(checkpoint, "sha256", errors)
        if expected_sha and not _valid_sha(expected_sha):
            errors.append("baseCheckpoint.sha256 must be a 64-character SHA-256 value.")
        if checkpoint_path:
            file_path = Path(checkpoint_path).expanduser()
            if not file_path.is_file():
                errors.append(f"Approved base checkpoint does not exist: {file_path}")
            elif expected_sha and sha256(file_path).lower() != expected_sha.lower():
                errors.append("Approved base checkpoint SHA-256 does not match the training config.")
    if errors:
        raise RefusalError("Training configuration refused:\n- " + "\n- ".join(errors))
    return config


def validate_evaluation_policy(path: Path) -> dict[str, Any]:
    policy = read_json(path)
    errors: list[str] = []
    for key in ("policyId", "approvedBy", "approvedAt"):
        required_text(policy, key, errors)
    for key in ("minPrecision", "minRecall", "maxFalsePositiveRate", "maxMedianInferenceMs"):
        value = policy.get(key)
        if not isinstance(value, (int, float)) or isinstance(value, bool):
            errors.append(f"{key} must be a reviewed numeric release gate, not empty.")
    if isinstance(policy.get("minPrecision"), (int, float)) and not 0 < policy["minPrecision"] <= 1:
        errors.append("minPrecision must be between 0 and 1.")
    if isinstance(policy.get("minRecall"), (int, float)) and not 0 < policy["minRecall"] <= 1:
        errors.append("minRecall must be between 0 and 1.")
    if isinstance(policy.get("maxFalsePositiveRate"), (int, float)) and not 0 <= policy["maxFalsePositiveRate"] < 1:
        errors.append("maxFalsePositiveRate must be between 0 inclusive and 1 exclusive.")
    if isinstance(policy.get("maxMedianInferenceMs"), (int, float)) and policy["maxMedianInferenceMs"] <= 0:
        errors.append("maxMedianInferenceMs must be positive.")
    if errors:
        raise RefusalError("Evaluation policy refused:\n- " + "\n- ".join(errors))
    return policy


def _valid_sha(value: str) -> bool:
    return len(value) == 64 and all(character in "0123456789abcdefABCDEF" for character in value)


def _tensorflow() -> Any:
    try:
        import tensorflow as tf  # type: ignore[import-not-found]
    except ModuleNotFoundError as error:
        raise RefusalError("TensorFlow is not installed. Install ml/requirements.txt in an isolated local environment after data approval.") from error
    return tf


def _dataset(tf: Any, manifest: dict[str, Any], split: str, batch_size: int, shuffle: bool, seed: int) -> Any:
    split_paths = manifest["splits"][split]
    # The parent must contain explicit/ and non_explicit/ exactly once; validation already confirms both.
    parent = Path(split_paths["explicit"]).parent
    dataset = tf.keras.utils.image_dataset_from_directory(
        parent,
        labels="inferred",
        label_mode="binary",
        class_names=list(MODEL_CLASS_ORDER),
        image_size=EXPECTED_SHAPE[:2],
        batch_size=batch_size,
        shuffle=shuffle,
        seed=seed,
    )
    return dataset.prefetch(tf.data.AUTOTUNE)


def train(manifest_path: Path, config_path: Path) -> Path:
    validated = validate_dataset_manifest(manifest_path)
    config = validate_training_config(config_path)
    tf = _tensorflow()
    seed = int(config["randomSeed"])
    random.seed(seed)
    tf.keras.utils.set_random_seed(seed)
    train_data = _dataset(tf, validated.manifest, "train", int(config["batchSize"]), shuffle=True, seed=seed)
    validation_data = _dataset(tf, validated.manifest, "validation", int(config["batchSize"]), shuffle=False, seed=seed)

    checkpoint_path = Path(config["baseCheckpoint"]["path"]).expanduser()
    backbone = tf.keras.applications.MobileNetV3Small(
        include_top=False,
        weights=None,
        input_shape=EXPECTED_SHAPE,
        pooling="avg",
    )
    backbone.load_weights(checkpoint_path)
    backbone.trainable = False
    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=EXPECTED_SHAPE),
        tf.keras.layers.Rescaling(1.0 / 255.0),
        backbone,
        tf.keras.layers.Dropout(0.2),
        tf.keras.layers.Dense(1, activation="sigmoid", name="explicit_confidence"),
    ])
    model.compile(
        optimizer=tf.keras.optimizers.Adam(float(config["learningRate"])),
        loss=tf.keras.losses.BinaryCrossentropy(),
        metrics=[tf.keras.metrics.Precision(name="precision"), tf.keras.metrics.Recall(name="recall")],
    )
    artifact_dir = Path(config["outputDirectory"]).expanduser()
    artifact_dir.mkdir(parents=True, exist_ok=True)
    history = model.fit(train_data, validation_data=validation_data, epochs=int(config["epochs"]))
    output = artifact_dir / "candidate.keras"
    model.save(output)
    _write_json(artifact_dir / "training-run.json", {
        "runId": config["runId"],
        "datasetId": validated.manifest["datasetId"],
        "datasetVersion": validated.manifest["version"],
        "evaluationSourceUrl": validated.manifest["evaluationSourceUrl"],
        "classCounts": validated.class_counts,
        "inputContract": {"shape": EXPECTED_SHAPE, "normalization": EXPECTED_NORMALIZATION, "positiveClass": "explicit"},
        "baseCheckpoint": config["baseCheckpoint"],
        "history": history.history,
        "candidateModel": str(output),
        "candidateModelSha256": sha256(output),
    })
    return output


def evaluate(manifest_path: Path, policy_path: Path, model_path: Path, threshold: float) -> Path:
    if not 0 < threshold < 1:
        raise RefusalError("threshold must be greater than 0 and less than 1.")
    validated = validate_dataset_manifest(manifest_path)
    policy = validate_evaluation_policy(policy_path)
    if not model_path.is_file():
        raise RefusalError(f"Candidate model does not exist: {model_path}")
    tf = _tensorflow()
    model = tf.keras.models.load_model(model_path)
    data = _dataset(tf, validated.manifest, "evaluation", batch_size=1, shuffle=False, seed=20260827)
    true_labels: list[int] = []
    probabilities: list[float] = []
    latencies_ms: list[float] = []
    for images, labels in data:
        started = time.perf_counter()
        prediction = model(images, training=False).numpy().reshape(-1)[0]
        latencies_ms.append((time.perf_counter() - started) * 1000)
        probabilities.append(float(prediction))
        true_labels.append(int(labels.numpy().reshape(-1)[0]))
    if not true_labels:
        raise RefusalError("Evaluation data contains no images.")
    predicted = [int(score >= threshold) for score in probabilities]
    metrics = binary_metrics(true_labels, predicted)
    median_ms = sorted(latencies_ms)[len(latencies_ms) // 2]
    passed = (
        metrics["precision"] >= policy["minPrecision"]
        and metrics["recall"] >= policy["minRecall"]
        and metrics["falsePositiveRate"] <= policy["maxFalsePositiveRate"]
        and median_ms <= policy["maxMedianInferenceMs"]
    )
    output = model_path.parent / "evaluation-report.json"
    _write_json(output, {
        "policyId": policy["policyId"],
        "approvedBy": policy["approvedBy"],
        "approvedAt": policy["approvedAt"],
        "datasetId": validated.manifest["datasetId"],
        "evaluationSourceUrl": validated.manifest["evaluationSourceUrl"],
        "modelPath": str(model_path),
        "modelSha256": sha256(model_path),
        "threshold": threshold,
        "imageCount": len(true_labels),
        "metrics": metrics,
        "medianInferenceMs": median_ms,
        "passed": passed,
        "limitations": [
            "This local workstation result is not a Galaxy M20 benchmark.",
            "An approved independent challenge set is required before Android enforcement.",
        ],
    })
    return output


def binary_metrics(true_labels: Iterable[int], predicted_labels: Iterable[int]) -> dict[str, float | int]:
    pairs = list(zip(true_labels, predicted_labels, strict=True))
    true_positive = sum(actual == 1 and predicted == 1 for actual, predicted in pairs)
    false_positive = sum(actual == 0 and predicted == 1 for actual, predicted in pairs)
    true_negative = sum(actual == 0 and predicted == 0 for actual, predicted in pairs)
    false_negative = sum(actual == 1 and predicted == 0 for actual, predicted in pairs)
    precision = true_positive / (true_positive + false_positive) if true_positive + false_positive else 0.0
    recall = true_positive / (true_positive + false_negative) if true_positive + false_negative else 0.0
    false_positive_rate = false_positive / (false_positive + true_negative) if false_positive + true_negative else 0.0
    return {
        "truePositive": true_positive,
        "falsePositive": false_positive,
        "trueNegative": true_negative,
        "falseNegative": false_negative,
        "precision": precision,
        "recall": recall,
        "falsePositiveRate": false_positive_rate,
    }


def export_tflite(evaluation_report_path: Path, policy_path: Path, model_path: Path, output: Path) -> Path:
    report = read_json(evaluation_report_path)
    validate_passing_evaluation_report(report, model_path)
    validate_evaluation_policy(policy_path)
    if not model_path.is_file():
        raise RefusalError(f"Candidate model does not exist: {model_path}")
    tf = _tensorflow()
    model = tf.keras.models.load_model(model_path)
    input_shape = tuple(model.inputs[0].shape.as_list()[1:])
    output_shape = tuple(model.outputs[0].shape.as_list()[1:])
    if input_shape != EXPECTED_SHAPE or output_shape != (1,):
        raise RefusalError(f"Candidate model contract must be input {EXPECTED_SHAPE} and one output score.")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    try:
        converted = converter.convert()
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_bytes(converted)
        interpreter = tf.lite.Interpreter(model_path=str(output))
        interpreter.allocate_tensors()
        details = interpreter.get_input_details()[0]
        output_details = interpreter.get_output_details()[0]
        input_dtype = tf.as_dtype(details["dtype"])
        output_dtype = tf.as_dtype(output_details["dtype"])
        if tuple(details["shape"].tolist()) != (1, *EXPECTED_SHAPE) or input_dtype != tf.float32:
            raise RefusalError("Converted TFLite input no longer satisfies Veil's float 224×224 RGB contract.")
        if tuple(output_details["shape"].tolist()) != (1, 1) or output_dtype != tf.float32:
            raise RefusalError("Converted TFLite output no longer satisfies Veil's one-float confidence contract.")
    except Exception:
        output.unlink(missing_ok=True)
        raise
    return output


def create_review_manifest(
    model_path: Path,
    dataset_manifest_path: Path,
    evaluation_report_path: Path,
    output: Path,
    source_url: str,
    evaluation_report_url: str,
    rollback_model_id: str,
    approved_by: str,
    approved_at: str,
) -> Path:
    if not model_path.is_file():
        raise RefusalError(f"TensorFlow Lite model does not exist: {model_path}")
    dataset = validate_dataset_manifest(dataset_manifest_path, verify_files=False).manifest
    report = read_json(evaluation_report_path)
    validate_passing_evaluation_report(report, model_path)
    if report.get("datasetId") != dataset["datasetId"]:
        raise RefusalError("Review manifest creation is blocked because the evaluation report names a different dataset.")
    for label, value in (("source_url", source_url), ("evaluation_report_url", evaluation_report_url)):
        if not value.startswith("https://"):
            raise RefusalError(f"{label} must be an https:// URL for Android release review.")
    for label, value in (("rollback_model_id", rollback_model_id), ("approved_by", approved_by), ("approved_at", approved_at)):
        if not value.strip():
            raise RefusalError(f"{label} must be completed for Android release review.")
    _write_json(output, {
        "modelId": "veil-explicit-v1",
        "sha256": sha256(model_path),
        "sourceUrl": source_url,
        "license": dataset["license"],
        "datasetProvenanceUrl": dataset["sourceUrl"],
        "evaluationReportUrl": evaluation_report_url,
        "rollbackModelId": rollback_model_id,
        "approvedBy": approved_by,
        "approvedAt": approved_at,
        "inputWidth": 224,
        "inputHeight": 224,
        "inputChannels": 3,
        "outputScores": 1,
        "normalization": EXPECTED_NORMALIZATION,
    })
    return output


def validate_passing_evaluation_report(report: dict[str, Any], model_path: Path) -> None:
    if report.get("passed") is not True:
        raise RefusalError("TensorFlow Lite export is blocked because the evaluation report did not pass all reviewed gates.")
    required_fields = ("policyId", "approvedBy", "approvedAt", "datasetId", "modelPath", "modelSha256", "metrics", "medianInferenceMs")
    missing = [field for field in required_fields if field not in report or report[field] in (None, "")]
    if missing:
        raise RefusalError("Passing evaluation report is incomplete: " + ", ".join(missing))
    if not _valid_sha(str(report["modelSha256"])) or str(report["modelSha256"]).lower() != sha256(model_path).lower():
        raise RefusalError("Passing evaluation report does not match the candidate model checksum.")


def _write_json(path: Path, contents: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(contents, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Veil local-only NSFW classifier workshop")
    commands = parser.add_subparsers(dest="command", required=True)
    validate = commands.add_parser("validate-data", help="Validate approved dataset evidence and local split paths")
    validate.add_argument("--manifest", type=Path, required=True)
    train_command = commands.add_parser("train", help="Train locally after data and base-checkpoint validation")
    train_command.add_argument("--manifest", type=Path, required=True)
    train_command.add_argument("--config", type=Path, required=True)
    evaluate_command = commands.add_parser("evaluate", help="Evaluate a local candidate against reviewed thresholds")
    evaluate_command.add_argument("--manifest", type=Path, required=True)
    evaluate_command.add_argument("--policy", type=Path, required=True)
    evaluate_command.add_argument("--model", type=Path, required=True)
    evaluate_command.add_argument("--threshold", type=float, default=0.90)
    export_command = commands.add_parser("export-tflite", help="Export only a passing candidate")
    export_command.add_argument("--evaluation-report", type=Path, required=True)
    export_command.add_argument("--policy", type=Path, required=True)
    export_command.add_argument("--model", type=Path, required=True)
    export_command.add_argument("--output", type=Path, required=True)
    review_command = commands.add_parser("create-review-manifest", help="Create Android review data only after a passing evaluation")
    review_command.add_argument("--model", type=Path, required=True)
    review_command.add_argument("--dataset-manifest", type=Path, required=True)
    review_command.add_argument("--evaluation-report", type=Path, required=True)
    review_command.add_argument("--output", type=Path, required=True)
    review_command.add_argument("--source-url", required=True)
    review_command.add_argument("--evaluation-report-url", required=True)
    review_command.add_argument("--rollback-model-id", required=True)
    review_command.add_argument("--approved-by", required=True)
    review_command.add_argument("--approved-at", required=True)
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    try:
        if args.command == "validate-data":
            result = validate_dataset_manifest(args.manifest)
            print(json.dumps({"status": "approved", "datasetId": result.manifest["datasetId"], "classCounts": result.class_counts}, indent=2))
        elif args.command == "train":
            print(train(args.manifest, args.config))
        elif args.command == "evaluate":
            print(evaluate(args.manifest, args.policy, args.model, args.threshold))
        elif args.command == "export-tflite":
            print(export_tflite(args.evaluation_report, args.policy, args.model, args.output))
        elif args.command == "create-review-manifest":
            print(create_review_manifest(
                args.model, args.dataset_manifest, args.evaluation_report, args.output,
                args.source_url, args.evaluation_report_url, args.rollback_model_id,
                args.approved_by, args.approved_at,
            ))
        return 0
    except RefusalError as error:
        print(str(error), file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
