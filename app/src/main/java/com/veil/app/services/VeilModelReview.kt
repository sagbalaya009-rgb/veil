package com.veil.app.services

import org.json.JSONObject

/**
 * Required local evidence for a bundled explicit-content model. The file is reviewed with the
 * model asset during release; it never contains user data or enables remote model download.
 */
data class VeilModelReview(
    val modelId: String,
    val sha256: String,
    val sourceUrl: String,
    val license: String,
    val datasetProvenanceUrl: String,
    val evaluationReportUrl: String,
    val rollbackModelId: String,
    val approvedBy: String,
    val approvedAt: String,
    val inputWidth: Int,
    val inputHeight: Int,
    val inputChannels: Int,
    val outputScores: Int,
    val normalization: String,
) {
    fun acceptanceErrors(): List<String> = buildList {
        if (modelId != VeilModelSpec.version) add("Model review modelId must match ${VeilModelSpec.version}.")
        if (!sha256.matches(Regex("[a-fA-F0-9]{64}"))) add("Model review must include a SHA-256 checksum.")
        if (!sourceUrl.startsWith("https://")) add("Model review must include an HTTPS source URL.")
        if (license.isBlank()) add("Model review must name the model license.")
        if (!datasetProvenanceUrl.startsWith("https://")) add("Model review must include an HTTPS dataset provenance URL.")
        if (!evaluationReportUrl.startsWith("https://")) add("Model review must include an HTTPS evaluation report URL.")
        if (rollbackModelId.isBlank()) add("Model review must identify a rollback model.")
        if (approvedBy.isBlank() || approvedAt.isBlank()) add("Model review requires named approval and approval date.")
        if (inputWidth != VeilModelSpec.inputSize || inputHeight != VeilModelSpec.inputSize || inputChannels != VeilModelSpec.inputChannels) {
            add("Model review input must be ${VeilModelSpec.inputSize}×${VeilModelSpec.inputSize} RGB.")
        }
        if (outputScores != VeilModelSpec.outputScores) add("Model review must declare one explicit-content score.")
        if (normalization != VeilModelSpec.normalization) add("Model review must declare ${VeilModelSpec.normalization} normalization.")
    }

    companion object {
        fun fromJson(json: String): VeilModelReview {
            val value = JSONObject(json)
            return VeilModelReview(
                modelId = value.getString("modelId"),
                sha256 = value.getString("sha256"),
                sourceUrl = value.getString("sourceUrl"),
                license = value.getString("license"),
                datasetProvenanceUrl = value.getString("datasetProvenanceUrl"),
                evaluationReportUrl = value.getString("evaluationReportUrl"),
                rollbackModelId = value.getString("rollbackModelId"),
                approvedBy = value.getString("approvedBy"),
                approvedAt = value.getString("approvedAt"),
                inputWidth = value.getInt("inputWidth"),
                inputHeight = value.getInt("inputHeight"),
                inputChannels = value.getInt("inputChannels"),
                outputScores = value.getInt("outputScores"),
                normalization = value.getString("normalization"),
            )
        }
    }
}
