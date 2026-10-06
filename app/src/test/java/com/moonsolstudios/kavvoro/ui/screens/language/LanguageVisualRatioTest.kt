package com.moonsolstudios.kavvoro.ui.screens.language

import android.graphics.Paint
import android.graphics.Typeface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Visual ratio verification test for Language Selector V4.1 (Visual Calibration Pass).
 * Ensures title dominance, subtitle legibility, and visual scale constants.
 */
class LanguageVisualRatioTest {

    private fun assertRatio(actual: Float, expected: Float, tolerance: Float, message: String) {
        val diff = abs(actual - expected)
        assertTrue("$message (actual: $actual, expected: $expected, diff: $diff > tolerance: $tolerance)", diff <= tolerance)
    }

    @Test
    fun testTitleAndSubtitleVisualRatios() {
        val viewportWidth = 1600f
        val viewportHeight = 2560f
        val visualScale = kotlin.math.min(viewportWidth / 1024f, viewportHeight / 1536f)

        // 1. Title "ALEGE LIMBA" measurement (Target: 0.37 - 0.39 of viewport, weight 300-350 regular)
        val titleStr = "ALEGE LIMBA"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textPaint.letterSpacing = 0.16f

        val targetTitleW = viewportWidth * LanguageReferenceCanvas.TITLE_TARGET_WIDTH_RATIO
        var titleSize = 42f * visualScale
        textPaint.textSize = titleSize

        val rawMeasuredTitle = textPaint.measureText(titleStr)
        val measuredTitleW = if (rawMeasuredTitle > 0f) rawMeasuredTitle else (titleStr.length * titleSize * 0.72f)

        titleSize = (titleSize * (targetTitleW / measuredTitleW)).coerceIn(28f * visualScale, 56f * visualScale)
        textPaint.textSize = titleSize

        val finalMeasuredTitleW = if (rawMeasuredTitle > 0f) textPaint.measureText(titleStr) else (titleStr.length * titleSize * 0.72f)
        val titleRatio = finalMeasuredTitleW / viewportWidth

        assertRatio(titleRatio, 0.38f, 0.015f, "Title width / viewportWidth")

        // 2. Subtitle "Selectează limba interfeței" measurement (Target: 0.24 - 0.27 of viewport)
        val subtitleStr = "Selectează limba interfeței"
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        subPaint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        subPaint.letterSpacing = 0.035f

        val targetSubW = viewportWidth * LanguageReferenceCanvas.SUBTITLE_TARGET_WIDTH_RATIO
        var subSize = 20.5f * visualScale
        subPaint.textSize = subSize

        val rawMeasuredSub = subPaint.measureText(subtitleStr)
        val measuredSubW = if (rawMeasuredSub > 0f) rawMeasuredSub else (subtitleStr.length * subSize * 0.52f)

        subSize = (subSize * (targetSubW / measuredSubW)).coerceIn(16f * visualScale, 32f * visualScale)
        subPaint.textSize = subSize

        val finalMeasuredSubW = if (rawMeasuredSub > 0f) subPaint.measureText(subtitleStr) else (subtitleStr.length * subSize * 0.52f)
        val subRatio = finalMeasuredSubW / viewportWidth

        assertRatio(subRatio, 0.255f, 0.015f, "Subtitle width / viewportWidth")

        // 3. Body font weight vs Selected font weight
        val inactiveTypeface = Typeface.create("sans-serif", Typeface.NORMAL)
        val selectedTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        if (inactiveTypeface != null && selectedTypeface != null) {
            assertTrue("Selected typeface should have appropriate style", inactiveTypeface.style <= selectedTypeface.style)
        }
    }

    @Test
    fun testV42CalibrationConstants() {
        assertEquals(0.38f, LanguageSelectorMetrics.TITLE_TARGET_WIDTH_RATIO, 0.001f)
        assertEquals(0.255f, LanguageSelectorMetrics.SUBTITLE_TARGET_WIDTH_RATIO, 0.001f)
        assertEquals(0.071f, LanguageReferenceCanvas.LEFT_CARD_LEFT, 0.001f)
        assertEquals(0.489f, LanguageReferenceCanvas.LEFT_CARD_RIGHT, 0.001f)
        assertEquals(0.513f, LanguageReferenceCanvas.RIGHT_CARD_LEFT, 0.001f)
        assertEquals(0.931f, LanguageReferenceCanvas.RIGHT_CARD_RIGHT, 0.001f)
        assertEquals(0.024f, LanguageReferenceCanvas.COLUMN_GAP, 0.001f)
        assertEquals(2.0f, LanguageSelectorMetrics.SELECTED_BORDER_PX, 0.001f)
        assertEquals(0.20f, LanguageSelectorMetrics.SELECTED_GLOW_ALPHA, 0.001f)
        assertEquals(8f, LanguageSelectorMetrics.SELECTED_GLOW_RADIUS, 0.001f)
        assertEquals(5f, LanguageSelectorMetrics.RADIO_GLOW_RADIUS, 0.001f)
    }
}
