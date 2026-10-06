package com.moonsolstudios.kavvoro.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModePickerLayoutCalculatorTest {

    @Test
    fun testLayoutCentersWithinContentBoundsWhenOffset() {
        val viewWidth = 1200f
        val viewHeight = 800f
        val leftOffset = 100f
        val contentWidth = 1000f
        val density = 2f

        val bounds = ModePickerLayoutCalculator.calculate(
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            uiDensity = density,
            left = leftOffset,
            contentWidth = contentWidth,
            top = 50f,
            classicProgress = 5,
            chaosProgress = 1
        )

        // Cards and back button must be centered around the content rect center: leftOffset + contentWidth * 0.5f = 600f
        val expectedCenter = leftOffset + contentWidth * 0.5f
        assertEquals(expectedCenter, bounds.classicCard.centerX(), 0.001f)
        assertEquals(expectedCenter, bounds.chaosCard.centerX(), 0.001f)
        assertEquals(expectedCenter, bounds.backButton.centerX(), 0.001f)

        // Must respect content bounds
        assertTrue(bounds.classicCard.left >= leftOffset)
        assertTrue(bounds.classicCard.right <= leftOffset + contentWidth)
        assertTrue(bounds.chaosCard.left >= leftOffset)
        assertTrue(bounds.chaosCard.right <= leftOffset + contentWidth)
    }

    @Test
    fun testProgressSwitchingBetweenSingleAndDualActionButtons() {
        val viewWidth = 400f
        val viewHeight = 800f
        val density = 1f

        // When progress <= 1: single button, continue empty
        val initialBounds = ModePickerLayoutCalculator.calculate(
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            uiDensity = density,
            left = 0f,
            contentWidth = viewWidth,
            top = 20f,
            classicProgress = 1,
            chaosProgress = 1
        )

        assertTrue("Classic continue must be empty when progress <= 1", initialBounds.classicContinueButton.isEmpty())
        assertFalse("Classic new must not be empty", initialBounds.classicNewButton.isEmpty())
        assertTrue(initialBounds.classicNewButton.width() > 0)
        assertTrue(initialBounds.classicNewButton.left < initialBounds.classicNewButton.right)

        assertTrue("Chaos continue must be empty when progress <= 1", initialBounds.chaosContinueButton.isEmpty())
        assertEquals(initialBounds.chaosNewButton, initialBounds.chaosStartButton)
        assertTrue(initialBounds.chaosNewButton.width() > 0)

        // When progress > 1: dual buttons (Continue + New)
        val progressingBounds = ModePickerLayoutCalculator.calculate(
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            uiDensity = density,
            left = 0f,
            contentWidth = viewWidth,
            top = 20f,
            classicProgress = 10,
            chaosProgress = 5
        )

        assertFalse("Classic continue must be populated when progress > 1", progressingBounds.classicContinueButton.isEmpty())
        assertFalse("Classic new must be populated when progress > 1", progressingBounds.classicNewButton.isEmpty())
        assertTrue(progressingBounds.classicContinueButton.right < progressingBounds.classicNewButton.left)
        assertTrue(progressingBounds.classicContinueButton.width() > 0)
        assertTrue(progressingBounds.classicNewButton.width() > 0)

        assertFalse("Chaos continue must be populated when progress > 1", progressingBounds.chaosContinueButton.isEmpty())
        assertFalse("Chaos new must be populated when progress > 1", progressingBounds.chaosNewButton.isEmpty())
        assertTrue(progressingBounds.chaosStartButton.isEmpty())
        assertTrue(progressingBounds.chaosContinueButton.right < progressingBounds.chaosNewButton.left)
    }

    @Test
    fun testNarrowViewportPreventsInvertedRectangles() {
        val narrowWidth = 280f
        val height = 600f
        val density = 1f

        val bounds = ModePickerLayoutCalculator.calculate(
            viewWidth = narrowWidth,
            viewHeight = height,
            uiDensity = density,
            left = 0f,
            contentWidth = narrowWidth,
            top = 10f,
            classicProgress = 8,
            chaosProgress = 4
        )

        // Verify valid positive dimensions for all buttons (no left > right)
        assertTrue(bounds.classicContinueButton.width() > 0)
        assertTrue(bounds.classicNewButton.width() > 0)
        assertTrue(bounds.classicContinueButton.height() > 0)
        assertTrue(bounds.classicNewButton.height() > 0)
        assertTrue(bounds.chaosContinueButton.width() > 0)
        assertTrue(bounds.chaosNewButton.width() > 0)
        assertTrue(bounds.backButton.width() > 0)
        assertTrue(bounds.backButton.height() > 0)

        assertTrue(bounds.classicContinueButton.left < bounds.classicContinueButton.right)
        assertTrue(bounds.classicNewButton.left < bounds.classicNewButton.right)
        assertTrue(bounds.chaosContinueButton.left < bounds.chaosContinueButton.right)
        assertTrue(bounds.chaosNewButton.left < bounds.chaosNewButton.right)
    }
}
