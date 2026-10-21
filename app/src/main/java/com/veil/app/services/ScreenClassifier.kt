package com.veil.app.services

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.tensorflow.lite.Interpreter

/** The narrow contract between the screen pipeline and the protection engine. */
interface ScreenClassifier : AutoCloseable {
    fun classify(frame: Bitmap): ClassificationResult
}

data class ClassificationResult(
    val explicitConfidence: Float,
    val modelVersion: String,
    val suggestiveConfidence: Float = 0f,
)

/** Offline GantMan MobileNetV2 adapter. Never retains or writes the source bitmap. */
class TensorFlowLiteScreenClassifier(
    modelBuffer: ByteBuffer,
    private val inputSize: Int = VeilModelSpec.inputSize,
    private val modelVersion: String = VeilModelSpec.version,
) : ScreenClassifier {
    private val interpreter = Interpreter(modelBuffer, Interpreter.Options().setNumThreads(2))
    private val input = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4)
        .order(ByteOrder.nativeOrder())
    private val pixels = IntArray(inputSize * inputSize)
    private val output = Array(1) { FloatArray(VeilModelSpec.outputScores) }
    private var closed = false

    @Synchronized
    override fun classify(frame: Bitmap): ClassificationResult {
        check(!closed) { "Classifier is closed." }
        // Upstream predict.py resizes to 224x224, converts to RGB, and divides by 255.
        // Nearest-neighbor matches Keras load_img's default interpolation.
        val scaled = Bitmap.createScaledBitmap(frame, inputSize, inputSize, false)
        try {
            scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
            input.clear()
            pixels.forEach { pixel ->
                input.putFloat(Color.red(pixel) / 255f)
                input.putFloat(Color.green(pixel) / 255f)
                input.putFloat(Color.blue(pixel) / 255f)
            }
            input.rewind()
            interpreter.run(input, output)
            val scores = output[0]
            require(scores.all { it.isFinite() && it in 0f..1f }) { "Invalid classifier scores." }
            require(kotlin.math.abs(scores.sum() - 1f) < 0.02f) { "Invalid softmax output." }
            // This is a product policy grouping of genuine upstream classes, not calibration.
            return ClassificationResult(
                explicitConfidence = (scores[1] + scores[3]).coerceIn(0f, 1f),
                suggestiveConfidence = scores[4],
                modelVersion = modelVersion,
            )
        } finally {
            // createScaledBitmap may return its source when already 224x224.
            if (scaled !== frame) scaled.recycle()
            pixels.fill(0)
            input.clear()
            while (input.hasRemaining()) input.put(0.toByte())
            input.rewind()
            output[0].fill(0f)
        }
    }

    @Synchronized
    override fun close() {
        if (!closed) {
            closed = true
            interpreter.close()
            pixels.fill(0)
            input.clear()
            while (input.hasRemaining()) input.put(0.toByte())
            output[0].fill(0f)
        }
    }
}
