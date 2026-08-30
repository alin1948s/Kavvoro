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
    fun voiceLocaleMappingMatchesTheShippedAssetInventory() {
        val englishFallbacks = setOf(
            KavvoroLanguage.SYSTEM,
            KavvoroLanguage.EN,
            KavvoroLanguage.CS,
            KavvoroLanguage.SV,
            KavvoroLanguage.FI,
            KavvoroLanguage.TH
        )

        englishFallbacks.forEach { language ->
            assertEquals("en", KavvoroI18n.audioLanguageCode(language))
        }
        assertEquals("zh", KavvoroI18n.audioLanguageCode(KavvoroLanguage.ZH))
        assertEquals("zh", KavvoroI18n.audioLanguageCode(KavvoroLanguage.ZH_TW))
    }

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
        assertEquals("JĘZYK", KavvoroI18n.t(KavvoroLanguage.PL, "LANGUAGE"))
        assertEquals("PIERWSZA GRA", KavvoroI18n.t(KavvoroLanguage.PL, "FIRST RUN"))
        assertEquals("ZACZNIJ POZIOM 01", KavvoroI18n.t(KavvoroLanguage.PL, "START LEVEL 01"))
        assertEquals("GRA PRZERWANA", KavvoroI18n.t(KavvoroLanguage.PL, "RUN INTERRUPTED"))
        assertEquals("RIFT ZAPADŁ SIĘ", KavvoroI18n.t(KavvoroLanguage.PL, "RIFT COLLAPSED"))
        assertEquals("KONTYNUUJ BEZPŁATNIE", KavvoroI18n.t(KavvoroLanguage.PL, "CONTINUE FREE"))
        assertEquals("ENERGIA RIFT / RESTART POZIOMU", KavvoroI18n.t(KavvoroLanguage.PL, "RIFT ENERGY RESETS / LEVEL RESTARTS"))
        assertEquals("Darmowe wznowienie. Seria %s trwa.", KavvoroI18n.t(KavvoroLanguage.PL, "Free recovery available. Streak %s stays active."))
        assertEquals("Brainball resetuje się. Stukaj czyściej.", KavvoroI18n.t(KavvoroLanguage.PL, "Brainball rebooting. Try cleaner taps."))
    }

    @Test
    fun romanianTranslationsAreNaturalAndAccurate() {
        assertEquals("CONFIGURARE JUCĂTOR", KavvoroI18n.t(KavvoroLanguage.RO, "PLAYER SETUP"))
        assertEquals("CONFIDENȚIALITATE", KavvoroI18n.t(KavvoroLanguage.RO, "PRIVACY"))
        assertEquals("POLITICA DE CONFIDENȚIALITATE", KavvoroI18n.t(KavvoroLanguage.RO, "PRIVACY POLICY"))
        assertEquals("SEIFUL SERIEI", KavvoroI18n.t(KavvoroLanguage.RO, "STREAK VAULT"))
        assertEquals("JOACĂ", KavvoroI18n.t(KavvoroLanguage.RO, "PLAY"))
        assertEquals("ALEGE MODUL", KavvoroI18n.t(KavvoroLanguage.RO, "CHOOSE MODE"))
        assertEquals("SERIE", KavvoroI18n.t(KavvoroLanguage.RO, "STREAK"))
        assertEquals("RECORD DE SERIE", KavvoroI18n.t(KavvoroLanguage.RO, "BEST STREAK"))
        assertEquals("AM ÎNȚELES!", KavvoroI18n.t(KavvoroLanguage.RO, "GOT IT"))
    }

    @Test
    fun newLocalesHaveCompleteAndAuditedTranslations() {
        // Czech
        assertEquals("NASTAVENÍ HRÁČE", KavvoroI18n.t(KavvoroLanguage.CS, "PLAYER SETUP"))
        assertEquals("SOUKROMÍ", KavvoroI18n.t(KavvoroLanguage.CS, "PRIVACY"))
        assertEquals("HRÁT", KavvoroI18n.t(KavvoroLanguage.CS, "PLAY"))

        // Swedish
        assertEquals("SPELARINSTÄLLNING", KavvoroI18n.t(KavvoroLanguage.SV, "PLAYER SETUP"))
        assertEquals("SEKRETESS", KavvoroI18n.t(KavvoroLanguage.SV, "PRIVACY"))
        assertEquals("SPELA", KavvoroI18n.t(KavvoroLanguage.SV, "PLAY"))

        // Finnish
        assertEquals("PELAAJAN ASETUKSET", KavvoroI18n.t(KavvoroLanguage.FI, "PLAYER SETUP"))
        assertEquals("TIETOSUOJA", KavvoroI18n.t(KavvoroLanguage.FI, "PRIVACY"))
        assertEquals("PELAA", KavvoroI18n.t(KavvoroLanguage.FI, "PLAY"))

        // Thai
        assertEquals("ตั้งค่าผู้เล่น", KavvoroI18n.t(KavvoroLanguage.TH, "PLAYER SETUP"))
        assertEquals("ความเป็นส่วนตัว", KavvoroI18n.t(KavvoroLanguage.TH, "PRIVACY"))
        assertEquals("เล่น", KavvoroI18n.t(KavvoroLanguage.TH, "PLAY"))

        // Traditional Chinese
        assertEquals("玩家設定", KavvoroI18n.t(KavvoroLanguage.ZH_TW, "PLAYER SETUP"))
        assertEquals("隱私權", KavvoroI18n.t(KavvoroLanguage.ZH_TW, "PRIVACY"))
        assertEquals("開始遊戲", KavvoroI18n.t(KavvoroLanguage.ZH_TW, "PLAY"))
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
