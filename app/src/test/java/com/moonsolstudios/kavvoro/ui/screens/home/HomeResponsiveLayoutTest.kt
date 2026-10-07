package com.moonsolstudios.kavvoro.ui.screens.home

import com.moonsolstudios.kavvoro.i18n.HomeCopy
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import com.moonsolstudios.kavvoro.model.LayoutMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class HomeResponsiveLayoutTest {

    private data class ResolutionTestSpec(
        val name: String,
        val width: Float,
        val height: Float,
        val density: Float,
        val expectedMode: LayoutMode
    )

    private val portraitMatrix = listOf(
        ResolutionTestSpec("320x640 (Compact Phone)", 320f, 640f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("360x800 (Compact Phone)", 360f, 800f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("393x852 (Compact Phone)", 393f, 852f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("412x915 (Compact Phone)", 412f, 915f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("480x854 (Compact Phone)", 480f, 854f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("600x1024 (Medium)", 600f, 1024f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("720x1280 (Medium)", 720f, 1280f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("800x1280 (Medium)", 800f, 1280f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("1024x1366 (Tablet)", 1024f, 1366f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1080x2400 (High DPI Phone)", 1080f, 2400f, 2.75f, LayoutMode.COMPACT),
        ResolutionTestSpec("1200x1920 (Tablet)", 1200f, 1920f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1536x2048 (Tablet)", 1536f, 2048f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1600x2560 (Tablet)", 1600f, 2560f, 1f, LayoutMode.TABLET)
    )

    @Test
    fun testPortraitRegressionMatrix() {
        val calculator = HomeLayoutCalculator()
        for (spec in portraitMatrix) {
            calculator.calculate(
                width = spec.width,
                height = spec.height,
                displayDensity = spec.density
            )

            // 1. Strict Portrait Isolation: landscapeClass MUST be null
            assertNull("landscapeClass must be null in portrait on ${spec.name}", calculator.landscapeClass)

            // 2. Verify LayoutMode
            assertEquals(
                "Resolution ${spec.name} layoutMode mismatch",
                spec.expectedMode,
                calculator.layoutMode
            )

            // 3. Verify contentRect is valid and centered
            val contentRect = calculator.contentRect
            assertTrue("contentRect width must be positive on ${spec.name}", contentRect.width() > 0f)
            assertTrue("contentRect width must not exceed screen width on ${spec.name}", contentRect.width() <= spec.width)
            val expectedLeft = (spec.width - contentRect.width()) / 2f
            assertTrue(
                "contentRect left must be centered on ${spec.name}",
                abs(contentRect.left - expectedLeft) < 0.1f
            )

            // 4. Verify Header bounds
            val brandRect = calculator.brandRect
            assertTrue("brandRect width must be positive on ${spec.name}", brandRect.width() > 0f)
            assertTrue("brandRect height must be positive on ${spec.name}", brandRect.height() > 0f)
            assertTrue("brandRect must be within content bounds on ${spec.name}", brandRect.left >= contentRect.left - 0.1f)
            assertTrue("brandRect must not collide with settings button on ${spec.name}", brandRect.right <= calculator.settingsButtonRect.left + 0.1f)
            assertTrue("Settings button must be within content bounds on ${spec.name}", calculator.settingsButtonRect.right <= contentRect.right + 0.1f)
            assertTrue("Sound button must be within content bounds on ${spec.name}", calculator.soundButtonRect.right <= contentRect.right + 0.1f)

            // 5. Verify Stats Cards
            for (i in 0 until 4) {
                val card = calculator.statCardRects[i]
                assertTrue("Stat card $i must have positive width on ${spec.name}", card.width() > 0f)
                assertTrue("Stat card $i must be within contentRect on ${spec.name}", card.left >= contentRect.left - 0.1f && card.right <= contentRect.right + 0.1f)
            }

            // 6. Verify Hero, Portal, Platform, and Character bounds
            val portalRect = calculator.portalRect
            val platformRect = calculator.platformRect
            val charRect = calculator.characterRect

            assertTrue("Portal width must be positive on ${spec.name}", portalRect.width() > 0f)
            assertTrue("Portal must be horizontally centered on ${spec.name}", abs(portalRect.centerX() - contentRect.centerX()) < 0.1f)
            assertTrue("Platform width must be positive on ${spec.name}", platformRect.width() > 0f)
            assertTrue("Platform must be horizontally centered on ${spec.name}", abs(platformRect.centerX() - contentRect.centerX()) < 0.1f)
            assertTrue("Character must have positive dimensions on ${spec.name}", charRect.width() > 0f && charRect.height() > 0f)

            val coins = calculator.coinsChipRect
            val readyBadge = calculator.coinsReadyBadgeRect
            assertTrue("Coins READY badge must sit below the balance on ${spec.name}", readyBadge.top >= coins.bottom)
            assertTrue("Coins READY badge must stay within the viewport on ${spec.name}", readyBadge.left >= 0f && readyBadge.right <= calculator.screenWidth)
            assertTrue("Coins tap target must include its READY badge on ${spec.name}", calculator.coinsChipTouchRect.bottom >= readyBadge.bottom)

            // 7. Verify Play CTA
            val playRect = calculator.playCtaRect
            assertTrue("Play CTA must be positive on ${spec.name}", playRect.width() > 0f && playRect.height() > 0f)
            assertTrue("Play CTA must fit within contentRect on ${spec.name}", playRect.left >= contentRect.left - 0.1f && playRect.right <= contentRect.right + 0.1f)

            // 8. Verify Lower Navigation Cards (3 cards side-by-side: Skins, Missions, Leaderboard)
            val leaderboards = calculator.leaderboardCardRect
            val skins = calculator.skinsCardRect
            val missions = calculator.missionsCardRect

            assertTrue("Leaderboards card must be positive on ${spec.name}", leaderboards.width() > 0f)
            assertTrue("Skins card must be positive on ${spec.name}", skins.width() > 0f)
            assertTrue("Missions card must be positive on ${spec.name}", missions.width() > 0f)
            assertTrue("Skins must be to the left of Missions on ${spec.name}", skins.right < missions.left)
            assertTrue("Missions must be to the left of Leaderboard on ${spec.name}", missions.right < leaderboards.left)
            assertEquals("All 3 cards should align vertically on ${spec.name}", skins.top, missions.top, 0.1f)
            assertEquals("All 3 cards should align vertically on ${spec.name}", missions.top, leaderboards.top, 0.1f)

            // 9. Verify Portrait Banner Card & Footer (MUST NOT BE EMPTY IN PORTRAIT)
            assertFalse("Banner card must not be empty in portrait on ${spec.name}", calculator.bannerCardRect.isEmpty())
            assertTrue("Banner card must have positive width on ${spec.name}", calculator.bannerCardRect.width() > 0f)
            assertTrue("Banner card must have positive height on ${spec.name}", calculator.bannerCardRect.height() > 0f)
            assertTrue("Banner must be below 3 navigation cards on ${spec.name}", calculator.bannerCardRect.top >= skins.bottom - 0.1f)
            assertTrue("Banner must fit within contentRect on ${spec.name}", calculator.bannerCardRect.left >= contentRect.left - 0.1f && calculator.bannerCardRect.right <= contentRect.right + 0.1f)

            assertFalse("Footer rect must not be empty in portrait on ${spec.name}", calculator.footerRect.isEmpty())
            assertTrue("Footer must have positive width on ${spec.name}", calculator.footerRect.width() > 0f)
        }
    }

    @Test
    fun testReferencePhoneHeaderKeepsStatsAlongsideLogo() {
        val calculator = HomeLayoutCalculator()
        calculator.calculate(1080f, 2400f, 2.625f)

        assertTrue("Reference phone stats should share the logo row", calculator.statsRect.top < calculator.brandRect.bottom)
        assertTrue("Reference phone stats should fit before the settings button", calculator.statsRect.right < calculator.settingsButtonRect.left)

        calculator.calculate(320f, 640f, 1f)
        assertTrue("Narrow phone stats should degrade below the logo row", calculator.statsRect.top >= calculator.headerRect.bottom)
    }

    @Test
    fun testLandscapeBreakpointsClassification() {
        val calculator = HomeLayoutCalculator()

        // 1. Compact: availableHeightDp < 500f
        calculator.calculate(width = 800f, height = 480f, displayDensity = 1.0f)
        assertEquals(LandscapeClass.COMPACT, calculator.landscapeClass)

        // 2. Compact: availableWidthDp < 840f
        calculator.calculate(width = 800f, height = 600f, displayDensity = 1.0f)
        assertEquals(LandscapeClass.COMPACT, calculator.landscapeClass)

        // 3. Standard: Pixel Tablet 1280x800 (deckWidthDp < 600f)
        calculator.calculate(width = 1280f, height = 800f, displayDensity = 1.0f)
        assertEquals(LandscapeClass.STANDARD, calculator.landscapeClass)

        // 4. Standard: High DPI Tablet 2560x1600 (density 2.0 -> 1280x800 dp)
        calculator.calculate(width = 2560f, height = 1600f, displayDensity = 2.0f)
        assertEquals(LandscapeClass.STANDARD, calculator.landscapeClass)

        // 5. Wide: Ultra-wide display 2560x1080 (density 1.0 -> deckWidthDp >= 600f)
        calculator.calculate(width = 2560f, height = 1080f, displayDensity = 1.0f)
        assertEquals(LandscapeClass.WIDE, calculator.landscapeClass)
    }

    @Test
    fun testLandscapeStandardLayoutStructureAndSizing() {
        val calculator = HomeLayoutCalculator()
        val landscapeSpecs = listOf(
            Triple(1280f, 800f, 1.0f),  // Standard Tablet
            Triple(2560f, 1600f, 2.0f)  // High-DPI Tablet
        )

        for ((width, height, density) in landscapeSpecs) {
            calculator.calculate(width, height, density)

            assertNotNull("landscapeClass must not be null in landscape", calculator.landscapeClass)
            assertEquals(LandscapeClass.STANDARD, calculator.landscapeClass)

            // A. Usable Width Occupancy (90-94% of safe screen width)
            val availableWidth = calculator.bodyRect.width()
            val occupiedWidth = calculator.heroStageRect.width() + (calculator.navigationDeckRect.left - calculator.heroStageRect.right) + calculator.navigationDeckRect.width()
            val widthUtilization = occupiedWidth / availableWidth
            assertTrue("Width utilization must be >= 90%, was $widthUtilization", widthUtilization >= 0.90f)

            // B. Asymmetric Spatial Allocation: Hero ~62%, Deck ~38%
            val totalStageWidth = calculator.heroStageRect.width() + calculator.navigationDeckRect.width()
            val heroFraction = calculator.heroStageRect.width() / totalStageWidth
            assertTrue("Hero stage should occupy approximately 62% of usable stage width, was $heroFraction", abs(heroFraction - 0.62f) < 0.05f)

            // C. Brainball Mascot: must occupy 44-58% of usable body height
            val bodyH = calculator.bodyRect.height()
            val mascotH = calculator.heroMascotRect.height()
            val mascotRatio = mascotH / bodyH
            assertTrue("Mascot height ratio must be between 44% and 58% of body height, was $mascotRatio", mascotRatio in 0.42f..0.60f)

            // D. Separation between Platform and PLAY CTA (20dp Hero Assembly)
            val separation = calculator.playCtaRect.top - calculator.platformRect.bottom
            assertTrue("Separation between platform and play CTA must be 20dp, was $separation", abs(separation - calculator.dp(20f)) < 0.2f)

            // E. PLAY CTA Dimensions (76-98dp)
            val playH = calculator.playCtaRect.height()
            assertTrue("Play CTA height must be >= 76dp, was $playH", playH >= calculator.dp(76f) - 0.1f)
            assertTrue("Play CTA height must be <= 98dp, was $playH", playH <= calculator.dp(98f) + 0.1f)
            assertTrue(
                "Play CTA width must follow the approved 76% / 620dp cap",
                calculator.playCtaRect.width() <= minOf(calculator.heroStageRect.width() * 0.76f, calculator.dp(620f)) + 0.1f
            )
            assertEquals("Play CTA must be centered in the hero", calculator.heroStageRect.centerX(), calculator.playCtaRect.centerX(), 0.1f)

            // F. 2+1 Navigation Deck Structure
            // Row 1: Skins + Missions side-by-side
            assertEquals("Skins and Missions must align vertically in Row 1", calculator.skinsCardRect.top, calculator.missionsCardRect.top, 0.1f)
            assertTrue("Skins must be to the left of Missions in Row 1", calculator.skinsCardRect.right < calculator.missionsCardRect.left)
            // Row 2: Leaderboard spans the deck below Row 1
            assertTrue("Leaderboard must be below Row 1 cards", calculator.leaderboardCardRect.top >= calculator.skinsCardRect.bottom)
            assertTrue("Leaderboard must span wider than individual Row 1 cards", calculator.leaderboardCardRect.width() > calculator.skinsCardRect.width())
            assertEquals("Leaderboard left should align with deck left", calculator.navigationDeckRect.left, calculator.leaderboardCardRect.left, 0.1f)
            assertEquals("Leaderboard right should align with deck right", calculator.navigationDeckRect.right, calculator.leaderboardCardRect.right, 0.1f)

            // G. Omitted Elements in Landscape (Zero clutter)
            assertTrue("Banner card must be empty in landscape", calculator.bannerCardRect.isEmpty())
            assertTrue("Footer rect must be empty in landscape", calculator.footerRect.isEmpty())
            assertTrue("Rift status rect must be empty in landscape", calculator.riftStatusRect.isEmpty())
        }
    }

    @Test
    fun testLandscapeInteractiveNonOverlapRule() {
        val calculator = HomeLayoutCalculator()
        calculator.calculate(1280f, 800f, 1.0f)

        val play = calculator.playCtaRect
        val deck = calculator.navigationDeckRect
        val skins = calculator.skinsCardRect
        val missions = calculator.missionsCardRect
        val lead = calculator.leaderboardCardRect

        // 1. Hero Stage CTA must not touch Navigation Deck
        assertTrue("Play CTA right must be <= Deck left", play.right <= deck.left)

        // 2. Play CTA must not intersect any navigation cards
        assertFalse("Play CTA must not intersect Skins card", play.intersects(skins))
        assertFalse("Play CTA must not intersect Missions card", play.intersects(missions))
        assertFalse("Play CTA must not intersect Leaderboard card", play.intersects(lead))

        // 3. Navigation cards must not intersect each other
        assertFalse("Skins card must not intersect Missions card", skins.intersects(missions))
        assertFalse("Skins card must not intersect Leaderboard card", skins.intersects(lead))
        assertFalse("Missions card must not intersect Leaderboard card", missions.intersects(lead))
    }

    @Test
    fun testLandscapeWideLayoutStructure() {
        val calculator = HomeLayoutCalculator()
        // 2560x1080 at density 1.0 gives deckWidthDp >= 600f -> LandscapeClass.WIDE
        calculator.calculate(2560f, 1080f, 1.0f)

        assertEquals(LandscapeClass.WIDE, calculator.landscapeClass)

        // In LandscapeClass.WIDE, 3 cards are in 1 row side-by-side
        val skins = calculator.skinsCardRect
        val missions = calculator.missionsCardRect
        val lead = calculator.leaderboardCardRect

        assertEquals("Skins and Missions must align vertically in Wide", skins.top, missions.top, 0.1f)
        assertEquals("Missions and Leaderboard must align vertically in Wide", missions.top, lead.top, 0.1f)
        assertTrue("Skins must be left of Missions", skins.right < missions.left)
        assertTrue("Missions must be left of Leaderboard", missions.right < lead.left)
    }

    @Test
    fun testSciFiCtaButtonUsesProceduralRendering() {
        // The approved Home pack requires a runtime-rendered gradient CTA.
        assertFalse("SciFiCtaButtonRenderer must not use a raster chassis", SciFiCtaButtonRenderer.usesRasterBackground)
    }

    @Test
    fun testOrientationTransitionPreservesState() {
        val calculator = HomeLayoutCalculator()

        // 1. Initial calculate in Landscape (1920x1200 at 1.5 density = 1280x800 dp)
        calculator.calculate(1920f, 1200f, 1.5f)
        assertEquals(LandscapeClass.STANDARD, calculator.landscapeClass)
        val landscapeHeroWidth = calculator.heroStageRect.width()
        val landscapePlayHeight = calculator.playCtaRect.height()
        assertTrue("Banner card must be empty in landscape", calculator.bannerCardRect.isEmpty())

        // 2. Rotate to Portrait (1200x1920 at 1.5 density)
        calculator.calculate(1200f, 1920f, 1.5f)
        assertNull("landscapeClass must be null in portrait", calculator.landscapeClass)
        assertFalse("Banner card must be active in portrait", calculator.bannerCardRect.isEmpty())
        assertTrue("Footer rect must be active in portrait", calculator.footerRect.width() > 0f)

        // 3. Rotate back to Landscape (1920x1200 at 1.5 density)
        calculator.calculate(1920f, 1200f, 1.5f)
        assertEquals(LandscapeClass.STANDARD, calculator.landscapeClass)
        assertEquals("Hero width must be perfectly restored", landscapeHeroWidth, calculator.heroStageRect.width(), 0.1f)
        assertEquals("Play CTA height must be perfectly restored", landscapePlayHeight, calculator.playCtaRect.height(), 0.1f)
        assertTrue("Banner card must be empty in landscape again", calculator.bannerCardRect.isEmpty())
    }

    @Test
    fun testLandscapeOccupancyChecklist() {
        val calculator = HomeLayoutCalculator()
        calculator.calculate(1280f, 800f, 1.0f)

        val availableWidth = calculator.bodyRect.width()
        val heroRect = calculator.heroStageRect
        val deckRect = calculator.navigationDeckRect
        val mascotRect = calculator.heroMascotRect
        val playCtaRect = calculator.playCtaRect
        val bodyRect = calculator.bodyRect

        assertTrue("Hero width must be >= 50% of available width", heroRect.width() >= availableWidth * 0.50f)
        assertTrue("Deck right must be >= 90% of screen width", deckRect.right >= calculator.contentRect.left + availableWidth * 0.90f)

        val occupiedWidth = maxOf(heroRect.right, deckRect.right) - minOf(heroRect.left, deckRect.left)
        assertTrue("Occupied width must be >= 90% of available width", occupiedWidth >= availableWidth * 0.90f)

        assertTrue("Mascot height must be >= 40% of body height", mascotRect.height() >= bodyRect.height() * 0.40f)
        assertTrue("Play CTA width must be >= 68% of hero width", playCtaRect.width() >= heroRect.width() * 0.68f)
    }

    @Test
    fun testPlayLocalizationContractCopies() {
        val testLanguages = listOf(
            KavvoroLanguage.EN to ("PLAY NOW" to "PLAY"),
            KavvoroLanguage.RO to ("JOACĂ ACUM" to "JOACĂ"),
            KavvoroLanguage.DE to ("JETZT SPIELEN" to "SPIELEN"),
            KavvoroLanguage.FR to ("JOUER" to "JOUER"),
            KavvoroLanguage.ES to ("JUGAR AHORA" to "JUGAR")
        )

        for ((lang, expected) in testLanguages) {
            val title = HomeCopy.ctaPlay(lang)
            val shortTitle = HomeCopy.ctaPlayShort(lang)
            val subtitle = HomeCopy.ctaSubtitle(lang)

            assertEquals("Title mismatch for $lang", expected.first, title)
            assertEquals("Short title mismatch for $lang", expected.second, shortTitle)
            assertTrue("Subtitle for $lang must not be empty", subtitle.isNotBlank())
        }

        assertFalse("SciFiCtaButtonRenderer must not use a raster chassis", SciFiCtaButtonRenderer.usesRasterBackground)
    }

    @Test
    fun testPortraitPlayAndMascotParitySpecs() {
        val calculator = HomeLayoutCalculator()
        for (spec in portraitMatrix) {
            calculator.calculate(spec.width, spec.height, spec.density)

            val playH = calculator.playCtaRect.height()
            val playW = calculator.playCtaRect.width()
            val ratio = playW / playH
            assertEquals("Play CTA aspect ratio must be ~3.25 in portrait on ${spec.name}", 3.25f, ratio, 0.05f)

            // Celestial decor rects must be populated
            assertFalse("planetBlueRect must not be empty on ${spec.name}", calculator.planetBlueRect.isEmpty())
            assertFalse("planetPinkRect must not be empty on ${spec.name}", calculator.planetPinkRect.isEmpty())
        }
    }

    @Test
    fun testLandscapeMascotAndPlayParitySpecs() {
        val calculator = HomeLayoutCalculator()
        val specs = listOf(
            Triple(1280f, 800f, 1.0f),
            Triple(1920f, 1080f, 1.5f),
            Triple(2560f, 1600f, 2.0f)
        )
        for ((w, h, d) in specs) {
            calculator.calculate(w, h, d)

            val bodyH = calculator.bodyRect.height()
            val mascotH = calculator.heroMascotRect.height()
            val mascotRatio = mascotH / bodyH
            assertTrue("Mascot ratio $mascotRatio must be in [0.44, 0.58]", mascotRatio in 0.43f..0.59f)

            val playH = calculator.playCtaRect.height()
            val minPlayH = calculator.dp(76f) - 0.1f
            val maxPlayH = calculator.dp(98f) + 0.1f
            assertTrue("Play CTA height $playH must be >= 76dp ($minPlayH)", playH >= minPlayH)
            assertTrue("Play CTA height $playH must be <= 98dp ($maxPlayH)", playH <= maxPlayH)

            // Celestial decor rects
            assertFalse("planetBlueRect must not be empty in landscape", calculator.planetBlueRect.isEmpty())
            assertFalse("planetPinkRect must not be empty in landscape", calculator.planetPinkRect.isEmpty())
            assertFalse("asteroidLeftRect must not be empty in landscape", calculator.asteroidLeftRect.isEmpty())
            assertFalse("asteroidRightRect must not be empty in landscape", calculator.asteroidRightRect.isEmpty())
        }
    }

    @Test
    fun testNavCardLocalizationContract() {
        val languages = listOf(
            KavvoroLanguage.EN,
            KavvoroLanguage.RO,
            KavvoroLanguage.ES,
            KavvoroLanguage.FR,
            KavvoroLanguage.DE,
            KavvoroLanguage.IT,
            KavvoroLanguage.PT,
            KavvoroLanguage.RU
        )
        for (lang in languages) {
            assertTrue("Skins title must not be empty for $lang", HomeCopy.skinsTitle(lang).isNotBlank())
            assertTrue("Skins subtitle must not be empty for $lang", HomeCopy.skinsSubtitle(lang).isNotBlank())
            assertTrue("Missions title must not be empty for $lang", HomeCopy.missionsTitle(lang).isNotBlank())
            assertTrue("Missions subtitle must not be empty for $lang", HomeCopy.missionsSubtitle(lang).isNotBlank())
            assertTrue("Leaderboard title must not be empty for $lang", HomeCopy.leaderboardTitle(lang).isNotBlank())
            assertTrue("Leaderboard subtitle must not be empty for $lang", HomeCopy.leaderboardSubtitle(lang).isNotBlank())
        }
    }

    @Test
    fun testCompactLandscapePhoneLayoutSafety() {
        val calculator = HomeLayoutCalculator()
        val phoneLandscapeSpecs = listOf(
            Triple(800f, 360f, 1.0f),
            Triple(854f, 480f, 1.0f),
            Triple(915f, 412f, 1.0f),
            Triple(640f, 360f, 1.0f)
        )
        for ((w, h, d) in phoneLandscapeSpecs) {
            calculator.calculate(w, h, d)
            assertEquals(LandscapeClass.COMPACT, calculator.landscapeClass)

            val body = calculator.bodyRect
            val play = calculator.playCtaRect
            val mascot = calculator.heroMascotRect
            val platform = calculator.platformRect
            val skins = calculator.skinsCardRect
            val missions = calculator.missionsCardRect
            val leaderboard = calculator.leaderboardCardRect

            assertTrue("Play button must not overflow body bottom on ${w}x${h}", play.bottom <= body.bottom + 0.5f)
            assertTrue("Leaderboard must not overflow body bottom on ${w}x${h}", leaderboard.bottom <= body.bottom + 0.5f)
            assertTrue("Mascot bottom should reach into platform disc on ${w}x${h}", mascot.bottom >= platform.top - 0.1f)
            assertTrue("Platform bottom must be above or at play top on ${w}x${h}", platform.bottom <= play.top + 0.1f)
            assertFalse("Skins card must not intersect Leaderboard on ${w}x${h}", skins.intersects(leaderboard))
            assertFalse("Missions card must not intersect Leaderboard on ${w}x${h}", missions.intersects(leaderboard))
        }
    }

    @Test
    fun testPortraitPortalDiscAspectAndEmergence() {
        val calculator = HomeLayoutCalculator()
        for (spec in portraitMatrix) {
            calculator.calculate(spec.width, spec.height, spec.density)

            val platform = calculator.platformRect
            val aspect = platform.width() / platform.height()
            val expectedAspect = 5.1f
            assertEquals("Portal disc aspect ratio must match ~5.1:1 on ${spec.name}", expectedAspect, aspect, 0.1f)

            val mascot = calculator.heroMascotRect
            val play = calculator.playCtaRect

            assertTrue("Brainball bottom must be below platformTop (submerged) on ${spec.name}", mascot.bottom > platform.top)
            assertTrue("Brainball bottom must be above or at platformBottom on ${spec.name}", mascot.bottom <= platform.bottom + 0.1f)
            assertTrue("Platform must be above PLAY CTA on ${spec.name}", platform.bottom < play.top)
        }
    }

    @Test
    fun testBannerCardLocalizationContract() {
        val languages = listOf(
            KavvoroLanguage.EN,
            KavvoroLanguage.RO,
            KavvoroLanguage.ES,
            KavvoroLanguage.FR,
            KavvoroLanguage.DE,
            KavvoroLanguage.IT,
            KavvoroLanguage.PT,
            KavvoroLanguage.RU
        )
        for (lang in languages) {
            assertTrue("Banner title must not be empty for $lang", HomeCopy.bannerTitle(lang).isNotBlank())
            assertTrue("Banner subtitle must not be empty for $lang", HomeCopy.bannerSubtitle(lang).isNotBlank())
        }
    }
}
