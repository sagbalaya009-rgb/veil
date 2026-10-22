# V2 evaluation system and evidence

There is no approved independent semantic dataset in this checkout. Precision, recall, false-positive/negative rates, three-category accuracy, real-video recall, demographic robustness and calibrated operating thresholds for Veil remain **unmeasured**. A passing unit test or generated color fixture does not fill this gap. The existing pretrained model remains unchanged.

## Implemented offline comparison

Run from repository root in a controlled Python environment with `ml/inference-requirements.txt`:

```text
python ml/evaluation/benchmark.py --manifest /controlled/evaluation/manifest.json --candidates /controlled/evaluation/candidates.json --output /controlled/reports/comparison-v1.json
```

Use `--view full-plus-center` for the explicitly experimental second center crop. Run it on the same approved images as `--view full`; compare both false alarms and CPU invoke time. Do not silently deploy the maximum-of-crops policy or assume it improves quality.

The manifest extends the existing reviewed manifest in `evaluate_bundled_model.py`: `approvedForEvaluation`, `adultOnlyAssurance`, `rightsVerified`, `independentOfTraining` must be actual JSON true, with HTTPS `sourceUrl`, named `license`, both boolean `explicit` labels, paths confined to the manifest folder, and no duplicate bytes. V2 additionally requires an opaque `datasetId`, `split` of `validation` or `held-out-test`, and for each image a reviewed `category` (`safe`, `suggestive`, `explicit`) and nonempty `strata`. The boolean label must agree with `category == explicit`. Supported strata are defined in the script and cover ordinary screens, feeds, thumbnails, small regions, videos, display modes, text overlays and hard negatives. Source assertions require external review; code cannot prove rights, adult assurance, or ground truth.

Optional sequence records carry an opaque `sequence`, strictly increasing `timestampMs` after ordering, and consistent annotated `explicitOnsetMs`. The tool replays the two-of-three score rule with a 2-second gap reset and reports aggregate misses, pre-onset triggers and median sampled trigger delay. It cannot reconstruct unseen frames, Android capture scheduling or end-to-end intervention latency. Frames from the same sequence are correlated, not independent accuracy observations.

Candidate configuration:

```json
{
  "models": [{
    "modelId": "reviewed-baseline-v1",
    "path": "models/baseline.tflite",
    "sha256": "replace-with-reviewed-model-sha256",
    "contract": "gantman-five-rgb01",
    "thresholds": [0.90]
  }]
}
```

The other implemented contract is `binary-explicit-rgb01`, for FLOAT32 `[1,224,224,3]` input and `[1,1]` output. Unsupported dtypes/shape/contracts fail. A model with different labels or normalization requires an explicitly tested adapter. Thresholds must be fixed before running the held-out final set; select them on a separate validation set and freeze the selection before final evaluation. The tool reports, not automatically selects or promotes, models.

Reports bind dataset manifest and candidate configuration hashes, model hash/size, runtime/host/thread count, confusion matrix, precision, recall, F1, false-positive and false-negative rates, Brier score, expected calibration error with reliability bins, fixed-stratum summaries and annotated-category intervention rates. Missing denominators are null, not invented zero performance. Category intervention rates do not claim that a binary model has three class probabilities. CPU invoke median/p95 exclude capture, image I/O/resizing, overlay and device battery. Full-plus-center reports summed invoke cost. No images, image paths, per-image scores, sequence IDs, private app names or journal entries are exported.

## Leakage and labels

Training validation now decodes files, rejects duplicate bytes/decoded content (including re-encoded copies), contradictory duplicate labels, and cross-split near-duplicate dHash candidates. The near-duplicate screen uses 64-bit horizontal differences and Hamming distance <=3 for textured images; flat images are excluded from that heuristic because trivial collisions are common. It is conservative and incomplete: crops, transformations and sequence/source correlations still need an independent split and provenance review. A refusal is a review request, never an automatic relabel or proof that two images are semantically equivalent. The current pairwise near-duplicate check is intended for reviewed experiment-sized sets; partition large collections with a controlled index before using it at scale.

## Release gates still open

An independent qualified reviewer/data provider must establish lawful source rights, adult/consent assurance where applicable, stable class definitions and reliable labels without requiring the owner to browse explicit galleries. Include ordinary mobile UI and hard negatives, separate source/group/sequence collections across training, validation and final test, and freeze acceptance limits before final evaluation. No user capture is used as a dataset. Crops/video/policy changes need representative sequence benchmarks. CPU/GPU utilization, energy, thermal, end-to-end latency and OEM stability require a separate fixed-duration phone protocol. Federated learning and model training have not occurred as part of this release.

See [MODEL_EVALUATION.md](MODEL_EVALUATION.md) for earlier actual desktop/emulator inference evidence and the physical test record in the delivered outputs for owner-device checks. Preserve each evidence class separately. `releaseQualified` remains false on comparison reports and converted model manifests.
