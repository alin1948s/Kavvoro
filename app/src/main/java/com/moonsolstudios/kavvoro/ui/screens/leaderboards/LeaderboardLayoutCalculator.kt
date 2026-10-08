package com.moonsolstudios.kavvoro.ui.screens.leaderboards

import android.graphics.RectF
import kotlin.math.min

/**
 * Dedicated responsive layout calculator for the Leaderboards screen.
 */
object LeaderboardLayoutCalculator {

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
        val backLeft = side
        val backBottom = backTop + backSize
        leaderboardBackButton.left = backLeft
        leaderboardBackButton.top = backTop
        leaderboardBackButton.right = backLeft + backSize
        leaderboardBackButton.bottom = backBottom
        leaderboardBackButton.set(backLeft, backTop, backLeft + backSize, backBottom)
        val gap = 10f * dp
        val height = min(88f * dp, (viewHeight - itemsTop - 94f * dp - gap * 3f) / 4f)
        repeat(4) { index ->
            val itemTop = itemsTop + index * (height + gap)
            val itemBottom = itemTop + height
            val rect = leaderboardItemRects.getOrNull(index) ?: RectF().also { leaderboardItemRects += it }
            rect.left = side
            rect.top = itemTop
            rect.right = contentRight
            rect.bottom = itemBottom
            rect.set(side, itemTop, contentRight, itemBottom)
        }
        while (leaderboardItemRects.size > 4) {
            leaderboardItemRects.removeAt(leaderboardItemRects.lastIndex)
        }
    }
}
