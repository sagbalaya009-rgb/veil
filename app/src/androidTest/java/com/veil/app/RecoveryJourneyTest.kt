package com.veil.app

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import com.veil.app.core.recovery.*
import com.veil.app.services.RecoveryStore
import com.veil.app.ui.RecoveryExperience
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.UUID

/** Actual Compose interactions + encrypted storage, isolated from the owner's Recovery data. */
class RecoveryJourneyTest {
    @get:Rule val compose = createComposeRule()
    private var directory: File? = null

    private fun isolatedContext(): Context {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val isolated = File(target.cacheDir, "recovery-ui-${UUID.randomUUID()}").apply { mkdirs() }
        directory = isolated
        return object : ContextWrapper(target) {
            override fun getNoBackupFilesDir(): File = isolated
        }
    }

    private fun show(store: RecoveryStore) {
        compose.setContent {
            MaterialTheme {
                Surface {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { RecoveryExperience(store) }
                }
            }
        }
    }

    private fun click(text: String) { compose.onNodeWithText(text).performScrollTo().performClick() }
    private fun type(label: String, value: String) {
        compose.onNodeWithText(label).performScrollTo().performTextInput(value)
        closeSoftKeyboard()
    }

    @After fun cleanIsolatedFiles() {
        directory?.listFiles()?.forEach { it.delete() }
        directory?.delete()
    }

    @Test fun goalsOptionalFaithSkipCheckInAndDeletionJourney() {
        val context = isolatedContext()
        val store = RecoveryStore(context)
        show(store)
        click("Choose goals and encouragement")
        click("Skip for now")
        compose.runOnIdle { assertTrue(store.state.value.goals.isEmpty()); assertTrue(store.state.value.records.isEmpty()) }

        click("Choose goals and encouragement")
        compose.onNodeWithContentDescription("Improve digital focus").performScrollTo().performClick()
        click("Practical and secular")
        click("Save my preferences")
        compose.runOnIdle {
            assertEquals(setOf(RecoveryGoal.IMPROVE_FOCUS), store.state.value.goals)
            assertEquals(EncouragementMode.PRACTICAL, store.state.value.encouragement)
        }
        type("When I notice…", "I reach for my phone")
        type("I will try…", "Read one page")
        click("Save coping plan")
        compose.runOnIdle { assertEquals("Read one page", RecoveryStore(context).state.value.planAction) }

        // Changing encouragement is voluntary and does not change goals or the coping plan.
        click("Choose goals and encouragement")
        click("My own faith reminder")
        type("Your own faith reminder (optional)", "Remember my chosen values")
        click("Save my preferences")
        compose.onNodeWithText("Remember my chosen values").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertEquals(EncouragementMode.FAITH, RecoveryStore(context).state.value.encouragement) }

        type("Private reflection (optional)", "Temporary private reflection")
        click("Skip check-in")
        compose.runOnIdle { assertTrue(store.state.value.records.isEmpty()) }
        click("Save check-in")
        compose.runOnIdle { assertTrue(store.state.value.records.isEmpty()) }

        click("I tried this activity")
        click("Helpful")
        click("I want to note a setback")
        compose.runOnIdle {
            assertEquals(1, store.state.value.completedActivities)
            assertEquals(1, store.state.value.helpfulActivities)
            assertEquals(2, store.state.value.records.size)
        }
        click("Delete all recovery data")
        compose.onNodeWithText("Delete recovery data").performClick()
        compose.runOnIdle {
            assertEquals(RecoveryState(), store.state.value)
            assertEquals(RecoveryState(), RecoveryStore(context).state.value)
        }
        compose.onNodeWithText("Remember my chosen values").assertDoesNotExist()
        compose.onNodeWithText("Temporary private reflection").assertDoesNotExist()
    }

    @Test fun unavailableStorageCannotClaimThatPreferencesOrCheckInWereSaved() {
        val context = isolatedContext()
        val initial = RecoveryStore(context)
        assertTrue(initial.configure(setOf(RecoveryGoal.IMPROVE_FOCUS), EncouragementMode.NONE))
        val file = File(directory, "veil_recovery_v1.enc")
        val corrupted = file.readBytes().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        file.writeBytes(corrupted)
        val store = RecoveryStore(context)
        show(store)
        compose.onNodeWithText(store.state.value.storageMessage!!).assertIsDisplayed()
        click("Choose goals and encouragement")
        compose.onNodeWithText("Save my preferences").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Save check-in").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Check-in saved privately.").assertDoesNotExist()
        compose.onNodeWithText("Preferences saved on this device.").assertDoesNotExist()
        assertArrayEquals(corrupted, file.readBytes())
        click("Delete all recovery data")
        compose.onNodeWithText("Delete recovery data").performClick()
        compose.runOnIdle { assertTrue(store.state.value.storageAvailable) }
    }
}
