package com.moonsolstudios.kavvoro.ui.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class ViewportLayoutCalculatorTest {
    @Test
    fun centeredContentUsesTheSmallerOfAvailableAndConfiguredWidths() {
        assertEquals(
            328f,
            ViewportLayoutCalculator.centeredContentWidth(
                viewWidth = 400f,
                density = 2f,
                horizontalInsetDp = 36f,
                maxWidthDp = 540f
            ),
            0.001f
        )
        assertEquals(
            800f,
            ViewportLayoutCalculator.centeredContentWidth(
                viewWidth = 1_200f,
                density = 2f,
                horizontalInsetDp = 36f,
                maxWidthDp = 400f
            ),
            0.001f
        )
    }

    @Test
    fun narrowViewportsNeverProduceNegativeContentWidth() {
        assertEquals(
            0f,
            ViewportLayoutCalculator.centeredContentWidth(20f, 1f, 36f, 540f),
            0f
        )
    }

    @Test
    fun centeredEdgesAndBottomInsetUseConsistentViewportCoordinates() {
        val left = ViewportLayoutCalculator.centeredLeft(500f, 300f)
        assertEquals(100f, left, 0.001f)
        assertEquals(400f, ViewportLayoutCalculator.contentRight(left, 300f), 0.001f)
        assertEquals(844f, ViewportLayoutCalculator.bottomInset(1_000f, 2f, 78f), 0.001f)
    }
}
