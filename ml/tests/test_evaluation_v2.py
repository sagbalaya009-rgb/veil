import json
import math
import tempfile
import unittest
from pathlib import Path

from PIL import Image
from evaluation.metrics import summarize, temporal_first_trigger
from evaluation.leakage import reject_split_leakage
from evaluation.benchmark import load_samples, validate_candidate
from veil_ml import RefusalError, sha256, validate_dataset_manifest, validate_report_policy


class EvaluationV2Test(unittest.TestCase):
    def test_known_confusion_and_calibration(self):
        result = summarize([1, 0, 1, 0], [.9, .8, .2, .1], .5)
        self.assertEqual(result["confusionMatrix"], {"tp": 1, "fp": 1, "tn": 1, "fn": 1})
        self.assertEqual(result["falseNegativeRate"], .5)
        self.assertAlmostEqual(result["brierScore"], .325)
        self.assertAlmostEqual(result["expectedCalibrationError"], .45)

    def test_absent_denominator_is_unavailable_not_zero(self):
        result = summarize([0, 0], [.1, .2])
        self.assertIsNone(result["precision"])
        self.assertIsNone(result["recall"])
        self.assertIsNone(result["falseNegativeRate"])
        self.assertEqual(result["falsePositiveRate"], 0)

    def test_nonfinite_scores_and_length_mismatch_refused(self):
        for scores in ([float("nan")], [float("inf")], [1.1], []):
            with self.assertRaises(ValueError):
                summarize([1], scores)

    def test_temporal_confirmation_resets_on_gap(self):
        self.assertEqual(temporal_first_trigger([0, 500, 1000], [.95, .1, .95]), 1000)
        self.assertIsNone(temporal_first_trigger([0, 3000, 3500], [.95, .1, .95]))
        with self.assertRaises(ValueError):
            temporal_first_trigger([500, 500], [.95, .95])

    def test_decoded_copy_with_different_encoding_cannot_cross_splits(self):
        with tempfile.TemporaryDirectory() as workspace:
            base = Path(workspace)
            image = Image.new("RGB", (12, 12), "blue")
            image.save(base / "a.png", compress_level=1)
            image.save(base / "b.png", compress_level=9)
            self.assertNotEqual((base / "a.png").read_bytes(), (base / "b.png").read_bytes())
            with self.assertRaisesRegex(ValueError, "Duplicate content"):
                reject_split_leakage([(base / "a.png", "train", 0), (base / "b.png", "test", 0)])

    def test_near_duplicate_across_splits_requires_review(self):
        with tempfile.TemporaryDirectory() as workspace:
            base = Path(workspace)
            image = Image.new("RGB", (64, 64))
            for x in range(64):
                for y in range(64):
                    image.putpixel((x, y), (x * 4, y * 4, 40))
            image.save(base / "a.png")
            image.putpixel((32, 32), (100, 100, 100))
            image.save(base / "b.png")
            with self.assertRaisesRegex(ValueError, "near-duplicate"):
                reject_split_leakage([(base / "a.png", "train", 0), (base / "b.png", "test", 0)])

    def test_low_information_hash_collision_does_not_relabel_solids(self):
        with tempfile.TemporaryDirectory() as workspace:
            base = Path(workspace)
            Image.new("RGB", (8, 8), "red").save(base / "a.png")
            Image.new("RGB", (8, 8), "blue").save(base / "b.png")
            reject_split_leakage([(base / "a.png", "train", 0), (base / "b.png", "test", 1)])

    def test_candidate_integrity_and_explicit_contract_required(self):
        with tempfile.TemporaryDirectory() as workspace:
            base = Path(workspace)
            (base / "model.bin").write_bytes(b"fixture not a model")
            candidate = {"modelId": "opaque-v1", "path": "model.bin", "sha256": sha256(base / "model.bin"),
                         "contract": "unsupported", "thresholds": [.9]}
            with self.assertRaisesRegex(ValueError, "contract"):
                validate_candidate(candidate, base)
            candidate["contract"] = "binary-explicit-rgb01"
            candidate["thresholds"] = [math.nan]
            with self.assertRaisesRegex(ValueError, "thresholds"):
                validate_candidate(candidate, base)

    def test_export_cannot_reuse_passing_report_with_substituted_policy(self):
        with tempfile.TemporaryDirectory() as workspace:
            path = Path(workspace) / "policy.json"
            policy = {"policyId": "gate-v1", "minPrecision": .9, "minRecall": .8,
                      "maxFalsePositiveRate": .05, "maxMedianInferenceMs": 200}
            path.write_text(json.dumps(policy), encoding="utf-8")
            report = {"policyId": "gate-v1", "evaluationPolicySha256": sha256(path),
                      "metrics": {"precision": .95, "recall": .9, "falsePositiveRate": .01}, "medianInferenceMs": 100}
            validate_report_policy(report, policy, path)
            report["metrics"]["recall"] = .1
            with self.assertRaisesRegex(RefusalError, "metrics"):
                validate_report_policy(report, policy, path)
            report["evaluationPolicySha256"] = "0" * 64
            with self.assertRaisesRegex(RefusalError, "policy differs"):
                validate_report_policy(report, policy, path)

    def test_benchmark_rejects_unreviewed_category_mapping(self):
        with tempfile.TemporaryDirectory() as workspace:
            base = Path(workspace)
            Image.new("RGB", (4, 4), "red").save(base / "a.png")
            Image.new("RGB", (4, 4), "blue").save(base / "b.png")
            manifest = {"approvedForEvaluation": True, "adultOnlyAssurance": True, "rightsVerified": True,
                        "independentOfTraining": True, "sourceUrl": "https://example.org/synthetic", "license": "test-only",
                        "datasetId": "test-only", "split": "held-out-test", "images": [
                            {"path": "a.png", "explicit": True, "category": "safe", "strata": ["other"]},
                            {"path": "b.png", "explicit": False, "category": "safe", "strata": ["other"]}]}
            path = base / "manifest.json"
            path.write_text(json.dumps(manifest), encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "labels disagree"):
                load_samples(path)


if __name__ == "__main__":
    unittest.main()
