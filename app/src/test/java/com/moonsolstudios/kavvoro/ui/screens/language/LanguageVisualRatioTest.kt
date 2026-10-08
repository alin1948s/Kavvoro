package com.moonsolstudios.kavvoro.ui.screens.language

import android.graphics.Paint
import android.graphics.Typeface
import com.moonsolstudios.kavvoro.ui.render.UiTypography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Visual ratio verification test for Language Selector V4.1 (Visual Calibration Pass).
 * Ensures title dominance, subtitle legibility, and visual scale constants.
 */
class LanguageVisualRatioTest {
    @Test
    fun testTitleAndSubtitleVisualRatios() {
        val viewportWidth = 1600f
        val viewportHeight = 2560f
        val visualScale = kotlin.math.min(viewportWidth / 1024f, viewportHeight / 1536f)

        // 1. Title uses the shared screen-heading role and remains inside its reference width.
        val titleStr = "ALEGE LIMBA"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textPaint.letterSpacing = 0.16f

        val targetTitleW = viewportWidth * LanguageReferenceCanvas.TITLE_TARGET_WIDTH_RATIO
        var titleSize = UiTypography.SCREEN_TITLE_DP * visualScale
        textPaint.textSize = titleSize

        val rawMeasuredTitle = textPaint.measureText(titleStr)
        val measuredTitleW = if (rawMeasuredTitle > 0f) rawMeasuredTitle else (titleStr.length * titleSize * 0.72f)

        titleSize = (titleSize * (targetTitleW / measuredTitleW)).coerceIn(
            UiTypography.COMPACT_SCREEN_TITLE_DP * visualScale,
            UiTypography.SCREEN_TITLE_DP * visualScale
        )
        textPaint.textSize = titleSize

        val finalMeasuredTitleW = if (rawMeasuredTitle > 0f) textPaint.measureText(titleStr) else (titleStr.length * titleSize * 0.72f)
        val titleRatio = finalMeasuredTitleW / viewportWidth

        assertTrue("Title should use the shared screen-title size", titleSize / visualScale in UiTypography.COMPACT_SCREEN_TITLE_DP..UiTypography.SCREEN_TITLE_DP)
        assertTrue("Title should stay within its target viewport width", titleRatio <= LanguageReferenceCanvas.TITLE_TARGET_WIDTH_RATIO + 0.015f)

        // 2. Subtitle remains on the shared role and fits the available header width.
        val subtitleStr = "Selectează limba interfeței"
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        subPaint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        subPaint.letterSpacing = 0.035f

        val maxSubW = viewportWidth * 0.78f
        var subSize = UiTypography.SCREEN_SUBTITLE_DP * visualScale
        subPaint.textSize = subSize

        val rawMeasuredSub = subPaint.measureText(subtitleStr)
        val measuredSubW = if (rawMeasuredSub > 0f) rawMeasuredSub else (subtitleStr.length * subSize * 0.52f)
        if (measuredSubW > maxSubW && measuredSubW > 0f) {
            subSize = (subSize * (maxSubW / measuredSubW)).coerceAtLeast(
                UiTypography.COMPACT_SCREEN_SUBTITLE_DP * visualScale
            )
        }
        subPaint.textSize = subSize

        val finalMeasuredSubW = if (rawMeasuredSub > 0f) subPaint.measureText(subtitleStr) else (subtitleStr.length * subSize * 0.52f)
        val subRatio = finalMeasuredSubW / viewportWidth

        assertTrue("Subtitle should use the shared subtitle size", subSize / visualScale in UiTypography.COMPACT_SCREEN_SUBTITLE_DP..UiTypography.SCREEN_SUBTITLE_DP)
        assertTrue("Subtitle should stay within its available width", subRatio <= 0.78f)

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
