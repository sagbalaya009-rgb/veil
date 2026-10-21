package com.veil.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.veil.app.core.ProtectionSettings
import com.veil.app.data.VeilDatabase
import com.veil.app.data.VeilRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LocalPersistenceTest {
    @Test fun roomSettingsAndNotesSurviveDatabaseReopen() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "veil-persistence-test-${UUID.randomUUID()}.db"
        fun open() = Room.databaseBuilder(context, VeilDatabase::class.java, name).build()
        try {
            val first = open()
            try {
                val repository = VeilRepository(first.settingsDao(), first.eventDao())
                repository.saveSettings(ProtectionSettings(recoveryNote = "Return to studying"), true)
            } finally { first.close() }
            val second = open()
            try {
                val repository = VeilRepository(second.settingsDao(), second.eventDao())
                val restored = repository.loadSettings()
                assertTrue(restored.onboardingComplete)
                assertEquals("Return to studying", repository.settingsFrom(restored).recoveryNote)
            } finally { second.close() }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
