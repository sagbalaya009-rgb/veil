package com.veil.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.veil.app.services.CommitmentStore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Fresh emulator user journey: actual activity/Hilt/Room, no permission fabrication. */
@RunWith(AndroidJUnit4::class)
class VeilJourneyTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun onboardingDashboardAndFocusDisclosure() {
        val store = CommitmentStore.get(compose.activity)
        if (!store.state.value.onboarded) {
            compose.onNodeWithText("Continue").performClick()
            compose.onNodeWithText("Your screen stays yours.").assertIsDisplayed()
            compose.onNodeWithText("Continue").performClick()
            compose.onNodeWithText("Open Veil").performClick()
        }
        compose.onNodeWithText("Protection status").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Shield").performClick()
        compose.onNodeWithText("Set up foreground awareness").performClick()
        compose.onNodeWithText("Foreground awareness").assertIsDisplayed()
        compose.onNodeWithText("Not now").performClick()
        compose.onNodeWithText("Protect your attention.").assertIsDisplayed()
    }

    @Test fun scheduledFocusSeparatesStartDelayFromDuration() {
        val store = CommitmentStore.get(compose.activity)
        store.onboard()
        try {
            store.startFocus(setOf("org.example.distraction"), 1, 10)
            compose.onNodeWithText("Shield").performClick()
            compose.onNodeWithText("Scheduled · 1 apps").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("duration 01:00", substring = true).assertIsDisplayed()
        } finally {
            store.endFocus()
        }
    }
}
