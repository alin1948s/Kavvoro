package com.moonsolstudios.kavvoro.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KavvoroI18nTest {
    @Test
    fun everyTutorialKeyHasAnExplicitTranslationInEveryLanguage() {
        KavvoroLanguage.entries
            .filterNot { it == KavvoroLanguage.SYSTEM }
            .forEach { language ->
                TutorialCopy.requiredKeys.forEach { key ->
                    assertTrue(
                        "${language.code}: $key",
                        TutorialCopy.hasExplicitTranslation(language, key)
                    )
                    assertTrue(
                        "${language.code}: $key is blank",
                        TutorialCopy.translation(language, key).orEmpty().isNotBlank()
                    )
                }
            }
    }

    @Test
    fun tutorialLookupUsesTheStrictCatalogBeforeLegacyOverrides() {
        assertEquals(
            "ابدأ المستوى",
            KavvoroI18n.t(KavvoroLanguage.AR, "START LEVEL")
        )
        assertNotEquals(
            "Pink crash nodes end the run.",
            KavvoroI18n.t(
                KavvoroLanguage.JA,
                "Pink crash nodes end the run."
            )
        )
    }
}
