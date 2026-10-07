package com.moonsolstudios.kavvoro.ui.screens.missions

import com.moonsolstudios.kavvoro.model.DailyMissionId
import com.moonsolstudios.kavvoro.ui.render.LayoutRect
import kotlin.math.max
import kotlin.math.min

class MissionsLayoutCalculator {
    val backButtonRect = LayoutRect()
    val titleRect = LayoutRect()
    val subtitleRect = LayoutRect()
    val cardRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val titleTextRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val progressTextRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val rewardTextRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val progressTrackRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    val claimButtonRects = Array(DailyMissionId.entries.size) { LayoutRect() }
    var gridLayout: Boolean = false
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

        val listTop = top + 78f * dp
        val listBottom = safeHeight - 22f * dp
        val gap = 12f * dp
        gridLayout = safeWidth / dp >= 760f && safeHeight / dp >= 520f && safeWidth > safeHeight

        if (gridLayout) {
            val cardGap = 16f * dp
            val cardWidth = (contentWidth - cardGap * 2f) / cardRects.size
            val cardHeight = (listBottom - listTop).coerceAtLeast(112f * dp).coerceAtMost(320f * dp)
            val rowTop = listTop + ((listBottom - listTop - cardHeight).coerceAtLeast(0f)) * 0.5f
            cardRects.indices.forEach { index ->
                val left = contentLeft + index * (cardWidth + cardGap)
                cardRects[index].set(left, rowTop, left + cardWidth, rowTop + cardHeight)
                layoutGridCard(index, cardRects[index], dp)
            }
        } else {
            val availableHeight = (listBottom - listTop).coerceAtLeast(1f)
            val compactGap = min(gap, availableHeight / (cardRects.size * 6f))
            val cardHeight = ((availableHeight - compactGap * 2f) / cardRects.size).coerceIn(68f * dp, 224f * dp)
            val totalHeight = cardHeight * cardRects.size + compactGap * 2f
            val stackTop = listTop + ((availableHeight - totalHeight).coerceAtLeast(0f)) * 0.5f
            cardRects.indices.forEach { index ->
                val cardTop = stackTop + index * (cardHeight + compactGap)
                cardRects[index].set(contentLeft, cardTop, contentRight, cardTop + cardHeight)
                layoutStackedCard(index, cardRects[index], dp)
            }
        }
    }

    private fun layoutStackedCard(index: Int, card: LayoutRect, dp: Float) {
        val buttonWidth = min(128f * dp, card.width() * 0.42f)
        val buttonHeight = min(42f * dp, card.height() * 0.32f)
        claimButtonRects[index].set(
            card.right - 16f * dp - buttonWidth,
            card.centerY() - buttonHeight * 0.5f + 7f * dp,
            card.right - 16f * dp,
            card.centerY() + buttonHeight * 0.5f + 7f * dp
        )
        if (card.height() < 92f * dp) {
            titleTextRects[index].set(card.left + 56f * dp, card.top + 5f * dp, claimButtonRects[index].left - 10f * dp, card.top + 27f * dp)
            progressTextRects[index].set(card.left + 56f * dp, card.top + 28f * dp, claimButtonRects[index].left - 10f * dp, card.top + 45f * dp)
            rewardTextRects[index].set(claimButtonRects[index].left - 6f * dp, card.top + 4f * dp, card.right - 12f * dp, card.top + 23f * dp)
            progressTrackRects[index].set(card.left + 20f * dp, card.bottom - 17f * dp, claimButtonRects[index].left - 10f * dp, card.bottom - 10f * dp)
        } else {
            titleTextRects[index].set(card.left + 56f * dp, card.top + 15f * dp, claimButtonRects[index].left - 10f * dp, card.top + 48f * dp)
            progressTextRects[index].set(card.left + 56f * dp, card.top + 54f * dp, claimButtonRects[index].left - 10f * dp, card.top + 76f * dp)
            rewardTextRects[index].set(claimButtonRects[index].left - 6f * dp, card.top + 12f * dp, card.right - 12f * dp, card.top + 36f * dp)
            progressTrackRects[index].set(card.left + 20f * dp, card.bottom - 22f * dp, claimButtonRects[index].left - 10f * dp, card.bottom - 14f * dp)
        }
    }

    private fun layoutGridCard(index: Int, card: LayoutRect, dp: Float) {
        val buttonWidth = min(card.width() - 32f * dp, 196f * dp).coerceAtLeast(92f * dp)
        val buttonHeight = 44f * dp
        claimButtonRects[index].set(
            card.centerX() - buttonWidth * 0.5f,
            card.bottom - 20f * dp - buttonHeight,
            card.centerX() + buttonWidth * 0.5f,
            card.bottom - 20f * dp
        )
        titleTextRects[index].set(card.left + 20f * dp, card.top + 64f * dp, card.right - 20f * dp, card.top + 102f * dp)
        rewardTextRects[index].set(card.left + 20f * dp, card.top + 108f * dp, card.right - 20f * dp, card.top + 136f * dp)
        progressTextRects[index].set(card.left + 20f * dp, claimButtonRects[index].top - 52f * dp, card.right - 20f * dp, claimButtonRects[index].top - 30f * dp)
        progressTrackRects[index].set(card.left + 20f * dp, claimButtonRects[index].top - 22f * dp, card.right - 20f * dp, claimButtonRects[index].top - 14f * dp)
    }
}
