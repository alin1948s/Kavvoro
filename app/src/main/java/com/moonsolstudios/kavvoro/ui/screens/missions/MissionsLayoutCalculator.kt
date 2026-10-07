package com.moonsolstudios.kavvoro.ui.screens.missions

import com.moonsolstudios.kavvoro.model.MissionCategory
import com.moonsolstudios.kavvoro.model.MissionId
import com.moonsolstudios.kavvoro.ui.render.LayoutRect
import kotlin.math.max
import kotlin.math.min

class MissionsLayoutCalculator {
    val backButtonRect = LayoutRect()
    val titleRect = LayoutRect()
    val subtitleRect = LayoutRect()
    val tabsRect = LayoutRect()
    val dailyTabRect = LayoutRect()
    val riftChallengesTabRect = LayoutRect()
    val summaryRect = LayoutRect()
    val summaryProgressTextRect = LayoutRect()
    val summaryRewardLabelRect = LayoutRect()
    val summaryRewardTextRect = LayoutRect()
    val summaryTrackRect = LayoutRect()
    val summaryArtRect = LayoutRect()
    val cardRects = Array(MissionId.entries.size) { LayoutRect() }
    val missionIconRects = Array(MissionId.entries.size) { LayoutRect() }
    val titleTextRects = Array(MissionId.entries.size) { LayoutRect() }
    val progressTextRects = Array(MissionId.entries.size) { LayoutRect() }
    val statusTextRects = Array(MissionId.entries.size) { LayoutRect() }
    val rewardTextRects = Array(MissionId.entries.size) { LayoutRect() }
    val progressTrackRects = Array(MissionId.entries.size) { LayoutRect() }
    val claimButtonRects = Array(MissionId.entries.size) { LayoutRect() }
    var gridLayout: Boolean = false
        private set
    var showSummary: Boolean = false
        private set
    var visibleMissionCount: Int = 0
        private set

    fun calculate(
        width: Float,
        height: Float,
        density: Float,
        missionCount: Int = MissionId.inCategory(MissionCategory.DAILY).size
    ) {
        val safeWidth = width.coerceAtLeast(1f)
        val safeHeight = height.coerceAtLeast(1f)
        val dp = density.coerceAtLeast(0.1f)
        visibleMissionCount = missionCount.coerceIn(0, cardRects.size)
        cardRects.forEach { it.setEmpty() }
        missionIconRects.forEach { it.setEmpty() }
        titleTextRects.forEach { it.setEmpty() }
        progressTextRects.forEach { it.setEmpty() }
        statusTextRects.forEach { it.setEmpty() }
        rewardTextRects.forEach { it.setEmpty() }
        progressTrackRects.forEach { it.setEmpty() }
        claimButtonRects.forEach { it.setEmpty() }
        val marginX = 20f * dp
        val contentWidth = min(safeWidth - marginX * 2f, 1_020f * dp).coerceAtLeast(1f)
        val contentLeft = (safeWidth - contentWidth) * 0.5f
        val contentRight = contentLeft + contentWidth
        val top = max(22f * dp, safeHeight * 0.035f)
        val shortCompact = safeHeight / dp < 430f

        backButtonRect.set(contentLeft, top, contentLeft + 48f * dp, top + 48f * dp)
        titleRect.set(contentLeft + 60f * dp, top + 1f * dp, contentRight, top + 30f * dp)
        subtitleRect.set(contentLeft + 60f * dp, top + 30f * dp, contentRight, top + 50f * dp)

        gridLayout = safeWidth / dp >= 760f && safeHeight / dp >= 520f && safeWidth > safeHeight
        val tabTop = top + (if (shortCompact) 51f else 62f) * dp
        val tabHeight = (if (shortCompact) 34f else 42f) * dp
        val tabInset = (if (shortCompact) 3f else 4f) * dp
        tabsRect.set(contentLeft, tabTop, contentRight, tabTop + tabHeight)
        val tabWidth = ((contentWidth - 12f * dp) * 0.5f).coerceAtLeast(1f)
        dailyTabRect.set(contentLeft + tabInset, tabTop + tabInset,
            contentLeft + tabInset + tabWidth, tabsRect.bottom - tabInset)
        riftChallengesTabRect.set(dailyTabRect.right + 4f * dp, tabTop + tabInset,
            contentRight - tabInset, tabsRect.bottom - tabInset)
        val summaryHeight = if (gridLayout) 104f * dp else 148f * dp
        val topDp = top / dp
        val minimumCardHeightDp = if (gridLayout) 128f else 160f
        val summaryRequiredHeightDp = topDp + 116f + summaryHeight / dp + 13f +
            visibleMissionCount * minimumCardHeightDp + (visibleMissionCount - 1).coerceAtLeast(0) * 12f + 22f
        showSummary = visibleMissionCount > 0 && safeWidth / dp >= 360f && safeHeight / dp >= summaryRequiredHeightDp
        val summaryTop = tabsRect.bottom + 12f * dp
        if (showSummary) {
            summaryRect.set(contentLeft, summaryTop, contentRight, summaryTop + summaryHeight)
            val artWidth = if (gridLayout) 86f * dp else 108f * dp
            if (gridLayout) {
                summaryProgressTextRect.set(contentLeft + 22f * dp, summaryTop + 36f * dp, contentLeft + 196f * dp, summaryTop + 78f * dp)
                summaryRewardLabelRect.set(contentLeft + 230f * dp, summaryTop + 25f * dp, contentRight - artWidth - 28f * dp, summaryTop + 45f * dp)
                summaryRewardTextRect.set(contentLeft + 230f * dp, summaryTop + 47f * dp, contentRight - artWidth - 28f * dp, summaryTop + 76f * dp)
            } else {
                summaryProgressTextRect.set(contentLeft + 22f * dp, summaryTop + 40f * dp, contentLeft + contentWidth - artWidth - 28f * dp, summaryTop + 78f * dp)
                summaryRewardLabelRect.set(contentLeft + 22f * dp, summaryTop + 88f * dp, contentLeft + contentWidth - artWidth - 28f * dp, summaryTop + 106f * dp)
                summaryRewardTextRect.set(contentLeft + 22f * dp, summaryTop + 105f * dp, contentLeft + contentWidth - artWidth - 28f * dp, summaryTop + 132f * dp)
            }
            summaryTrackRect.set(contentLeft + 22f * dp, summaryTop + summaryHeight - 11f * dp, contentRight - 22f * dp, summaryTop + summaryHeight - 6f * dp)
            val artTop = summaryTop + (summaryHeight - artWidth) * 0.5f
            summaryArtRect.set(contentRight - artWidth - 12f * dp, artTop, contentRight - 12f * dp, artTop + artWidth)
        } else {
            summaryRect.setEmpty()
            summaryProgressTextRect.setEmpty()
            summaryRewardLabelRect.setEmpty()
            summaryRewardTextRect.setEmpty()
            summaryTrackRect.setEmpty()
            summaryArtRect.setEmpty()
        }

        if (visibleMissionCount == 0) return
        val listTop = if (showSummary) summaryRect.bottom + 13f * dp else tabsRect.bottom + (if (shortCompact) 8f else 13f) * dp
        val listBottom = safeHeight - 22f * dp
        val gap = (if (shortCompact) 8f else 12f) * dp

        if (gridLayout) {
            val cardGap = 16f * dp
            val cardWidth = (contentWidth - cardGap * (visibleMissionCount - 1)) / visibleMissionCount
            val cardHeight = (listBottom - listTop).coerceAtLeast(128f * dp).coerceAtMost(304f * dp)
            val rowTop = listTop + ((listBottom - listTop - cardHeight).coerceAtLeast(0f)) * 0.5f
            for (index in 0 until visibleMissionCount) {
                val left = contentLeft + index * (cardWidth + cardGap)
                cardRects[index].set(left, rowTop, left + cardWidth, rowTop + cardHeight)
                layoutGridCard(index, cardRects[index], dp)
            }
        } else {
            val availableHeight = (listBottom - listTop).coerceAtLeast(1f)
            val compact = !showSummary
            val compactGap = if (compact) min(gap, availableHeight / (visibleMissionCount * 6f)) else gap
            val maxCardHeight = if (compact) 224f * dp else 176f * dp
            val minCardHeight = if (compact) {
                val preferredMinimum = (if (shortCompact) 48f else 68f) * dp
                val heightPerCard = (availableHeight - compactGap * (visibleMissionCount - 1)) / visibleMissionCount
                min(preferredMinimum, heightPerCard.coerceAtLeast(1f))
            } else minimumCardHeightDp * dp
            val cardHeight = ((availableHeight - compactGap * (visibleMissionCount - 1)) / visibleMissionCount).coerceIn(minCardHeight, maxCardHeight)
            val totalHeight = cardHeight * visibleMissionCount + compactGap * (visibleMissionCount - 1)
            val stackTop = if (compact) {
                listTop + ((availableHeight - totalHeight).coerceAtLeast(0f)) * 0.5f
            } else {
                listTop + ((availableHeight - totalHeight).coerceAtLeast(0f)) * 0.12f
            }
            for (index in 0 until visibleMissionCount) {
                val cardTop = stackTop + index * (cardHeight + compactGap)
                cardRects[index].set(contentLeft, cardTop, contentRight, cardTop + cardHeight)
                layoutStackedCard(index, cardRects[index], dp, compact)
            }
        }
    }

    private fun layoutStackedCard(index: Int, card: LayoutRect, dp: Float, compact: Boolean) {
        val narrowCompact = compact && card.height() / dp < 72f
        val buttonWidth = if (narrowCompact) {
            min(card.width() - 16f * dp, 120f * dp).coerceAtLeast(72f * dp)
        } else {
            min(card.width() - 36f * dp, 174f * dp).coerceAtLeast(92f * dp)
        }
        val buttonHeight = when {
            narrowCompact -> min(32f * dp, (card.height() - 8f * dp).coerceAtLeast(1f))
            compact -> min(36f * dp, card.height() * 0.38f)
            else -> 44f * dp
        }
        val buttonTop = when {
            narrowCompact -> card.centerY() - buttonHeight * 0.5f
            compact -> card.bottom - 10f * dp - buttonHeight
            else -> card.bottom - 14f * dp - buttonHeight
        }
        val buttonLeft = when {
            narrowCompact -> card.right - 8f * dp - buttonWidth
            compact -> card.centerX() - buttonWidth * 0.5f
            else -> card.right - 16f * dp - buttonWidth
        }
        claimButtonRects[index].set(
            buttonLeft,
            buttonTop,
            buttonLeft + buttonWidth,
            buttonTop + buttonHeight
        )
        if (compact) {
            if (narrowCompact) {
                val iconSize = min(34f * dp, (card.height() - 8f * dp).coerceAtLeast(1f))
                val iconLeft = card.left + 8f * dp
                val iconTop = card.centerY() - iconSize * 0.5f
                val textRight = (claimButtonRects[index].left - 8f * dp).coerceAtLeast(card.left + 56f * dp)
                missionIconRects[index].set(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                titleTextRects[index].set(card.left + 48f * dp, card.top + 1f * dp, textRight, card.top + 19f * dp)
                progressTextRects[index].set(card.left + 48f * dp, card.top + 19f * dp, textRight, card.top + 33f * dp)
                progressTrackRects[index].set(card.left + 48f * dp, card.bottom - 5f * dp, textRight, card.bottom - 2f * dp)
            } else {
                missionIconRects[index].set(card.left + 10f * dp, card.top + 4f * dp, card.left + 44f * dp, card.top + 38f * dp)
                titleTextRects[index].set(card.left + 50f * dp, card.top + 5f * dp, claimButtonRects[index].right, card.top + 27f * dp)
                progressTextRects[index].set(card.left + 50f * dp, card.top + 30f * dp, card.right - 18f * dp, card.top + 46f * dp)
                if (card.height() / dp >= 140f) {
                    rewardTextRects[index].set(card.left + 50f * dp, card.top + 54f * dp,
                        min(card.left + 132f * dp, card.right - 18f * dp), card.top + 78f * dp)
                } else {
                    rewardTextRects[index].setEmpty()
                }
                val trackTop = if (card.height() / dp >= 140f) {
                    min(card.top + 90f * dp, claimButtonRects[index].top - 12f * dp)
                } else {
                    card.bottom - buttonHeight - 22f * dp
                }
                progressTrackRects[index].set(card.left + 50f * dp, trackTop,
                    card.right - 18f * dp, trackTop + 6f * dp)
            }
            statusTextRects[index].setEmpty()
        } else {
            val rewardWidth = min(112f * dp, card.width() * 0.34f)
            missionIconRects[index].set(card.left + 16f * dp, card.top + 16f * dp, card.left + 62f * dp, card.top + 62f * dp)
            titleTextRects[index].set(card.left + 72f * dp, card.top + 15f * dp, card.right - rewardWidth - 22f * dp, card.top + 45f * dp)
            rewardTextRects[index].set(card.right - rewardWidth - 16f * dp, card.top + 15f * dp, card.right - 16f * dp, card.top + 43f * dp)
            progressTextRects[index].set(card.left + 22f * dp, card.top + 63f * dp, card.centerX(), card.top + 85f * dp)
            statusTextRects[index].setEmpty()
            progressTrackRects[index].set(card.left + 22f * dp, card.top + 92f * dp, card.right - 22f * dp, card.top + 99f * dp)
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
        missionIconRects[index].set(card.centerX() - 25f * dp, card.top + 17f * dp, card.centerX() + 25f * dp, card.top + 67f * dp)
        titleTextRects[index].set(card.left + 18f * dp, card.top + 70f * dp, card.right - 18f * dp, card.top + 104f * dp)
        rewardTextRects[index].set(card.left + 22f * dp, card.top + 111f * dp, card.right - 22f * dp, card.top + 137f * dp)
        progressTextRects[index].set(card.left + 20f * dp, claimButtonRects[index].top - 56f * dp, card.right - 20f * dp, claimButtonRects[index].top - 34f * dp)
        statusTextRects[index].set(card.left + 20f * dp, claimButtonRects[index].top - 32f * dp, card.right - 20f * dp, claimButtonRects[index].top - 18f * dp)
        progressTrackRects[index].set(card.left + 20f * dp, claimButtonRects[index].top - 12f * dp, card.right - 20f * dp, claimButtonRects[index].top - 5f * dp)
    }
}
