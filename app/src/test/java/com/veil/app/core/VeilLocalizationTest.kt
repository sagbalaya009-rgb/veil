package com.veil.app.core

import org.junit.Assert.assertFalse
import org.junit.Test

class VeilLocalizationTest {
    @Test
    fun everySupportedLanguageHasEssentialOfflineCopy() {
        UiLanguage.entries.forEach { language ->
            val strings = VeilLocalization.stringsFor(language)
            assertFalse("${language.name} is missing Home copy", strings.home.isBlank())
            assertFalse("${language.name} is missing privacy disclosure", strings.home_intro.isBlank())
            assertFalse("${language.name} is missing cooldown copy", strings.test_cooldown.isBlank())
            assertFalse("${language.name} is missing notification copy", strings.quiet_notification.isBlank())
            assertFalse("${language.name} is missing Qur’an translation label", strings.translation.isBlank())
        }
    }
}
