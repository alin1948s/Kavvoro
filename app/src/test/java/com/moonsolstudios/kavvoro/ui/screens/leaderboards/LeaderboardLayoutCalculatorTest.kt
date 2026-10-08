package com.moonsolstudios.kavvoro.ui.screens.leaderboards

import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderboardLayoutCalculatorTest {
    @Test
    fun backButtonUsesTheSharedLeadingEdgePosition() {
        val backButton = RectF()
        val cards = mutableListOf<RectF>()

        LeaderboardLayoutCalculator.layoutLeaderboards(
            side = 24f,
            contentRight = 516f,
            backTop = 28f,
            itemsTop = 176f,
            viewHeight = 840f,
            dp = 1f,
            leaderboardBackButton = backButton,
            leaderboardItemRects = cards
        )

        assertEquals(24f, backButton.left, 0.01f)
        assertEquals(backButton.left + 44f, backButton.right, 0.01f)
        assertTrue(cards.isNotEmpty())
    }
}
