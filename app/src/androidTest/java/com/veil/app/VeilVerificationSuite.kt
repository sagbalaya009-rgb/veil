package com.veil.app

import com.veil.app.services.BundledModelInferenceTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/** Fresh-install onboarding runs before tests that deliberately mutate local app state. */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    VeilJourneyTest::class,
    LocalPersistenceTest::class,
    CommitmentStoreTest::class,
    BundledModelInferenceTest::class,
    RecoveryStorePersistenceTest::class,
    RecoveryJourneyTest::class,
    FeedbackStoreTest::class,
    AccessibilityInterruptTest::class,
    CaptureJourneyTest::class,
)
class VeilVerificationSuite
