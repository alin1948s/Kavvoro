package com.moonsolstudios.kavvoro.ui.screens.language

import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Geometric verification against target reference mockup (1024x1536).
 * Validates normalized coordinates and proportions of Language Selector V4.
 */
class LanguageReferenceGeometryTest {

    private fun assertNear(actual: Float, expected: Float, tolerance: Float, message: String) {
        val diff = abs(actual - expected)
        assertTrue("$message (actual: $actual, expected: $expected, diff: $diff > tolerance: $tolerance)", diff <= tolerance)
    }

    @Test
    fun testV4NormalizedGeometryOnTabletPortrait() {
        val w = 1600f
        val h = 2560f
        val dp = 2.0f

        val backButton = RectF()
        val itemRects = mutableListOf<RectF>()
        val deckRect = RectF()
        val footerRect = RectF()

        val layout = LanguageSelectorLayoutCalculator.layoutLanguageSelector(
            side = 0f,
            contentWidth = w,
            compact = false,
            headerY = 28f * dp,
            viewportTop = 0f,
            viewportBottom = h,
            languageScroll = 0f,
            dp = dp,
            languageBackButton = backButton,
            languageItemRects = itemRects,
            languageDeckRect = deckRect,
            languageFooterRect = footerRect,
            viewportWidth = w
        )

        // 1. Single Main Cyber Frame Bounds
        assertNear(layout.panel.left / w, 0.042f, 0.005f, "FRAME_LEFT")
        assertNear(layout.panel.right / w, 0.958f, 0.005f, "FRAME_RIGHT")
        assertNear(layout.panel.top / h, 0.040f, 0.005f, "FRAME_TOP")
        assertNear(layout.panel.bottom / h, 0.929f, 0.005f, "FRAME_BOTTOM")

        // 2. Grid Position & Proportions
        val firstCard = layout.languageCards.first().bounds
        assertNear(firstCard.top / h, 0.150f, 0.005f, "GRID_TOP")

        val cardH = firstCard.bottom - firstCard.top
        assertNear(cardH / h, 0.0495f, 0.003f, "CARD_HEIGHT")

        val leftCard = layout.languageCards[0].bounds
        val rightCard = layout.languageCards[1].bounds
        val colGap = rightCard.left - leftCard.right
        assertNear(colGap / w, 0.024f, 0.003f, "COLUMN_GAP")
        assertNear(leftCard.left / w, 0.071f, 0.003f, "LEFT_CARD_LEFT")
        assertNear(leftCard.right / w, 0.489f, 0.003f, "LEFT_CARD_RIGHT")
        assertNear(rightCard.left / w, 0.513f, 0.003f, "RIGHT_CARD_LEFT")
        assertNear(rightCard.right / w, 0.931f, 0.003f, "RIGHT_CARD_RIGHT")

        // 3. Row Gap ratio
        val secondRowLeftCard = layout.languageCards[2].bounds
        val rowGap = secondRowLeftCard.top - leftCard.bottom
        assertNear(rowGap / cardH, 0.145f, 0.005f, "ROW_GAP_RATIO")

        // 4. Enclosed Cyber Footer Bar Bounds
        assertNear(layout.footer.top / h, 0.878f, 0.006f, "FOOTER_TOP")
        assertNear(layout.footer.bottom / h, 0.921f, 0.006f, "FOOTER_BOTTOM")
        assertNear(layout.footer.left / w, 0.066f, 0.005f, "FOOTER_LEFT")
        assertNear(layout.footer.right / w, 0.934f, 0.005f, "FOOTER_RIGHT")

        // 5. Hero Flag Dimensions (> 1.5x of V3)
        val firstFlag = layout.languageCards.first().flagBounds
        val flagW = firstFlag.right - firstFlag.left
        val flagH = firstFlag.bottom - firstFlag.top
        assertTrue("Hero Flag Width >= 46dp (actual: ${flagW / dp}dp)", flagW / dp >= 46f)
        assertTrue("Hero Flag Height >= 31dp (actual: ${flagH / dp}dp)", flagH / dp >= 31f)

        // 6. Generous Flag to Text Spacing
        val textX = layout.languageCards.first().textX
        val flagToText = textX - firstFlag.right
        val cardW = leftCard.right - leftCard.left
        assertNear(flagToText / cardW, 0.055f, 0.010f, "FLAG_TEXT_GAP")
    }
}
