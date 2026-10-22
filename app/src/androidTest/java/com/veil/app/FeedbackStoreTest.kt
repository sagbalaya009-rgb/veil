package com.veil.app

import androidx.test.platform.app.InstrumentationRegistry
import com.veil.app.core.Correction
import com.veil.app.services.FeedbackStore
import com.veil.app.services.VeilModelSpec
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class FeedbackStoreTest {
    @Test fun feedbackReopensAndUndoResetNeverAlterPolicy() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "isolated_feedback_${UUID.randomUUID()}"
        try {
            val store = FeedbackStore(context, name)
            store.report(Correction.NOT_EXPLICIT)
            assertEquals(Correction.NOT_EXPLICIT, FeedbackStore(context, name).state.value.single().correction)
            assertTrue(store.undo())
            assertTrue(store.state.value.isEmpty())
            assertFalse(store.undo())
            store.report(Correction.MISSED)
            store.clear()
            assertTrue(FeedbackStore(context, name).state.value.isEmpty())
            assertEquals(.90f, VeilModelSpec.threshold, 0f)
        } finally { context.deleteSharedPreferences(name) }
    }
}
