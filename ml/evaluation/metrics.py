"""Aggregate binary screening metrics; missing denominators remain unavailable."""
from __future__ import annotations

import math


def summarize(labels, scores, threshold=0.9, bins=10):
    pairs = list(zip(labels, scores, strict=True))
    if not pairs or not 0 < threshold < 1 or type(bins) is not int or bins < 1:
        raise ValueError("Nonempty samples, an interior threshold, and positive bin count required.")
    if any(type(y) not in (bool, int) or y not in (0, 1) or
           type(p) not in (int, float) or not math.isfinite(p) or not 0 <= p <= 1 for y, p in pairs):
        raise ValueError("Binary labels and finite scores in [0,1] required.")
    tp = sum(y == 1 and p >= threshold for y, p in pairs)
    fp = sum(y == 0 and p >= threshold for y, p in pairs)
    tn = sum(y == 0 and p < threshold for y, p in pairs)
    fn = sum(y == 1 and p < threshold for y, p in pairs)
    def ratio(n, d):
        return n / d if d else None
    reliability = []
    for index in range(bins):
        bucket = [(y, p) for y, p in pairs if min(int(p * bins), bins - 1) == index]
        reliability.append({"lower": index / bins, "upper": (index + 1) / bins,
                            "count": len(bucket), "meanScore": ratio(sum(p for _, p in bucket), len(bucket)),
                            "positiveFraction": ratio(sum(y for y, _ in bucket), len(bucket))})
    ece = sum(b["count"] * abs(b["meanScore"] - b["positiveFraction"]) for b in reliability if b["count"]) / len(pairs)
    return {"count": len(pairs), "threshold": threshold, "confusionMatrix": {"tp": tp, "fp": fp, "tn": tn, "fn": fn},
            "precision": ratio(tp, tp + fp), "recall": ratio(tp, tp + fn),
            "falsePositiveRate": ratio(fp, fp + tn), "falseNegativeRate": ratio(fn, fn + tp),
            "f1": ratio(2 * tp, 2 * tp + fp + fn), "brierScore": sum((p-y)**2 for y, p in pairs) / len(pairs),
            "expectedCalibrationError": ece, "reliabilityBins": reliability}


def temporal_first_trigger(timestamps_ms, scores, threshold=0.9, window=3, confirmations=2, max_gap_ms=2000):
    """Replay sampled scores, not unobserved video. Reset across stale-frame gaps."""
    points = list(zip(timestamps_ms, scores, strict=True))
    if not 0 < threshold < 1 or not 1 <= confirmations <= window or max_gap_ms <= 0:
        raise ValueError("Invalid temporal policy.")
    recent, previous = [], None
    for timestamp, score in points:
        if not math.isfinite(timestamp) or timestamp < 0 or not math.isfinite(score) or not 0 <= score <= 1:
            raise ValueError("Finite nonnegative timestamps and bounded scores required.")
        if previous is not None and timestamp <= previous:
            raise ValueError("Sequence timestamps must strictly increase.")
        if previous is not None and timestamp - previous > max_gap_ms:
            recent.clear()
        previous = timestamp
        recent.append(score >= threshold)
        recent = recent[-window:]
        if sum(recent) >= confirmations:
            return timestamp
    return None
