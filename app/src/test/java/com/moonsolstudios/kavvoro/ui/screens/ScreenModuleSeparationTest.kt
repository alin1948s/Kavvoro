package com.moonsolstudios.kavvoro.ui.screens

import android.graphics.RectF
import com.moonsolstudios.kavvoro.engine.CurseSpec
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.LevelDirector
import com.moonsolstudios.kavvoro.engine.PortalPair
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.model.ButtonId
import com.moonsolstudios.kavvoro.model.CollectionFilter
import com.moonsolstudios.kavvoro.model.CollectionSort
import com.moonsolstudios.kavvoro.model.GameMode
import com.moonsolstudios.kavvoro.model.GameState
import com.moonsolstudios.kavvoro.model.MenuButton
import com.moonsolstudios.kavvoro.model.MenuState
import com.moonsolstudios.kavvoro.repository.BallSkinCatalog
import com.moonsolstudios.kavvoro.share.ReplayShareController
import com.moonsolstudios.kavvoro.ui.render.BrandTitleRenderer
import com.moonsolstudios.kavvoro.ui.screens.ad.AdScreenTouchController
import com.moonsolstudios.kavvoro.ui.screens.collection.CollectionLayoutCalculator
import com.moonsolstudios.kavvoro.ui.screens.gameplay.GameplayHudRenderer
import com.moonsolstudios.kavvoro.ui.screens.gameplay.GameplayTouchController
import com.moonsolstudios.kavvoro.ui.screens.home.HomeLayoutCalculator
import com.moonsolstudios.kavvoro.ui.screens.home.HomeMenuTouchController
import com.moonsolstudios.kavvoro.ui.screens.language.LanguageSelectorLayoutCalculator
import com.moonsolstudios.kavvoro.ui.screens.leaderboards.LeaderboardLayoutCalculator
import com.moonsolstudios.kavvoro.ui.screens.outcome.OutcomeTouchController
import com.moonsolstudios.kavvoro.ui.screens.outcome.OutcomeUiRenderer
import com.moonsolstudios.kavvoro.ui.tutorial.TutorialRenderer
import com.moonsolstudios.kavvoro.ui.tutorial.TutorialTouchController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenModuleSeparationTest {

    @Test
    fun `LeaderboardLayoutCalculator populates all 4 board rects and BrandTitleRenderer matches HomeLayoutCalculator`() {
        val backA = RectF()
        val itemsA = mutableListOf<RectF>()
        LeaderboardLayoutCalculator.layoutLeaderboards(
            side = 24f,
            contentRight = 516f,
            backTop = 28f,
            itemsTop = 176f,
            viewHeight = 840f,
            dp = 1f,
            leaderboardBackButton = backA,
            leaderboardItemRects = itemsA
        )

        assertEquals(4, itemsA.size)
        assertTrue(backA.right > backA.left)

        val calc = HomeLayoutCalculator()
        calc.calculate(412f, 915f, 1f, brandAspect = HomeLayoutCalculator.BRAND_ASPECT)
        val sharedBrand = BrandTitleRenderer.placement(412f, 915f, 1f)
        assertEquals(calc.brandRect.left, sharedBrand.left, 0.01f)
        assertEquals(calc.brandRect.top, sharedBrand.top, 0.01f)
        assertEquals(calc.brandRect.right, sharedBrand.right, 0.01f)
        assertEquals(calc.brandRect.bottom, sharedBrand.bottom, 0.01f)
    }

    @Test
    fun `CollectionLayoutCalculator layouts filters and collection grid with scroll clamping`() {
        val backButton = RectF()
        val restoreButton = RectF()
        val filterRects = List(CollectionFilter.entries.size) { RectF() }
        val itemRects = mutableListOf<RectF>()

        val (scroll, maxScroll) = CollectionLayoutCalculator.layoutCollection(
            contentLeft = 18f,
            contentRight = 394f,
            safeTop22 = 22f,
            safeTop68 = 68f,
            safeTop88 = 88f,
            safeTop192 = 192f,
            viewportTop = 284f,
            viewportBottom = 800f,
            scroll = 99999f, // Out-of-bounds scroll should clamp to maxScroll
            ballSkins = BallSkinCatalog.ALL_SKINS,
            filter = CollectionFilter.ALL,
            sort = CollectionSort.entries.first(),
            isSkinUnlocked = { it.id == "nodlo" },
            dp = 1f,
            backButton = backButton,
            restoreButton = restoreButton,
            filterRects = filterRects,
            itemRects = itemRects
        )

        assertTrue(filterRects.all { it.right > it.left && it.bottom > it.top })
        assertTrue(filterRects[1].left > filterRects[0].left)
        assertTrue(filterRects[0].top > 0f)
        assertEquals(BallSkinCatalog.ALL_SKINS.size, itemRects.size)
        assertTrue(maxScroll > 0f)
        assertEquals(maxScroll, scroll, 0.01f)
    }

    @Test
    fun `LanguageSelectorLayoutCalculator computes single-column mobile and dual-column tablet layouts`() {
        val mobileResult = LanguageSelectorLayoutCalculator.layoutLanguageSelector(
            side = 0f,
            contentWidth = 412f,
            compact = true,
            headerY = 28f,
            viewportTop = 0f,
            viewportBottom = 915f,
            languageScroll = -50f, // Negative scroll clamps to 0f
            dp = 1f,
            languageBackButton = RectF(),
            languageItemRects = mutableListOf(),
            languageDeckRect = RectF(),
            languageFooterRect = RectF(),
            viewportWidth = 412f
        )

        val tabletResult = LanguageSelectorLayoutCalculator.layoutLanguageSelector(
            side = 0f,
            contentWidth = 800f,
            compact = false,
            headerY = 28f,
            viewportTop = 0f,
            viewportBottom = 1280f,
            languageScroll = 0f,
            dp = 1f,
            languageBackButton = RectF(),
            languageItemRects = mutableListOf(),
            languageDeckRect = RectF(),
            languageFooterRect = RectF(),
            viewportWidth = 800f
        )

        assertEquals(0f, mobileResult.scroll, 0.001f)
        assertEquals(1, mobileResult.columns)
        assertEquals(2, tabletResult.columns)
    }

    @Test
    fun `GameplayHudRenderer and OutcomeUiRenderer layout toolbar and outcome buttons distinctly`() {
        val home = RectF()
        val restart = RectF()
        val sfx = RectF()
        val music = RectF()
        val share = RectF(1f, 1f, 10f, 10f)
        val next = RectF(1f, 1f, 10f, 10f)

        GameplayHudRenderer.layoutToolbarButtons(
            viewWidth = 412f,
            dp = { it },
            homeButton = home,
            restartButton = restart,
            sfxButton = sfx,
            musicButton = music,
            shareButton = share,
            nextButton = next
        )

        assertTrue(home.left > restart.left)
        assertTrue(restart.left > sfx.left)
        assertTrue(sfx.left > music.left)
        assertEquals(0f, share.width(), 0.001f)
        assertEquals(0f, next.width(), 0.001f)

        val resultShare = RectF()
        val resultNext = RectF()
        val resultRetry = RectF()

        OutcomeUiRenderer.layoutOutcomeButtons(
            viewWidth = 412f,
            viewHeight = 915f,
            won = true,
            dp = { it },
            resultShareButton = resultShare,
            resultNextButton = resultNext,
            resultRetryButton = resultRetry
        )
        assertTrue(resultShare.left < resultNext.left)

        OutcomeUiRenderer.layoutOutcomeButtons(
            viewWidth = 412f,
            viewHeight = 915f,
            won = false,
            dp = { it },
            resultShareButton = resultShare,
            resultNextButton = resultNext,
            resultRetryButton = resultRetry
        )
        assertEquals(0f, resultShare.width(), 0.001f)
        assertEquals(resultNext.left, resultRetry.left, 0.001f)
    }

    @Test
    fun `GameplayHudRenderer levelArchetype and TutorialRenderer tutorialIconKey classify levels accurately`() {
        val baseSpec = LevelDirector.createClassic(1, 17.78f, 42L)
        val overheatSpec = baseSpec.copy(
            portals = emptyList<PortalPair>(),
            curses = listOf(CurseSpec(CurseType.OVERHEAT, "OVERHEAT", "Desc", 0xFFFF5757.toInt()))
        )
        val overheatArchetype = GameplayHudRenderer.levelArchetype(overheatSpec, GameMode.CHAOS)
        assertEquals("ENERGY TAX", overheatArchetype.label)
        assertEquals(
            "danger_beacon",
            TutorialRenderer.tutorialIconKey(overheatSpec) { it == CurseType.OVERHEAT }
        )
    }

    @Test
    fun `AdScreenTouchControllerOutcomeTouchController and GameplayTouchController hit-test screen buttons cleanly`() {
        val adButton = RectF()
        AdScreenTouchController.layoutAdButton(412f, 915f, 1f, adButton)
        assertTrue(adButton.right > adButton.left && adButton.bottom > adButton.top)
        val adCenterX = (adButton.left + adButton.right) * 0.5f
        val adCenterY = (adButton.top + adButton.bottom) * 0.5f
        assertEquals(ButtonId.AD_CONTINUE, AdScreenTouchController.buttonAt(adCenterX, adCenterY, adButton))
        assertEquals(ButtonId.NONE, AdScreenTouchController.buttonAt(5f, 5f, adButton))

        val resultShare = RectF()
        val resultNext = RectF()
        val resultRetry = RectF()
        OutcomeUiRenderer.layoutOutcomeButtons(412f, 915f, won = true, dp = { it }, resultShareButton = resultShare, resultNextButton = resultNext, resultRetryButton = resultRetry)
        assertEquals(
            ButtonId.NEXT,
            OutcomeTouchController.buttonAt((resultNext.left + resultNext.right) * 0.5f, (resultNext.top + resultNext.bottom) * 0.5f, GameState.WON, resultNext, resultShare, resultRetry)
        )
        assertEquals(
            ButtonId.SHARE,
            OutcomeTouchController.buttonAt((resultShare.left + resultShare.right) * 0.5f, (resultShare.top + resultShare.bottom) * 0.5f, GameState.WON, resultNext, resultShare, resultRetry)
        )

        val home = RectF()
        val restart = RectF()
        val sfx = RectF()
        val music = RectF()
        GameplayHudRenderer.layoutToolbarButtons(412f, { it }, home, restart, sfx, music, RectF(), RectF())
        assertTrue(home.top > 0f)
        assertEquals(
            ButtonId.HOME,
            GameplayTouchController.buttonAt(
                x = (home.left + home.right) * 0.5f,
                y = (home.top + home.bottom) * 0.5f,
                state = GameState.SIMULATING,
                homeButton = home,
                restartButton = restart,
                sfxButton = sfx,
                musicButton = music,
                shareButton = RectF(),
                nextButton = RectF(),
                resultNextButton = resultNext,
                resultShareButton = resultShare,
                resultRetryButton = resultRetry
            )
        )
    }

    @Test
    fun `HomeMenuTouchControllerTutorialTouchController and ReplayShareController isolate screen logic`() {
        val dummyListener = object : com.moonsolstudios.kavvoro.ui.screens.home.HomeMenuActionListener {
            override fun performHaptic(feedbackConstant: Int) {}
            override fun onMenuButtonSelected(button: MenuButton) {}
            override fun onDailyRiftClaim() {}
            override fun onDailyRiftOpenCollection() {}
            override fun onDailyRiftDismissed() {}
        }
        val homeController = HomeMenuTouchController(dummyListener)
        val playRect = RectF().apply { left = 40f; top = 500f; right = 360f; bottom = 580f }
        val empty = RectF()
        assertEquals(
            MenuButton.PLAY,
            homeController.menuButtonAt(
                x = 200f, y = 540f,
                menuState = MenuState.MODES,
                menuPrivacyButton = empty, menuSfxButton = empty, menuStartButton = playRect,
                menuLeaderboardButton = empty, menuVaultButton = empty, menuCollectionButton = empty,
                menuBannerButton = empty, menuClassicContinueButton = empty, menuClassicNewButton = empty,
                menuChaosContinueButton = empty, menuChaosNewButton = empty, menuChaosStartButton = empty,
                menuContinueButton = empty, menuBackButton = empty, menuClassicCard = empty, menuChaosCard = empty
            )
        )

        val tutorialController = TutorialTouchController()
        val startBtn = RectF().apply { left = 100f; top = 700f; right = 300f; bottom = 760f }
        val cardBounds = RectF().apply { left = 40f; top = 400f; right = 360f; bottom = 780f }
        assertEquals(
            com.moonsolstudios.kavvoro.ui.tutorial.TutorialTouchTarget.ACTION_BUTTON,
            tutorialController.touchTarget(200f, 730f, tutorialCardBounds = cardBounds, tutorialStartButton = startBtn)
        )

        val code = ReplayShareController.challengeCode(levelSeed = 999L, levelIndex = 4, lastHypeScore = 450)
        assertTrue(code.startsWith("KAV-"))
        assertEquals(10, code.length)

        val points = listOf(Point2(0f, 0f), Point2(0.01f, 0.01f), Point2(2f, 2f))
        val simplified = ReplayShareController.simplifyLine(points)
        assertEquals(2, simplified.size)

        val cursedSpec = LevelDirector.createClassic(1, 17.78f, 42L).copy(
            curses = listOf(CurseSpec(CurseType.RIFT_WIND, "Rift Wind", "Desc", 0xFFFF5757.toInt()))
        )
        val curseLabel = ReplayShareController.curseStackLabel(cursedSpec) { key ->
            if (key == "WIND GUARD") "SCUT DE VÂNT" else key
        }
        assertEquals("SCUT DE VÂNT", curseLabel)
    }

    @Test
    fun `SettingsTouchController syncs layout rects and hit-tests audio and tab controls`() {
        com.moonsolstudios.kavvoro.ui.screens.settings.SettingsTouchController.layoutCalculator.calculate(
            left = 20f,
            right = 392f,
            contentWidth = 372f,
            viewportTop = 120f,
            viewportBottom = 860f,
            settingsScroll = 0f,
            dp = 1f,
            currentTab = com.moonsolstudios.kavvoro.model.SettingsTab.AUDIO,
            isLandscape = false,
            viewWidth = 412f,
            viewHeight = 915f
        )
        com.moonsolstudios.kavvoro.ui.screens.settings.SettingsTouchController.syncLayoutRects()
        val tabGameplay = com.moonsolstudios.kavvoro.ui.screens.settings.SettingsTouchController.tabGameplay
        assertTrue(tabGameplay.right > tabGameplay.left && tabGameplay.bottom > tabGameplay.top)
        assertEquals(
            com.moonsolstudios.kavvoro.model.SettingsButton.TAB_GAMEPLAY,
            com.moonsolstudios.kavvoro.ui.screens.settings.SettingsTouchController.buttonAt(
                (tabGameplay.left + tabGameplay.right) * 0.5f,
                (tabGameplay.top + tabGameplay.bottom) * 0.5f,
                120f,
                860f
            )
        )
    }

    @Test
    fun `GameMode menuTitle requires and applies localization function`() {
        val roTranslator: (String) -> String = { key ->
            when (key) {
                "CLASSIC" -> "Clasic"
                "CHAOS" -> "Haos"
                else -> key
            }
        }
        assertEquals("CLASIC", GameMode.CLASSIC.menuTitle(roTranslator))
        assertEquals("HAOS", GameMode.CHAOS.menuTitle(roTranslator))
    }

    @Test
    fun `HomeMenuTouchController syncHomeLayoutRects populates owned Home button bounds`() {
        val controller = com.moonsolstudios.kavvoro.ui.screens.home.HomeMenuTouchController(
            object : com.moonsolstudios.kavvoro.ui.screens.home.HomeMenuActionListener {
                override fun onMenuButtonSelected(button: com.moonsolstudios.kavvoro.model.MenuButton) = Unit
                override fun onDailyRiftClaim() = Unit
                override fun onDailyRiftOpenCollection() = Unit
                override fun onDailyRiftDismissed() = Unit
                override fun performHaptic(feedbackConstant: Int) = Unit
            }
        )
        val calc = com.moonsolstudios.kavvoro.ui.screens.home.HomeLayoutCalculator()
        calc.calculate(width = 412f, height = 915f, displayDensity = 1f)
        controller.syncHomeLayoutRects(calc)
        assertTrue(controller.menuStartButton.right > controller.menuStartButton.left)
        assertTrue(controller.menuLeaderboardButton.right > controller.menuLeaderboardButton.left)
        assertTrue(controller.menuCollectionButton.right > controller.menuCollectionButton.left)
    }

    @Test
    fun `screen modules have zero cross-screen imports and shared ui layers never import screens`() {
        val projectRoot = listOf(
            java.io.File("."),
            java.io.File("..")
        ).map { it.canonicalFile }
            .firstOrNull { java.io.File(it, "app/src/main/java/com/moonsolstudios/kavvoro/ui").exists() }
            ?: error("Could not locate project root")

        val uiRoot = java.io.File(projectRoot, "app/src/main/java/com/moonsolstudios/kavvoro/ui")
        val screensRoot = java.io.File(uiRoot, "screens")
        val screenCrossImports = mutableListOf<String>()
        screensRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val ownScreenPkg = file.parentFile?.name.orEmpty()
                file.readLines().forEachIndexed { idx, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import com.moonsolstudios.kavvoro.ui.screens.")) {
                        val importedPkg = trimmed
                            .removePrefix("import com.moonsolstudios.kavvoro.ui.screens.")
                            .substringBefore(".")
                        if (importedPkg != ownScreenPkg) {
                            screenCrossImports += "${ownScreenPkg}/${file.name}:${idx + 1} -> $trimmed"
                        }
                    }
                }
            }
        assertTrue(
            "Screen packages must not cross-import other screen packages, found: $screenCrossImports",
            screenCrossImports.isEmpty()
        )

        val sharedUiLayers = listOf("render", "layout", "controller", "tutorial")
        val upwardScreenImports = mutableListOf<String>()
        sharedUiLayers.forEach { layer ->
            java.io.File(uiRoot, layer).walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    file.readLines().forEachIndexed { idx, line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("import com.moonsolstudios.kavvoro.ui.screens.")) {
                            upwardScreenImports += "$layer/${file.name}:${idx + 1} -> $trimmed"
                        }
                    }
                }
        }
        assertTrue(
            "Shared UI layers (render, layout, controller, tutorial) must never import from ui/screens/*, found: $upwardScreenImports",
            upwardScreenImports.isEmpty()
        )

        val chaosViewFile = java.io.File(uiRoot, "ChaosGameView.kt")
        val directRectFAllocations = chaosViewFile.readLines()
            .map { it.trim() }
            .filter { it.startsWith("private val ") && it.contains("= RectF()") }
        assertEquals(
            "ChaosGameView should only allocate its single internal scratch RectF; all screen hit-targets belong in their screen controllers",
            listOf("private val scratch = RectF()"),
            directRectFAllocations
        )
    }
}


