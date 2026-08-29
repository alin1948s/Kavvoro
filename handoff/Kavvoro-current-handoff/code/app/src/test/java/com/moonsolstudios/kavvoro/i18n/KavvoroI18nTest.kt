package com.moonsolstudios.kavvoro.i18n

import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.engine.CurseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KavvoroI18nTest {
    @Test
    fun testerReportedPolishVisibleKeysAreNotLeftInEnglish() {
        listOf(
            "AGE CHECK",
            "Enter your age in years.",
            "CONTINUE",
            "Only the age group is saved locally.",
            "BEST STREAK",
            "SELECTED BRAINBALL",
            "REWARD SIGNAL",
            "READY",
            "NEXT CLEAR",
            "RIFT ONLINE",
            "TRAINING",
            "Tap to fire a short Rift tether."
        ).forEach { key ->
            assertNotEquals("pl-PL still uses English for $key", key, KavvoroI18n.t(KavvoroLanguage.PL, key))
        }

        assertEquals("DARMOWE NAGRODY ODBLOKOWANE", KavvoroI18n.t(KavvoroLanguage.PL, "ALL FREE REWARDS UNLOCKED"))
        assertEquals("Język", KavvoroI18n.t(KavvoroLanguage.PL, "LANGUAGE"))
        assertEquals("Pierwsza gra", KavvoroI18n.t(KavvoroLanguage.PL, "FIRST RUN"))
        assertEquals("Zacznij poziom 01", KavvoroI18n.t(KavvoroLanguage.PL, "START LEVEL 01"))
        assertEquals("Gra przerwana", KavvoroI18n.t(KavvoroLanguage.PL, "RUN INTERRUPTED"))
        assertEquals("Rift zapadł się", KavvoroI18n.t(KavvoroLanguage.PL, "RIFT COLLAPSED"))
        assertEquals("Kontynuuj bezpłatnie", KavvoroI18n.t(KavvoroLanguage.PL, "CONTINUE FREE"))
        assertEquals("Energia Rift / restart poziomu", KavvoroI18n.t(KavvoroLanguage.PL, "RIFT ENERGY RESETS / LEVEL RESTARTS"))
        assertEquals("Darmowe wznowienie. Seria %s trwa.", KavvoroI18n.t(KavvoroLanguage.PL, "Free recovery available. Streak %s stays active."))
        assertEquals("Brainball resetuje się. Stukaj czyściej.", KavvoroI18n.t(KavvoroLanguage.PL, "Brainball rebooting. Try cleaner taps."))
    }

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
            "فهمت",
            KavvoroI18n.t(KavvoroLanguage.AR, "GOT IT")
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
    fun gameplayStatusLabelsArePartOfTheStrictTutorialCatalog() {
        val dynamicGameplayLabels = setOf(
            "START",
            "TAP TO PULL",
            "FOCUS",
            "POWER",
            "HEAT",
            "WIND GUARD",
            "STORM"
        )

        assertTrue(TutorialCopy.fieldLabelKeys.containsAll(dynamicGameplayLabels))
        dynamicGameplayLabels.forEach { key ->
            assertTrue(TutorialCopy.hasExplicitTranslation(KavvoroLanguage.AR, key))
            assertNotEquals(key, TutorialCopy.translation(KavvoroLanguage.AR, key))
        }
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

    @Test
    fun actualCurseAndPowerRibbonSelectorsAreStrictlyLocalized() {
        val selectedCurseKeys = CurseType.entries
            .map(TutorialCopy::curseRibbonKey)
            .toSet()
        val selectedPowerKeys = BallPower.entries
            .filterNot { it == BallPower.NONE }
            .map(TutorialCopy::ballPowerRibbonKey)
            .toSet()

        assertEquals(TutorialCopy.curseRibbonKeys, selectedCurseKeys)
        assertEquals(TutorialCopy.ballPowerRibbonKeys, selectedPowerKeys)
        assertTrue(TutorialCopy.ribbonKeys.all { it in TutorialCopy.requiredKeys })
        TutorialCopy.ribbonKeys.forEach { key ->
            assertNotEquals(key, TutorialCopy.translation(KavvoroLanguage.AR, key))
        }
    }
}
