package com.veil.app

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.veil.app.core.recovery.*
import com.veil.app.services.RecoveryStore
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

/** Uses a unique test directory; never reads, clears, or overwrites the owner's Recovery file. */
class RecoveryStorePersistenceTest {
    private fun withIsolatedStore(test: (Context, File) -> Unit) {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(target.cacheDir, "recovery-test-${UUID.randomUUID()}").apply { mkdirs() }
        val isolated = object : ContextWrapper(target) {
            override fun getNoBackupFilesDir(): File = directory
        }
        try { test(isolated, directory) } finally {
            directory.listFiles()?.forEach { it.delete() }
            directory.delete()
        }
    }

    @Test fun encryptedColdReadAndScopedDeletionPreserveOtherChoices() = withIsolatedStore { context, directory ->
        val first = RecoveryStore(context)
        assertTrue(first.configure(setOf(RecoveryGoal.REDUCE_UNWANTED_USE), EncouragementMode.FAITH, "private-faith-reminder-unique"))
        assertTrue(first.savePlan("private-trigger-unique", "private-action-unique"))
        assertNotNull(first.logCheckIn(intensity = 6, note = "private-note-unique"))
        assertNotNull(first.completeActivity(RecoveryActivity.GROUNDING, helpful = true))
        assertTrue(first.reportSetback())
        val disk = File(directory, "veil_recovery_v1.enc").readBytes().toString(Charsets.ISO_8859_1)
        assertFalse(disk.contains("private-note-unique"))
        assertFalse(disk.contains("private-faith-reminder-unique"))
        val reopened = RecoveryStore(context)
        assertEquals(3, reopened.state.value.records.size)
        assertEquals(1, reopened.state.value.completedActivities)
        assertEquals(6, reopened.state.value.checkIns.single().intensity)
        assertTrue(reopened.deletePreferences())
        assertTrue(reopened.state.value.goals.isEmpty())
        assertEquals(3, reopened.state.value.records.size)
        assertTrue(reopened.configure(setOf(RecoveryGoal.IMPROVE_FOCUS), EncouragementMode.NONE))
        assertTrue(reopened.deleteRecords())
        assertEquals(setOf(RecoveryGoal.IMPROVE_FOCUS), reopened.state.value.goals)
        assertTrue(reopened.state.value.records.isEmpty())
        assertTrue(reopened.deleteAll())
        assertFalse(File(directory, "veil_recovery_v1.enc").exists())
    }

    @Test fun authenticatedCorruptionNeverOverwritesExistingBytes() = withIsolatedStore { context, directory ->
        val first = RecoveryStore(context)
        assertTrue(first.configure(setOf(RecoveryGoal.IMPROVE_FOCUS), EncouragementMode.PRACTICAL))
        val file = File(directory, "veil_recovery_v1.enc")
        val corrupt = file.readBytes().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        file.writeBytes(corrupt)
        val reopened = RecoveryStore(context)
        assertFalse(reopened.state.value.storageAvailable)
        assertFalse(reopened.savePlan("test", "test"))
        assertArrayEquals(corrupt, file.readBytes())
        assertTrue(reopened.deleteAll())
        assertTrue(reopened.state.value.storageAvailable)
    }
}
