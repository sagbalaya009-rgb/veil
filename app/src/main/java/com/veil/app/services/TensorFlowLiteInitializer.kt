/**
 * Veil TensorFlow Lite initialization boundary.
 *
 * This loader is intentionally local-only: it reads a bundled, reviewed model asset, initializes
 * the existing in-memory classifier adapter, and never sends screen content or model inputs away
 * from the device. The actual .tflite artifact is not bundled until it completes the documented
 * evaluation, privacy review, and rollback requirements.
 */
package com.veil.app.services

import android.content.Context
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

object VeilModelSpec {
    const val assetPath = "models/veil_explicit_v1.tflite"
    const val inputSize = 224
    const val version = "veil-explicit-v1"
    const val threshold = 0.90f
}

sealed interface ClassifierInitialization {
    data class Ready(val classifier: ScreenClassifier) : ClassifierInitialization
    data class MissingModel(val assetPath: String) : ClassifierInitialization
    data class Failed(val reason: String) : ClassifierInitialization
}

class TensorFlowLiteInitializer {
    fun initialize(context: Context): ClassifierInitialization = try {
        val modelBuffer = loadAssetBuffer(context, VeilModelSpec.assetPath)
        ClassifierInitialization.Ready(
            TensorFlowLiteScreenClassifier(
                modelBuffer = modelBuffer,
                inputSize = VeilModelSpec.inputSize,
                modelVersion = VeilModelSpec.version,
            ),
        )
    } catch (_: FileNotFoundException) {
        ClassifierInitialization.MissingModel(VeilModelSpec.assetPath)
    } catch (exception: Exception) {
        ClassifierInitialization.Failed(
            exception.message ?: "Unable to initialize the local TensorFlow Lite classifier.",
        )
    }

    private fun loadAssetBuffer(context: Context, assetPath: String): ByteBuffer {
        val bytes = context.assets.open(assetPath).use { input -> input.readBytes() }
        return ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .put(bytes)
            .also { it.rewind() }
    }
}
