package com.veil.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WorldviewSupportTest {
    @Test
    fun returnsNoMessageUntilTheUserSelectsAWorldview() {
        assertNull(supportiveMessageFor(null))
    }

    @Test
    fun returnsTheSelectedLocalReminder() {
        val islam = supportiveMessageFor(WorldviewPreference.ISLAM)
        val christianity = supportiveMessageFor(WorldviewPreference.CHRISTIANITY)
        val atheist = supportiveMessageFor(WorldviewPreference.ATHEIST)

        assertEquals("Qur’an 39:53 · The Clear Quran", islam?.reference)
        assertEquals("1 John 1:9 · King James Version", christianity?.reference)
        assertEquals("A secular recovery reminder", atheist?.reference)
        assertNotNull(islam?.sourceUrl)
        assertNotNull(christianity?.sourceUrl)
    }
}
