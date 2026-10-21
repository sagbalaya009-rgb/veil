package com.veil.app.services

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Real bundled inference on synthetic pixels. This establishes no content accuracy claim. */
@RunWith(AndroidJUnit4::class)
class BundledModelInferenceTest {
    @Test
    fun reviewedAssetLoadsAndRunsRepeatedlyWithoutRecyclingSource() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val initialized = TensorFlowLiteInitializer().initialize(context)
        assertTrue("Actual bundled model must initialize: $initialized", initialized is ClassifierInitialization.Ready)
        val classifier = (initialized as ClassifierInitialization.Ready).classifier
        // Exact input dimensions exercise the createScaledBitmap(source)==source case.
        val bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
        try {
            for (color in listOf(Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE)) {
                bitmap.eraseColor(color)
                repeat(3) {
                    val result = classifier.classify(bitmap)
                    assertTrue(result.explicitConfidence.isFinite())
                    assertTrue(result.explicitConfidence in 0f..1f)
                    assertTrue(result.suggestiveConfidence in 0f..1f)
                    assertEquals(VeilModelSpec.version, result.modelVersion)
                    assertFalse("Classifier must not recycle the caller bitmap", bitmap.isRecycled)
                }
            }
        } finally {
            classifier.close()
            classifier.close() // Shutdown must be idempotent.
            bitmap.recycle()
        }
    }
}
