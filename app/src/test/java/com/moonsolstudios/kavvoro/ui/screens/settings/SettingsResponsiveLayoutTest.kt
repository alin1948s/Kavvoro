package com.moonsolstudios.kavvoro.ui.screens.settings

import com.moonsolstudios.kavvoro.model.LayoutMode
import com.moonsolstudios.kavvoro.model.SettingsTab
import com.moonsolstudios.kavvoro.ui.render.BrandTitleRenderer
import com.moonsolstudios.kavvoro.ui.render.UiTypography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsResponsiveLayoutTest {

    private data class ResolutionTestSpec(
        val name: String,
        val width: Float,
        val height: Float,
        val density: Float,
        val expectedMode: LayoutMode
    )

    private val testMatrix = listOf(
        ResolutionTestSpec("320x568 (Small Phone)", 320f, 568f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("320x720 (Small Phone)", 320f, 720f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("360x800 (Compact Phone)", 360f, 800f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("375x812 (Mockup Mobile)", 375f, 812f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("390x844", 390f, 844f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("412x915 (Compact Phone)", 412f, 915f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("480x854 (Compact Phone)", 480f, 854f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("600x1024 (Mobile boundary)", 600f, 1024f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("720x1280 (Tablet)", 720f, 1280f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("768x1024 (Mockup Tablet)", 768f, 1024f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("800x1280 (Medium)", 800f, 1280f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("1024x1366 (Tablet)", 1024f, 1366f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1080x2400 (High DPI Phone)", 1080f, 2400f, 2.75f, LayoutMode.COMPACT),
        ResolutionTestSpec("1200x1920 (Desktop-width tablet)", 1200f, 1920f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1280x720 (Desktop landscape)", 1280f, 720f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1366x768 (Desktop)", 1366f, 768f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1440x900 (Desktop)", 1440f, 900f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1536x864 (Desktop)", 1536f, 864f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1536x2048 (Tablet)", 1536f, 2048f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1600x2560 (Tablet)", 1600f, 2560f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1600x2560 @320dpi (Pixel Tablet)", 1600f, 2560f, 2f, LayoutMode.MEDIUM),
        ResolutionTestSpec("1920x1080 (Mockup Desktop)", 1920f, 1080f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("2560x1440 (Large desktop)", 2560f, 1440f, 1f, LayoutMode.TABLET)
    )

    private fun layoutFor(
        spec: ResolutionTestSpec,
        tab: SettingsTab = SettingsTab.SYSTEM,
        safeInsetTop: Float = 0f
    ): SettingsLayoutCalculator {
        val dp = spec.density
        val contentWidth = SettingsLayoutCalculator.contentWidth(spec.width, dp, spec.height)
        val left = (spec.width - contentWidth) * 0.5f
        val right = left + contentWidth
        val header = SettingsLayoutCalculator.computeHeader(spec.width, contentWidth, dp, spec.height, safeInsetTop)
        val calculator = SettingsLayoutCalculator()
        calculator.calculate(
            left = left,
            right = right,
            contentWidth = contentWidth,
            viewportTop = header.viewportTop,
            viewportBottom = spec.height - 24f * dp,
            settingsScroll = 0f,
            dp = dp,
            currentTab = tab,
            isLandscape = spec.width > spec.height,
            viewWidth = spec.width,
            viewHeight = spec.height
        )
        return calculator
    }

    @Test
    fun testAllResolutionsInSettingsLayout() {
        for (spec in testMatrix) {
            for (tab in SettingsTab.values()) {
                val calculator = layoutFor(spec, tab)
                val contentWidth = SettingsLayoutCalculator.contentWidth(spec.width, spec.density, spec.height)
                val left = (spec.width - contentWidth) * 0.5f
                val right = left + contentWidth

                val allTabs = listOf(calculator.tabAudio, calculator.tabGameplay, calculator.tabSystem, calculator.tabInfo)
                for ((tabIdx, tRect) in allTabs.withIndex()) {
                    assertTrue("Tab $tabIdx on ${spec.name} must have positive width", tRect.width() > 0f)
                    assertTrue("Tab $tabIdx on ${spec.name} must have positive height", tRect.height() > 0f)
                    assertTrue(
                        "Tab $tabIdx on ${spec.name} must stay in the content column",
                        tRect.left >= left - 0.5f && tRect.right <= right + 0.5f
                    )
                    assertTrue(
                        "Tab $tabIdx on ${spec.name} must be within the screen",
                        tRect.left >= -0.5f && tRect.right <= spec.width + 0.5f
                    )
                }

                when (tab) {
                    SettingsTab.AUDIO -> {
                        assertTrue("masterButton must be valid", calculator.masterButton.width() > 0f)
                        assertTrue("musicButton must be valid", calculator.musicButton.width() > 0f)
                        assertTrue("sfxButton must be valid", calculator.sfxButton.width() > 0f)
                        assertTrue("hapticToggle must be valid", calculator.hapticToggle.width() > 0f)
                        assertTrue("masterSlider must be valid", calculator.masterSlider.width() > 0f)
                        assertTrue("shakeToggle should be empty in audio tab", calculator.shakeToggle.isEmpty())
                    }
                    SettingsTab.GAMEPLAY -> {
                        assertTrue("shakeToggle must be valid", calculator.shakeToggle.width() > 0f)
                        assertTrue("performanceToggle must be valid", calculator.performanceToggle.width() > 0f)
                        assertTrue("masterButton should be empty in gameplay tab", calculator.masterButton.isEmpty())
                    }
                    SettingsTab.SYSTEM -> {
                        assertTrue("languageButton must be valid", calculator.languageButton.width() > 0f)
                        assertTrue("accountButton must be valid", calculator.accountButton.width() > 0f)
                        assertTrue("resetButton must be valid", calculator.resetButton.width() > 0f)
                        assertTrue("shakeToggle should be empty in system tab", calculator.shakeToggle.isEmpty())
                    }
                    SettingsTab.INFO -> {
                        assertTrue("aboutButton must be valid", calculator.aboutButton.width() > 0f)
                        assertTrue("privacyButton must be valid", calculator.privacyButton.width() > 0f)
                        assertTrue("termsButton must be valid", calculator.termsButton.width() > 0f)
                        assertTrue("dataDeletionButton must be valid", calculator.dataDeletionButton.width() > 0f)
                        assertTrue("resetButton should be empty in info tab", calculator.resetButton.isEmpty())
                    }
                }

                assertTrue("Content panel on ${spec.name} must have positive dimensions", calculator.contentPanel.width() > 0f && calculator.contentPanel.height() > 0f)
                assertTrue("Diagnostic card must stay hidden on ${spec.name}", calculator.telemetryCard.isEmpty())
                assertTrue("Back button on ${spec.name} must be valid", calculator.backButton.width() > 0f && calculator.backButton.height() > 0f)
                assertTrue("Back button on ${spec.name} must stay on-screen horizontally", calculator.backButton.left >= -0.5f && calculator.backButton.right <= spec.width + 0.5f)
                assertTrue("Scroll must be non-negative on ${spec.name}", calculator.scroll >= 0f && calculator.maxScroll >= 0f)
                assertFalse("Settings must not use a left rail on ${spec.name}", calculator.isRailMode)
                if (!calculator.stickyFooter && calculator.maxScroll > 0f) {
                    assertTrue(
                        "Back button on ${spec.name} must be reachable below content",
                        calculator.backButton.top >= calculator.contentPanel.bottom
                    )
                } else {
                    assertTrue(
                        "Back button on ${spec.name} must stay in the safe viewport",
                        calculator.backButton.bottom <= spec.height - 24f * spec.density + 1f
                    )
                }
                if (calculator.stickyFooter) {
                    assertTrue("Sticky back on ${spec.name} requires scroll", calculator.maxScroll > 0f)
                }
            }
        }
    }

    @Test
    fun mobileUsesTwoByTwoTabs() {
        val mobile = listOf(
            ResolutionTestSpec("360x800", 360f, 800f, 1f, LayoutMode.COMPACT),
            ResolutionTestSpec("375x812", 375f, 812f, 1f, LayoutMode.COMPACT),
            ResolutionTestSpec("412x915", 412f, 915f, 1f, LayoutMode.COMPACT),
            ResolutionTestSpec("480x854", 480f, 854f, 1f, LayoutMode.COMPACT),
            ResolutionTestSpec("600x1024", 600f, 1024f, 1f, LayoutMode.MEDIUM)
        )
        for (spec in mobile) {
            val calc = layoutFor(spec)
            assertTrue("${spec.name} should use 2x2 tabs", calc.isTwoByTwoTabs)
            assertTrue("SYSTEM tab should sit on the second row on ${spec.name}", calc.tabSystem.top >= calc.tabAudio.bottom - 1f)
            assertEquals("SUNET and JOC share the first row", calc.tabAudio.top, calc.tabGameplay.top, 0.5f)
            assertEquals("SISTEM and ABOUT share the second row", calc.tabSystem.top, calc.tabInfo.top, 0.5f)
        }
    }

    @Test
    fun audioSliderRailIsLargeAlignedAndClearOfPercentCapsule() {
        for (spec in testMatrix) {
            val calculator = layoutFor(spec, SettingsTab.AUDIO)
            val row = calculator.masterButton
            val slider = calculator.masterSlider
            val largestCapsuleLeft = calculator.masterButton.right - 44f * spec.density
            val activeThumbRight = slider.right + 8.4f * spec.density
            val clearance = largestCapsuleLeft - activeThumbRight
            val expectedTrackY = row.centerY() + minOf(20f * spec.density, row.height() * 0.24f)

            assertTrue(
                "100% thumb must stay clear of the value capsule on ${spec.name} (clearance=$clearance)",
                clearance >= 4f * spec.density
            )
            assertTrue(
                "Volume rail should remain comfortably wide on ${spec.name} (width=${slider.width()})",
                slider.width() >= 120f * spec.density
            )
            assertEquals("Volume rail should align with the lower control row on ${spec.name}", expectedTrackY, slider.centerY(), 0.5f)
            assertTrue(
                "Slider glow should stay within its card on ${spec.name}",
                slider.centerY() + 18f * spec.density <= row.bottom + 0.5f
            )
        }
    }

    @Test
    fun tabletAndDesktopKeepFourTabsInOneRow() {
        val wide = listOf(
            ResolutionTestSpec("720x1280", 720f, 1280f, 1f, LayoutMode.MEDIUM),
            ResolutionTestSpec("800x1280", 800f, 1280f, 1f, LayoutMode.MEDIUM),
            ResolutionTestSpec("1024x1366", 1024f, 1366f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("1080x2400", 1080f, 2400f, 1f, LayoutMode.COMPACT),
            ResolutionTestSpec("1200x1920", 1200f, 1920f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("1600x2560", 1600f, 2560f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("1366x768", 1366f, 768f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("1920x1080", 1920f, 1080f, 1f, LayoutMode.TABLET)
        )
        for (spec in wide) {
            val calc = layoutFor(spec)
            assertFalse("${spec.name} should keep a single tab row", calc.isTwoByTwoTabs)
            assertEquals(calc.tabAudio.top, calc.tabGameplay.top, 0.5f)
            assertEquals(calc.tabAudio.top, calc.tabSystem.top, 0.5f)
            assertEquals(calc.tabAudio.top, calc.tabInfo.top, 0.5f)
            val tabH = calc.tabAudio.height()
            assertTrue(
                "Tab height on ${spec.name} was $tabH",
                tabH >= 58f * spec.density - 0.5f && tabH <= 122f * spec.density + 0.5f
            )
        }
    }

    @Test
    fun contentFollowsTabsWithoutAGiantVerticalGap() {
        for (spec in testMatrix) {
            val calc = layoutFor(spec, SettingsTab.SYSTEM)
            val tabsBottom = maxOf(calc.tabAudio.bottom, calc.tabSystem.bottom)
            val gap = calc.contentPanel.top - tabsBottom
            val minGap = (if (spec.height / spec.density <= 640f && spec.height > spec.width) 12f else 15f) * spec.density
            val maxGap = 48f * spec.density
            assertTrue(
                "Gap between tabs and card on ${spec.name} was $gap",
                gap >= minGap - 0.5f && gap <= maxGap + 0.5f
            )
        }
    }

    @Test
    fun backButtonStaysCloseToContentWhenThePageFits() {
        val calc = layoutFor(
            ResolutionTestSpec("768x1024", 768f, 1024f, 1f, LayoutMode.MEDIUM),
            SettingsTab.SYSTEM
        )
        assertFalse(calc.stickyFooter)
        val gap = calc.backButton.top - calc.contentPanel.bottom
        assertTrue("Back gap was $gap", gap >= 21.5f && gap <= 40.5f)
        assertTrue(calc.backButton.width() <= calc.contentPanel.width() + 1f)
    }

    @Test
    fun mobileBackButtonIsFullWidth() {
        val calc = layoutFor(
            ResolutionTestSpec("375x812", 375f, 812f, 1f, LayoutMode.COMPACT),
            SettingsTab.SYSTEM
        )
        val contentWidth = SettingsLayoutCalculator.contentWidth(375f, 1f, 812f)
        assertEquals(contentWidth, calc.backButton.width(), 1f)
        assertTrue("Mobile back button must be at least 56dp", calc.backButton.height() >= 55.5f)
    }

    @Test
    fun desktopBackButtonIsCapped() {
        val calc = layoutFor(
            ResolutionTestSpec("1920x1080", 1920f, 1080f, 1f, LayoutMode.TABLET),
            SettingsTab.SYSTEM
        )
        assertEquals(520f, calc.backButton.width(), 0.5f)
        assertTrue(calc.backButton.height() >= 55.5f && calc.backButton.height() <= 70.5f)
    }

    @Test
    fun systemRowsAreCompactAndEntirelyClickable() {
        val calc = layoutFor(
            ResolutionTestSpec("1920x1080", 1920f, 1080f, 1f, LayoutMode.TABLET),
            SettingsTab.SYSTEM
        )
        assertTrue(calc.languageButton.height() >= 75.5f && calc.languageButton.height() <= 124.5f)
        assertEquals(calc.languageButton.height(), calc.accountButton.height(), 0.5f)
        assertEquals(calc.languageButton.height(), calc.resetButton.height(), 0.5f)
        assertEquals("Rows sit on the divider, not in isolated cards", calc.languageButton.bottom, calc.accountButton.top, 0.5f)
    }

    @Test
    fun shortPhoneRowsStayReadableAndDoNotOverlap() {
        val spec = ResolutionTestSpec("360x800", 360f, 800f, 1f, LayoutMode.COMPACT)
        val calc = layoutFor(spec, SettingsTab.SYSTEM)
        val minRow = 72f * spec.density
        assertTrue("language row was ${calc.languageButton.height()}, expected >= $minRow", calc.languageButton.height() >= minRow - 0.5f)
        assertTrue("account row was ${calc.accountButton.height()}, expected >= $minRow", calc.accountButton.height() >= minRow - 0.5f)
        assertTrue("reset row was ${calc.resetButton.height()}, expected >= $minRow", calc.resetButton.height() >= minRow - 0.5f)
        assertEquals(calc.languageButton.bottom, calc.accountButton.top, 0.5f)
        assertEquals(calc.accountButton.bottom, calc.resetButton.top, 0.5f)
        assertTrue(
            "Reset row must stay above the back button (reset=${calc.resetButton.bottom}, back=${calc.backButton.top})",
            calc.resetButton.bottom <= calc.backButton.top + 1f
        )
        val contentWidth = SettingsLayoutCalculator.contentWidth(360f, 1f, 800f)
        val left = (360f - contentWidth) * 0.5f
        val right = left + contentWidth
        assertTrue("Reset row must stay inside the card", calc.resetButton.left >= left - 0.5f && calc.resetButton.right <= right + 0.5f)
        assertTrue(calc.telemetryCard.isEmpty())
    }

    @Test
    fun fluidContentWidthCapsOnDesktopAndFillsPhones() {
        assertEquals(360f - 28f, SettingsLayoutCalculator.contentWidth(360f, 1f, 800f), 0.5f)
        assertEquals(1080f, SettingsLayoutCalculator.contentWidth(1920f, 1f, 1080f), 0.5f)
        assertEquals(1080f, SettingsLayoutCalculator.contentWidth(2560f, 1f, 1440f), 0.5f)
        assertEquals(1080f, SettingsLayoutCalculator.contentWidth(1600f, 1f, 2560f), 0.5f)
        assertEquals(1080f * 0.92f, SettingsLayoutCalculator.contentWidth(1080f, 1f, 2400f), 0.5f)
        val tabletShare = SettingsLayoutCalculator.contentWidth(1600f, 1f, 2560f) / 1600f
        assertTrue("1600 portrait container share was $tabletShare", tabletShare in 0.65f..0.75f)
        assertEquals(1600f * 0.92f, SettingsLayoutCalculator.contentWidth(1600f, 2f, 2560f), 0.5f)
        assertFalse(SettingsLayoutCalculator.useTwoByTwoTabs(1600f, 2560f, 2f))
        assertTrue(SettingsLayoutCalculator.useTwoByTwoTabs(1080f, 2400f, 2.75f))
    }

    @Test
    fun headerUsesClampSizedTitleWithoutOverlap() {
        val phone = SettingsLayoutCalculator.computeHeader(375f, 343f, 1f, 812f)
        val desktop = SettingsLayoutCalculator.computeHeader(1920f, 1080f, 1f, 1080f)
        assertEquals(UiTypography.SCREEN_TITLE_DP, phone.titleSize, 0.5f)
        assertEquals(UiTypography.SCREEN_TITLE_DP, desktop.titleSize, 0.5f)
        assertTrue(phone.subtitleSize >= 10f)
        assertTrue(desktop.subtitleSize >= 10f)
        assertTrue("Title must sit below the brand", phone.titleTop >= phone.brandTop + phone.brandHeight)
        assertTrue("Title must sit below the Home brand motto", phone.titleTop >= phone.brandMottoBottom)
        assertTrue("Title must sit below the brand on desktop", desktop.titleTop >= desktop.brandTop + desktop.brandHeight)
        assertTrue("Subtitle follows the title", phone.subtitleTop >= phone.titleBaseline)
        assertTrue("Diamond follows the subtitle", phone.dividerY >= phone.subtitleBaseline)
        assertTrue("Tabs follow the hero", phone.viewportTop >= phone.dividerY)
        assertFalse(phone.profileStacked)
        assertFalse(desktop.profileStacked)
        assertTrue(phone.stackedHeader)
        assertFalse(desktop.stackedHeader)
        assertTrue("Profile stays on the right", phone.profileRight > phone.profileLeft)
        assertTrue("Desktop profile stays on the right", desktop.profileLeft >= 1920f * 0.5f)
        assertTrue("Logo must match Home header top", phone.brandTop >= 12f)
        val homeBrand = BrandTitleRenderer.placement(
            375f,
            812f,
            1f,
            brandAspect = BrandTitleRenderer.HOME_LOGO_ASPECT
        )
        assertEquals(homeBrand.top, phone.brandTop, 0.5f)
        assertEquals(homeBrand.height(), phone.brandHeight, 0.5f)
        assertEquals(homeBrand.width(), phone.brandMaxWidth, 0.5f)
        assertEquals(homeBrand.left, phone.brandLeft, 0.5f)
        val insetPhone = SettingsLayoutCalculator.computeHeader(375f, 343f, 1f, 812f, safeInsetTop = 24f)
        assertEquals("Status-bar inset must not move the Home wordmark", homeBrand.top, insetPhone.brandTop, 0.5f)
        assertEquals(homeBrand.left, insetPhone.brandLeft, 0.5f)
        assertEquals(homeBrand.width(), insetPhone.brandMaxWidth, 0.5f)
        assertEquals(homeBrand.height(), insetPhone.brandHeight, 0.5f)
    }

    @Test
    fun settingsBrandMatchesHomeOnEveryCaptureViewport() {
        val viewports = listOf(
            Triple(360f, 800f, 1f),
            Triple(412f, 915f, 1f),
            Triple(480f, 854f, 1f),
            Triple(600f, 1024f, 1f),
            Triple(720f, 1280f, 2f),
            Triple(800f, 1280f, 1f),
            Triple(1024f, 1366f, 1f),
            Triple(1080f, 2400f, 420f / 160f),
            Triple(1200f, 1920f, 1.5f),
            Triple(1536f, 2048f, 1.5f),
            Triple(1600f, 2560f, 2f)
        )
        for ((width, height, density) in viewports) {
            val home = BrandTitleRenderer.placement(
                width,
                height,
                density,
                brandAspect = BrandTitleRenderer.HOME_LOGO_ASPECT
            )
            val contentWidth = SettingsLayoutCalculator.contentWidth(width, density, height)
            val header = SettingsLayoutCalculator.computeHeader(width, contentWidth, density, height)
            val compactBrandScale = if (height / density <= 640f && height > width) 0.82f else 1f
            assertEquals("brand left ${width}x${height}@${density}", home.left, header.brandLeft, 0.5f)
            assertEquals("brand top ${width}x${height}@${density}", home.top, header.brandTop, 0.5f)
            assertEquals("brand width ${width}x${height}@${density}", home.width() * compactBrandScale, header.brandMaxWidth, 0.5f)
            assertEquals("brand height ${width}x${height}@${density}", home.height() * compactBrandScale, header.brandHeight, 0.5f)
            assertEquals("motto visibility ${width}x${height}@${density}", height / density > 640f, header.brandMottoVisible)
            assertEquals(
                "Home logo aspect ${width}x${height}@${density}",
                BrandTitleRenderer.HOME_LOGO_ASPECT,
                header.brandMaxWidth / header.brandHeight,
                0.01f
            )
        }
    }

    @Test
    fun breakpointMatchesCssZones() {
        assertEquals(SettingsBreakpoint.MOBILE, SettingsLayoutCalculator.breakpointFor(360f, 1f, 800f))
        assertEquals(SettingsBreakpoint.MOBILE, SettingsLayoutCalculator.breakpointFor(480f, 1f, 854f))
        assertEquals(SettingsBreakpoint.TABLET, SettingsLayoutCalculator.breakpointFor(720f, 1f, 1280f))
        assertEquals(SettingsBreakpoint.TABLET, SettingsLayoutCalculator.breakpointFor(1080f, 1f, 2400f))
        assertEquals(SettingsBreakpoint.TABLET, SettingsLayoutCalculator.breakpointFor(1024f, 1f, 1366f))
        assertEquals(SettingsBreakpoint.DESKTOP, SettingsLayoutCalculator.breakpointFor(1366f, 1f, 768f))
        assertEquals(SettingsBreakpoint.DESKTOP, SettingsLayoutCalculator.breakpointFor(1920f, 1f, 1080f))
        assertEquals(SettingsBreakpoint.MOBILE, SettingsLayoutCalculator.breakpointFor(1080f, 2.75f, 2400f))
    }

    @Test
    fun rowIconSizesGrowWithViewportNotBreakpointBuckets() {
        assertEquals(42f, SettingsLayoutCalculator.rowIconSize(360f, 1f, 800f), 0.5f)
        assertEquals(54f, SettingsLayoutCalculator.rowIconSize(1080f, 1f, 2400f), 0.5f)
        assertEquals(54f, SettingsLayoutCalculator.rowIconSize(1600f, 1f, 2560f), 0.5f)
        assertEquals(50f * 2f, SettingsLayoutCalculator.rowIconSize(1600f, 2f, 2560f), 0.5f)
        assertEquals(30f, SettingsLayoutCalculator.chevronSlot(1600f, 1f, 2560f), 0.5f)
        assertEquals(28f * 2f, SettingsLayoutCalculator.chevronSlot(1600f, 2f, 2560f), 0.5f)
    }

    @Test
    fun tallPortraitDoesNotUseMiniaturizedDesktopChrome() {
        val phone = layoutFor(ResolutionTestSpec("1080x2400", 1080f, 2400f, 1f, LayoutMode.COMPACT))
        assertFalse(phone.isTwoByTwoTabs)
        assertEquals(96f, phone.languageButton.height(), 0.5f)
        assertEquals(1080f * 0.92f, SettingsLayoutCalculator.contentWidth(1080f, 1f, 2400f), 0.5f)
        val tablet = layoutFor(ResolutionTestSpec("1600x2560", 1600f, 2560f, 1f, LayoutMode.TABLET))
        assertFalse(tablet.isTwoByTwoTabs)
        assertEquals(96f, tablet.languageButton.height(), 0.5f)
        assertEquals(1080f, SettingsLayoutCalculator.contentWidth(1600f, 1f, 2560f), 0.5f)
    }

    @Test
    fun xlPortraitBodyAndIconsStepUpWithSharedHeadingScale() {
        val phone = SettingsLayoutCalculator.scale(360f, 800f, 1f)
        val mid = SettingsLayoutCalculator.scale(800f, 1280f, 1f)
        val tablet1024 = SettingsLayoutCalculator.scale(1024f, 1366f, 1f)
        val tablet1200 = SettingsLayoutCalculator.scale(1200f, 1920f, 1f)
        val tablet1600 = SettingsLayoutCalculator.scale(1600f, 2560f, 1f)
        assertEquals(UiTypography.SCREEN_TITLE_DP, phone.titleSize, 0.5f)
        assertEquals(UiTypography.SCREEN_TITLE_DP, mid.titleSize, 0.5f)
        assertEquals(UiTypography.SCREEN_TITLE_DP, tablet1024.titleSize, 0.5f)
        assertEquals(UiTypography.SCREEN_TITLE_DP, tablet1200.titleSize, 0.5f)
        assertEquals(UiTypography.SCREEN_TITLE_DP, tablet1600.titleSize, 0.5f)
        assertEquals(16f, phone.bodySize, 0.5f)
        assertEquals(19f, mid.bodySize, 0.5f)
        assertEquals(20f, tablet1024.bodySize, 0.5f)
        assertEquals(tablet1024.rowHeight, tablet1600.rowHeight, 0.5f)
        assertTrue(tablet1600.xlPortrait)
        assertFalse(phone.xlPortrait)
        assertFalse(mid.xlPortrait)
    }

    @Test
    fun equivalentLogicalViewportsShareTheSameSettingsStructure() {
        val group800 = listOf(
            ResolutionTestSpec("800x1280@160", 800f, 1280f, 1f, LayoutMode.MEDIUM),
            ResolutionTestSpec("1200x1920@240", 1200f, 1920f, 1.5f, LayoutMode.MEDIUM),
            ResolutionTestSpec("1600x2560@320", 1600f, 2560f, 2f, LayoutMode.MEDIUM)
        )
        assertLogicalEquivalence(group800)

        val group412 = listOf(
            ResolutionTestSpec("412x915@160", 412f, 915f, 1f, LayoutMode.COMPACT),
            ResolutionTestSpec("1080x2400@420", 1080f, 2400f, 420f / 160f, LayoutMode.COMPACT)
        )
        assertLogicalEquivalence(group412)

        val group1024 = listOf(
            ResolutionTestSpec("1024x1366@160", 1024f, 1366f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("1536x2048@240", 1536f, 2048f, 1.5f, LayoutMode.TABLET)
        )
        assertLogicalEquivalence(group1024)
    }

    @Test
    fun hdPhoneAt320DpiIsCompact360NotTablet() {
        val spec = ResolutionTestSpec("720x1280@320", 720f, 1280f, 2f, LayoutMode.COMPACT)
        val calc = layoutFor(spec)
        assertEquals(360f, SettingsLayoutCalculator.widthDp(720f, 2f), 0.5f)
        assertEquals(640f, SettingsLayoutCalculator.heightDp(1280f, 2f), 0.5f)
        assertTrue("720x1280@320 must stay 2x2 phone tabs, not a tablet row", calc.isTwoByTwoTabs)
        val tokens = SettingsLayoutCalculator.scale(720f, 1280f, 2f)
        assertFalse(tokens.largePortrait)
        assertFalse(tokens.xlPortrait)
        assertEquals(UiTypography.COMPACT_SCREEN_TITLE_DP * 2f, tokens.titleSize, 0.5f)
        assertTrue(tokens.compactHeight)
        assertEquals(54f * 2f, tokens.tabHeight, 0.5f)
        assertEquals(70f * 2f, tokens.rowHeight, 0.5f)
        assertEquals(52f * 2f, tokens.backHeight, 0.5f)
    }

    private fun assertLogicalEquivalence(group: List<ResolutionTestSpec>) {
        val baseline = group.first()
        val baseCalc = layoutFor(baseline)
        val baseD = baseline.density
        for (spec in group.drop(1)) {
            val calc = layoutFor(spec)
            val d = spec.density
            assertEquals("${spec.name} tab grid vs ${baseline.name}", baseCalc.isTwoByTwoTabs, calc.isTwoByTwoTabs)
            assertEquals(
                "${spec.name} content width dp vs ${baseline.name}",
                SettingsLayoutCalculator.contentWidth(baseline.width, baseD, baseline.height) / baseD,
                SettingsLayoutCalculator.contentWidth(spec.width, d, spec.height) / d,
                1.5f
            )
            assertEquals(
                "${spec.name} title dp vs ${baseline.name}",
                SettingsLayoutCalculator.scale(baseline.width, baseline.height, baseD).titleSize / baseD,
                SettingsLayoutCalculator.scale(spec.width, spec.height, d).titleSize / d,
                1.5f
            )
            assertEquals(
                "${spec.name} row dp vs ${baseline.name}",
                baseCalc.languageButton.height() / baseD,
                calc.languageButton.height() / d,
                1.5f
            )
            assertEquals(
                "${spec.name} tab dp vs ${baseline.name}",
                baseCalc.tabAudio.height() / baseD,
                calc.tabAudio.height() / d,
                1.5f
            )
            assertEquals(
                "${spec.name} icon dp vs ${baseline.name}",
                SettingsLayoutCalculator.rowIconSize(baseline.width, baseD, baseline.height) / baseD,
                SettingsLayoutCalculator.rowIconSize(spec.width, d, spec.height) / d,
                0.5f
            )
            assertEquals(
                "${spec.name} card left fraction vs ${baseline.name}",
                baseCalc.contentPanel.left / baseline.width,
                calc.contentPanel.left / spec.width,
                0.02f
            )
        }
    }

    @Test
    fun headingsUseSharedScaleWhileContentTypographyFollowsLogicalWidth() {
        val phone = SettingsLayoutCalculator.scale(360f, 800f, 1f)
        val tablet600 = SettingsLayoutCalculator.scale(600f, 1024f, 1f)
        val tablet800 = SettingsLayoutCalculator.scale(800f, 1280f, 1f)
        val tablet1024 = SettingsLayoutCalculator.scale(1024f, 1366f, 1f)
        val same800HiDpi = SettingsLayoutCalculator.scale(1600f, 2560f, 2f)

        assertEquals(UiTypography.SCREEN_TITLE_DP, phone.titleSize, 0.5f)
        assertEquals(11f, phone.tabTextSize, 0.5f)
        assertEquals(16f, phone.bodySize, 0.5f)
        assertEquals(12f, phone.smallSize, 0.5f)
        assertEquals(42f, phone.iconSize, 0.5f)
        assertEquals(30f, phone.tabIconSize, 0.5f)
        assertTrue("Phone tab icons should visually lead the labels", phone.tabIconSize > phone.tabTextSize * 2f)

        assertEquals(UiTypography.SCREEN_TITLE_DP, tablet600.titleSize, 0.5f)
        assertEquals(12f, tablet600.tabTextSize, 0.5f)
        assertEquals(18f, tablet600.bodySize, 0.5f)
        assertEquals(14f, tablet600.smallSize, 0.5f)
        assertEquals(46f, tablet600.iconSize, 0.5f)
        assertEquals(32f, tablet600.tabIconSize, 0.5f)

        assertEquals(UiTypography.SCREEN_TITLE_DP, tablet800.titleSize, 0.5f)
        assertEquals(13f, tablet800.tabTextSize, 0.5f)
        assertEquals(19f, tablet800.bodySize, 0.5f)
        assertEquals(15f, tablet800.smallSize, 0.5f)
        assertEquals(50f, tablet800.iconSize, 0.5f)
        assertEquals(34f, tablet800.tabIconSize, 0.5f)

        assertEquals(UiTypography.SCREEN_TITLE_DP, tablet1024.titleSize, 0.5f)
        assertEquals(14f, tablet1024.tabTextSize, 0.5f)
        assertEquals(20f, tablet1024.bodySize, 0.5f)
        assertEquals(15.5f, tablet1024.smallSize, 0.5f)
        assertEquals(54f, tablet1024.iconSize, 0.5f)
        assertEquals(36f, tablet1024.tabIconSize, 0.5f)

        assertEquals(tablet800.titleSize * 2f, same800HiDpi.titleSize, 0.5f)
        assertEquals(tablet800.bodySize * 2f, same800HiDpi.bodySize, 0.5f)
        assertEquals(tablet800.iconSize * 2f, same800HiDpi.iconSize, 0.5f)
        assertEquals(tablet800.tabTextSize * 2f, same800HiDpi.tabTextSize, 0.5f)
        assertEquals(tablet800.tabIconSize * 2f, same800HiDpi.tabIconSize, 0.5f)
    }

    @Test
    fun compactPhoneScaleStaysCloseToCurrent360Layout() {
        val tokens = SettingsLayoutCalculator.scale(360f, 800f, 1f)
        assertFalse(tokens.largePortrait)
        assertEquals(UiTypography.SCREEN_TITLE_DP, tokens.titleSize, 0.5f)
        assertEquals(74f, tokens.rowHeight, 0.5f)
        assertEquals(58f, tokens.tabHeight, 0.5f)
        assertEquals(42f, tokens.iconSize, 0.5f)
        assertFalse(tokens.compactHeight)
    }

    @Test
    fun shortPhoneUsesCompactHeightAndKeepsBackVisible() {
        val normal = layoutFor(ResolutionTestSpec("360x800", 360f, 800f, 1f, LayoutMode.COMPACT))
        val short = layoutFor(ResolutionTestSpec("360x640", 360f, 640f, 1f, LayoutMode.COMPACT))
        val normalTokens = SettingsLayoutCalculator.scale(360f, 800f, 1f)
        val shortTokens = SettingsLayoutCalculator.scale(360f, 640f, 1f)

        assertFalse(normalTokens.compactHeight)
        assertTrue(shortTokens.compactHeight)
        assertEquals(UiTypography.SCREEN_TITLE_DP, normalTokens.titleSize, 0.5f)
        assertEquals(UiTypography.COMPACT_SCREEN_TITLE_DP, shortTokens.titleSize, 0.5f)
        assertEquals(normalTokens.bodySize, shortTokens.bodySize, 0.5f)
        assertEquals(normalTokens.tabTextSize, shortTokens.tabTextSize, 0.5f)
        assertEquals(normalTokens.iconSize, shortTokens.iconSize, 0.5f)
        assertEquals(normalTokens.smallSize, shortTokens.smallSize, 0.5f)
        assertEquals(54f, shortTokens.tabHeight, 0.5f)
        assertEquals(70f, shortTokens.rowHeight, 0.5f)
        assertEquals(52f, shortTokens.backHeight, 0.5f)
        assertEquals(12f, shortTokens.tabsToCard, 0.5f)
        assertEquals(28f, normalTokens.tabsToCard, 0.5f)
        assertEquals(32f, SettingsLayoutCalculator.scale(600f, 1024f, 1f).tabsToCard, 0.5f)
        assertEquals(36f, SettingsLayoutCalculator.scale(800f, 1280f, 1f).tabsToCard, 0.5f)
        assertEquals(40f, SettingsLayoutCalculator.scale(1024f, 1366f, 1f).tabsToCard, 0.5f)

        assertTrue(short.isTwoByTwoTabs)
        assertEquals(short.languageButton.bottom, short.accountButton.top, 0.5f)
        assertEquals(short.accountButton.bottom, short.resetButton.top, 0.5f)
        assertTrue(short.languageButton.height() >= 65.5f)
        assertTrue(short.resetButton.bottom <= short.backButton.top + 1f)

        val viewportBottom = 640f - 24f
        assertTrue(
            "Back must stay fully visible at 360x640 (bottom=${short.backButton.bottom}, viewport=$viewportBottom)",
            short.backButton.bottom <= viewportBottom - 3f
        )
        val inset = layoutFor(
            ResolutionTestSpec("360x640-inset", 360f, 640f, 1f, LayoutMode.COMPACT),
            SettingsTab.SYSTEM,
            safeInsetTop = 24f
        )
        assertTrue(
            "Back must stay visible even with a 24dp status bar (bottom=${inset.backButton.bottom})",
            inset.backButton.bottom <= viewportBottom - 3f
        )
        assertTrue(short.backButton.top >= short.contentPanel.bottom)
        assertTrue(short.tabAudio.top >= 0f)
        assertTrue(short.maxScroll >= 0f)
    }

    @Test
    fun landscapeTabletDoesNotPutBackOnTopOfTheCard() {
        val viewports = listOf(
            ResolutionTestSpec("2560x1600 @320 (Pixel Tablet)", 2560f, 1600f, 2f, LayoutMode.TABLET),
            ResolutionTestSpec("1920x1080", 1920f, 1080f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("1366x768", 1366f, 768f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("1280x720", 1280f, 720f, 1f, LayoutMode.TABLET)
        )
        for (spec in viewports) {
            val tokens = SettingsLayoutCalculator.scale(spec.width, spec.height, spec.density)
            val landscapeShort = spec.height / spec.density < 900f
            assertEquals(
                "compactVertical on ${spec.name}",
                landscapeShort,
                tokens.compactVertical
            )
            assertEquals(
                UiTypography.screenTitleDp(tokens.compactVertical) * spec.density,
                tokens.titleSize,
                0.5f
            )
            for (tab in SettingsTab.values()) {
                val calc = layoutFor(spec, tab)
                val gap = calc.backButton.top - calc.contentPanel.bottom
                assertTrue(
                    "Back overlaps the card on ${spec.name} tab=$tab (gap=$gap, sticky=${calc.stickyFooter})",
                    gap >= 8f * spec.density - 0.5f
                )
                val viewportBottom = spec.height - 24f * spec.density
                assertTrue(
                    "Back cropped on ${spec.name} tab=$tab",
                    calc.backButton.bottom <= viewportBottom - 3f * spec.density + 0.5f
                )
                assertFalse("${spec.name} must keep a single tab row", calc.isTwoByTwoTabs)
            }
        }
    }

    @Test
    fun compact500dpHeightActivatesScrollWithoutOverlap() {
        val viewports = listOf(
            ResolutionTestSpec("800x500 (Landscape Phone/Compact)", 800f, 500f, 1f, LayoutMode.MEDIUM),
            ResolutionTestSpec("1000x500 (Short Tablet Split)", 1000f, 500f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("360x500 (Ultra-compact)", 360f, 500f, 1f, LayoutMode.COMPACT)
        )
        for (spec in viewports) {
            for (tab in SettingsTab.values()) {
                val calc = layoutFor(spec, tab)
                val gap = calc.backButton.top - calc.contentPanel.bottom
                assertTrue(
                    "Back button must not overlap card on ${spec.name} tab=$tab (gap=$gap, sticky=${calc.stickyFooter})",
                    gap >= 8f * spec.density - 0.5f
                )
                assertTrue(
                    "Card bottom must sit entirely above Back on ${spec.name} tab=$tab",
                    calc.contentPanel.bottom <= calc.backButton.top
                )
                assertTrue(
                    "Scroll must be activated on ${spec.name} tab=$tab (maxScroll=${calc.maxScroll})",
                    calc.maxScroll > 0f
                )
                assertFalse(
                    "Sticky footer must be false when content overflows to prevent overlap on ${spec.name} tab=$tab",
                    calc.stickyFooter
                )
            }
        }
    }

    @Test
    fun thumbnailAndLetterboxModeNeverOverlapsBackButton() {
        val letterboxSpecs = listOf(
            ResolutionTestSpec("500x800 @320 (Pixel Tablet Portrait Thumbnail)", 500f, 800f, 2f, LayoutMode.COMPACT),
            ResolutionTestSpec("600x800 @320 (Tablet Pillarbox)", 600f, 800f, 2f, LayoutMode.MEDIUM),
            ResolutionTestSpec("720x960 @320 (Square-ish Thumbnail)", 720f, 960f, 2f, LayoutMode.MEDIUM)
        )
        for (spec in letterboxSpecs) {
            for (tab in SettingsTab.values()) {
                val calc = layoutFor(spec, tab)
                val gap = calc.backButton.top - calc.contentPanel.bottom
                assertTrue(
                    "Thumbnail mode Back button overlap on ${spec.name} tab=$tab (gap=$gap)",
                    gap >= 8f * spec.density - 0.5f
                )
                assertTrue(
                    "Thumbnail mode card bottom exceeds Back top on ${spec.name} tab=$tab",
                    calc.contentPanel.bottom <= calc.backButton.top
                )
            }
        }
    }
}
