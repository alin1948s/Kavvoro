package com.moonsolstudios.kavvoro.ui.screens.missions

import com.moonsolstudios.kavvoro.model.DailyMissionId
import com.moonsolstudios.kavvoro.ui.render.LayoutRect
import kotlin.math.max
import kotlin.math.min

class MissionsLayoutCalculator {
    val backButtonRect = LayoutRect()
    val titleRect = LayoutRect()
    val subtitleRect = LayoutRect()
    val summaryRect = LayoutRect()
    val summaryProgressTextRect = LayoutRect()
    val summaryRewardLabelRect = LayoutRect()
    val summaryRewardTextRect = LayoutRect()
    val summaryTrackRect = LayoutRect()
    val cardRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val titleTextRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val progressTextRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val statusTextRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val rewardTextRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val progressTrackRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val claimButtonRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    var gridLayout: Boolean = false
        private set
    var showSummary: Boolean = false
        private set

    fun calculate(width: Float, height: Float, density: Float) {
        val safeWidth = width.coerceAtLeast(1f)
        val safeHeight = height.coerceAtLeast(1f)
        val dp = density.coerceAtLeast(0.1f)
        val marginX = 20f * dp
        val contentWidth = min(safeWidth - marginX * 2f, 1_020f * dp).coerceAtLeast(1f)
        val contentLeft = (safeWidth - contentWidth) * 0.5f
        val contentRight = contentLeft + contentWidth
        val top = max(22f * dp, safeHeight * 0.035f)

        backButtonRect.set(contentLeft, top, contentLeft + 48f * dp, top + 48f * dp)
        titleRect.set(contentLeft + 60f * dp, top + 1f * dp, contentRight, top + 30f * dp)
        subtitleRect.set(contentLeft + 60f * dp, top + 30f * dp, contentRight, top + 50f * dp)

        gridLayout = safeWidth / dp >= 760f && safeHeight / dp >= 520f && safeWidth > safeHeight
        showSummary = safeHeight / dp >= 640f && safeWidth / dp >= 360f
        val summaryTop = top + 62f * dp
        val summaryHeight = if (gridLayout) 88f * dp else 96f * dp
        if (showSummary) {
            summaryRect.set(contentLeft, summaryTop, contentRight, summaryTop + summaryHeight)
            summaryProgressTextRect.set(contentLeft + 20f * dp, summaryTop + 42f * dp, contentLeft + contentWidth * 0.47f, summaryTop + 72f * dp)
            summaryRewardLabelRect.set(contentLeft + contentWidth * 0.49f, summaryTop + 21f * dp, contentRight - 20f * dp, summaryTop + 39f * dp)
            summaryRewardTextRect.set(contentLeft + contentWidth * 0.49f, summaryTop + 39f * dp, contentRight - 20f * dp, summaryTop + 66f * dp)
            summaryTrackRect.set(contentLeft + 20f * dp, summaryTop + summaryHeight - 13f * dp, contentRight - 20f * dp, summaryTop + summaryHeight - 8f * dp)
        } else {
            summaryRect.setEmpty()
            summaryProgressTextRect.setEmpty()
            summaryRewardLabelRect.setEmpty()
            summaryRewardTextRect.setEmpty()
            summaryTrackRect.setEmpty()
        }

        val listTop = if (showSummary) summaryRect.bottom + 13f * dp else top + 78f * dp
        val listBottom = safeHeight - 22f * dp
        val gap = 12f * dp

        if (gridLayout) {
            val cardGap = 16f * dp
            val cardWidth = (contentWidth - cardGap * 2f) / cardRects.size
            val cardHeight = (listBottom - listTop).coerceAtLeast(128f * dp).coerceAtMost(304f * dp)
            val rowTop = listTop + ((listBottom - listTop - cardHeight).coerceAtLeast(0f)) * 0.5f
            cardRects.indices.forEach { index ->
                val left = contentLeft + index * (cardWidth + cardGap)
                cardRects[index].set(left, rowTop, left + cardWidth, rowTop + cardHeight)
                layoutGridCard(index, cardRects[index], dp)
            }
        } else {
            val availableHeight = (listBottom - listTop).coerceAtLeast(1f)
            val compact = !showSummary
            val compactGap = if (compact) min(gap, availableHeight / (cardRects.size * 6f)) else gap
            val maxCardHeight = if (compact) 224f * dp else 168f * dp
            val minCardHeight = if (compact) 68f * dp else 120f * dp
            val cardHeight = ((availableHeight - compactGap * 2f) / cardRects.size).coerceIn(minCardHeight, maxCardHeight)
            val totalHeight = cardHeight * cardRects.size + compactGap * 2f
            val stackTop = if (compact) {
                listTop + ((availableHeight - totalHeight).coerceAtLeast(0f)) * 0.5f
            } else {
                listTop + ((availableHeight - totalHeight).coerceAtLeast(0f)) * 0.12f
            }
            cardRects.indices.forEach { index ->
                val cardTop = stackTop + index * (cardHeight + compactGap)
                cardRects[index].set(contentLeft, cardTop, contentRight, cardTop + cardHeight)
                layoutStackedCard(index, cardRects[index], dp, compact)
            }
        }
    }

    private fun layoutStackedCard(index: Int, card: LayoutRect, dp: Float, compact: Boolean) {
        val buttonWidth = min(card.width() - 32f * dp, 196f * dp).coerceAtLeast(92f * dp)
        val buttonHeight = if (compact) min(36f * dp, card.height() * 0.38f) else 40f * dp
        val buttonTop = if (compact) card.bottom - 11f * dp - buttonHeight else {
            card.top + min(99f * dp, card.height() - 11f * dp - buttonHeight)
        }
        claimButtonRects[index].set(
            card.centerX() - buttonWidth * 0.5f,
            buttonTop,
            card.centerX() + buttonWidth * 0.5f,
            buttonTop + buttonHeight
        )
        if (compact) {
            titleTextRects[index].set(card.left + 50f * dp, card.top + 5f * dp, claimButtonRects[index].right, card.top + 27f * dp)
            progressTextRects[index].set(card.left + 18f * dp, card.top + 30f * dp, card.right - 18f * dp, card.top + 46f * dp)
            statusTextRects[index].setEmpty()
            rewardTextRects[index].setEmpty()
            progressTrackRects[index].set(card.left + 18f * dp, card.bottom - buttonHeight - 22f * dp, card.right - 18f * dp, card.bottom - buttonHeight - 16f * dp)
        } else {
            val rewardWidth = min(108f * dp, card.width() * 0.32f)
            titleTextRects[index].set(card.left + 54f * dp, card.top + 13f * dp, card.right - rewardWidth - 18f * dp, card.top + 40f * dp)
            rewardTextRects[index].set(card.right - rewardWidth - 14f * dp, card.top + 12f * dp, card.right - 14f * dp, card.top + 37f * dp)
            progressTextRects[index].set(card.left + 20f * dp, card.top + 49f * dp, card.centerX(), card.top + 68f * dp)
            statusTextRects[index].set(card.centerX(), card.top + 49f * dp, card.right - 20f * dp, card.top + 68f * dp)
            progressTrackRects[index].set(card.left + 20f * dp, card.top + 76f * dp, card.right - 20f * dp, card.top + 83f * dp)
        }
    }

    private fun layoutGridCard(index: Int, card: LayoutRect, dp: Float) {
        val buttonWidth = min(card.width() - 36f * dp, 208f * dp).coerceAtLeast(92f * dp)
        val buttonHeight = 44f * dp
        claimButtonRects[index].set(
            card.centerX() - buttonWidth * 0.5f,
            card.bottom - 20f * dp - buttonHeight,
            card.centerX() + buttonWidth * 0.5f,
            card.bottom - 20f * dp
        )
        titleTextRects[index].set(card.left + 18f * dp, card.top + 70f * dp, card.right - 18f * dp, card.top + 104f * dp)
        rewardTextRects[index].set(card.left + 22f * dp, card.top + 111f * dp, card.right - 22f * dp, card.top + 137f * dp)
        progressTextRects[index].set(card.left + 20f * dp, claimButtonRects[index].top - 56f * dp, card.right - 20f * dp, claimButtonRects[index].top - 34f * dp)
        statusTextRects[index].set(card.left + 20f * dp, claimButtonRects[index].top - 32f * dp, card.right - 20f * dp, claimButtonRects[index].top - 18f * dp)
        progressTrackRects[index].set(card.left + 20f * dp, claimButtonRects[index].top - 12f * dp, card.right - 20f * dp, claimButtonRects[index].top - 5f * dp)
    }
}
