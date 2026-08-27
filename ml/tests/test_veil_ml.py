import json
import tempfile
import unittest
from pathlib import Path

from veil_ml import MODEL_CLASS_ORDER, RefusalError, binary_metrics, export_tflite, validate_dataset_manifest


class VeilMlSafetyTest(unittest.TestCase):
    def test_explicit_is_the_positive_model_class(self):
        self.assertEqual(MODEL_CLASS_ORDER, ("non_explicit", "explicit"))

    def test_incomplete_dataset_manifest_is_refused_before_any_training(self):
        with tempfile.TemporaryDirectory() as workspace:
            manifest_path = Path(workspace) / "dataset-manifest.json"
            manifest_path.write_text(json.dumps({"datasetId": "unapproved"}), encoding="utf-8")
            with self.assertRaisesRegex(RefusalError, "Dataset validation refused"):
                validate_dataset_manifest(manifest_path)

    def test_failed_evaluation_blocks_tflite_export_before_tensorflow_is_loaded(self):
        with tempfile.TemporaryDirectory() as workspace:
            root = Path(workspace)
            report_path = root / "evaluation.json"
            policy_path = root / "policy.json"
            model_path = root / "candidate.keras"
            report_path.write_text(json.dumps({"passed": False}), encoding="utf-8")
            policy_path.write_text(json.dumps({}), encoding="utf-8")
            model_path.write_bytes(b"not a model")
            with self.assertRaisesRegex(RefusalError, "evaluation report did not pass"):
                export_tflite(report_path, policy_path, model_path, root / "candidate.tflite")

    def test_binary_metrics_calculates_false_positive_rate(self):
        metrics = binary_metrics([1, 1, 0, 0], [1, 0, 1, 0])
        self.assertEqual(metrics["truePositive"], 1)
        self.assertEqual(metrics["falsePositive"], 1)
        self.assertEqual(metrics["trueNegative"], 1)
        self.assertEqual(metrics["falseNegative"], 1)
        self.assertEqual(metrics["precision"], 0.5)
        self.assertEqual(metrics["recall"], 0.5)
        self.assertEqual(metrics["falsePositiveRate"], 0.5)
