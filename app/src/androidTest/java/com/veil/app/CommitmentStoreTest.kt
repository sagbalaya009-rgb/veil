package com.veil.app

import android.content.Context
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.veil.app.services.CommitmentStore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommitmentStoreTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun selectedRulesPersistAndRestrictionsEndSafely() {
        val store = CommitmentStore.get(context)
        store.clearAll()
        store.startFocus(setOf("org.example.distraction"), 1)
        assertNotNull(store.blockingReason("org.example.distraction"))
        assertNull(store.blockingReason(context.packageName))
        assertNull(store.blockingReason("com.android.settings"))
        assertTrue(store.state.value.focusRemaining(SystemClock.elapsedRealtime(), store.bootCount()) > 0)
        val persisted = context.getSharedPreferences("veil_commitments", Context.MODE_PRIVATE).getString("state", "")!!
        assertTrue(persisted.contains("org.example.distraction"))
        assertFalse(persisted.contains("confidence"))
        store.endFocus()
        assertNull(store.blockingReason("org.example.distraction"))
        store.clearHistory()
        assertTrue(store.state.value.events.isEmpty())
    }
    @Test fun interventionRequiresConnectionAndAlwaysHasExit() {
        val store = CommitmentStore.get(context)
        store.clearAll(); store.connection(false)
        store.intervene(.99f, "org.example.content")
        assertEquals("", store.state.value.cooldownPackage)
        store.connection(true)
        store.intervene(.99f, "org.example.content")
        assertNotNull(store.blockingReason("org.example.content"))
        store.dismissCooldown(true)
        assertNull(store.blockingReason("org.example.content"))
        assertEquals("FALSE_POSITIVE", store.state.value.events.last().kind)
        store.connection(false); store.clearAll()
    }
}
