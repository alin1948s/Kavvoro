package com.moonsolstudios.kavvoro.ui.screens.collection

import android.graphics.RectF
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.CollectionFilter
import com.moonsolstudios.kavvoro.model.CollectionSort
import kotlin.math.min

/**
 * Dedicated responsive layout calculator for the Collection & Vault screen.
 */
object CollectionLayoutCalculator {

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
            val itemLeft = side + index * (width + gap)
            val rect = filterRects[index]
            rect.left = itemLeft
            rect.top = top
            rect.right = itemLeft + width
            rect.bottom = top + height
            rect.set(itemLeft, top, itemLeft + width, top + height)
        }
    }

    fun layoutCollection(
        contentLeft: Float,
        contentRight: Float,
        safeTop22: Float,
        safeTop68: Float,
        safeTop88: Float,
        safeTop192: Float,
        viewportTop: Float,
        viewportBottom: Float,
        scroll: Float,
        ballSkins: List<BallSkin>,
        filter: CollectionFilter,
        sort: CollectionSort = CollectionSort.AURA_DESC,
        isSkinUnlocked: (BallSkin) -> Boolean = { true },
        dp: Float,
        backButton: RectF,
        restoreButton: RectF,
        filterRects: List<RectF>,
        itemRects: MutableList<RectF>,
        heroStageRect: RectF = CollectionTouchController.heroStageRect,
        heroActionRect: RectF = CollectionTouchController.heroActionRect,
        sortButtonRect: RectF = CollectionTouchController.sortButtonRect
    ): Pair<Float, Float> {
        val contentWidth = contentRight - contentLeft
        val size = 42f * dp
        backButton.apply {
            left = contentRight - size
            top = safeTop22
            right = contentRight
            bottom = safeTop22 + size
        }
        restoreButton.apply {
            left = contentRight - size - 8f * dp - 42f * dp
            top = safeTop22
            right = contentRight - size - 8f * dp
            bottom = safeTop22 + size
        }

        val heroHeight = if (contentWidth >= 560f * dp) 175f * dp else 156f * dp
        val stageTop = safeTop22 + 48f * dp
        heroStageRect.apply {
            left = contentLeft
            top = stageTop
            right = contentRight
            bottom = stageTop + heroHeight
        }

        val actionW = minOf(210f * dp, contentWidth * 0.52f)
        val actionH = 36f * dp
        heroActionRect.apply {
            left = contentRight - 14f * dp - actionW
            top = heroStageRect.bottom - 12f * dp - actionH
            right = contentRight - 14f * dp
            bottom = heroStageRect.bottom - 12f * dp
        }

        val filtersTop = heroStageRect.bottom + 6f * dp
        layoutCollectionFilters(
            side = contentLeft,
            contentWidth = contentWidth,
            top = filtersTop,
            filterCount = CollectionFilter.entries.size,
            dp = dp,
            filterRects = filterRects
        )

        // Sort pill button right below the filters
        val sortTop = filtersTop + 32f * dp
        val sortWidth = minOf(136f * dp, contentWidth * 0.44f)
        val sortHeight = 24f * dp
        sortButtonRect.left = contentRight - sortWidth
        sortButtonRect.top = sortTop
        sortButtonRect.right = contentRight
        sortButtonRect.bottom = sortTop + sortHeight

        val side = contentLeft
        val gap = 8f * dp
        val columns = when {
            contentWidth >= 840f * dp -> 5
            contentWidth >= 560f * dp -> 4
            contentWidth >= 310f * dp -> 3
            else -> 2
        }
        val itemWidth = (contentWidth - gap * (columns - 1)) / columns
        val itemHeight = (itemWidth * 1.34f).coerceIn(136f * dp, 175f * dp)
        val effectiveViewportTop = if (sortButtonRect.right > sortButtonRect.left) sortButtonRect.bottom + 6f * dp else viewportTop
        val top = effectiveViewportTop + 4f * dp - scroll

        while (itemRects.size < ballSkins.size) {
            itemRects += RectF()
        }
        itemRects.forEach {
            it.left = 0f
            it.top = 0f
            it.right = 0f
            it.bottom = 0f
        }
        val visibleIndexes = CollectionTouchController.sortedFilteredIndexes(ballSkins, filter, sort, isSkinUnlocked)
        visibleIndexes.forEachIndexed { slotIndex, skinIndex ->
            val col = slotIndex % columns
            val row = slotIndex / columns
            val left = side + col * (itemWidth + gap)
            val itemTop = top + row * (itemHeight + gap)
            val rect = itemRects[skinIndex]
            rect.left = left
            rect.top = itemTop
            rect.right = left + itemWidth
            rect.bottom = itemTop + itemHeight
        }
        while (itemRects.size > ballSkins.size) {
            itemRects.removeAt(itemRects.lastIndex)
        }

        val rows = ((visibleIndexes.size + columns - 1) / columns).coerceAtLeast(1)
        val contentHeight = rows * itemHeight + (rows - 1) * gap + 16f * dp
        val viewportHeight = viewportBottom - effectiveViewportTop
        val maxScroll = (contentHeight - viewportHeight).coerceAtLeast(0f)
        val clampedScroll = scroll.coerceIn(0f, maxScroll)
        return clampedScroll to maxScroll
    }
}
