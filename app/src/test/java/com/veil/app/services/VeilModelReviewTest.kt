package com.veil.app.services

import org.junit.Assert.assertTrue
import org.junit.Test

class VeilModelReviewTest {
    @Test
    fun validReviewedModelManifestPassesTheLocalAcceptanceGate() {
        val manifest = VeilModelReview(
            modelId = VeilModelSpec.version,
            sha256 = "a".repeat(64),
            sourceUrl = "https://example.org/model",
            license = "Apache-2.0",
            datasetProvenanceUrl = "https://example.org/dataset",
            evaluationReportUrl = "https://example.org/evaluation",
            rollbackModelId = "veil-explicit-v0",
            approvedBy = "Veil privacy review",
            approvedAt = "2026-08-27",
            inputWidth = 224,
            inputHeight = 224,
            inputChannels = 3,
            outputScores = VeilModelSpec.outputScores,
            normalization = "rgb_float_0_to_1",
        )
        assertTrue(manifest.acceptanceErrors().isEmpty())
    }

    @Test
    fun incompleteOrMismatchedManifestCannotPassTheAcceptanceGate() {
        val manifest = VeilModelReview(
            modelId = "unknown",
            sha256 = "unverified",
            sourceUrl = "http://example.org/model",
            license = "",
            datasetProvenanceUrl = "",
            evaluationReportUrl = "",
            rollbackModelId = "",
            approvedBy = "",
            approvedAt = "",
            inputWidth = 299,
            inputHeight = 299,
            inputChannels = 3,
            outputScores = 1,
            normalization = "unknown",
        )
        assertTrue(manifest.acceptanceErrors().isNotEmpty())
    }
}
