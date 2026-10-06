package com.moonsolstudios.kavvoro.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Automated guard suite enforcing the architectural and i18n rules codified in
 * `AGENTS.md` and `.agents/rules/architecture-and-i18n.md`.
 */
class ProjectArchitectureRulesTest {

    private fun locateProjectRoot(): File =
        listOf(File("."), File(".."))
            .map { it.canonicalFile }
            .firstOrNull { File(it, "app/src/main/java/com/moonsolstudios/kavvoro").exists() }
            ?: error("Could not locate Kavvoro project root")

    @Test
    fun `AI rules files AGENTS_md and architecture-and-i18n_md exist and are populated`() {
        val root = locateProjectRoot()
        val agentsMd = File(root, "AGENTS.md")
        val cursorRuleMd = File(root, ".agents/rules/architecture-and-i18n.md")

        assertTrue("AGENTS.md must exist in repository root for AI agent discovery", agentsMd.isFile)
        assertTrue("AGENTS.md must not be empty", agentsMd.length() > 500)
        assertTrue(
            ".agents/rules/architecture-and-i18n.md must exist for IDE agent discovery",
            cursorRuleMd.isFile
        )
        assertTrue(".agents/rules/architecture-and-i18n.md must not be empty", cursorRuleMd.length() > 200)
    }

    @Test
    fun `ChaosGameView stays under line ceiling and delegates screen RectFs and Bridges`() {
        val root = locateProjectRoot()
        val chaosViewFile = File(root, "app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt")
        val lines = chaosViewFile.readLines()

        assertTrue(
            "ChaosGameView.kt (${lines.size} lines) exceeds the 3,350-line architectural ceiling (see AGENTS.md). Move new screen rendering/layout/touch logic to ui/screens/<screen>/.",
            lines.size <= 3350
        )

        val directRectFs = lines.map { it.trim() }
            .filter { it.startsWith("private val ") && it.contains("= RectF()") }
        assertEquals(
            "ChaosGameView must only own its internal scratch RectF (see AGENTS.md)",
            listOf("private val scratch = RectF()"),
            directRectFs
        )

        val nestedBridges = lines.map { it.trim() }
            .filter { it.startsWith("interface ") && it.contains("Bridge") }
        assertTrue(
            "Bridge interfaces must live in their domain packages (ads/, playgames/, privacy/, billing/), not nested in ChaosGameView: $nestedBridges",
            nestedBridges.isEmpty()
        )
    }

    @Test
    fun `ChaosGameView delegates drawing and screen viewport geometry`() {
        val root = locateProjectRoot()
        val source = File(root, "app/src/main/java/com/moonsolstudios/kavvoro/ui/ChaosGameView.kt").readText()

        assertFalse("ChaosGameView must delegate Canvas drawing to renderers", Regex("\\bcanvas\\.draw[A-Z]\\w*\\s*\\(").containsMatchIn(source))
        assertTrue("Shared viewport geometry should live in ui/layout", source.contains("ViewportLayoutCalculator"))
        assertFalse("Collection viewport inset should live in its screen layout calculator", source.contains("viewHeight - dp(78f)"))
        assertFalse("Page centering arithmetic should live outside the screen orchestrator", source.contains("(viewWidth - pageContentWidth()) * 0.5f"))
        assertFalse("Settings centering arithmetic should live outside the screen orchestrator", source.contains("(viewWidth - settingsContentWidth()) * 0.5f"))
    }

    @Test
    fun `screen packages have zero cross-screen imports and shared ui layers never import screens`() {
        val root = locateProjectRoot()
        val uiRoots = listOf(
            "main" to File(root, "app/src/main/java/com/moonsolstudios/kavvoro/ui"),
            "test" to File(root, "app/src/test/java/com/moonsolstudios/kavvoro/ui")
        )

        val crossScreenViolations = mutableListOf<String>()
        val upwardImportViolations = mutableListOf<String>()

        uiRoots.forEach { (treeLabel, uiDir) ->
            val screensDir = File(uiDir, "screens")
            if (screensDir.isDirectory) {
                screensDir.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" && it.parentFile != screensDir }
                    .forEach { file ->
                        val ownScreen = file.parentFile?.name.orEmpty()
                        file.readLines().forEachIndexed { idx, line ->
                            val trimmed = line.trim()
                            if (trimmed.startsWith("import com.moonsolstudios.kavvoro.ui.screens.")) {
                                val targetScreen = trimmed
                                    .removePrefix("import com.moonsolstudios.kavvoro.ui.screens.")
                                    .substringBefore(".")
                                if (targetScreen != ownScreen) {
                                    crossScreenViolations += "$treeLabel:$ownScreen/${file.name}:${idx + 1} -> $trimmed"
                                }
                            }
                        }
                    }
            }

            listOf("render", "layout", "controller", "tutorial").forEach { layer ->
                val layerDir = File(uiDir, layer)
                if (layerDir.isDirectory) {
                    layerDir.walkTopDown()
                        .filter { it.isFile && it.extension == "kt" }
                        .forEach { file ->
                            file.readLines().forEachIndexed { idx, line ->
                                val trimmed = line.trim()
                                if (trimmed.startsWith("import com.moonsolstudios.kavvoro.ui.screens.")) {
                                    upwardImportViolations += "$treeLabel:$layer/${file.name}:${idx + 1} -> $trimmed"
                                }
                            }
                        }
                }
            }
        }

        assertTrue(
            "Files in ui/screens/<screen>/ (main & test) must never import from another ui/screens/<other>/ package: $crossScreenViolations",
            crossScreenViolations.isEmpty()
        )
        assertTrue(
            "Shared UI packages (render, layout, controller, tutorial in main & test) must never import upward from ui/screens/*: $upwardImportViolations",
            upwardImportViolations.isEmpty()
        )
    }

    @Test
    fun `screen-specific tests are nested under their screen packages`() {
        val root = locateProjectRoot()
        val screensTestRoot = File(root, "app/src/test/java/com/moonsolstudios/kavvoro/ui/screens")
        val filesAtScreensRoot = screensTestRoot.listFiles()
            .orEmpty()
            .filter { it.isFile && it.extension == "kt" }

        assertTrue(
            "Screen tests belong in ui/screens/<screen>/; cross-screen architecture tests belong in architecture/: $filesAtScreensRoot",
            filesAtScreensRoot.isEmpty()
        )
    }

    @Test
    fun `engine and model packages have zero imports from ui package`() {
        val root = locateProjectRoot()
        val basePkgDir = File(root, "app/src/main/java/com/moonsolstudios/kavvoro")
        val violations = mutableListOf<String>()

        listOf("engine", "model").forEach { pkg ->
            File(basePkgDir, pkg).walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    file.readLines().forEachIndexed { index, line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("import com.moonsolstudios.kavvoro.ui.")) {
                            violations += "${pkg}/${file.name}:${index + 1} -> $trimmed"
                        }
                    }
                }
        }

        assertTrue(
            "engine/ and model/ must never import from com.moonsolstudios.kavvoro.ui.* (see AGENTS.md): $violations",
            violations.isEmpty()
        )
    }

    @Test
    fun `all 5 domain bridges exist in their dedicated domain packages`() {
        val root = locateProjectRoot()
        val basePkgDir = File(root, "app/src/main/java/com/moonsolstudios/kavvoro")
        val expectedBridges = listOf(
            "ads/AdBridge.kt",
            "playgames/AccountBridge.kt",
            "playgames/LeaderboardBridge.kt",
            "privacy/PrivacyBridge.kt",
            "billing/PurchaseBridge.kt"
        )
        expectedBridges.forEach { relPath ->
            assertTrue(
                "Expected domain bridge $relPath to exist (see AGENTS.md)",
                File(basePkgDir, relPath).isFile
            )
        }
    }

    @Test
    fun `i18n key-definition files contain zero inline translation maps and all 24 catalog files exist`() {
        val root = locateProjectRoot()
        val i18nDir = File(root, "app/src/main/java/com/moonsolstudios/kavvoro/i18n")
        val catalogDir = File(i18nDir, "catalog")
        val catalogFiles = catalogDir.listFiles { file ->
            file.isFile && file.name.endsWith("Translations.kt")
        }.orEmpty()

        assertEquals(
            "Expected exactly 24 per-language *Translations.kt files in i18n/catalog/ (see AGENTS.md)",
            24,
            catalogFiles.size
        )

        listOf("HomeCopy.kt", "TutorialCopy.kt", "UiTranslations.kt").forEach { fileName ->
            val content = File(i18nDir, fileName).readText()
            assertFalse(
                "$fileName must not contain inline mapOf(...) translations; all translations belong in i18n/catalog/*Translations.kt",
                content.contains("mapOf(")
            )
        }
    }

    @Test
    fun `no empty directories dead facades package mismatches or root scratch directories exist`() {
        val root = locateProjectRoot()
        val mainJavaDir = File(root, "app/src/main/java")
        val testJavaDir = File(root, "app/src/test/java")

        listOf("main" to mainJavaDir, "test" to testJavaDir).forEach { (label, baseDir) ->
            val emptyDirs = baseDir.walkTopDown()
                .filter { it.isDirectory && (it.listFiles()?.isEmpty() == true) }
                .map { it.relativeTo(baseDir).path }
                .toList()
            assertTrue(
                "Found empty package directories under app/src/$label/java: $emptyDirs",
                emptyDirs.isEmpty()
            )

            val packageMismatches = mutableListOf<String>()
            baseDir.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    val expectedPkg = file.parentFile!!.relativeTo(baseDir).path
                        .replace('\\', '.')
                        .replace('/', '.')
                    val actualPkg = file.readLines()
                        .firstOrNull { it.trim().startsWith("package ") }
                        ?.trim()
                        ?.removePrefix("package ")
                        ?.trim()
                    if (actualPkg != expectedPkg) {
                        packageMismatches += "${file.relativeTo(baseDir).path}: expected '$expectedPkg', got '$actualPkg'"
                    }
                }
            assertTrue(
                "All Kotlin files in app/src/$label/java must match their directory package: $packageMismatches",
                packageMismatches.isEmpty()
            )
        }

        val kavvoroMain = File(mainJavaDir, "com/moonsolstudios/kavvoro")
        assertFalse("Orphan ui/core directory must not exist", File(kavvoroMain, "ui/core").exists())
        assertFalse("Orphan ui/accessibility directory must not exist", File(kavvoroMain, "ui/accessibility").exists())
        assertFalse("Dead ScreenLayoutManager facade must not exist", File(kavvoroMain, "ui/layout/ScreenLayoutManager.kt").exists())
        assertFalse(
            "Duplicate HomeUiRenderer must not exist alongside HomeMenuRenderer",
            File(kavvoroMain, "ui/screens/home/HomeUiRenderer.kt").exists()
        )
        assertFalse("Root scratch/ directory must not exist", File(root, "scratch").exists())
        assertFalse("Root temp_crops/ directory must not exist", File(root, "temp_crops").exists())
        assertFalse("Root implementation_pack/ directory must not exist", File(root, "implementation_pack").exists())
        assertFalse("Root preview/ directory must not exist", File(root, "preview").exists())
        assertFalse("Root .superpowers/ scratch directory must not exist", File(root, ".superpowers").exists())
        assertFalse("One-off scratch script tools/apply_home_redesign.py must not exist", File(root, "tools/apply_home_redesign.py").exists())

        val resDir = File(root, "app/src/main/res")
        val localeValuesDirs = resDir.listFiles { f ->
            f.isDirectory && f.name.startsWith("values-") && f.name != "values-v31"
        }.orEmpty().map { it.name }.sorted()
        assertTrue(
            "All translations must live exclusively in i18n/catalog/*Translations.kt, not in res/values-<lang>: $localeValuesDirs",
            localeValuesDirs.isEmpty()
        )

        val drawableDir = File(resDir, "drawable")
        val flagFiles = drawableDir.listFiles { f -> f.isFile && f.name.startsWith("flag_") && f.extension == "xml" }
            .orEmpty()
            .map { it.name }
            .sorted()
        assertEquals(
            "drawable/ must contain exactly the 24 language-code flag XMLs without duplicate country-code flags",
            24,
            flagFiles.size
        )
        assertFalse(
            "Unused brand_kavvoro.xml must not exist in drawable/",
            File(drawableDir, "brand_kavvoro.xml").exists()
        )
        assertFalse(
            "Conflicting brand_kavvoro.webp must not exist in drawable-nodpi",
            File(resDir, "drawable-nodpi/brand_kavvoro.webp").exists()
        )
        assertFalse(
            "Duplicate bg_space_base_1440x2560.webp must not exist in drawable-nodpi (home_bg_cosmic_clean.webp is canonical)",
            File(resDir, "drawable-nodpi/bg_space_base_1440x2560.webp").exists()
        )
        assertFalse(
            "Duplicate home_portal_platform.webp must not exist in drawable-nodpi (home_portal_disc.webp is canonical)",
            File(resDir, "drawable-nodpi/home_portal_platform.webp").exists()
        )
    }
}
