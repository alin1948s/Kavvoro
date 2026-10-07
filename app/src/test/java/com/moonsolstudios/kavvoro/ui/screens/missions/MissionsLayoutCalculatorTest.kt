package com.moonsolstudios.kavvoro.ui.screens.missions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionsLayoutCalculatorTest {
    @Test
    fun phoneLayoutKeepsMissionCardsAndClaimsInsideTheViewport() {
        val layout = MissionsLayoutCalculator()
        layout.calculate(width = 1080f, height = 2400f, density = 2.625f)

        assertFalse(layout.gridLayout)
        assertTrue(layout.showSummary)
        assertTrue(layout.summaryRect.left >= 0f && layout.summaryRect.right <= 1080f)
        assertTrue(layout.summaryRect.bottom < layout.cardRects.first().top)
        layout.cardRects.forEachIndexed { index, card ->
            assertTrue(card.width() > 0f && card.height() > 0f)
            assertTrue(card.left >= 0f && card.right <= 1080f)
            assertTrue(card.top >= 0f && card.bottom <= 2400f)
            assertTrue(layout.claimButtonRects[index].left >= card.left)
            assertTrue(layout.claimButtonRects[index].right <= card.right)
        }
        assertTrue(layout.cardRects[0].bottom < layout.cardRects[1].top)
        assertTrue(layout.cardRects[1].bottom < layout.cardRects[2].top)
    }

    @Test
    fun tabletLandscapeUsesThreeColumnsWithoutOverlappingCards() {
        val layout = MissionsLayoutCalculator()
        layout.calculate(width = 1920f, height = 1200f, density = 1.5f)

        assertTrue(layout.gridLayout)
        assertEquals(layout.cardRects[0].top, layout.cardRects[1].top, 0.1f)
        assertEquals(layout.cardRects[1].top, layout.cardRects[2].top, 0.1f)
        assertTrue(layout.cardRects[0].right < layout.cardRects[1].left)
        assertTrue(layout.cardRects[1].right < layout.cardRects[2].left)
        layout.claimButtonRects.forEachIndexed { index, button ->
            assertTrue(button.left >= layout.cardRects[index].left)
            assertTrue(button.right <= layout.cardRects[index].right)
            assertTrue(button.bottom <= layout.cardRects[index].bottom)
        }
    }

    @Test
    fun shortLandscapePhoneUsesCompactStackedCardsInsideTheViewport() {
        val layout = MissionsLayoutCalculator()
        layout.calculate(width = 720f, height = 360f, density = 1f)

        assertFalse(layout.gridLayout)
        assertFalse(layout.showSummary)
        layout.cardRects.forEachIndexed { index, card ->
            assertTrue(card.height() < 92f)
            assertTrue(card.top >= 0f && card.bottom <= 360f)
            assertTrue(layout.titleTextRects[index].bottom <= card.bottom)
            assertTrue(layout.progressTextRects[index].bottom <= card.bottom)
            assertTrue(layout.progressTrackRects[index].bottom <= card.bottom)
            assertTrue(layout.claimButtonRects[index].bottom <= card.bottom)
        }
    }
}
