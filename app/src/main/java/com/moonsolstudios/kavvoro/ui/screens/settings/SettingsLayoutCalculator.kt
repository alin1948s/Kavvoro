package com.moonsolstudios.kavvoro.ui.screens.settings

import android.graphics.RectF
import com.moonsolstudios.kavvoro.model.SettingsTab
import com.moonsolstudios.kavvoro.ui.render.BrandTitleRenderer
import com.moonsolstudios.kavvoro.ui.render.LayoutRect
import com.moonsolstudios.kavvoro.ui.render.UiTypography

enum class SettingsBreakpoint {
    MOBILE,
    TABLET,
    DESKTOP
}

data class SettingsHeaderMetrics(
    val padTop: Float,
    val padHorizontal: Float,
    val brandLeft: Float,
    val brandTop: Float,
    val brandHeight: Float,
    val brandMaxWidth: Float,
    val brandMottoBaseline: Float,
    val brandMottoSize: Float,
    val brandMottoBottom: Float,
    val brandMottoVisible: Boolean,
    val profileLeft: Float,
    val profileTop: Float,
    val profileRight: Float,
    val profileBottom: Float,
    val profileStacked: Boolean,
    val titleTop: Float,
    val titleBaseline: Float,
    val titleSize: Float,
    val subtitleTop: Float,
    val subtitleBaseline: Float,
    val subtitleSize: Float,
    val dividerY: Float,
    val viewportTop: Float,
    val stackedHeader: Boolean
)

class SettingsLayoutCalculator {
    var isRailMode: Boolean = false
        private set
    var isTwoByTwoTabs: Boolean = false
        private set
    var stickyFooter: Boolean = false
        private set
    var breakpoint: SettingsBreakpoint = SettingsBreakpoint.MOBILE
        private set
    var activeTab: SettingsTab = SettingsTab.AUDIO
    var scroll: Float = 0f
        private set
    var maxScroll: Float = 0f
        private set

    val tabAudio = LayoutRect()
    val tabGameplay = LayoutRect()
    val tabSystem = LayoutRect()
    val tabInfo = LayoutRect()

    val navRailPanel = LayoutRect()
    val contentPanel = LayoutRect()
    val telemetryCard = LayoutRect()

    val audioPanel = LayoutRect()
    val gameplayPanel = LayoutRect()
    val systemPanel = LayoutRect()
    val legalPanel = LayoutRect()
    val dangerPanel = LayoutRect()

    val masterButton = LayoutRect()
    val musicButton = LayoutRect()
    val sfxButton = LayoutRect()
    val hapticToggle = LayoutRect()
    val shakeToggle = LayoutRect()
    val performanceToggle = LayoutRect()
    val languageButton = LayoutRect()
    val accountButton = LayoutRect()
    val privacyButton = LayoutRect()
    val termsButton = LayoutRect()
    val dataDeletionButton = LayoutRect()
    val aboutButton = LayoutRect()
    val resetButton = LayoutRect()
    val backButton = LayoutRect()

    val masterSlider = LayoutRect()
    val musicSlider = LayoutRect()
    val sfxSlider = LayoutRect()

    fun calculate(
        left: Float,
        right: Float,
        contentWidth: Float,
        viewportTop: Float,
        viewportBottom: Float,
        settingsScroll: Float,
        dp: Float,
        currentTab: SettingsTab = activeTab,
        isLandscape: Boolean = false,
        viewWidth: Float = contentWidth + left * 2f,
        viewHeight: Float = 0f
    ) {
        this.activeTab = currentTab
        this.scroll = settingsScroll
        val resolvedHeight = if (viewHeight > 0f) {
            viewHeight
        } else if (isLandscape) {
            viewWidth * 9f / 16f
        } else {
            viewWidth * 16f / 9f
        }
        this.breakpoint = breakpointFor(viewWidth, dp, resolvedHeight)
        this.isRailMode = false
        this.stickyFooter = false

        masterButton.setEmpty()
        musicButton.setEmpty()
        sfxButton.setEmpty()
        hapticToggle.setEmpty()
        shakeToggle.setEmpty()
        performanceToggle.setEmpty()
        languageButton.setEmpty()
        accountButton.setEmpty()
        privacyButton.setEmpty()
        termsButton.setEmpty()
        dataDeletionButton.setEmpty()
        aboutButton.setEmpty()
        resetButton.setEmpty()
        masterSlider.setEmpty()
        musicSlider.setEmpty()
        sfxSlider.setEmpty()
        telemetryCard.setEmpty()
        audioPanel.setEmpty()
        gameplayPanel.setEmpty()
        systemPanel.setEmpty()
        legalPanel.setEmpty()
        dangerPanel.setEmpty()
        navRailPanel.setEmpty()

        calculateFlowLayout(
            left = left,
            right = right,
            contentWidth = contentWidth,
            viewportTop = viewportTop,
            viewportBottom = viewportBottom,
            settingsScroll = settingsScroll,
            dp = dp,
            viewWidth = viewWidth,
            viewHeight = resolvedHeight
        )
    }

    private fun calculateFlowLayout(
        left: Float,
        right: Float,
        contentWidth: Float,
        viewportTop: Float,
        viewportBottom: Float,
        settingsScroll: Float,
        dp: Float,
        viewWidth: Float,
        viewHeight: Float
    ) {
        isTwoByTwoTabs = useTwoByTwoTabs(viewWidth, viewHeight, dp)

        val tokens = scale(viewWidth, viewHeight, dp)
        val tabGap = tokens.tabGap
        val tabRowGap = tokens.tabRowGap
        val tabHeight = tokens.tabHeight
        val tabsToCardGap = tokens.tabsToCard
        val cardToBackGap = tokens.cardToBack

        val scrollY = settingsScroll
        val tabTop = viewportTop - scrollY
        if (isTwoByTwoTabs) {
            val colW = (contentWidth - tabGap) * 0.5f
            val row2Top = tabTop + tabHeight + tabRowGap
            tabAudio.set(left, tabTop, left + colW, tabTop + tabHeight)
            tabGameplay.set(left + colW + tabGap, tabTop, right, tabTop + tabHeight)
            tabSystem.set(left, row2Top, left + colW, row2Top + tabHeight)
            tabInfo.set(left + colW + tabGap, row2Top, right, row2Top + tabHeight)
        } else {
            val colW = (contentWidth - tabGap * 3f) / 4f
            var x = left
            tabAudio.set(x, tabTop, x + colW, tabTop + tabHeight)
            x += colW + tabGap
            tabGameplay.set(x, tabTop, x + colW, tabTop + tabHeight)
            x += colW + tabGap
            tabSystem.set(x, tabTop, x + colW, tabTop + tabHeight)
            x += colW + tabGap
            tabInfo.set(x, tabTop, right, tabTop + tabHeight)
        }

        val tabsBottom = if (isTwoByTwoTabs) tabSystem.bottom else tabAudio.bottom
        navRailPanel.set(left, tabTop, right, tabsBottom)

        val cardPadX = cardPaddingX(viewWidth, viewHeight, dp)
        val cardPadTop = cardPaddingY(viewWidth, viewHeight, dp)
        val cardPadBottom = cardPadTop
        val sectionHeaderHeight = tokens.sectionTitleSize + tokens.sectionHeaderGap
        val rowHeight = tokens.rowHeight
        val itemCount = itemCountFor(activeTab)
        val deckTop = tabsBottom + tabsToCardGap
        val deckHeight = cardPadTop + sectionHeaderHeight + itemCount * rowHeight + cardPadBottom
        val deckBottom = deckTop + deckHeight
        contentPanel.set(left, deckTop, right, deckBottom)

        val itemsTop = deckTop + cardPadTop + sectionHeaderHeight
        layoutTabItems(
            left = left + cardPadX,
            right = right - cardPadX,
            startY = itemsTop,
            rowHeight = rowHeight,
            dp = dp
        )

        val backHeight = backMinHeight(viewWidth, viewHeight, dp)
        val backWidth = backButtonWidth(contentWidth, dp, breakpoint, viewWidth, viewHeight)
        val backLeft = left + (contentWidth - backWidth) * 0.5f
        // drawBackHomeButton paints a 2.5dp glow below the rect; keep that fully on-canvas.
        val glowPad = BACK_GLOW_PAD_DP * dp
        val bottomLimit = viewportBottom - glowPad
        val naturalBackTop = contentPanel.bottom + cardToBackGap
        val pinnedTop = (bottomLimit - backHeight).coerceAtLeast(viewportTop)
        if (naturalBackTop + backHeight <= bottomLimit) {
            stickyFooter = false
            backButton.set(backLeft, naturalBackTop, backLeft + backWidth, naturalBackTop + backHeight)
        } else if (contentPanel.bottom + 8f * dp <= pinnedTop) {
            stickyFooter = true
            backButton.set(backLeft, pinnedTop, backLeft + backWidth, pinnedTop + backHeight)
        } else {
            stickyFooter = false
            backButton.set(backLeft, naturalBackTop, backLeft + backWidth, naturalBackTop + backHeight)
        }

        val flowBottom = contentPanel.bottom + cardToBackGap + backHeight
        val contentBottom = maxOf(backButton.bottom, flowBottom) + 24f * dp
        val viewportHeight = (viewportBottom - viewportTop).coerceAtLeast(1f)
        maxScroll = (contentBottom + scrollY - viewportTop - viewportHeight).coerceAtLeast(0f)
        scroll = settingsScroll.coerceIn(0f, maxScroll)
    }

    fun copyTo(
        tabAudio: RectF,
        tabGameplay: RectF,
        tabSystem: RectF,
        tabInfo: RectF,
        contentPanel: RectF,
        masterButton: RectF,
        musicButton: RectF,
        sfxButton: RectF,
        hapticToggle: RectF,
        shakeToggle: RectF,
        performanceToggle: RectF,
        languageButton: RectF,
        accountButton: RectF,
        privacyButton: RectF,
        termsButton: RectF,
        dataDeletionButton: RectF,
        aboutButton: RectF,
        resetButton: RectF,
        backButton: RectF,
        masterSlider: RectF,
        musicSlider: RectF,
        sfxSlider: RectF
    ) {
        this.tabAudio.toRectF(tabAudio)
        this.tabGameplay.toRectF(tabGameplay)
        this.tabSystem.toRectF(tabSystem)
        this.tabInfo.toRectF(tabInfo)
        this.contentPanel.toRectF(contentPanel)
        this.masterButton.toRectF(masterButton)
        this.musicButton.toRectF(musicButton)
        this.sfxButton.toRectF(sfxButton)
        this.hapticToggle.toRectF(hapticToggle)
        this.shakeToggle.toRectF(shakeToggle)
        this.performanceToggle.toRectF(performanceToggle)
        this.languageButton.toRectF(languageButton)
        this.accountButton.toRectF(accountButton)
        this.privacyButton.toRectF(privacyButton)
        this.termsButton.toRectF(termsButton)
        this.dataDeletionButton.toRectF(dataDeletionButton)
        this.aboutButton.toRectF(aboutButton)
        this.resetButton.toRectF(resetButton)
        this.backButton.toRectF(backButton)
        this.masterSlider.toRectF(masterSlider)
        this.musicSlider.toRectF(musicSlider)
        this.sfxSlider.toRectF(sfxSlider)
    }

    private fun layoutTabItems(
        left: Float,
        right: Float,
        startY: Float,
        rowHeight: Float,
        dp: Float
    ): Float {
        var cursor = startY
        when (activeTab) {
            SettingsTab.AUDIO -> {
                masterButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                musicButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                sfxButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                hapticToggle.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                audioPanel.set(left, masterButton.top, right, hapticToggle.bottom)
            }
            SettingsTab.GAMEPLAY -> {
                shakeToggle.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                performanceToggle.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                gameplayPanel.set(left, shakeToggle.top, right, performanceToggle.bottom)
            }
            SettingsTab.SYSTEM -> {
                languageButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                accountButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                resetButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                systemPanel.set(left, languageButton.top, right, resetButton.bottom)
            }
            SettingsTab.INFO -> {
                aboutButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                privacyButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                termsButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                dataDeletionButton.set(left, cursor, right, cursor + rowHeight)
                cursor += rowHeight
                legalPanel.set(left, aboutButton.top, right, dataDeletionButton.bottom)
            }
        }

        layoutSlider(masterButton, masterSlider, dp)
        layoutSlider(musicButton, musicSlider, dp)
        layoutSlider(sfxButton, sfxSlider, dp)
        return cursor
    }

    private fun layoutSlider(buttonRect: LayoutRect, sliderRect: LayoutRect, dp: Float) {
        if (buttonRect.isEmpty()) {
            sliderRect.setEmpty()
            return
        }
        val cardWidth = buttonRect.width()
        val capsuleAndGap = 44f * dp
        val thumbClearance = 13f * dp
        val sliderRight = buttonRect.right - capsuleAndGap - thumbClearance
        val targetSliderWidth = (cardWidth * 0.62f).coerceIn(150f * dp, 420f * dp)
        val availableWidth = (sliderRight - buttonRect.left).coerceAtLeast(1f)
        val sliderWidth = minOf(targetSliderWidth, availableWidth)
        val sliderLeft = sliderRight - sliderWidth
        val trackY = buttonRect.centerY() + minOf(20f * dp, buttonRect.height() * 0.24f)
        sliderRect.set(sliderLeft, trackY, sliderRight, trackY)
    }

    companion object {
        const val COMPACT_MAX_DP = 480f
        const val TWO_BY_TWO_MAX_DP = 600f
        const val MOBILE_MAX_DP = 600f
        const val TABLET_MAX_DP = 1024f
        const val LARGE_MIN_DP = 900f
        const val COMPACT_HEIGHT_DP = 700f
        const val CONTENT_MAX_DP = 1080f
        const val PORTRAIT_CONTENT_MAX_DP = 1080f
        const val XL_PORTRAIT_MIN_DP = 900f
        const val CHEVRON_SLOT_DP = 24f
        const val BACK_GLOW_PAD_DP = 4f

        data class Scale(
            val titleSize: Float,
            val bodySize: Float,
            val smallSize: Float,
            val screenSubtitleSize: Float,
            val sectionTitleSize: Float,
            val tabTextSize: Float,
            val backTextSize: Float,
            val profileTextSize: Float,
            val iconSize: Float,
            val tabIconSize: Float,
            val chevronSize: Float,
            val rowHeight: Float,
            val tabHeight: Float,
            val tabGap: Float,
            val tabRowGap: Float,
            val padTop: Float,
            val headerToTitle: Float,
            val titleToTabs: Float,
            val titleToSubtitle: Float,
            val subtitleToLine: Float,
            val tabsToCard: Float,
            val cardToBack: Float,
            val cardPadY: Float,
            val backHeight: Float,
            val sectionHeaderGap: Float,
            val compactHeight: Boolean,
            val compactVertical: Boolean,
            val largePortrait: Boolean,
            val xlPortrait: Boolean
        )

        fun clamp(min: Float, preferred: Float, max: Float): Float = preferred.coerceIn(min, max)

        fun cssClamp(minPx: Float, vwFraction: Float, maxPx: Float, viewWidth: Float, dp: Float): Float {
            val preferredDp = widthDp(viewWidth, dp) * vwFraction
            return preferredDp.coerceIn(minPx, maxPx) * dp
        }

        fun cssClampVmin(
            minPx: Float,
            vminFraction: Float,
            maxPx: Float,
            viewWidth: Float,
            viewHeight: Float,
            density: Float
        ): Float {
            val vminDp = minOf(widthDp(viewWidth, density), heightDp(viewHeight, density))
            return (vminDp * vminFraction).coerceIn(minPx, maxPx) * density
        }

        fun heightDp(viewHeight: Float, density: Float): Float =
            if (density > 0f) viewHeight / density else viewHeight

        fun isPortrait(viewWidth: Float, viewHeight: Float): Boolean =
            viewHeight > viewWidth

        fun widthDp(viewWidth: Float, dp: Float): Float =
            if (dp > 0f) viewWidth / dp else viewWidth

        fun isCompactHeight(viewWidth: Float, viewHeight: Float, density: Float): Boolean =
            heightDp(viewHeight, density) < COMPACT_HEIGHT_DP

        fun lerp(from: Float, to: Float, t: Float): Float =
            from + (to - from) * t.coerceIn(0f, 1f)

        fun typeByWidth(
            widthDp: Float,
            phone: Float,
            compactTablet: Float,
            midTablet: Float,
            large: Float
        ): Float = when {
            widthDp <= COMPACT_MAX_DP -> phone
            widthDp < 600f -> lerp(phone, compactTablet, (widthDp - COMPACT_MAX_DP) / (600f - COMPACT_MAX_DP))
            widthDp < 800f -> compactTablet
            widthDp < 1024f -> midTablet
            else -> large
        }

        fun iconByWidth(
            widthDp: Float,
            phone: Float,
            compactTablet: Float,
            midTablet: Float,
            large: Float
        ): Float = typeByWidth(widthDp, phone, compactTablet, midTablet, large)

        fun scale(viewWidth: Float, viewHeight: Float, dp: Float): Scale {
            val width = widthDp(viewWidth, dp)
            val compactHeight = isCompactHeight(viewWidth, viewHeight, dp)
            val portrait = isPortrait(viewWidth, viewHeight)
            val xlPortrait = portrait && width >= XL_PORTRAIT_MIN_DP
            val largePortrait = portrait && width >= MOBILE_MAX_DP

            val height = heightDp(viewHeight, dp)
            val compactVertical = compactHeight || (!portrait && height <= 900f)
            val titleSize = UiTypography.screenTitleDp(compactVertical) * dp
            val bodySize = typeByWidth(width, 16f, 18f, 19f, 20f) * dp
            val smallSize = typeByWidth(width, 12f, 14f, 15f, 15.5f) * dp
            val screenSubtitleSize = UiTypography.screenSubtitleDp(compactVertical) * dp
            val sectionTitleSize = UiTypography.sectionTitleDp(compactVertical) * dp
            val tabTextSize = typeByWidth(width, 11f, 12f, 13f, 14f) * dp
            val backTextSize = typeByWidth(width, 14f, 15f, 16f, 17f) * dp
            val profileTextSize = typeByWidth(width, 13f, 14f, 15f, 15f) * dp
            val iconSize = iconByWidth(width, 42f, 46f, 50f, 54f) * dp
            val tabIconSize = iconByWidth(width, 30f, 32f, 34f, 36f) * dp
            val chevronSize = iconByWidth(width, 24f, 26f, 28f, 30f) * dp

            // Landscape tablets (Pixel Tablet 1280x800dp, 1366x768, …) are wide
            // but short. Keep width-based fonts/icons; compress only vertical
            // rhythm so Back cannot sit on the settings card.
            val tallBoost = if (compactVertical || height <= 900f) {
                0f
            } else {
                lerp(8f, 32f, ((height - 900f) / 500f).coerceIn(0f, 1f))
            }
            var padTop = (if (compactVertical) {
                16f
            } else {
                typeByWidth(width, 36f, 40f, 44f, 48f) + tallBoost
            }) * dp
            val shortPhone = portrait && height <= 640f
            val headerToTitle = when {
                shortPhone -> 12f * dp
                compactVertical -> 18f * dp
                else -> (typeByWidth(width, 24f, 28f, 30f, 32f) + tallBoost * 0.4f) * dp
            }
            val titleToTabs = when {
                shortPhone -> 14f * dp
                compactVertical -> 18f * dp
                else -> typeByWidth(width, 20f, 22f, 22f, 24f) * dp
            }
            var titleToSubtitle = (if (compactVertical) 8f else 10f) * dp
            var subtitleToLine = (if (compactVertical) 8f else 10f) * dp
            var tabHeight = when {
                compactHeight || height < 750f -> 54f
                compactVertical -> 58f
                else -> typeByWidth(width, 58f, 64f, 68f, 72f)
            } * dp
            var tabGap = 8f * dp
            var tabRowGap = (if (compactVertical) 6f else 8f) * dp
            var tabsToCard = (if (shortPhone) 12f else if (compactVertical) 18f else typeByWidth(width, 28f, 32f, 36f, 40f)) * dp
            var cardPadY = (if (shortPhone) 10f else if (compactVertical) 12f else typeByWidth(width, 16f, 18f, 18f, 20f)) * dp
            var rowHeight = (if (compactVertical) 70f else typeByWidth(width, 74f, 86f, 90f, 96f)) * dp
            var cardToBack = (if (shortPhone) 12f else if (compactVertical) 16f else typeByWidth(width, 20f, 24f, 24f, 28f)) * dp
            var backHeight = (if (compactVertical) 52f else typeByWidth(width, 56f, 56f, 56f, 60f)) * dp
            var sectionHeaderGap = (if (compactVertical) 8f else 14f) * dp

            return Scale(
                titleSize = titleSize,
                bodySize = bodySize,
                smallSize = smallSize,
                screenSubtitleSize = screenSubtitleSize,
                sectionTitleSize = sectionTitleSize,
                tabTextSize = tabTextSize,
                backTextSize = backTextSize,
                profileTextSize = profileTextSize,
                iconSize = iconSize,
                tabIconSize = tabIconSize,
                chevronSize = chevronSize,
                rowHeight = rowHeight,
                tabHeight = tabHeight,
                tabGap = tabGap,
                tabRowGap = tabRowGap,
                padTop = padTop,
                headerToTitle = headerToTitle,
                titleToTabs = titleToTabs,
                titleToSubtitle = titleToSubtitle,
                subtitleToLine = subtitleToLine,
                tabsToCard = tabsToCard,
                cardToBack = cardToBack,
                cardPadY = cardPadY,
                backHeight = backHeight,
                sectionHeaderGap = sectionHeaderGap,
                compactHeight = compactHeight,
                compactVertical = compactVertical,
                largePortrait = largePortrait,
                xlPortrait = xlPortrait
            )
        }

        fun useTwoByTwoTabs(viewWidth: Float, viewHeight: Float, dp: Float): Boolean =
            widthDp(viewWidth, dp) <= TWO_BY_TWO_MAX_DP

        fun breakpointFor(
            viewWidth: Float,
            dp: Float,
            viewHeight: Float = viewWidth
        ): SettingsBreakpoint {
            val widthDp = widthDp(viewWidth, dp)
            return when {
                widthDp <= COMPACT_MAX_DP -> SettingsBreakpoint.MOBILE
                useTwoByTwoTabs(viewWidth, viewHeight, dp) && isPortrait(viewWidth, viewHeight) ->
                    SettingsBreakpoint.MOBILE
                isPortrait(viewWidth, viewHeight) -> SettingsBreakpoint.TABLET
                widthDp <= TABLET_MAX_DP -> SettingsBreakpoint.TABLET
                else -> SettingsBreakpoint.DESKTOP
            }
        }

        fun contentWidth(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float {
            val widthDp = widthDp(viewWidth, dp)
            return when {
                widthDp <= COMPACT_MAX_DP -> (viewWidth - 28f * dp).coerceAtLeast(1f)
                widthDp < LARGE_MIN_DP ->
                    (viewWidth * 0.92f).coerceAtLeast(1f)
                isPortrait(viewWidth, viewHeight) ->
                    minOf(viewWidth * 0.92f, PORTRAIT_CONTENT_MAX_DP * dp).coerceAtLeast(1f)
                else ->
                    minOf(viewWidth * 0.90f, CONTENT_MAX_DP * dp).coerceAtLeast(1f)
            }
        }

        fun horizontalPad(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            ((viewWidth - contentWidth(viewWidth, dp, viewHeight)) * 0.5f).coerceAtLeast(0f)

        fun rowIconSize(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            scale(viewWidth, viewHeight, dp).iconSize

        fun tabIconSize(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            scale(viewWidth, viewHeight, dp).tabIconSize

        fun tabTextSize(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            scale(viewWidth, viewHeight, dp).tabTextSize

        fun sectionTitleSize(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            scale(viewWidth, viewHeight, dp).sectionTitleSize

        fun backTextSize(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            scale(viewWidth, viewHeight, dp).backTextSize

        fun profileTextSize(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            scale(viewWidth, viewHeight, dp).profileTextSize

        fun chevronSlot(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            scale(viewWidth, viewHeight, dp).chevronSize

        fun rowGap(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            cssClampVmin(12f, 0.015f, 20f, viewWidth, viewHeight, dp)

        fun rowTitleSize(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            scale(viewWidth, viewHeight, dp).bodySize

        fun rowSubtitleSize(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            scale(viewWidth, viewHeight, dp).smallSize

        fun cardPaddingX(viewWidth: Float, viewHeight: Float, dp: Float): Float =
            typeByWidth(widthDp(viewWidth, dp), 16f, 18f, 18f, 20f) * dp

        fun cardPaddingY(viewWidth: Float, viewHeight: Float, dp: Float): Float =
            scale(viewWidth, viewHeight, dp).cardPadY

        fun cardRadius(viewWidth: Float, dp: Float, viewHeight: Float = viewWidth): Float =
            cssClampVmin(14f, 0.015f, 20f, viewWidth, viewHeight, dp)

        fun backMinHeight(viewWidth: Float, viewHeight: Float, dp: Float): Float =
            scale(viewWidth, viewHeight, dp).backHeight

        fun profileNameMaxWidth(viewWidth: Float, dp: Float): Float =
            cssClamp(110f, 0.30f, 220f, viewWidth, dp)

        fun backButtonWidth(
            contentWidth: Float,
            dp: Float,
            breakpoint: SettingsBreakpoint,
            viewWidth: Float = contentWidth,
            viewHeight: Float = viewWidth
        ): Float {
            val width = widthDp(viewWidth, dp)
            return when {
                width <= COMPACT_MAX_DP || useTwoByTwoTabs(viewWidth, viewHeight, dp) ->
                    contentWidth
                width < 1024f ->
                    minOf(560f * dp, contentWidth)
                else ->
                    minOf(520f * dp, contentWidth)
            }
        }

        fun computeHeader(
            viewWidth: Float,
            contentWidth: Float,
            dp: Float,
            viewHeight: Float = viewWidth * 1.8f,
            safeInsetTop: Float = 0f,
            brandOverride: LayoutRect? = null
        ): SettingsHeaderMetrics {
            val tokens = scale(viewWidth, viewHeight, dp)
            val breakpoint = breakpointFor(viewWidth, dp, viewHeight)
            val padH = horizontalPad(viewWidth, dp, viewHeight)
            val homeBrandBase = brandOverride ?: BrandTitleRenderer.placement(
                viewWidth,
                viewHeight,
                dp,
                brandAspect = BrandTitleRenderer.HOME_LOGO_ASPECT
            )
            val shortPhone = isPortrait(viewWidth, viewHeight) && heightDp(viewHeight, dp) <= 640f
            val brandScale = if (brandOverride == null && shortPhone) 0.82f else 1f
            val homeBrand = if (brandScale < 1f) {
                LayoutRect(
                    homeBrandBase.left,
                    homeBrandBase.top,
                    homeBrandBase.left + homeBrandBase.width() * brandScale,
                    homeBrandBase.top + homeBrandBase.height() * brandScale
                )
            } else {
                homeBrandBase
            }
            val brandLeft = homeBrand.left
            // Home ignores the status-bar inset for the wordmark. Settings must use the
            // same origin and size so the logo does not shift down (and steal vertical
            // space from the Back button on short phones such as 360x640).
            val brandTop = homeBrand.top
            val brandHeight = homeBrand.height()
            val brandMaxWidth = homeBrand.width()
            val mottoScale = (heightDp(viewHeight, dp) / 800f).coerceIn(
                if (viewWidth > viewHeight) 0.75f else 0.72f,
                1.25f
            )
            val brandMottoSize = 8.5f * dp
            val brandMottoVisible = heightDp(viewHeight, dp) > 640f
            val brandMottoBottom = brandTop + brandHeight +
                if (brandMottoVisible) 13f * mottoScale * dp else 0f
            val brandMottoBaseline = brandTop + brandHeight + 1f * dp +
                6.5f * mottoScale * dp + brandMottoSize * 0.38f
            val padTop = maxOf(brandTop, safeInsetTop)
            val contentLeft = (viewWidth - contentWidth) * 0.5f
            val contentRight = contentLeft + contentWidth

            val profileIcon = iconByWidth(widthDp(viewWidth, dp), 26f, 30f, 34f, 38f) * dp
            val profileTextCap = profileNameMaxWidth(viewWidth, dp)
            val profileWidth = profileIcon + 8f * dp + profileTextCap + 8f * dp
            val profileHeight = maxOf(profileIcon + 8f * dp, 32f * dp)
            val profileRight = contentRight
            val profileLeft = (profileRight - profileWidth).coerceAtLeast(contentLeft + contentWidth * 0.40f)
            val profileTop = brandTop
            val profileBottom = profileTop + profileHeight

            val headerBottom = maxOf(brandMottoBottom, profileBottom)
            val heroGap = tokens.headerToTitle
            val titleSize = tokens.titleSize
            val subtitleSize = tokens.screenSubtitleSize
            val titleTop = headerBottom + heroGap
            val titleBaseline = titleTop + titleSize
            val subtitleTop = titleBaseline + tokens.titleToSubtitle
            val subtitleBaseline = subtitleTop + subtitleSize
            val dividerY = subtitleBaseline + tokens.subtitleToLine
            val viewportTop = dividerY + tokens.titleToTabs

            return SettingsHeaderMetrics(
                padTop = padTop,
                padHorizontal = padH,
                brandLeft = brandLeft,
                brandTop = brandTop,
                brandHeight = brandHeight,
                brandMaxWidth = brandMaxWidth,
                brandMottoBaseline = brandMottoBaseline,
                brandMottoSize = brandMottoSize,
                brandMottoBottom = brandMottoBottom,
                brandMottoVisible = brandMottoVisible,
                profileLeft = profileLeft,
                profileTop = profileTop,
                profileRight = profileRight,
                profileBottom = profileBottom,
                profileStacked = false,
                titleTop = titleTop,
                titleBaseline = titleBaseline,
                titleSize = titleSize,
                subtitleTop = subtitleTop,
                subtitleBaseline = subtitleBaseline,
                subtitleSize = subtitleSize,
                dividerY = dividerY,
                viewportTop = viewportTop,
                stackedHeader = breakpoint != SettingsBreakpoint.DESKTOP
            )
        }

        private fun itemCountFor(tab: SettingsTab): Int = when (tab) {
            SettingsTab.AUDIO -> 4
            SettingsTab.GAMEPLAY -> 2
            SettingsTab.SYSTEM -> 3
            SettingsTab.INFO -> 4
        }
    }
}
