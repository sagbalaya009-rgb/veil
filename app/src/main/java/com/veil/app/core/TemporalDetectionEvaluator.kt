package com.veil.app.core

import java.util.ArrayDeque

/**
 * Confirms a detection across a short rolling window before the protection engine acts.
 * This reduces one-frame false positives while preserving a bounded latency budget.
 */
class TemporalDetectionEvaluator(
    private val threshold: Float = ProtectionSettings.INITIAL_THRESHOLD,
    private val windowSize: Int = 3,
    private val confirmationsRequired: Int = 2,
) {
    private val scores = ArrayDeque<Float>()

    init {
        require(threshold.isFinite() && threshold in 0f..1f)
        require(windowSize > 0)
        require(confirmationsRequired in 1..windowSize)
    }

    fun observe(score: Float): Boolean {
        if (!score.isFinite() || score !in 0f..1f) { reset(); return false }
        scores.addLast(score)
        if (scores.size > windowSize) scores.removeFirst()
        return scores.count { it >= threshold } >= confirmationsRequired
    }

    fun reset() {
        scores.clear()
    }
}
