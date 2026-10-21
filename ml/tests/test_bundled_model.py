import json
import tempfile
import unittest
from pathlib import Path
from PIL import Image
from evaluate_bundled_model import read_evaluation_manifest


class BundledEvaluationManifestTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.base = Path(self.temp.name) / "approved"
        self.base.mkdir()
        Image.new("RGB", (2, 2), "red").save(self.base / "positive.png")
        Image.new("RGB", (2, 2), "blue").save(self.base / "negative.png")
        self.manifest = {
            "approvedForEvaluation": True, "adultOnlyAssurance": True,
            "rightsVerified": True, "independentOfTraining": True,
            "sourceUrl": "https://example.org/independent-review", "license": "reviewed test fixtures",
            "images": [{"path": "positive.png", "explicit": True}, {"path": "negative.png", "explicit": False}],
        }

    def read(self):
        target = self.base / "manifest.json"
        target.write_text(json.dumps(self.manifest), encoding="utf-8")
        return read_evaluation_manifest(target)

    def test_valid_local_two_class_manifest_is_accepted(self):
        _, records = self.read()
        self.assertEqual([label for _, label in records], [True, False])

    def test_parent_traversal_is_refused_even_if_file_exists(self):
        Image.new("RGB", (2, 2), "green").save(self.base.parent / "outside.png")
        self.manifest["images"][0]["path"] = "../outside.png"
        with self.assertRaisesRegex(ValueError, "escapes"):
            self.read()

    def test_absolute_path_is_refused(self):
        self.manifest["images"][0]["path"] = str((self.base / "positive.png").resolve())
        with self.assertRaisesRegex(ValueError, "relative"):
            self.read()

    def test_duplicate_bytes_under_different_names_are_refused(self):
        (self.base / "negative.png").write_bytes((self.base / "positive.png").read_bytes())
        with self.assertRaisesRegex(ValueError, "Duplicate image bytes"):
            self.read()

    def test_truthy_strings_do_not_replace_review_assertions(self):
        self.manifest["rightsVerified"] = "true"
        with self.assertRaisesRegex(ValueError, "reviewed"):
            self.read()

    def test_non_boolean_label_is_refused(self):
        self.manifest["images"][0]["explicit"] = 1
        with self.assertRaisesRegex(ValueError, "boolean"):
            self.read()
