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
)

/**
 * TensorFlow Lite implementation for the Veil MVP.
 *
 * The model is supplied by the app module and is executed entirely on-device. The Bitmap is
 * converted to a bounded in-memory tensor and is not retained after classify returns.
 */
class TensorFlowLiteScreenClassifier(
    modelBuffer: ByteBuffer,
    private val inputSize: Int = 224,
    private val modelVersion: String = "unbundled-mvp",
) : ScreenClassifier {
    private val interpreter = Interpreter(
        modelBuffer,
        Interpreter.Options().setNumThreads(2),
    )

    override fun classify(frame: Bitmap): ClassificationResult {
        val input = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * FLOAT_BYTES)
            .order(ByteOrder.nativeOrder())
        val scaled = Bitmap.createScaledBitmap(frame, inputSize, inputSize, true)
        val pixels = IntArray(inputSize * inputSize)
        scaled.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        pixels.forEach { pixel ->
            input.putFloat(Color.red(pixel) / 255f)
            input.putFloat(Color.green(pixel) / 255f)
            input.putFloat(Color.blue(pixel) / 255f)
        }
        input.rewind()

        val output = Array(1) { FloatArray(1) }
        interpreter.run(input, output)
        scaled.recycle()

        return ClassificationResult(
            explicitConfidence = output[0][0].coerceIn(0f, 1f),
            modelVersion = modelVersion,
        )
    }

    override fun close() {
        interpreter.close()
    }

    companion object {
        private const val FLOAT_BYTES = 4
    }
}
