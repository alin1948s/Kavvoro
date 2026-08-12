package com.moonsolstudios.kavvoro.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class KavvoroI18nTest {
    @Test
    fun startLevelIsTranslatedForEverySupportedLanguage() {
        assertEquals(
            "START LEVEL",
            KavvoroI18n.t(KavvoroLanguage.EN, "START LEVEL")
        )

        KavvoroLanguage.entries
            .filterNot {
                it == KavvoroLanguage.SYSTEM || it == KavvoroLanguage.EN
            }
            .forEach { language ->
                assertNotEquals(
                    language.code,
                    "START LEVEL",
                    KavvoroI18n.t(language, "START LEVEL")
                )
            }
    }
}
