package com.moonsolstudios.kavvoro.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizationCatalogTest {
    @Test
    fun selectableLanguageSetContainsExactlyTheTwentyFourAuditedLanguages() {
        val auditedLanguages = setOf(
            KavvoroLanguage.EN, KavvoroLanguage.RO, KavvoroLanguage.ES, KavvoroLanguage.FR,
            KavvoroLanguage.DE, KavvoroLanguage.IT, KavvoroLanguage.PT, KavvoroLanguage.NL,
            KavvoroLanguage.PL, KavvoroLanguage.CS, KavvoroLanguage.SV, KavvoroLanguage.FI,
            KavvoroLanguage.TR, KavvoroLanguage.RU, KavvoroLanguage.UK, KavvoroLanguage.AR,
            KavvoroLanguage.HI, KavvoroLanguage.TH, KavvoroLanguage.ID, KavvoroLanguage.VI,
            KavvoroLanguage.JA, KavvoroLanguage.KO, KavvoroLanguage.ZH, KavvoroLanguage.ZH_TW
        )
        assertEquals(24, LocalizationCatalog.supportedLanguages.size)
        assertEquals(auditedLanguages, LocalizationCatalog.supportedLanguages)
    }

    @Test
    fun everySelectableLocaleHasExactInventoryCoverageAndNonBlankValues() {
        KavvoroLanguage.entries
            .filterNot { it == KavvoroLanguage.SYSTEM }
            .forEach { language ->
                val values = LocalizationCatalog.locale(language)
                assertEquals(
                    "${language.code}: locale key set differs from the frozen inventory",
                    LocalizationCatalog.requiredKeys,
                    values.keys
                )
                val blankKeys = values.filterValues { it.isBlank() }.keys
                assertTrue("${language.code}: blank values $blankKeys", blankKeys.isEmpty())
            }
    }

    @Test
    fun everyTutorialKeyBelongsToTheUnifiedRenderedInventory() {
        assertTrue(
            "Tutorial keys missing from unified inventory",
            LocalizationCatalog.requiredKeys.containsAll(TutorialCopy.requiredKeys)
        )
    }

    @Test
    fun everyHomeKeyBelongsToTheUnifiedRenderedInventory() {
        assertTrue(
            "Home keys missing from unified inventory",
            LocalizationCatalog.requiredKeys.containsAll(HomeCopy.requiredKeys)
        )
    }

    @Test
    fun everyModuleKeySetIsStrictlyDisjoint() {
        val modules = listOf(
            "renderedSourceKeys" to LocalizationCatalog.renderedSourceKeys,
            "TutorialCopy.requiredKeys" to TutorialCopy.requiredKeys,
            "UiTranslations.requiredKeys" to UiTranslations.requiredKeys,
            "HomeCopy.requiredKeys" to HomeCopy.requiredKeys
        )
        for (i in modules.indices) {
            for (j in i + 1 until modules.size) {
                val (nameA, setA) = modules[i]
                val (nameB, setB) = modules[j]
                val overlap = setA.intersect(setB)
                assertTrue(
                    "Overlap between $nameA and $nameB: $overlap",
                    overlap.isEmpty()
                )
            }
        }
    }

    @Test
    fun everyStaticTCallInCodebaseBelongsToTheStrictCatalog() {
        val srcDir = java.io.File("src/main/java")
        if (!srcDir.isDirectory) return
        val tCallRegex = Regex("""\bt\(\s*(?:[a-zA-Z0-9_.]+\s*,\s*)?"((?:\\.|[^"\\])*)"\s*\)""")
        val missing = mutableListOf<String>()
        srcDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val text = file.readText(Charsets.UTF_8)
                tCallRegex.findAll(text).forEach { match ->
                    val key = match.groupValues[1]
                    if ('$' !in key && key !in LocalizationCatalog.requiredKeys) {
                        missing += "${file.relativeTo(srcDir).path}: \"$key\""
                    }
                }
            }
        assertTrue(
            "Uncataloged t(...) calls found in source: $missing",
            missing.isEmpty()
        )
    }

    @Test
    fun proceduralUiVocabularyIsExplicitlyLocalizedForEveryLanguage() {
        assertTrue(LocalizationCatalog.requiredKeys.containsAll(UiTranslations.requiredKeys))
        KavvoroLanguage.entries
            .filterNot { it == KavvoroLanguage.SYSTEM }
            .forEach { language ->
                val values = LocalizationCatalog.locale(language)
                UiTranslations.requiredKeys.forEach { key ->
                    assertTrue("${language.code}: missing UI key $key", values[key].orEmpty().isNotBlank())
                }
            }
    }

    @Test
    fun frozenInventoryMatchesTheCurrentSourceBackedInventory() {
        assertEquals(LocalizationCatalog.sourceInventory, LocalizationCatalog.requiredKeys)
    }

    @Test
    fun everyLocalePreservesTheEnglishPlaceholderSignature() {
        val english = LocalizationCatalog.locale(KavvoroLanguage.EN)
        KavvoroLanguage.entries
            .filterNot { it == KavvoroLanguage.SYSTEM || it == KavvoroLanguage.EN }
            .forEach { language ->
                val values = LocalizationCatalog.locale(language)
                val mismatches = LocalizationCatalog.requiredKeys.filter { key ->
                    LocalizationCatalog.placeholderSignature(english.getValue(key)) !=
                        LocalizationCatalog.placeholderSignature(values.getValue(key))
                }
                assertTrue("${language.code}: placeholder mismatches $mismatches", mismatches.isEmpty())
            }
    }

    @Test
    fun placeholderSignatureIsOrderedAndSupportsPositionalPlaceholders() {
        assertEquals(
            listOf("%s", "%d", "%1$" + "s"),
            LocalizationCatalog.placeholderSignature("%s %d %1\$s")
        )
        assertEquals(
            listOf("%mode", "%level", "%ball", "%rank", "%hype", "%chain", "%streak", "%code"),
            LocalizationCatalog.placeholderSignature(
                "%mode %level %ball %rank %hype %chain %streak %code"
            )
        )
    }

    @Test
    fun englishShareCopyUsesRuntimeTemplatesRatherThanShortSourceKeys() {
        assertEquals(
            listOf("%mode", "%level", "%ball", "%rank", "%hype", "%chain", "%streak", "%code"),
            LocalizationCatalog.placeholderSignature(
                KavvoroI18n.t(KavvoroLanguage.EN, "Can you beat my Kavvoro rift?")
            )
        )
        assertEquals(
            listOf("%mode", "%level", "%ball", "%code"),
            LocalizationCatalog.placeholderSignature(
                KavvoroI18n.t(KavvoroLanguage.EN, "Trying Brainrot Chaos: Kavvoro")
            )
        )
    }

    @Test
    fun noUnapprovedEnglishFallbackRemainsInTheStrictCatalog() {
        val english = LocalizationCatalog.locale(KavvoroLanguage.EN)
        val offenders = mutableListOf<String>()

        KavvoroLanguage.entries
            .filterNot { it == KavvoroLanguage.SYSTEM || it == KavvoroLanguage.EN }
            .forEach { language ->
                LocalizationCatalog.locale(language).forEach { (key, value) ->
                    if (
                        value == english.getValue(key) &&
                        value !in LocalizationCatalog.allowlistedEnglishValues
                    ) {
                        offenders += "${language.code}: $key -> $value"
                    }
                }
            }

        assertTrue(
            "Unapproved English fallback remains (${offenders.size}): ${offenders.take(12)}",
            offenders.isEmpty()
        )
    }
}
