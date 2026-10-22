package com.veil.app

import android.content.Context
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.veil.app.services.CommitmentStore
import com.veil.app.services.MonitoringRuntime
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
        store.startFocus(setOf("com.android.chrome"), 1)
        assertNotNull(store.blockingReason("com.android.chrome"))
        store.startFocus(setOf("org.example.distraction"), 1)
        assertTrue(store.state.value.focusRemaining(SystemClock.elapsedRealtime(), store.bootCount()) > 0)
        val persisted = context.getSharedPreferences("veil_commitments", Context.MODE_PRIVATE).getString("state", "")!!
        assertTrue(persisted.contains("org.example.distraction"))
        assertFalse(persisted.contains("confidence"))
        store.endFocus()
        assertNull(store.blockingReason("org.example.distraction"))
        store.clearHistory()
        assertTrue(store.state.value.events.isEmpty())
        store.endFocus(); store.dismissCooldown()
        assertTrue("Stopping inactive protection must not invent history", store.state.value.events.isEmpty())
    }
    @Test fun expiredHistoryIsRemovedFromPrivateDiskWhenStoreReopens() {
        val store = CommitmentStore.get(context)
        store.clearAll()
        val prefs = context.getSharedPreferences("veil_commitments", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val expiredAt = now - 31L * 24 * 60 * 60 * 1000
        val fixture = org.json.JSONObject().put("onboarded", true).put("events", org.json.JSONArray()
            .put(org.json.JSONObject().put("kind", "EXPIRED_RETENTION_TEST").put("at", expiredAt))
            .put(org.json.JSONObject().put("kind", "FRESH_RETENTION_TEST").put("at", now)))
        assertTrue(prefs.edit().putString("state", fixture.toString()).commit())
        try {
            // Exercise the actual cold-construction path without killing instrumentation.
            // No production test hook or public multi-writer constructor is introduced.
            val constructor = CommitmentStore::class.java.getDeclaredConstructor(Context::class.java)
                .apply { isAccessible = true }
            val reopened = constructor.newInstance(context)
            assertEquals(listOf("FRESH_RETENTION_TEST"), reopened.state.value.events.map { it.kind })
            val file = java.io.File(context.dataDir, "shared_prefs/veil_commitments.xml")
            val backup = java.io.File(context.dataDir, "shared_prefs/veil_commitments.xml.bak")
            val deadline = SystemClock.elapsedRealtime() + 5_000
            var prunedOnDisk = false
            while (SystemClock.elapsedRealtime() < deadline) {
                val bytes = runCatching { file.readText() }.getOrDefault("")
                val backupBytes = runCatching { backup.readText() }.getOrDefault("")
                if ("FRESH_RETENTION_TEST" in bytes && "EXPIRED_RETENTION_TEST" !in bytes &&
                    "EXPIRED_RETENTION_TEST" !in backupBytes) {
                    prunedOnDisk = true
                    break
                }
                SystemClock.sleep(20)
            }
            assertTrue("Reopening must remove expired metadata from the private file and backup", prunedOnDisk)
        } finally {
            store.clearAll()
        }
    }

    @Test fun interventionRequiresConnectionAndAlwaysHasExit() {
        val store = CommitmentStore.get(context)
        store.clearAll(); store.connection(false)
        store.intervene(.99f, "org.example.content")
        assertEquals("", store.state.value.cooldownPackage)
        store.connection(true)
        MonitoringRuntime.updateRunning(true) // Isolated policy-boundary test; capture is tested separately.
        store.intervene(.99f, "org.example.content")
        assertNotNull(store.blockingReason("org.example.content"))
        store.dismissCooldown(true)
        assertNull(store.blockingReason("org.example.content"))
        assertEquals("FALSE_POSITIVE", store.state.value.events.last().kind)
        MonitoringRuntime.updateRunning(false)
        store.connection(false); store.clearAll()
    }
}
