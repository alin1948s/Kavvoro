package com.moonsolstudios.kavvoro.layout

import android.graphics.RectF
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import com.moonsolstudios.kavvoro.model.CollectionFilter
import com.moonsolstudios.kavvoro.model.LayoutMode
import kotlin.math.min

/**
 * Procedural responsive layout engine for Kavvoro screens, HUDs, buttons, and dialogs.
 */
object ScreenLayoutManager {

    const val TARGET_STAGE_HEIGHT = 17.78f

    fun layoutLanguageSelector(
        side: Float,
        contentWidth: Float,
        compact: Boolean,
        headerY: Float,
        viewportTop: Float,
        viewportBottom: Float,
        languageScroll: Float,
        dp: Float,
        languageBackButton: RectF,
        languageItemRects: MutableList<RectF>
    ): Pair<Float, Float> {
        val backSize = (if (compact) 34f else 38f) * dp
        languageBackButton.set(side, headerY, side + backSize, headerY + backSize)

        val gapX = (if (compact) 8f else 10f) * dp
        val gapY = (if (compact) 8f else 10f) * dp
        val top = viewportTop + 2f * dp
        val columns = 2
        val itemWidth = (contentWidth - gapX * (columns - 1)) / columns
        val itemHeight = (itemWidth / 3.08f).coerceIn(44f * dp, 60f * dp)
        val displayCount = KavvoroLanguage.entries.size - 1

        while (languageItemRects.size < displayCount) languageItemRects.add(RectF())
        for (index in 0 until displayCount) {
            val row = index / columns
            val column = index % columns
            val left = side + column * (itemWidth + gapX)
            val itemTop = top + row * (itemHeight + gapY) - languageScroll
            languageItemRects[index].set(left, itemTop, left + itemWidth, itemTop + itemHeight)
        }
        val rows = ((displayCount + columns - 1) / columns).coerceAtLeast(1)
        val contentHeight = rows * itemHeight + (rows - 1) * gapY + 8f * dp
        val viewportHeight = viewportBottom - viewportTop
        val maxScroll = (contentHeight - viewportHeight).coerceAtLeast(0f)
        val scroll = languageScroll.coerceIn(0f, maxScroll)
        return scroll to maxScroll
    }

    fun layoutLeaderboards(
        side: Float,
        contentRight: Float,
        backTop: Float,
        itemsTop: Float,
        viewHeight: Float,
        dp: Float,
        leaderboardBackButton: RectF,
        leaderboardItemRects: MutableList<RectF>
    ) {
        val backSize = 44f * dp
        leaderboardBackButton.set(contentRight - backSize, backTop, contentRight, backTop + backSize)
        val gap = 10f * dp
        val height = min(88f * dp, (viewHeight - itemsTop - 94f * dp - gap * 3f) / 4f)
        repeat(4) { index ->
            val itemTop = itemsTop + index * (height + gap)
            val rect = leaderboardItemRects.getOrNull(index) ?: RectF().also { leaderboardItemRects += it }
            rect.set(side, itemTop, contentRight, itemTop + height)
        }
        while (leaderboardItemRects.size > 4) {
            leaderboardItemRects.removeAt(leaderboardItemRects.lastIndex)
        }
    }

    fun layoutSettings(
        left: Float,
        right: Float,
        contentWidth: Float,
        actionTop: Float,
        viewportTop: Float,
        viewportBottom: Float,
        compact: Boolean,
        settingsScroll: Float,
        dp: Float,
        headerGearButton: RectF,
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
        aboutButton: RectF,
        resetButton: RectF,
        backButton: RectF,
        masterSlider: RectF,
        musicSlider: RectF,
        sfxSlider: RectF
    ): Pair<Float, Float> {
        val actionSize = (if (compact) 38f else 44f) * dp
        headerGearButton.set(right - actionSize, actionTop, right, actionTop + actionSize)

        val rowHeight = (if (compact) 42f else 48f) * dp
        val gap = (if (compact) 14f else 18f) * dp
        var cursor = viewportTop + 48f * dp - settingsScroll

        masterButton.set(left, cursor, right, cursor + rowHeight)
        musicButton.set(left, masterButton.bottom, right, masterButton.bottom + rowHeight)
        sfxButton.set(left, musicButton.bottom, right, musicButton.bottom + rowHeight)
        hapticToggle.set(left, sfxButton.bottom, right, sfxButton.bottom + rowHeight)

        cursor = hapticToggle.bottom + gap * 2f
        shakeToggle.set(left, cursor, right, cursor + rowHeight)
        performanceToggle.set(left, shakeToggle.bottom, right, shakeToggle.bottom + rowHeight)

        cursor = performanceToggle.bottom + gap * 2f
        languageButton.set(left, cursor, right, cursor + rowHeight)

        cursor = languageButton.bottom + gap * 2f
        accountButton.set(left, cursor, right, cursor + rowHeight)

        cursor = accountButton.bottom + gap * 2f
        privacyButton.set(left, cursor, right, cursor + rowHeight)
        termsButton.set(left, privacyButton.bottom, right, privacyButton.bottom + rowHeight)
        aboutButton.set(left, termsButton.bottom, right, termsButton.bottom + rowHeight)

        cursor = aboutButton.bottom + gap
        resetButton.set(left, cursor, right, cursor + rowHeight + 8f * dp)

        cursor = resetButton.bottom + gap * 1.5f
        val backHeight = (if (compact) 46f else 54f) * dp
        backButton.set(left + 16f * dp, cursor, right - 16f * dp, cursor + backHeight)

        val contentBottom = backButton.bottom + settingsScroll + 20f * dp
        val viewportHeight = viewportBottom - viewportTop
        val maxScroll = (contentBottom - viewportTop - viewportHeight).coerceAtLeast(0f)
        val scroll = settingsScroll.coerceIn(0f, maxScroll)

        masterSlider.set(left + contentWidth * 0.43f, masterButton.centerY(), right - 58f * dp, masterButton.centerY())
        musicSlider.set(left + contentWidth * 0.43f, musicButton.centerY(), right - 58f * dp, musicButton.centerY())
        sfxSlider.set(left + contentWidth * 0.43f, sfxButton.centerY(), right - 58f * dp, sfxButton.centerY())

        return scroll to maxScroll
    }

    fun layoutCollectionFilters(
        side: Float,
        contentWidth: Float,
        top: Float,
        filterCount: Int,
        dp: Float,
        filterRects: List<RectF>
    ) {
        val gap = 5f * dp
        val height = 28f * dp
        val width = (contentWidth - gap * (filterCount - 1)) / filterCount.coerceAtLeast(1)
        for (index in 0 until min(filterCount, filterRects.size)) {
            val left = side + index * (width + gap)
            filterRects[index].set(left, top, left + width, top + height)
        }
    }

    fun layoutHudButtons(
        viewWidth: Float,
        compactControls: Boolean,
        safeTop: Float,
        safeInsetRight: Float,
        dp: Float,
        homeButton: RectF,
        restartButton: RectF,
        sfxButton: RectF,
        musicButton: RectF,
        shareButton: RectF,
        nextButton: RectF
    ) {
        val size = (if (compactControls) 30f else 38f) * dp
        val gap = (if (compactControls) 4f else 8f) * dp
        val right = viewWidth - safeInsetRight - (if (compactControls) 12f else 16f) * dp
        var cursor = right
        homeButton.set(cursor - size, safeTop, cursor, safeTop + size)
        cursor -= size + gap
        restartButton.set(cursor - size, safeTop, cursor, safeTop + size)
        cursor -= size + gap
        sfxButton.set(cursor - size, safeTop, cursor, safeTop + size)
        cursor -= size + gap
        musicButton.set(cursor - size, safeTop, cursor, safeTop + size)
        shareButton.setEmpty()
        nextButton.setEmpty()
    }

    fun layoutOutcomeButtons(
        viewWidth: Float,
        stateWon: Boolean,
        safeBottom: Float,
        dp: Float,
        resultShareButton: RectF,
        resultNextButton: RectF,
        resultRetryButton: RectF
    ) {
        val panelWidth = min(viewWidth - 68f * dp, 500f * dp)
        val panelLeft = (viewWidth - panelWidth) * 0.5f
        val panelRight = panelLeft + panelWidth
        val resultHeight = 48f * dp
        if (stateWon) {
            val gapWidth = 10f * dp
            val half = (panelRight - panelLeft - gapWidth) * 0.5f
            resultShareButton.set(panelLeft, safeBottom - resultHeight, panelLeft + half, safeBottom)
            resultNextButton.set(panelLeft + half + gapWidth, safeBottom - resultHeight, panelRight, safeBottom)
        } else {
            resultShareButton.setEmpty()
            resultNextButton.set(panelLeft, safeBottom - resultHeight, panelRight, safeBottom)
        }
        resultRetryButton.set(resultNextButton)
    }

    data class HomeLayoutMetrics(
        val scale: Float,
        val statsTop: Float,
        val statsHeight: Float,
        val heroTop: Float,
        val heroBottom: Float
    )

    fun layoutHomeModes(
        viewWidth: Float,
        viewHeight: Float,
        safeInsetLeft: Float,
        safeInsetRight: Float,
        safeInsetTop: Float,
        safeInsetBottom: Float,
        contentWidth: Float,
        side: Float,
        mode: LayoutMode,
        density: Float,
        dp: Float,
        menuPrivacyButton: RectF,
        menuSfxButton: RectF,
        menuMusicButton: RectF,
        menuLanguageButton: RectF,
        menuStartButton: RectF,
        menuLeaderboardButton: RectF,
        menuVaultButton: RectF,
        menuCollectionButton: RectF,
        menuContinueButton: RectF,
        menuChaosButton: RectF,
        menuActionStartButton: RectF,
        menuBackButton: RectF
    ): HomeLayoutMetrics {
        val usableW = (viewWidth - safeInsetLeft - safeInsetRight).coerceAtLeast(1f)
        val usableH = (viewHeight - safeInsetTop - safeInsetBottom).coerceAtLeast(1f)
        val aspect = usableH / usableW
        val right = side + contentWidth

        val currentHeightDp = usableH / density.coerceAtLeast(0.1f)
        val scale = (currentHeightDp / 780f).coerceIn(0.72f, 1.15f)

        val headerTop = safeInsetTop + (if (aspect >= 2.0f) 16f else 10f) * scale * dp
        val actionSize = (if (mode == LayoutMode.TABLET) 44f else 38f) * scale * dp
        val actionGap = 7f * scale * dp
        menuSfxButton.set(right - actionSize * 2f - actionGap, headerTop, right - actionSize - actionGap, headerTop + actionSize)
        menuPrivacyButton.set(right - actionSize, headerTop, right, headerTop + actionSize)
        menuMusicButton.setEmpty()
        menuLanguageButton.setEmpty()

        val statsTop = headerTop + actionSize + 14f * scale * dp
        val statsHeight = (if (mode == LayoutMode.TABLET) 50f else 44f) * scale * dp

        val footerBottom = viewHeight - safeInsetBottom - (if (aspect >= 2.0f) 12f else 6f) * scale * dp
        val footerNoteHeight = 14f * scale * dp
        val rowsBottom = footerBottom - footerNoteHeight - 6f * scale * dp
        val rowHeight = (if (mode == LayoutMode.TABLET) 54f else 50f) * scale * dp
        val rowGap = 7f * scale * dp

        val playAspect = 2048f / 559f
        if (mode == LayoutMode.TABLET) {
            val colGap = 16f * scale * dp
            val halfColWidth = (contentWidth - colGap) * 0.5f
            menuVaultButton.set(side, rowsBottom - rowHeight, side + halfColWidth, rowsBottom)
            menuCollectionButton.set(side + halfColWidth + colGap, rowsBottom - rowHeight, right, rowsBottom)
            menuLeaderboardButton.set(side, menuVaultButton.top - rowGap - rowHeight, right, menuVaultButton.top - rowGap)

            val maxPlayW = min(contentWidth, 820f * dp)
            val targetPlayH = 78f * scale * dp
            val playW = min(maxPlayW, targetPlayH * playAspect)
            val playH = playW / playAspect
            val playCx = (side + right) * 0.5f
            val playBottom = menuLeaderboardButton.top - rowGap
            menuStartButton.set(playCx - playW * 0.5f, playBottom - playH, playCx + playW * 0.5f, playBottom)
        } else {
            menuCollectionButton.set(side, rowsBottom - rowHeight, right, rowsBottom)
            menuVaultButton.set(side, menuCollectionButton.top - rowGap - rowHeight, right, menuCollectionButton.top - rowGap)
            menuLeaderboardButton.set(side, menuVaultButton.top - rowGap - rowHeight, right, menuVaultButton.top - rowGap)

            val maxPlayW = contentWidth
            val targetPlayH = 72f * scale * dp
            val playW = min(maxPlayW, targetPlayH * playAspect)
            val playH = playW / playAspect
            val playCx = (side + right) * 0.5f
            val playBottom = menuLeaderboardButton.top - rowGap
            menuStartButton.set(playCx - playW * 0.5f, playBottom - playH, playCx + playW * 0.5f, playBottom)
        }

        val heroTop = statsTop + statsHeight + 8f * scale * dp
        val heroBottom = menuStartButton.top - 8f * scale * dp

        menuContinueButton.setEmpty()
        menuChaosButton.setEmpty()
        menuActionStartButton.setEmpty()
        menuBackButton.setEmpty()

        return HomeLayoutMetrics(scale, statsTop, statsHeight, heroTop, heroBottom)
    }

    fun layoutPlayModes(
        side: Float,
        right: Float,
        compact: Boolean,
        short: Boolean,
        safeTop: Float,
        safeBottom: Float,
        backInset: Float,
        dp: Float,
        menuPrivacyButton: RectF,
        menuSfxButton: RectF,
        menuMusicButton: RectF,
        menuLanguageButton: RectF,
        menuCollectionButton: RectF,
        menuVaultButton: RectF,
        menuLeaderboardButton: RectF,
        menuContinueButton: RectF,
        menuChaosCard: RectF,
        menuClassicCard: RectF,
        menuClassicContinueButton: RectF,
        menuClassicNewButton: RectF,
        menuChaosStartButton: RectF,
        menuStartButton: RectF,
        menuBackButton: RectF,
        menuActionStartButton: RectF
    ) {
        val gap = (if (short) 6f else if (compact) 9f else 14f) * dp
        val actionSize = (if (short) 38f else if (compact) 42f else 46f) * dp
        menuSfxButton.set(right - actionSize * 2f - gap, safeTop, right - actionSize - gap, safeTop + actionSize)
        menuPrivacyButton.set(right - actionSize, safeTop, right, safeTop + actionSize)
        menuMusicButton.setEmpty()
        menuLanguageButton.setEmpty()
        menuCollectionButton.setEmpty()
        menuVaultButton.setEmpty()
        menuLeaderboardButton.setEmpty()

        val backHeight = (if (short) 42f else if (compact) 48f else 54f) * dp
        menuContinueButton.set(side + (if (short) 0f else backInset), safeBottom - backHeight, right - (if (short) 0f else backInset), safeBottom)
        val cardHeight = (if (short) 122f else if (compact) 156f else 226f) * dp
        val chaosBottom = menuContinueButton.top - gap
        menuChaosCard.set(side, chaosBottom - cardHeight, right, chaosBottom)
        val classicBottom = menuChaosCard.top - gap
        menuClassicCard.set(side, classicBottom - cardHeight, right, classicBottom)
        val cardInset = (if (short) 10f else if (compact) 18f else 22f) * dp
        val buttonGap = (if (short) 6f else 10f) * dp
        val buttonHeight = (if (short) 28f else if (compact) 34f else 42f) * dp
        val half = (menuClassicCard.width() - cardInset * 2f - buttonGap) * 0.5f
        menuClassicContinueButton.set(menuClassicCard.left + cardInset, menuClassicCard.bottom - cardInset - buttonHeight, menuClassicCard.left + cardInset + half, menuClassicCard.bottom - cardInset)
        menuClassicNewButton.set(menuClassicContinueButton.right + buttonGap, menuClassicContinueButton.top, menuClassicCard.right - cardInset, menuClassicContinueButton.bottom)
        menuChaosStartButton.set(menuChaosCard.left + cardInset, menuChaosCard.bottom - cardInset - buttonHeight, menuChaosCard.right - cardInset, menuChaosCard.bottom - cardInset)
        menuStartButton.setEmpty()
        menuBackButton.set(menuContinueButton)
        menuActionStartButton.setEmpty()
    }

    fun isLargeScreenLayout(smallestScreenWidthDp: Int, homeLayoutMode: LayoutMode): Boolean =
        smallestScreenWidthDp >= 600 || homeLayoutMode == LayoutMode.TABLET

    fun homeLayoutMode(viewWidth: Int, safeInsetLeft: Int, safeInsetRight: Int, density: Float): LayoutMode {
        val usableW = (viewWidth - safeInsetLeft - safeInsetRight).coerceAtLeast(1).toFloat()
        val widthDp = usableW / density.coerceAtLeast(0.1f)
        return when {
            widthDp <= 480f -> LayoutMode.COMPACT
            widthDp <= 840f -> LayoutMode.MEDIUM
            else -> LayoutMode.TABLET
        }
    }

    fun homeContentWidth(viewWidth: Int, safeInsetLeft: Int, safeInsetRight: Int, density: Float, dp: Float): Float {
        val usableW = (viewWidth - safeInsetLeft - safeInsetRight).coerceAtLeast(1).toFloat()
        val widthDp = usableW / density.coerceAtLeast(0.1f)
        val fraction = when {
            widthDp <= 480f -> 0.94f
            widthDp <= 840f -> 0.90f
            else -> 0.82f
        }
        val maxWidthPx = (when {
            widthDp <= 480f -> 460f
            widthDp <= 840f -> 720f
            else -> 1100f
        }) * dp
        return min(usableW * fraction, maxWidthPx)
    }

    fun homeContentLeft(viewWidth: Int, safeInsetLeft: Int, safeInsetRight: Int, contentWidth: Float): Float {
        val usableW = (viewWidth - safeInsetLeft - safeInsetRight).coerceAtLeast(1).toFloat()
        return safeInsetLeft + (usableW - contentWidth) * 0.5f
    }
}

