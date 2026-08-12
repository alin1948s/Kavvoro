package com.moonsolstudios.kavvoro.i18n

import com.moonsolstudios.kavvoro.engine.LevelDirector
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

    @Test
    fun strictCatalogExactlyCoversTheRenderedTutorialInventory() {
        assertEquals(
            TutorialCopy.renderedKeyInventory,
            TutorialCopy.requiredKeys
        )
    }

    @Test
    fun generatedTutorialTitlesAreExactlyTheStrictTitleInventory() {
        val generatedTitles = (1..10).flatMap { level ->
            listOf(
                LevelDirector.createClassic(level, 20f, 1234L).title.uppercase(),
                LevelDirector.createChaos(level, 20f, 1234L).title.uppercase()
            )
        }.toSet()

        assertEquals(TutorialCopy.levelTitleKeys, generatedTitles)
    }

    @Test
    fun tutorialSelectorsOnlyReturnCatalogKeys() {
        assertEquals(
            listOf(
                "Tap to fire a short Rift tether.",
                "The ball accelerates toward the tap point.",
                "Chain clean taps to steer without wasting energy."
            ),
            TutorialCopy.lessonKeys(1, hasPortals = false)
        )
        assertEquals(
            listOf(
                "Wind pushes the ball sideways.",
                "Overheat punishes tap spam.",
                "Use short bursts for the tiny gate."
            ),
            TutorialCopy.lessonKeys(10, hasPortals = false)
        )

        (1..10).forEach { level ->
            TutorialCopy.lessonKeys(level, hasPortals = false).forEach { key ->
                assertTrue(key in TutorialCopy.lessonKeys)
            }
        }
        TutorialCopy.lessonKeys(1, hasPortals = true).forEach { key ->
            assertTrue(key in TutorialCopy.portalLessonKeys)
        }

        val selectedKeys = setOf(
            TutorialCopy.actionLabelKey(false, false, false, false),
            TutorialCopy.actionLabelKey(true, false, false, false),
            TutorialCopy.actionLabelKey(false, true, false, false),
            TutorialCopy.actionLabelKey(false, false, true, false),
            TutorialCopy.actionLabelKey(false, false, false, true),
            TutorialCopy.obstacleKey(true, false, false, false, false),
            TutorialCopy.obstacleKey(false, true, false, false, false),
            TutorialCopy.obstacleKey(false, false, true, false, false),
            TutorialCopy.obstacleKey(false, false, false, true, false),
            TutorialCopy.obstacleKey(false, false, false, false, true),
            TutorialCopy.obstacleKey(false, false, false, false, false)
        )

        selectedKeys.forEach { key ->
            assertTrue(key in TutorialCopy.requiredKeys)
        }
    }
}
