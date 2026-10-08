package com.moonsolstudios.kavvoro.ui.screens.language

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.moonsolstudios.kavvoro.i18n.KavvoroLanguage
import com.moonsolstudios.kavvoro.model.LayoutMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class LanguageResponsiveLayoutTest {

    private data class ResolutionTestSpec(
        val name: String,
        val width: Float,
        val height: Float,
        val density: Float,
        val expectedMode: LayoutMode
    )

    private val testMatrix = listOf(
        ResolutionTestSpec("360x800 (Compact Phone)", 360f, 800f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("412x915 (Compact Phone)", 412f, 915f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("480x854 (Compact Phone)", 480f, 854f, 1f, LayoutMode.COMPACT),
        ResolutionTestSpec("600x1024 (Medium)", 600f, 1024f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("720x1280 (Medium)", 720f, 1280f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("800x1280 (Medium)", 800f, 1280f, 1f, LayoutMode.MEDIUM),
        ResolutionTestSpec("1024x1366 (Tablet)", 1024f, 1366f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1080x2400 (High DPI Phone)", 1080f, 2400f, 2.75f, LayoutMode.COMPACT),
        ResolutionTestSpec("1200x1920 (Tablet)", 1200f, 1920f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1536x2048 (Tablet)", 1536f, 2048f, 1f, LayoutMode.TABLET),
        ResolutionTestSpec("1600x2560 (Tablet)", 1600f, 2560f, 1f, LayoutMode.TABLET)
    )

    @Test
    fun testSelectableLanguagesCountAndIntegrity() {
        val selectable = KavvoroLanguage.selectableLanguages
        assertEquals(24, selectable.size)
        assertTrue(selectable.none { it == KavvoroLanguage.SYSTEM })
        for (lang in selectable) {
            assertTrue(lang.code.isNotBlank())
            assertTrue(lang.nativeName.isNotBlank())
            assertTrue(lang.shortCode.isNotBlank())
        }
    }

    @Test
    fun testLanguageSelectorMetricsConstants() {
        assertEquals(0.916f, LanguageSelectorMetrics.PANEL_WIDTH_RATIO, 0.001f)
        assertEquals(0.889f, LanguageSelectorMetrics.PANEL_HEIGHT_RATIO, 0.001f)
        assertEquals(55f, LanguageSelectorMetrics.CARD_HEIGHT_DP, 0.001f)
        assertEquals(8f, LanguageSelectorMetrics.ROW_GAP_DP, 0.001f)
        assertEquals(16f, LanguageSelectorMetrics.COLUMN_GAP_DP, 0.001f)
        assertEquals(48f, LanguageSelectorMetrics.FLAG_WIDTH_DP, 0.001f)
        assertEquals(32f, LanguageSelectorMetrics.FLAG_HEIGHT_DP, 0.001f)
        assertEquals(20f, LanguageSelectorMetrics.RADIO_SIZE_DP, 0.001f)
        assertEquals(7f, LanguageSelectorMetrics.RADIO_INNER_DOT_DP, 0.001f)
        assertEquals(48f, LanguageSelectorMetrics.FOOTER_HEIGHT_DP, 0.001f)
    }

    @Test
    fun testAllElevenResolutionsInLanguageLayout() {
        val layoutManager = LanguageSelectorLayoutCalculator
        val expectedLanguages = KavvoroLanguage.selectableLanguages.size

        for (spec in testMatrix) {
            val dp = spec.density
            val contentWidth = spec.width
            val left = 0f

            val compact = (spec.width / dp) < 430f
            val headerY = 28f * dp
            val viewportTop = 24f * dp
            val viewportBottom = spec.height - 24f * dp

            val backButton = RectF()
            val itemRects = mutableListOf<RectF>()
            val deckRect = RectF()
            val footerRect = RectF()

            val layout = layoutManager.layoutLanguageSelector(
                side = left,
                contentWidth = contentWidth,
                compact = compact,
                headerY = headerY,
                viewportTop = viewportTop,
                viewportBottom = viewportBottom,
                languageScroll = 0f,
                dp = dp,
                languageBackButton = backButton,
                languageItemRects = itemRects,
                languageDeckRect = deckRect,
                languageFooterRect = footerRect,
                viewportWidth = spec.width
            )

            // Panel bounds
            val panelW = layout.panel.right - layout.panel.left
            val panelH = layout.panel.bottom - layout.panel.top
            assertTrue("${spec.name}: Panel width <= viewportWidth", panelW <= spec.width + 0.1f)
            assertTrue("${spec.name}: Panel height > 0", panelH > 0f)

            // Back button
            assertTrue("${spec.name}: Back button inside panel", layout.backButton.left >= layout.panel.left)
            assertTrue("${spec.name}: Back touch hitbox contains visual back button",
                layout.backButtonHitbox.left <= layout.backButton.left &&
                layout.backButtonHitbox.right >= layout.backButton.right &&
                layout.backButtonHitbox.top <= layout.backButton.top &&
                layout.backButtonHitbox.bottom >= layout.backButton.bottom
            )

            // Cards count
            assertEquals("${spec.name}: Items count must equal selectable languages", expectedLanguages, layout.languageCards.size)

            // Cards don't overlap check
            for (i in layout.languageCards.indices) {
                val r1 = layout.languageCards[i].bounds
                assertTrue("${spec.name}: Card $i width > 0", r1.right - r1.left > 0f)
                assertTrue("${spec.name}: Card $i height > 0", r1.bottom - r1.top > 0f)
                assertTrue("${spec.name}: Card $i horizontally inside panel",
                    r1.left >= layout.panel.left && r1.right <= layout.panel.right
                )

                for (j in i + 1 until layout.languageCards.size) {
                    val r2 = layout.languageCards[j].bounds
                    val overlaps = (r1.left < r2.right && r1.right > r2.left && r1.top < r2.bottom && r1.bottom > r2.top)
                    assertTrue("${spec.name}: Card $i and $j must not overlap", !overlaps)
                }
            }

            // In 2-column mode: card width left == card width right & column gap
            if (layout.columns == 2) {
                for (row in 0 until (expectedLanguages / 2)) {
                    val leftCard = layout.languageCards[row * 2].bounds
                    val rightCard = layout.languageCards[row * 2 + 1].bounds
                    val leftW = leftCard.right - leftCard.left
                    val rightW = rightCard.right - rightCard.left
                    assertEquals("${spec.name}: Row $row left width == right width", leftW, rightW, 0.05f)
                    val gap = rightCard.left - leftCard.right
                    val expectedMinGap = if (spec.width > spec.height) {
                        LanguageSelectorMetrics.COLUMN_GAP_DP * dp - 0.1f
                    } else {
                        spec.width * LanguageReferenceCanvas.COLUMN_GAP - 0.1f
                    }
                    assertTrue("${spec.name}: Row $row column gap >= expected", gap >= expectedMinGap)
                }
            }

            // Footer does not overlap grid when content fits completely
            if (layout.maxScroll == 0f) {
                val lastCard = layout.languageCards.last().bounds
                assertTrue("${spec.name}: Footer below last card", layout.footer.top >= lastCard.bottom - 0.1f)
            }
        }
    }

    @Test
    fun testRequestedSpecificResolutions() {
        val layoutManager = LanguageSelectorLayoutCalculator
        val specificSpecs = listOf(
            ResolutionTestSpec("360x800", 360f, 800f, 1f, LayoutMode.COMPACT),
            ResolutionTestSpec("412x915", 412f, 915f, 1f, LayoutMode.COMPACT),
            ResolutionTestSpec("600x960", 600f, 960f, 1f, LayoutMode.MEDIUM),
            ResolutionTestSpec("800x1280", 800f, 1280f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("1280x800", 1280f, 800f, 1f, LayoutMode.TABLET)
        )

        for (spec in specificSpecs) {
            val dp = spec.density
            val layout = layoutManager.layoutLanguageSelector(
                side = 0f,
                contentWidth = spec.width,
                compact = (spec.width / dp) < 430f,
                headerY = 28f * dp,
                viewportTop = 24f * dp,
                viewportBottom = spec.height - 24f * dp,
                languageScroll = 0f,
                dp = dp,
                languageBackButton = RectF(),
                languageItemRects = mutableListOf(),
                viewportWidth = spec.width
            )

            val widthDp = spec.width / dp
            if (widthDp < 520f) {
                assertEquals("${spec.name}: Should use 1 column for <520dp", 1, layout.columns)
            } else {
                assertEquals("${spec.name}: Should use 2 columns for >=520dp", 2, layout.columns)
            }

            val panelW = layout.panel.right - layout.panel.left
            if (widthDp > 700f) {
                val ratio = panelW / spec.width
                assertTrue("${spec.name}: Tablet panel ratio must be between 0.65 and 0.93: ratio=$ratio",
                    ratio in 0.65f..0.93f
                )
            }
        }
    }

    @Test
    fun lastLanguageRemainsReachableAtMaximumScrollAcrossScreenSizes() {
        val specs = testMatrix + listOf(
            ResolutionTestSpec("320x568 small phone", 320f, 568f, 1f, LayoutMode.COMPACT),
            ResolutionTestSpec("393x873 high density phone", 1179f, 2619f, 3f, LayoutMode.COMPACT),
            ResolutionTestSpec("1280x720 landscape tablet", 1280f, 720f, 1f, LayoutMode.TABLET),
            ResolutionTestSpec("1920x1080 landscape", 1920f, 1080f, 1f, LayoutMode.TABLET)
        )

        for (spec in specs) {
            val backButton = RectF()
            val itemRects = mutableListOf<RectF>()
            val deckRect = RectF()
            val footerRect = RectF()
            val viewportTop = 24f * spec.density
            val viewportBottom = spec.height - 24f * spec.density

            val initial = LanguageSelectorLayoutCalculator.layoutLanguageSelector(
                side = 0f,
                contentWidth = spec.width,
                compact = spec.width / spec.density < 430f,
                headerY = 28f * spec.density,
                viewportTop = viewportTop,
                viewportBottom = viewportBottom,
                languageScroll = 0f,
                dp = spec.density,
                languageBackButton = backButton,
                languageItemRects = itemRects,
                languageDeckRect = deckRect,
                languageFooterRect = footerRect,
                viewportWidth = spec.width
            )
            val bottom = LanguageSelectorLayoutCalculator.layoutLanguageSelector(
                side = 0f,
                contentWidth = spec.width,
                compact = spec.width / spec.density < 430f,
                headerY = 28f * spec.density,
                viewportTop = viewportTop,
                viewportBottom = viewportBottom,
                languageScroll = initial.maxScroll,
                dp = spec.density,
                languageBackButton = backButton,
                languageItemRects = itemRects,
                languageDeckRect = deckRect,
                languageFooterRect = footerRect,
                viewportWidth = spec.width
            )
            val lastCard = bottom.languageCards.last().bounds

            if (bottom.maxScroll > 0f) {
                val expectedBottom = bottom.gridViewport.bottom - LanguageSelectorMetrics.SCROLL_END_CLEARANCE_DP * spec.density
                assertEquals("${spec.name}: last language should stop above footer", expectedBottom, lastCard.bottom, 0.75f)
                assertTrue("${spec.name}: last language must remain inside the visible grid", lastCard.bottom <= bottom.gridViewport.bottom)
            } else {
                assertTrue("${spec.name}: all languages fit above footer", lastCard.bottom <= bottom.footer.top)
            }

            val languageTextSize = LanguageSelectorMetrics.languageNameTextSize(bottom.languageCards.first().bounds.height(), spec.density)
            assertTrue(
                "${spec.name}: language labels should stay within accessible responsive size",
                languageTextSize / spec.density in LanguageSelectorMetrics.LANGUAGE_NAME_MIN_SIZE_DP..LanguageSelectorMetrics.LANGUAGE_NAME_MAX_SIZE_DP
            )
        }
    }

    @Test
    fun testV3ConsoleProportions() {
        val layoutManager = LanguageSelectorLayoutCalculator
        // Tablet portrait spec: 800x1280 (matches Pixel Tablet 1600x2560 with dp=2.0)
        val viewportW = 800f
        val viewportH = 1280f
        val dp = 1f
        val layout = layoutManager.layoutLanguageSelector(
            side = 0f,
            contentWidth = viewportW,
            compact = false,
            headerY = 28f,
            viewportTop = 0f,
            viewportBottom = viewportH,
            languageScroll = 0f,
            dp = dp,
            languageBackButton = RectF(),
            languageItemRects = mutableListOf(),
            viewportWidth = viewportW
        )

        val panelW = layout.panel.right - layout.panel.left
        val panelH = layout.panel.bottom - layout.panel.top

        val widthRatio = panelW / viewportW
        val heightRatio = panelH / viewportH

        assertTrue("Tablet portrait: panel width ratio must be 91-93%, got $widthRatio", widthRatio in 0.91f..0.93f)
        assertTrue("Tablet portrait: panel height ratio must be 88-90%, got $heightRatio", heightRatio in 0.88f..0.90f)

        val cardBounds = layout.languageCards.first().bounds
        val cardH = cardBounds.bottom - cardBounds.top
        val cardHeightRatio = cardH / panelH
        assertTrue("Card height to panel ratio in 0.035..0.065, got $cardHeightRatio", cardHeightRatio in 0.035f..0.065f)

        // Verify margins: lateral 4-4.5%, vertical top ~4%, bottom ~7%
        val leftMargin = layout.panel.left / viewportW
        val rightMargin = (viewportW - layout.panel.right) / viewportW
        val topMargin = layout.panel.top / viewportH
        val bottomMargin = (viewportH - layout.panel.bottom) / viewportH

        assertTrue("Left margin ~4.2%: got $leftMargin", leftMargin in 0.038f..0.048f)
        assertTrue("Right margin ~4.2%: got $rightMargin", rightMargin in 0.038f..0.048f)
        assertTrue("Top margin ~4.0%: got $topMargin", topMargin in 0.035f..0.048f)
        assertTrue("Bottom margin ~7.1%: got $bottomMargin", bottomMargin in 0.065f..0.078f)
    }

    @Test
    fun testLanguageSelectorRendererExecutesSafely() {
        val canvas = Canvas()
        val paint = Paint()
        val dp = 2.0f
        val rect = RectF(10f, 10f, 50f, 50f)

        LanguageSelectorRenderer.drawBackButton(canvas, rect, active = false, paint = paint, dp = dp)
        LanguageSelectorRenderer.drawBackButton(canvas, rect, active = true, paint = paint, dp = dp)

        val footerRect = RectF(10f, 200f, 300f, 250f)
        LanguageSelectorRenderer.drawCurrentBar(
            canvas = canvas,
            rect = footerRect,
            activeLanguageName = "ROMÂNĂ",
            currentPrefix = "LIMBA CURENTĂ:",
            typeface = null,
            paint = paint,
            dp = dp
        )
    }
}
