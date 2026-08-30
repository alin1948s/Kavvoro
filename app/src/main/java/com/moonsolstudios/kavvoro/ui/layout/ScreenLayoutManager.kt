package com.moonsolstudios.kavvoro.ui.layout

import android.graphics.RectF
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import com.moonsolstudios.kavvoro.model.CollectionFilter
import kotlin.math.min

/**
 * Procedural responsive layout engine for secondary Kavvoro screens.
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
        val displayCount = KavvoroLanguage.selectableLanguages.size

        while (languageItemRects.size < displayCount) languageItemRects.add(RectF())
        while (languageItemRects.size > displayCount) languageItemRects.removeAt(languageItemRects.lastIndex)
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
        dataDeletionButton: RectF,
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
        dataDeletionButton.set(left, termsButton.bottom, right, termsButton.bottom + rowHeight)
        aboutButton.set(left, dataDeletionButton.bottom, right, dataDeletionButton.bottom + rowHeight)

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

}
