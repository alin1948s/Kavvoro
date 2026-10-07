package com.moonsolstudios.kavvoro.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards audit item UI-1.
 *
 * The Home screen used to resolve its copy from hand-written `when (language)` tables that
 * covered only RO/ES/FR/DE/IT/PT/RU and silently fell back to English for the other 17
 * selectable languages. These tests fail if Home copy ever stops resolving through
 * [LocalizationCatalog] for the complete language set.
 */
class HomeCopyCatalogCoverageTest {

    private val languages = KavvoroLanguage.entries.filterNot { it == KavvoroLanguage.SYSTEM }

    private val accessors: List<Pair<String, (KavvoroLanguage) -> String>> = listOf(
        "ctaPlay" to { language -> HomeCopy.ctaPlay(language) },
        "ctaPlayShort" to { language -> HomeCopy.ctaPlayShort(language) },
        "ctaSubtitle" to { language -> HomeCopy.ctaSubtitle(language) },
        "skinsTitle" to { language -> HomeCopy.skinsTitle(language) },
        "skinsSubtitle" to { language -> HomeCopy.skinsSubtitle(language) },
        "missionsTitle" to { language -> HomeCopy.missionsTitle(language) },
        "missionsSubtitle" to { language -> HomeCopy.missionsSubtitle(language) },
        "leaderboardTitle" to { language -> HomeCopy.leaderboardTitle(language) },
        "leaderboardSubtitle" to { language -> HomeCopy.leaderboardSubtitle(language) },
        "streak" to { language -> HomeCopy.streak(language) },
        "level" to { language -> HomeCopy.level(language) },
        "coins" to { language -> HomeCopy.coins(language) },
        "brandMotto" to { language -> HomeCopy.brandMotto(language) },
        "landscapeWatermark" to { language -> HomeCopy.landscapeWatermark(language) }
    )

    private val homeSourceKeys = listOf(
        "PLAY NOW",
        "PLAY",
        "JUMP INTO A BRIGHTER UNIVERSE",
        "SKINS",
        "CUSTOMIZE YOUR STYLE",
        "MISSIONS",
        "COMPLETE TASKS EARN REWARDS",
        "LEADERBOARD",
        "SEE TOP PLAYERS AROUND THE WORLD",
        "STREAK",
        "LEVEL",
        "COINS",
        "SMALL MINDS BIG WORLDS",
        "DIFFERENT WORLDS. SAME CHAOS.",
        "LEVELS",
        "MULTIVERSE PORTAL",
        "DIVISION 1",
        "OPEN",
        "TOP",
        "TOP PLAYERS"
    )

    @Test
    fun homeCopyCoversEverySelectableLanguage() {
        assertEquals(24, languages.size)
        for (language in languages) {
            for ((name, resolve) in accessors) {
                assertTrue(
                    "HomeCopy.$name is blank for ${language.code}",
                    resolve(language).isNotBlank()
                )
            }
        }
    }

    @Test
    fun homeSourceKeysBelongToTheStrictCatalogInventory() {
        assertEquals(homeSourceKeys.toSet(), HomeCopy.requiredKeys)
        val missing = HomeCopy.requiredKeys.filterNot { it in LocalizationCatalog.requiredKeys }
        assertTrue("Home source keys missing from the catalog inventory: $missing", missing.isEmpty())
    }

    @Test
    fun noNonEnglishLanguageFallsBackToEnglishHomeCopy() {
        val english = languages.first { it == KavvoroLanguage.EN }
        val offenders = mutableListOf<String>()

        for (language in languages) {
            if (language == english) continue
            for ((name, resolve) in accessors) {
                val value = resolve(language)
                if (value == resolve(english) && value !in LocalizationCatalog.allowlistedEnglishValues) {
                    offenders += "${language.code}.$name -> $value"
                }
            }
        }

        assertTrue(
            "Home copy falls back to English (${offenders.size}): ${offenders.take(12)}",
            offenders.isEmpty()
        )
    }

    @Test
    fun homeCopyResolvesTheLocalizedValueForEveryLanguage() {
        val expectations = mapOf(
            KavvoroLanguage.RO to ("JOACĂ ACUM" to "Clasament"),
            KavvoroLanguage.JA to ("今すぐプレイ" to "ランキング"),
            KavvoroLanguage.AR to ("العب الآن" to "لوحة الصدارة"),
            KavvoroLanguage.PT to ("JOGAR AGORA" to "Classificação"),
            KavvoroLanguage.VI to ("CHƠI NGAY" to "Bảng xếp hạng")
        )

        for ((language, expected) in expectations) {
            assertEquals(
                "Play CTA mismatch for ${language.code}",
                expected.first,
                HomeCopy.ctaPlay(language)
            )
            assertEquals(
                "Leaderboard title mismatch for ${language.code}",
                expected.second,
                HomeCopy.leaderboardTitle(language)
            )
        }
    }
}
