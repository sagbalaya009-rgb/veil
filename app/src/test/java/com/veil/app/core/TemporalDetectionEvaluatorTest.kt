package com.veil.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TemporalDetectionEvaluatorTest {
    @Test
    fun oneHighConfidenceFrameDoesNotTrigger() {
        val evaluator = TemporalDetectionEvaluator()

        assertFalse(evaluator.observe(0.95f))
    }

    @Test
    fun twoHighConfidenceFramesWithinWindowTrigger() {
        val evaluator = TemporalDetectionEvaluator()

        evaluator.observe(0.95f)
        assertTrue(evaluator.observe(0.93f))
    }

    @Test
    fun lowConfidenceFramesDoNotTrigger() {
        val evaluator = TemporalDetectionEvaluator()

        assertFalse(evaluator.observe(0.89f))
        assertFalse(evaluator.observe(0.88f))
        assertFalse(evaluator.observe(0.70f))
    }

    @Test
    fun resetClearsPriorConfirmation() {
        val evaluator = TemporalDetectionEvaluator()

        evaluator.observe(0.95f)
        evaluator.reset()

        assertFalse(evaluator.observe(0.95f))
    }
}
