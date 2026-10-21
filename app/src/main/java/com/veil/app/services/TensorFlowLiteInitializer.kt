/**
 * Veil TensorFlow Lite initialization boundary.
 *
 * This loader is intentionally local-only: it reads a bundled, reviewed model asset, initializes
 * the existing in-memory classifier adapter, and never sends screen content or model inputs away
 * from the device. The bundled artifact is an experimental, attributed upstream classifier. Semantic
 * accuracy and target-device performance remain unqualified; see docs/ai/MODEL_EVALUATION.md.
 */
package com.veil.app.services

import android.content.Context
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType

object VeilModelSpec {
    const val assetPath = "models/veil_explicit_v1.tflite"
    const val reviewAssetPath = "models/veil_explicit_v1.review.json"
    const val inputSize = 224
    const val inputChannels = 3
    const val outputScores = 5
    const val normalization = "rgb_float_0_to_1"
    const val version = "gantman-mobilenet-v2-140-224-tflite-v1"
    val labels = listOf("drawings", "hentai", "neutral", "porn", "sexy")
    const val sha256 = "380f98f7685f9d8a386f8cc595b6dfcb972989aae3d1b8b270d3a4a5b96fab40"
    const val threshold = 0.90f
}

sealed interface ClassifierInitialization {
    data class Ready(val classifier: ScreenClassifier) : ClassifierInitialization
    data class MissingModel(val assetPath: String) : ClassifierInitialization
    data class Failed(val reason: String) : ClassifierInitialization
}

class TensorFlowLiteInitializer {
    fun initialize(context: Context): ClassifierInitialization {
        val modelBytes = try {
            loadAssetBytes(context, VeilModelSpec.assetPath)
        } catch (_: FileNotFoundException) {
            return ClassifierInitialization.MissingModel(VeilModelSpec.assetPath)
        } catch (exception: Exception) {
            return ClassifierInitialization.Failed(exception.message ?: "Unable to read the local TensorFlow Lite model.")
        }

        val review = try {
            VeilModelReview.fromJson(readAssetText(context, VeilModelSpec.reviewAssetPath))
        } catch (_: FileNotFoundException) {
            return ClassifierInitialization.Failed("The local TensorFlow Lite model is missing its required review manifest.")
        } catch (exception: Exception) {
            return ClassifierInitialization.Failed(exception.message ?: "The local TensorFlow Lite model review manifest is invalid.")
        }
        review.acceptanceErrors().firstOrNull()?.let { return ClassifierInitialization.Failed(it) }
        if (!review.sha256.equals(VeilModelSpec.sha256, ignoreCase = true) ||
            !sha256(modelBytes).equals(review.sha256, ignoreCase = true)) {
            return ClassifierInitialization.Failed("The local TensorFlow Lite model checksum does not match its review manifest.")
        }

        val modelBuffer = directBuffer(modelBytes)
        validateTensorContract(modelBuffer)?.let { return ClassifierInitialization.Failed(it) }
        modelBuffer.rewind()
        return try {
            ClassifierInitialization.Ready(
                TensorFlowLiteScreenClassifier(
                modelBuffer = modelBuffer,
                inputSize = VeilModelSpec.inputSize,
                modelVersion = VeilModelSpec.version,
            ),
            )
        } catch (exception: Exception) {
            ClassifierInitialization.Failed(
                exception.message ?: "Unable to initialize the local TensorFlow Lite classifier.",
            )
        }
    }

    private fun loadAssetBytes(context: Context, assetPath: String): ByteArray =
        context.assets.open(assetPath).use { input -> input.readBytes() }

    private fun readAssetText(context: Context, assetPath: String): String =
        context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { input -> input.readText() }

    private fun directBuffer(bytes: ByteArray): ByteBuffer = ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .put(bytes)
            .also { it.rewind() }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private fun validateTensorContract(modelBuffer: ByteBuffer): String? = try {
        Interpreter(modelBuffer).use { interpreter ->
            val inputShape = interpreter.getInputTensor(0).shape()
            val outputShape = interpreter.getOutputTensor(0).shape()
            val outputElements = interpreter.getOutputTensor(0).numElements()
            when {
                interpreter.inputTensorCount != 1 || interpreter.outputTensorCount != 1 ->
                    "Model must have exactly one input and one output."
                interpreter.getInputTensor(0).dataType() != DataType.FLOAT32 || interpreter.getOutputTensor(0).dataType() != DataType.FLOAT32 ->
                    "Model tensors must use FLOAT32."
                !inputShape.contentEquals(intArrayOf(1, VeilModelSpec.inputSize, VeilModelSpec.inputSize, VeilModelSpec.inputChannels)) ->
                    "Model input must be [1, ${VeilModelSpec.inputSize}, ${VeilModelSpec.inputSize}, ${VeilModelSpec.inputChannels}]."
                outputElements != VeilModelSpec.outputScores || !outputShape.contentEquals(intArrayOf(1, VeilModelSpec.outputScores)) ->
                    "Model output must be [1, 5]: drawings, hentai, neutral, porn, sexy."
                else -> null
            }
        }
    } catch (exception: Exception) {
        exception.message ?: "Unable to inspect the TensorFlow Lite model contract."
    }
}


